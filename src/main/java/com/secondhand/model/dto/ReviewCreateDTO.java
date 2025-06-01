package com.secondhand.model.dto;

import lombok.Data;

@Data
public class ReviewCreateDTO {
    private Long orderId;
    private Long productId;
    private Integer rating;
    private String content;
    private String images;
} 