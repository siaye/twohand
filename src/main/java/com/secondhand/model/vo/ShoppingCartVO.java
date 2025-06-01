package com.secondhand.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShoppingCartVO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String sellerName;  // 卖家姓名
    
    // 获取状态文本
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 1: return "正常";
            case 2: return "已选中";
            case 3: return "已失效";
            default: return "未知";
        }
    }
} 