package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.model.entity.Order;
import com.secondhand.model.entity.Wallet;
import com.secondhand.repository.WalletMapper;
import com.secondhand.repository.OrderMapper;
import com.secondhand.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.secondhand.model.entity.Transaction;
import com.secondhand.service.TransactionService;
import com.secondhand.common.exception.BusinessException;
import com.secondhand.model.vo.TransactionVO;
import com.secondhand.repository.TransactionMapper;
import com.secondhand.common.constants.TransactionType;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WalletServiceImpl extends ServiceImpl<WalletMapper, Wallet> implements WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public void initializeWallet(Long userId) {
        log.info("Initializing wallet for user: {}", userId);
        try {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(new BigDecimal("100.00")); // 初始金额100元
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(wallet);
            log.info("Wallet initialized successfully for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to initialize wallet for user: {}", userId, e);
            throw new RuntimeException("Failed to initialize wallet", e);
        }
    }

    @Override
    public Wallet getWalletByUserId(Long userId) {
        log.info("Getting wallet for user: {}", userId);
        LambdaQueryWrapper<Wallet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Wallet::getUserId, userId);
        Wallet wallet = walletMapper.selectOne(queryWrapper);
        if (wallet == null) {
            log.warn("Wallet not found for user: {}, initializing now.", userId);
            // 如果钱包不存在，则初始化一个
            initializeWallet(userId);
            // 再次查询以返回新创建的钱包
            wallet = walletMapper.selectOne(queryWrapper);
            if (wallet != null) {
                log.info("Wallet initialized and retrieved for user: {}", userId);
            } else {
                 log.error("Failed to retrieve wallet after initialization for user: {}", userId);
                 throw new RuntimeException("Failed to retrieve wallet after initialization");
            }
        }
        log.info("Successfully retrieved wallet for user: {}", userId);
        return wallet;
    }

    @Override
    public Wallet updateBalance(Long userId, BigDecimal amount) {
        log.info("Updating wallet balance for user: {}, amount: {}", userId, amount);
        Wallet wallet = getWalletByUserId(userId);
        if (wallet == null) {
            // This case should ideally not be reached due to getWalletByUserId logic,
            // but keeping the check for safety.
            log.error("Wallet not found for user {} in updateBalance.", userId);
            throw new RuntimeException("钱包不存在");
        }
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        int rows = walletMapper.updateById(wallet);
        log.info("Wallet balance updated for user {}: rows updated = {}", userId, rows);

        // 创建充值交易记录
        log.info("Attempting to create transaction record for user: {}, amount: {}", userId, amount);
        try {
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setAmount(amount);
            transaction.setType(1); // 1 表示充值
            transaction.setStatus(1); // 1 表示成功
            // createdAt 和 updatedAt 会在 createTransaction 方法中设置

            transactionService.createTransaction(transaction);
            log.info("Transaction record created successfully for user: {}", userId);
        } catch (Exception e) {
            // 记录异常，但不要 rethrow，因为余额已经更新成功
            log.error("Failed to create transaction record for user: {}, amount: {}", userId, amount, e);
        }

        return wallet;
    }

    @Override
    public BigDecimal getBalance(Long userId) {
        Wallet wallet = getWalletByUserId(userId);
        return wallet != null ? wallet.getBalance() : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public boolean recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }

        // 更新钱包余额
        Wallet wallet = updateBalance(userId, amount);

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(1); // 充值
        transaction.setStatus(1); // 成功
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        return true;
    }

    @Override
    @Transactional
    public boolean deduct(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("扣款金额必须大于0");
        }

        Wallet wallet = getWalletByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }

        // 更新钱包余额
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(amount.negate()); // 负数表示支出
        transaction.setType(2); // 扣款
        transaction.setStatus(1); // 成功
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        return true;
    }

    @Override
    public Page<TransactionVO> getTransactions(Long userId, int page, int size, String keyword, Integer type) {
        log.info("Getting transactions for user: {}, page: {}, size: {}, keyword: {}, type: {}", 
                userId, page, size, keyword, type);

        // 构建查询条件
        LambdaQueryWrapper<Transaction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Transaction::getUserId, userId);

        // 添加关键词搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(Transaction::getOrderId, keyword)
                .or()
                .like(Transaction::getAmount, keyword)
            );
        }

        // 添加交易类型筛选
        if (type != null) {
            queryWrapper.eq(Transaction::getType, type);
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Transaction::getCreatedAt);

        // 分页查询
        Page<Transaction> pageResult = transactionMapper.selectPage(new Page<>(page, size), queryWrapper);

        // 转换为VO
        Page<TransactionVO> voPage = new Page<>();
        BeanUtils.copyProperties(pageResult, voPage, "records");

        List<TransactionVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Transaction createTransaction(Transaction transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);
        return transaction;
    }

    private TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);

        // 设置交易类型文本
        switch (transaction.getType()) {
            case TransactionType.RECHARGE:
                vo.setTypeText("充值");
                break;
            case TransactionType.WITHDRAW:
                vo.setTypeText("提现");
                break;
            case TransactionType.PURCHASE:
                vo.setTypeText("商品购买");
                break;
            case TransactionType.SALE_INCOME:
                vo.setTypeText("商品出售收入");
                break;
            case TransactionType.REFUND_INCOME:
                vo.setTypeText("退款收入");
                break;
            case TransactionType.REFUND_EXPENSE:
                vo.setTypeText("退款支出");
                break;
            case TransactionType.FEE_INCOME:
                vo.setTypeText("手续费收入");
                break;
            default:
                vo.setTypeText("未知类型");
        }

        // 设置交易状态文本
        switch (transaction.getStatus()) {
            case 1:
                vo.setStatusText("成功");
                break;
            case 2:
                vo.setStatusText("失败");
                break;
            case 3:
                vo.setStatusText("处理中");
                break;
            default:
                vo.setStatusText("未知状态");
        }

        // 设置金额显示格式
        if (transaction.getAmount() != null) {
            vo.setAmountText(transaction.getAmount().toString());
        }

        // 设置时间显示格式
        if (transaction.getCreatedAt() != null) {
            vo.setCreatedAtText(transaction.getCreatedAt().toString());
        }
        if (transaction.getUpdatedAt() != null) {
            vo.setUpdatedAtText(transaction.getUpdatedAt().toString());
        }

        return vo;
    }

    @Override
    @Transactional
    public void addBalance(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }

        Wallet wallet = getWalletByUserId(userId);
        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(amount);
            wallet.setCreatedAt(LocalDateTime.now());
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.insert(wallet);
        } else {
            wallet.setBalance(wallet.getBalance().add(amount));
            wallet.setUpdatedAt(LocalDateTime.now());
            walletMapper.updateById(wallet);
        }
    }

    @Override
    @Transactional
    public void deductBalance(Long userId, BigDecimal amount, Integer transactionType) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("金额必须大于0");
        }

        Wallet wallet = getWalletByUserId(userId);
        if (wallet == null || wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletMapper.updateById(wallet);

        // 创建交易记录
        try {
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setAmount(amount.negate()); // 负数表示支出
            transaction.setType(transactionType);
            transaction.setStatus(1); // 成功
            transaction.setCreatedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());
            
            // 如果是商品购买类型的交易，设置订单ID
            if (transactionType == TransactionType.PURCHASE) {
                // 获取最新的订单ID
                Order order = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getCreatedAt)
                        .last("LIMIT 1")
                );
                if (order != null) {
                    transaction.setOrderId(order.getId());
                }
            }
            
            transactionService.createTransaction(transaction);
            log.info("创建扣款交易记录成功: userId={}, amount={}, type={}", userId, amount, transaction.getType());
        } catch (Exception e) {
            log.error("创建扣款交易记录失败: userId={}, amount={}, error={}", userId, amount, e.getMessage());
            throw new BusinessException("创建交易记录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void freezeBalance(Long userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserId(userId);
        if (wallet == null) {
            throw new BusinessException("钱包不存在");
        }
        
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("余额不足");
        }
        
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        updateById(wallet);
    }
} 