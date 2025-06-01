package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.model.entity.Transaction;
import com.secondhand.model.vo.TransactionVO;

public interface TransactionService extends IService<Transaction> {
    /**
     * 创建交易记录
     */
    void createTransaction(Transaction transaction);

    /**
     * 获取用户的交易记录
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @param keyword 搜索关键词（可选）
     * @param type 交易类型（可选）
     * @return 交易记录分页列表
     */
    Page<TransactionVO> getTransactionsByUserId(Long userId, int page, int size, String keyword, Integer type);

    /**
     * 将交易记录实体转换为VO
     * @param transaction 交易记录实体
     * @return 交易记录VO
     */
    TransactionVO convertToVO(Transaction transaction);
} 