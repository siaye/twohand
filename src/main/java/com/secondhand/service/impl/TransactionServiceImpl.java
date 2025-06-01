package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.model.entity.Transaction;
import com.secondhand.model.vo.TransactionVO;
import com.secondhand.repository.TransactionMapper;
import com.secondhand.service.TransactionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements TransactionService {

    @Override
    public void createTransaction(Transaction transaction) {
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        save(transaction);
    }

    @Override
    public Page<TransactionVO> getTransactionsByUserId(Long userId, int page, int size, String keyword, Integer type) {
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
        Page<Transaction> pageResult = page(new Page<>(page, size), queryWrapper);
        
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
    public TransactionVO convertToVO(Transaction transaction) {
        TransactionVO vo = new TransactionVO();
        BeanUtils.copyProperties(transaction, vo);

        // 设置交易类型文本
        switch (transaction.getType()) {
            case 1:
                vo.setTypeText("充值");
                break;
            case 2:
                vo.setTypeText("提现");
                break;
            case 3:
                vo.setTypeText("商品购买");
                break;
            case 4:
                vo.setTypeText("商品出售收入");
                break;
            case 5:
                vo.setTypeText("退款收入");
                break;
            case 6:
                vo.setTypeText("退款支出");
                break;
            case 7:
                vo.setTypeText("平台手续费");
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
} 