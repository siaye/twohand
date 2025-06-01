package com.secondhand.model.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {
    /**
     * 收货地址
     */
    @NotNull(message = "收货地址不能为空")
    private String address;

    /**
     * 支付方式（1：余额支付，2：支付宝，3：微信支付）
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentType;

    /**
     * 卖家ID
     */
    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    /**
     * 订单商品列表
     */
    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemDTO> items;

    private BigDecimal totalAmount;

    private Long couponId;

    /**
     * 使用的积分数量
     */
    private Integer pointsToUse;
}

