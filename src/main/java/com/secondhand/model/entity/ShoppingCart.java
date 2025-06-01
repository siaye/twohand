package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("shopping_cart")
public class ShoppingCart {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long productId;
    
    private Integer quantity;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer status; // 1-正常，2-已选中，3-已失效
} 