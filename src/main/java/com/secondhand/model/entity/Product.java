package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long sellerId;  // 卖家ID
    private String title;   // 商品名称
    private String description;  // 商品描述
    private BigDecimal price;  // 商品价格
    private BigDecimal originalPrice;  // 原价
    private Long categoryId;  // 商品分类ID
    private String brand;              // 品牌
    private String size;  // 商品尺寸
    private String color;              // 颜色
    @TableField("`condition`")
    private String productCondition;  // 修改字段名，但保持数据库列名不变
    private String location;           // 位置
    private String images;  // 商品图片，JSON数组格式
    private Integer stock;  // 库存数量
    private Integer salesCount;  // 销售数量
    private Integer status;  // 商品状态：0-待审核，1-已上架，2-已下架，3-已售罄
    @TableField("audit_status")
    private Integer auditStatus;  // 审核状态：0-待审核，1-已通过，2-已拒绝
    @TableField("audit_reason")
    private String auditReason;  // 审核原因
    @TableField("audit_time")
    private LocalDateTime auditTime;  // 审核时间
    private Boolean isNegotiable;      // 是否允许议价
    private String tags;               // 商品标签
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    // 非数据库字段
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String sellerName;
    
    @TableField(exist = false)
    private String sellerAvatar;
    
    @TableField(exist = false)
    private List<String> imageList;
} 