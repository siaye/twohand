package com.secondhand.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewVO {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
    private LocalDateTime createdAt;
} 