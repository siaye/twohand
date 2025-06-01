package com.secondhand.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionVO {
    private Long id;
    private Long userId;
    private Long orderId;
    private BigDecimal amount;
    private String amountText;  // 格式化后的金额文本
    private Integer type; // 交易类型
    private String typeText; // 交易类型文本
    private Integer status; // 交易状态
    private String statusText; // 交易状态文本
    private LocalDateTime createdAt; // 创建时间原始值
    private String createdAtText; // 格式化后的创建时间
    private LocalDateTime updatedAt; // 更新时间原始值
    private String updatedAtText; // 格式化后的更新时间
    
    // 关联信息
    private String orderNo;     // 订单编号
    private String productName; // 商品名称（如果是商品交易）
    private String remark;      // 交易备注
} 