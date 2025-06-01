package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.common.exception.BusinessException;
import com.secondhand.repository.OrderMapper;
import com.secondhand.repository.OrderItemMapper;
import com.secondhand.repository.ProductMapper;
import com.secondhand.repository.UserMapper;
import com.secondhand.model.dto.OrderCreateDTO;
import com.secondhand.model.dto.OrderItemDTO;
import com.secondhand.model.entity.Order;
import com.secondhand.model.entity.OrderItem;
import com.secondhand.model.entity.Product;
import com.secondhand.model.entity.User;
import com.secondhand.model.entity.Transaction;
import com.secondhand.model.vo.OrderVO;
import com.secondhand.model.vo.OrderItemVO;
import com.secondhand.service.OrderService;
import com.secondhand.service.WalletService;
import com.secondhand.service.TransactionService;
import com.secondhand.service.CouponService;
import com.secondhand.service.UserPointsService;
import com.secondhand.utils.SecurityUtils;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.common.constants.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderItemMapper orderItemMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WalletService walletService;
    
    @Autowired
    private TransactionService transactionService;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private UserPointsService userPointsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Page<OrderVO> getUserOrders(Long userId, Integer status, Integer page, Integer size) {
        // 使用自定义的SQL查询
        List<Order> orders = orderMapper.selectByUserId(userId, status);
        
        // 转换为VO
        List<OrderVO> voList = orders.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        // 创建分页对象
        Page<OrderVO> voPage = new Page<>(page, size);
        voPage.setRecords(voList);
        voPage.setTotal(orders.size());
        return voPage;
    }

    @Override
    @Transactional
    public OrderVO createOrder(OrderCreateDTO orderDTO) {
        // 1. 创建订单
        Order order = new Order();
        order.setUserId(SecurityUtils.getCurrentUserId());
        order.setSellerId(orderDTO.getSellerId());
        
        // 计算订单总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemDTO item : orderDTO.getItems()) {
            totalAmount = totalAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待付款
        order.setPaymentType(orderDTO.getPaymentType());
        order.setAddress(orderDTO.getAddress());
        order.setPointsUsed(0); // 初始化使用的积分为0

        // 2. 处理优惠券
        Long userCouponId = orderDTO.getCouponId();
        if (userCouponId != null) {
            try {
                logger.info("开始处理优惠券: userCouponId={}, userId={}, totalAmount={}", 
                    userCouponId, SecurityUtils.getCurrentUserId(), totalAmount);
                
                // 验证优惠券有效性并计算折扣金额
                BigDecimal discountAmount = couponService.validateAndCalculateDiscount(userCouponId, SecurityUtils.getCurrentUserId(), totalAmount);
                logger.info("优惠券验证结果: discountAmount={}", discountAmount);
                
                if (discountAmount.compareTo(BigDecimal.ZERO) > 0) {
                    // 如果优惠券有效，计算最终金额
                    totalAmount = totalAmount.subtract(discountAmount);
                    // 确保最终金额不小于0
                    if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                        totalAmount = BigDecimal.ZERO;
                    }
                    order.setCouponId(userCouponId);
                    logger.info("优惠券使用成功: userCouponId={}, discountAmount={}, finalAmount={}", 
                        userCouponId, discountAmount, totalAmount);
                } else {
                    // 如果优惠券无效，记录警告日志
                    logger.warn("用户 {} 使用的优惠券 {} 无效，折扣金额为0", SecurityUtils.getCurrentUserId(), userCouponId);
                    order.setCouponId(null); // 确保不记录无效优惠券
                }
            } catch (BusinessException e) {
                // 如果优惠券验证失败，抛出业务异常
                logger.error("优惠券验证失败: {}", e.getMessage());
                throw e; // 继续抛出，中断订单创建
            } catch (Exception e) {
                // 其他优惠券处理异常
                logger.error("优惠券处理异常: {}", e.getMessage(), e);
                throw new BusinessException("优惠券处理异常"); // 抛出业务异常，中断订单创建
            }
        }

        // 3. 处理积分抵扣
        Integer pointsToUse = orderDTO.getPointsToUse();
        if (pointsToUse != null && pointsToUse > 0) {
            try {
                // 验证用户积分是否足够
                int userPoints = userPointsService.getUserPoints(SecurityUtils.getCurrentUserId());
                if (pointsToUse > userPoints) {
                    throw new BusinessException("用户积分不足");
                }

                // TODO: 积分兑换比例应从配置或数据库获取
                BigDecimal pointsExchangeRate = new BigDecimal("100"); // 100积分 = 1元

                // 计算积分可抵扣的最大金额 (向下取整到分)
                BigDecimal maxPointsDiscountAmount = totalAmount;
                BigDecimal pointsCanRedeemAmount = new BigDecimal(pointsToUse).divide(pointsExchangeRate, 2, BigDecimal.ROUND_DOWN);

                // 积分抵扣金额不能超过订单当前剩余金额
                BigDecimal actualPointsDiscountAmount = pointsCanRedeemAmount.min(maxPointsDiscountAmount);

                // 计算实际使用的积分数量 (根据实际抵扣金额反算，向上取整到整数积分)
                Integer actualPointsUsed = actualPointsDiscountAmount.multiply(pointsExchangeRate).setScale(0, BigDecimal.ROUND_UP).intValue();
                
                // 确保实际使用的积分不超过用户尝试使用的数量
                if (actualPointsUsed > pointsToUse) {
                     actualPointsUsed = pointsToUse;
                     // 重新计算实际抵扣金额 based on actual points used
                     actualPointsDiscountAmount = new BigDecimal(actualPointsUsed).divide(pointsExchangeRate, 2, BigDecimal.ROUND_DOWN);
                }

                if (actualPointsUsed > 0) {
                    // 扣除用户积分
                    userPointsService.decreasePoints(SecurityUtils.getCurrentUserId(), actualPointsUsed);

                    // 更新最终订单金额
                    totalAmount = totalAmount.subtract(actualPointsDiscountAmount);
                    // 确保最终金额不小于0
                    if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                        totalAmount = BigDecimal.ZERO;
                    }

                    // 记录实际使用的积分数量
                    order.setPointsUsed(actualPointsUsed);
                    logger.info("用户 {} 订单 {} 使用 {} 积分，抵扣 {} 元", SecurityUtils.getCurrentUserId(), order.getOrderSn(), actualPointsUsed, actualPointsDiscountAmount);
                }

            } catch (BusinessException e) {
                // 如果积分处理失败，抛出业务异常
                logger.error("积分处理失败: {}", e.getMessage());
                throw e; // 继续抛出，中断订单创建
            } catch (Exception e) {
                // 其他积分处理异常
                logger.error("积分处理异常: {}", e.getMessage(), e);
                throw new BusinessException("积分处理异常"); // 抛出业务异常，中断订单创建
            }
        }

        // 设置最终订单金额 (经过优惠券和积分抵扣后的)
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount); // 实付金额等于最终金额（假设没有其他支付手续费）

        // 验证最终金额是否有效
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) < 0) {
             throw new BusinessException("订单最终金额无效");
        }

        orderMapper.insert(order);

        // 4. 创建订单项
        for (OrderItemDTO item : orderDTO.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getPrice());
            
            // 获取商品信息
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                logger.error("创建订单失败：商品不存在, productId={}", item.getProductId());
                throw new BusinessException("商品不存在");
            }
            
            orderItem.setProductTitle(product.getTitle());
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String[] images = product.getImages().split(",");
                if (images.length > 0) {
                    orderItem.setProductImage(images[0]);
                }
            }
            
            // 计算订单项总金额
            orderItem.setTotalAmount(item.getPrice().multiply(new BigDecimal(item.getQuantity())));

            orderItemMapper.insert(orderItem);
        }

        // 5. 使用优惠券 (如果使用了有效的优惠券)
        if (order.getCouponId() != null) {
             try {
                 couponService.useCoupon(order.getCouponId());
             } catch (Exception e) {
                 logger.error("标记优惠券已使用失败: {}", e.getMessage(), e);
                 // 标记优惠券失败不应回滚整个订单，可以记录日志或进行补偿处理
             }
        }

        return convertToVO(order);
    }

    @Override
    @Transactional
    public void payOrder(Long orderId) {
        logger.info("【支付流程】开始处理订单支付，订单ID: {}", orderId);
        
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            logger.error("【支付流程】订单不存在，订单ID: {}", orderId);
            throw new BusinessException("订单不存在");
        }
        
        logger.info("【支付流程】订单信息: {}", order);
        
        if (order.getStatus() != 0) {
            logger.warn("【支付流程】订单状态不正确，订单ID: {}, 当前状态: {}", orderId, order.getStatus());
            throw new BusinessException("订单状态不正确");
        }
        
        try {
            // 1. 扣除买家余额
            logger.info("【支付流程】开始扣除买家余额，用户ID: {}, 金额: {}", order.getUserId(), order.getPayAmount());
            walletService.deductBalance(order.getUserId(), order.getPayAmount(), TransactionType.PURCHASE);
            logger.info("【支付流程】买家余额扣除成功");
            
            // 2. 更新商品库存和销量
            logger.info("【支付流程】开始更新商品库存和销量");
            List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, orderId)
            );
            
            for (OrderItem item : orderItems) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    logger.info("【支付流程】更新商品信息，商品ID: {}, 原库存: {}, 原销量: {}", 
                        product.getId(), product.getStock(), product.getSalesCount());
                    
                    // 更新库存
                    product.setStock(product.getStock() - item.getQuantity());
                    // 更新销量
                    product.setSalesCount(product.getSalesCount() + item.getQuantity());
                    productMapper.updateById(product);
                    
                    logger.info("【支付流程】商品信息更新成功，商品ID: {}, 新库存: {}, 新销量: {}", 
                        product.getId(), product.getStock(), product.getSalesCount());
                } else {
                    logger.warn("【支付流程】商品不存在，商品ID: {}", item.getProductId());
                }
            }
            
            // 3. 更新订单状态
            logger.info("【支付流程】开始更新订单状态");
            order.setStatus(1); // 待发货
            orderMapper.updateById(order);
            logger.info("【支付流程】订单状态更新成功，订单ID: {}, 新状态: {}", orderId, order.getStatus());
            
            logger.info("【支付流程】订单支付流程完成，订单ID: {}", orderId);
        } catch (Exception e) {
            logger.error("【支付流程】订单支付失败，订单ID: {}, 错误信息: {}", orderId, e.getMessage(), e);
            throw new BusinessException("支付失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void confirmReceipt(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != 2) {
            throw new BusinessException("订单状态不正确");
        }
        
        // 获取卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller == null) {
            throw new BusinessException("卖家不存在");
        }

        // 计算手续费和实际收入
        BigDecimal totalAmount = order.getPayAmount();
        BigDecimal merchantFee = BigDecimal.ZERO;
        BigDecimal actualAmount = totalAmount;

        // 如果是商家，需要扣除手续费
        if (seller.getUserType() == 1) { // 1表示商家
            merchantFee = totalAmount.multiply(seller.getMerchantFee());
            actualAmount = totalAmount.subtract(merchantFee);
            
            // 将手续费转给管理员
            if (merchantFee.compareTo(BigDecimal.ZERO) > 0) {
                // 获取管理员账户
                User admin = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                        .eq(User::getRole, 2) // 2表示管理员
                        .last("LIMIT 1")
                );
                
                if (admin != null) {
                    // 将手续费转给管理员
                    walletService.addBalance(admin.getId(), merchantFee);
                    
                    // 创建管理员收入交易记录
                    Transaction adminTransaction = new Transaction();
                    adminTransaction.setUserId(admin.getId());
                    adminTransaction.setOrderId(order.getId());
                    adminTransaction.setAmount(merchantFee);
                    adminTransaction.setType(7); // 新增交易类型：7-手续费收入
                    adminTransaction.setStatus(1); // 成功
                    transactionService.createTransaction(adminTransaction);
                    
                    logger.info("订单 {} 的手续费 {} 已转给管理员 {}", orderId, merchantFee, admin.getId());
                }
            }
        }
        
        // 将实际收入转给卖家
        walletService.addBalance(order.getSellerId(), actualAmount);
        
        // 创建卖家收入交易记录
        Transaction sellerTransaction = new Transaction();
        sellerTransaction.setUserId(order.getSellerId());
        sellerTransaction.setOrderId(order.getId());
        sellerTransaction.setAmount(actualAmount);
        sellerTransaction.setType(4); // 商品出售收入
        sellerTransaction.setStatus(1); // 成功
        transactionService.createTransaction(sellerTransaction);
        
        // 更新订单状态
        order.setStatus(3); // 已完成
        orderMapper.updateById(order);

        // 发放用户积分
        try {
            // 根据订单实付金额计算积分 (每1元1积分，向下取整)
            int pointsToAward = order.getPayAmount().intValue();
            if (pointsToAward > 0) {
                userPointsService.increasePoints(order.getUserId(), pointsToAward);
                logger.info("用户 {} 确认收货订单 {}，获得 {} 积分", order.getUserId(), orderId, pointsToAward);
            }
        } catch (Exception e) {
            logger.error("用户 {} 确认收货订单 {} 发放积分失败", order.getUserId(), orderId, e);
            // 发放积分失败不应回滚整个订单确认，记录日志或进行补偿处理
        }
    }

    @Override
    @Transactional
    public void refundOrder(Long orderId) {
        logger.info("收到退款申请，订单ID: {}", orderId);
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            logger.warn("退款失败，订单不存在: {}", orderId);
            throw new BusinessException("订单不存在");
        }
        
        // 允许在待收货和已完成状态下申请退款
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            logger.warn("退款失败，订单状态不正确: orderId={}, status={}", orderId, order.getStatus());
            throw new BusinessException("订单状态不正确，只能在待收货或已完成状态下申请退款");
        }
        
        try {
            // 6. 处理退款
            // 6.1 计算手续费
            BigDecimal merchantFee = order.getPayAmount().multiply(new BigDecimal("0.05")); // 5%手续费
            
            // 6.2 从卖家账户扣除退款金额（包含5%手续费）
            BigDecimal sellerDeductAmount = order.getPayAmount().subtract(merchantFee);
            try {
                walletService.deductBalance(order.getSellerId(), sellerDeductAmount, TransactionType.REFUND_EXPENSE);
                logger.info("卖家余额扣除成功: orderId={}, sellerId={}, amount={}", orderId, order.getSellerId(), sellerDeductAmount);
            } catch (Exception e) {
                logger.error("卖家余额扣除失败: orderId={}, sellerId={}, amount={}, error={}", 
                    orderId, order.getSellerId(), sellerDeductAmount, e.getMessage());
                throw new BusinessException("卖家余额扣除失败: " + e.getMessage());
            }
            
            // 6.3 退还买家金额
            logger.info("开始退还买家金额: userId={}, amount={}", 
                    order.getUserId(), order.getPayAmount());
            
            walletService.addBalance(order.getUserId(), order.getPayAmount());
            
            // 6.4 创建买家交易记录
            Transaction buyerTransaction = new Transaction();
            buyerTransaction.setUserId(order.getUserId());
            buyerTransaction.setAmount(order.getPayAmount());
            buyerTransaction.setType(TransactionType.REFUND_INCOME); // 退款收入
            buyerTransaction.setStatus(1); // 成功
            buyerTransaction.setOrderId(orderId);
            buyerTransaction.setCreatedAt(LocalDateTime.now());
            buyerTransaction.setUpdatedAt(LocalDateTime.now());
            transactionService.createTransaction(buyerTransaction);
            
            logger.info("买家交易记录创建成功: userId={}, amount={}", 
                    order.getUserId(), order.getPayAmount());
            
            // 7. 更新订单状态
            order.setStatus(6); // 7 表示已退款
            order.setUpdatedAt(LocalDateTime.now());
            updateById(order);
            
            logger.info("订单状态更新成功: orderId={}, newStatus={}", orderId, 7);
            
            // 8. 恢复商品库存和销量
            List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, orderId)
            );
            
            for (OrderItem item : orderItems) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setSalesCount(product.getSalesCount() - item.getQuantity());
                    product.setUpdatedAt(LocalDateTime.now());
                    productMapper.updateById(product);
                    logger.info("商品库存和销量已恢复: productId={}, stock={}, salesCount={}", 
                        product.getId(), product.getStock(), product.getSalesCount());
                }
            }
            
            logger.info("退款确认处理完成: orderId={}", orderId);
            
        } catch (BusinessException e) {
            logger.error("退款确认处理失败(业务异常): orderId={}, error={}", orderId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("退款确认处理失败(系统异常): orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("退款确认处理失败，请稍后重试");
        }
    }

    @Override
    @Transactional
    public void shipOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确");
        }
        
        order.setStatus(2); // 待收货
        orderMapper.updateById(order);
    }

    @Override
    public Page<OrderVO> getSellerOrders(Long sellerId, Integer status, Integer page, Integer size) {
        // 使用自定义的SQL查询
        List<Order> orders = orderMapper.selectBySellerId(sellerId, status);
        
        // 转换为VO
        List<OrderVO> voList = orders.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        // 创建分页对象
        Page<OrderVO> voPage = new Page<>(page, size);
        voPage.setRecords(voList);
        voPage.setTotal(orders.size());
        return voPage;
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待付款订单");
        }
        
        order.setStatus(5); // 已取消
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        try {
            logger.info("开始获取订单详情: orderId={}", orderId);
            
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                logger.warn("订单不存在: orderId={}", orderId);
                throw new BusinessException("订单不存在");
            }
            logger.info("订单查询成功: orderId={}, userId={}, sellerId={}, status={}", 
                orderId, order.getUserId(), order.getSellerId(), order.getStatus());
            
            // 获取当前用户ID
            Long currentUserId = SecurityUtils.getCurrentUserId();
            logger.info("当前用户ID: {}", currentUserId);
            
            // 获取当前用户角色
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                logger.error("无法获取当前请求上下文");
                throw new BusinessException("无法获取当前请求");
            }
            
            String token = attributes.getRequest().getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                logger.warn("未找到有效的认证信息");
                throw new BusinessException("用户未登录");
            }
            
            // 验证权限：只有订单的买家、卖家可以查看订单详情
            if (!order.getUserId().equals(currentUserId) && 
                !order.getSellerId().equals(currentUserId)) {
                // 如果不是买家或卖家，检查是否是管理员
                try {
                    Integer role = jwtUtil.getRoleFromToken(token.substring(7));
                    logger.info("当前用户角色: {}", role);
                    if (role == null || role != 2) {
                        logger.warn("用户无权查看订单: userId={}, orderId={}", currentUserId, orderId);
                        throw new BusinessException("无权查看该订单");
                    }
                } catch (Exception e) {
                    logger.error("获取用户角色失败: {}", e.getMessage(), e);
                    throw new BusinessException("无权查看该订单");
                }
            }
            
            OrderVO orderVO = convertToVO(order);
            logger.info("订单详情获取成功: orderId={}", orderId);
            return orderVO;
            
        } catch (BusinessException e) {
            logger.warn("业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("获取订单详情失败: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new BusinessException("获取订单详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void confirmRefund(Long orderId) {
        logger.info("开始处理退款确认: orderId={}", orderId);
        
        // 1. 获取订单信息
        Order order = getById(orderId);
        if (order == null) {
            logger.error("订单不存在: orderId={}", orderId);
            throw new BusinessException("订单不存在");
        }
        
        // 2. 验证订单状态
        if (order.getStatus() != 5) { // 5 表示退款中
            logger.error("订单状态不正确，无法确认退款: orderId={}, status={}", orderId, order.getStatus());
            throw new BusinessException("订单状态不正确，无法确认退款");
        }
        
        // 3. 获取卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller == null) {
            logger.error("卖家不存在: sellerId={}", order.getSellerId());
            throw new BusinessException("卖家不存在");
        }
        
        // 4. 获取买家信息
        User buyer = userMapper.selectById(order.getUserId());
        if (buyer == null) {
            logger.error("买家不存在: userId={}", order.getUserId());
            throw new BusinessException("买家不存在");
        }
        
        // 5. 验证订单金额
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("订单金额无效: orderId={}, amount={}", orderId, order.getPayAmount());
            throw new BusinessException("订单金额无效");
        }
        
        try {
            // 6. 处理退款
            // 6.1 计算手续费
            BigDecimal merchantFee = order.getPayAmount().multiply(new BigDecimal("0.05")); // 5%手续费
            
            // 6.2 从卖家账户扣除退款金额（包含5%手续费）
            BigDecimal sellerDeductAmount = order.getPayAmount().subtract(merchantFee);
            try {
                walletService.deductBalance(order.getSellerId(), sellerDeductAmount, TransactionType.REFUND_EXPENSE);
                logger.info("卖家余额扣除成功: orderId={}, sellerId={}, amount={}", orderId, order.getSellerId(), sellerDeductAmount);
            } catch (Exception e) {
                logger.error("卖家余额扣除失败: orderId={}, sellerId={}, amount={}, error={}", 
                    orderId, order.getSellerId(), sellerDeductAmount, e.getMessage());
                throw new BusinessException("卖家余额扣除失败: " + e.getMessage());
            }
            
            // 6.3 退还买家金额
            logger.info("开始退还买家金额: userId={}, amount={}", 
                    order.getUserId(), order.getPayAmount());
            
            walletService.addBalance(order.getUserId(), order.getPayAmount());
            
            // 6.4 创建买家交易记录
            Transaction buyerTransaction = new Transaction();
            buyerTransaction.setUserId(order.getUserId());
            buyerTransaction.setAmount(order.getPayAmount());
            buyerTransaction.setType(TransactionType.REFUND_INCOME); // 退款收入
            buyerTransaction.setStatus(1); // 成功
            buyerTransaction.setOrderId(orderId);
            buyerTransaction.setCreatedAt(LocalDateTime.now());
            buyerTransaction.setUpdatedAt(LocalDateTime.now());
            transactionService.createTransaction(buyerTransaction);
            
            logger.info("买家交易记录创建成功: userId={}, amount={}", 
                    order.getUserId(), order.getPayAmount());
            
            // 7. 更新订单状态
            order.setStatus(6); // 7 表示已退款
            order.setUpdatedAt(LocalDateTime.now());
            updateById(order);
            
            logger.info("订单状态更新成功: orderId={}, newStatus={}", orderId, 7);
            
            // 8. 恢复商品库存和销量
            List<OrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>()
                    .eq(OrderItem::getOrderId, orderId)
            );
            
            for (OrderItem item : orderItems) {
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setSalesCount(product.getSalesCount() - item.getQuantity());
                    product.setUpdatedAt(LocalDateTime.now());
                    productMapper.updateById(product);
                    logger.info("商品库存和销量已恢复: productId={}, stock={}, salesCount={}", 
                        product.getId(), product.getStock(), product.getSalesCount());
                }
            }
            
            logger.info("退款确认处理完成: orderId={}", orderId);
            
        } catch (BusinessException e) {
            logger.error("退款确认处理失败(业务异常): orderId={}, error={}", orderId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("退款确认处理失败(系统异常): orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("退款确认处理失败，请稍后重试");
        }
    }

    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 获取订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        
        // 转换为 OrderItemVO
        List<OrderItemVO> itemVOs = items.stream()
            .map(item -> {
                OrderItemVO itemVO = new OrderItemVO();
                BeanUtils.copyProperties(item, itemVO);
                
                // 获取商品信息
                Product product = productMapper.selectById(item.getProductId());
                if (product != null) {
                    itemVO.setProductTitle(product.getTitle());
                    if (product.getImages() != null && !product.getImages().isEmpty()) {
                        String[] images = product.getImages().split(",");
                        if (images.length > 0) {
                            String image = images[0];
                            if (!image.startsWith("http")) {
                                image = "http://localhost:8080" + image;
                            }
                            itemVO.setProductImage(image);
                        }
                    }
                }
                
                // 计算总金额
                itemVO.setTotalAmount(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                
                return itemVO;
            })
            .collect(Collectors.toList());
        
        vo.setItems(itemVOs);
        
        // 获取买家信息
        User buyer = userMapper.selectById(order.getUserId());
        if (buyer != null) {
            vo.setBuyerName(buyer.getUsername());
        }
        
        // 获取卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getUsername());
        }
        
        return vo;
    }

    @Override
    public long count(Wrapper<Order> wrapper) {
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public BigDecimal getTotalAmount(Wrapper<Order> wrapper) {
        return baseMapper.selectList(wrapper).stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalAmount() {
        return getTotalAmount(new LambdaQueryWrapper<>());
    }
} 