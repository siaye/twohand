package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transaction")
public class Transaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private Integer type; // 交易类型，例如 1-充值, 2-提现, 3-商品购买, 4-商品出售收入
    private Integer status; // 交易状态，例如 1-成功, 2-失败, 3-处理中
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 