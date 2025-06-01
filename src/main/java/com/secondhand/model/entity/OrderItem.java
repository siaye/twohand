package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {
    /**
     * 订单项ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品标题
     */
    private String productTitle;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    // 计算小计金额
    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }
} 