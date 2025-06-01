package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProductReviewMapper extends BaseMapper<ProductReview> {
    
    @Select("SELECT * FROM product_review WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<ProductReview> selectByProductId(Long productId);
    
    @Select("SELECT * FROM product_review WHERE order_id = #{orderId}")
    List<ProductReview> selectByOrderId(Long orderId);
    
    @Select("SELECT * FROM product_review WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ProductReview> selectByUserId(Long userId);
} 