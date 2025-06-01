package com.secondhand.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;  // 订单ID
    private Long userId;  // 买家ID
    private String buyerName;  // 买家姓名
    private Long sellerId;  // 卖家ID
    private String sellerName;  // 卖家姓名
    private BigDecimal totalAmount;  // 订单总金额
    private Integer status;  // 订单状态：0-待付款，1-待发货，2-待收货，3-已完成，4-退款中，5-已取消
    private Integer paymentStatus;  // 支付状态：0-未支付，1-已支付，2-已退款
    private Integer paymentType;  // 支付方式：1-平台余额，2-线下交易
    private String paymentTypeText;  // 支付方式文本
    private String statusText;  // 订单状态文本
    private String address;  // 收货地址
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;
    private List<OrderItemVO> items;  // 订单项列表
    
    public String getPaymentTypeText() {
        if (paymentType == null) return "";
        return switch (paymentType) {
            case 1 -> "余额支付";
            case 2 -> "线下交易";
            default -> "未知";
        };
    }

    // 获取状态文本
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待付款";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "已退款";
            default: return "未知";
        }
    }
} 