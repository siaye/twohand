package com.secondhand.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderItemDTO {
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 商品数量
     */
    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量必须大于0")
    private Integer quantity;

    /**
     * 商品单价
     */
    @NotNull(message = "商品单价不能为空")
    private BigDecimal price;
} 