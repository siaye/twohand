package com.secondhand.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemVO {
    private Long id;  // 订单项ID
    private Long orderId;  // 订单ID
    private Long productId;  // 商品ID
    private String productTitle;  // 商品标题
    private String productImage;  // 商品图片
    private Integer quantity;  // 购买数量
    private BigDecimal price;  // 商品单价
    private BigDecimal totalAmount;  // 总金额
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
    
    public BigDecimal getSubtotal() {
        if (price == null || quantity == null) return BigDecimal.ZERO;
        return price.multiply(new BigDecimal(quantity));
    }
} 