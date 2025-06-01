package com.secondhand.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String size;
    private Integer stock;
    private Integer salesCount;
    private Integer status;
    private Integer auditStatus;      // 审核状态：0-待审核，1-已通过，2-已拒绝
    private String auditReason;       // 审核原因
    private LocalDateTime auditTime;  // 审核时间
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private Long categoryId;
    private String categoryName;
    private String images;
    private List<String> imageList;
    private String image;  // 主图
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String brand;              // 品牌
    private String color;              // 颜色
    private String productCondition;   // 修改字段名
    private String location;           // 位置
    private Boolean isNegotiable;      // 是否允许议价
    private String tags;               // 商品标签

    // 获取状态文本
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "在售";
            case 2: return "已下架";
            case 3: return "已售罄";
            default: return "未知";
        }
    }

    // 获取审核状态文本
    public String getAuditStatusText() {
        if (auditStatus == null) return "未知";
        switch (auditStatus) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }

    // 格式化销量
    public String getFormattedSales() {
        if (salesCount == null) {
            return "0";
        }
        if (salesCount >= 10000) {
            return String.format("%.1f万", salesCount / 10000.0);
        }
        return salesCount.toString();
    }

    // 计算折扣率
    public BigDecimal getDiscountRate() {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ONE;
        }
        return price.divide(originalPrice, 2, RoundingMode.HALF_UP);
    }
} 