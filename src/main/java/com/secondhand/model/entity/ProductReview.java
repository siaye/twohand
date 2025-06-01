package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_review")
public class ProductReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long productId;
    
    private Long userId;
    
    private Long orderId;
    
    private Integer rating;
    
    private String content;
    
    private String images;
    
    private LocalDateTime createdAt;
} 