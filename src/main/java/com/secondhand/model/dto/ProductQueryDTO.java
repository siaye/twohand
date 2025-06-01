package com.secondhand.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductQueryDTO {
    private Integer current = 1;
    private Integer size = 12;
    private String keyword;
    private Long categoryId;
    private String brand;
    private String productCondition;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<String> tags;
    private String location;
    private String sortBy;
} 