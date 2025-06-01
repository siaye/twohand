package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.entity.Wallet;
import java.math.BigDecimal;
import com.secondhand.model.entity.Transaction;
import com.secondhand.model.vo.TransactionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface WalletService extends IService<Wallet> {
    /**
     * 为用户初始化钱包
     * @param userId 用户ID
     */
    void initializeWallet(Long userId);

    /**
     * 根据用户ID获取钱包信息
     * @param userId 用户ID
     * @return 钱包实体
     */
    Wallet getWalletByUserId(Long userId);

    /**
     * 更新钱包余额
     * @param userId 用户ID
     * @param amount 变动金额，正数表示增加，负数表示减少
     * @return 更新后的钱包实体
     */
    Wallet updateBalance(Long userId, BigDecimal amount);

    /**
     * 获取用户钱包余额
     * @param userId 用户ID
     * @return 钱包余额
     */
    BigDecimal getBalance(Long userId);

    /**
     * 充值
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 是否充值成功
     */
    boolean recharge(Long userId, BigDecimal amount);

    /**
     * 扣款
     * @param userId 用户ID
     * @param amount 扣款金额
     * @return 是否扣款成功
     */
    boolean deduct(Long userId, BigDecimal amount);

    /**
     * 获取用户交易记录
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词（可选）
     * @param type 交易类型（可选）
     * @return 交易记录分页列表
     */
    Page<TransactionVO> getTransactions(Long userId, int page, int size, String keyword, Integer type);

    /**
     * 创建交易记录
     * @param transaction 交易记录实体
     * @return 创建的交易记录
     */
    Transaction createTransaction(Transaction transaction);

    /**
     * 增加余额
     */
    void addBalance(Long userId, BigDecimal amount);
    
    /**
     * 扣除余额
     * @param userId 用户ID
     * @param amount 扣除金额
     * @param transactionType 交易类型
     */
    void deductBalance(Long userId, BigDecimal amount, Integer transactionType);
    
    /**
     * 冻结余额
     */
    void freezeBalance(Long userId, BigDecimal amount);
} 