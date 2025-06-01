package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.model.entity.Order;
import com.secondhand.model.entity.OrderItem;
import com.secondhand.model.entity.User;
import com.secondhand.model.entity.Product;
import com.secondhand.model.vo.OrderItemVO;
import com.secondhand.model.vo.OrderVO;
import com.secondhand.common.exception.BusinessException;
import com.secondhand.repository.OrderItemMapper;
import com.secondhand.repository.OrderMapper;
import com.secondhand.repository.UserMapper;
import com.secondhand.repository.ProductMapper;
import com.secondhand.service.AdminOrderService;
import com.secondhand.model.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private static final Logger log = LoggerFactory.getLogger(AdminOrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageResult<OrderVO> getOrderList(int page, int size, String orderNo, String productTitle, String buyerName, String sellerName, Integer status, String startTime, String endTime) {
        log.info("开始查询订单列表，参数：page={}, size={}, orderNo={}, productTitle={}, buyerName={}, sellerName={}, status={}, startTime={}, endTime={}", 
            page, size, orderNo, productTitle, buyerName, sellerName, status, startTime, endTime);
            
        // 构建查询条件
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.select("id", "user_id", "seller_id", "total_amount", "status", "address", "payment_type", "created_at", "updated_at");
        
        // 订单号搜索
        if (StringUtils.hasText(orderNo)) {
            wrapper.like("id", orderNo);
        }
        
        // 商品名称搜索
        if (StringUtils.hasText(productTitle)) {
            wrapper.exists("SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id WHERE oi.order_id = `order`.id AND p.title LIKE '%" + productTitle + "%'");
        }
        
        // 买家名称搜索
        if (StringUtils.hasText(buyerName)) {
            wrapper.inSql("user_id", "SELECT id FROM user WHERE username LIKE '%" + buyerName + "%'");
        }
        
        // 卖家名称搜索
        if (StringUtils.hasText(sellerName)) {
            wrapper.inSql("seller_id", "SELECT id FROM user WHERE username LIKE '%" + sellerName + "%'");
        }
        
        // 状态筛选
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        // 时间范围筛选
        if (StringUtils.hasText(startTime)) {
            wrapper.ge("created_at", startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le("created_at", endTime + " 23:59:59");
        }
        
        // 按创建时间倒序排序
        wrapper.orderByDesc("created_at");
        
        // 分页查询
        Page<Order> pageParam = new Page<>(page, size);
        log.info("执行分页查询，SQL: {}", wrapper.getSqlSegment());
        
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        log.info("查询完成，总记录数：{}，当前页记录数：{}", result.getTotal(), result.getRecords().size());
        
        // 转换为VO
        List<OrderVO> voList = result.getRecords().stream()
            .map(order -> {
                log.info("处理订单：id={}, status={}", order.getId(), order.getStatus());
                return convertToVO(order);
            })
            .collect(Collectors.toList());
        
        // 构建分页结果
        PageResult<OrderVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(page);
        pageResult.setPageSize(size);
        
        log.info("返回结果：总记录数={}, 当前页记录数={}", pageResult.getTotal(), pageResult.getList().size());
        return pageResult;
    }

    @Override
    public OrderVO getOrderDetail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return convertToVO(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long id, Integer status) {
        log.info("【AdminOrderService】更新订单状态：id={}, status={}", id, status);

        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查状态转换是否合法
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new BusinessException("非法的状态转换");
        }

        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    // 将 Order 实体转换为 OrderVO
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 设置状态文本
        vo.setStatusText(getStatusText(order.getStatus()));
        
        // 设置支付方式文本
        vo.setPaymentTypeText(getPaymentTypeText(order.getPaymentType()));
        
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
                
                // 计算小计金额
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

    // 获取状态文本
    private String getStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "退款中";
            case 6 -> "已退款";
            default -> "未知状态";
        };
    }

    // 获取支付方式文本
    private String getPaymentTypeText(Integer paymentType) {
        if (paymentType == null) return "";
        return switch (paymentType) {
            case 1 -> "微信支付";
            case 2 -> "支付宝";
            case 3 -> "银行卡";
            default -> "未知支付方式";
        };
    }

    // 检查状态转换是否合法
    private boolean isValidStatusTransition(Integer oldStatus, Integer newStatus) {
        if (oldStatus == null || newStatus == null) {
            return false;
        }

        // 定义合法的状态转换
        return switch (oldStatus) {
            case 0 -> newStatus == 1 || newStatus == 4; // 待付款 -> 待发货/已取消
            case 1 -> newStatus == 2 || newStatus == 4; // 待发货 -> 待收货/已取消
            case 2 -> newStatus == 3 || newStatus == 5; // 待收货 -> 已完成/退款中
            case 3 -> newStatus == 5; // 已完成 -> 退款中
            case 5 -> newStatus == 6; // 退款中 -> 已退款
            case 4, 6 -> false; // 已取消/已退款 不能转换
            default -> false;
        };
    }
} 