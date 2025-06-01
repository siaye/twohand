package com.secondhand.service;

import com.secondhand.model.dto.ReviewCreateDTO;
import com.secondhand.model.vo.ReviewVO;
import java.util.List;

public interface ReviewService {
    /**
     * 创建评价
     * @param reviewDTO 评价创建DTO
     * @return 创建的评价
     */
    ReviewVO createReview(ReviewCreateDTO reviewDTO);
    
    /**
     * 获取商品评价列表
     * @param productId 商品ID
     * @return 评价列表
     */
    List<ReviewVO> getProductReviews(Long productId);
    
    /**
     * 获取用户评价列表
     * @param userId 用户ID
     * @return 评价列表
     */
    List<ReviewVO> getUserReviews(Long userId);
    
    /**
     * 获取订单评价
     * @param orderId 订单ID
     * @return 评价列表
     */
    List<ReviewVO> getOrderReviews(Long orderId);
    
    /**
     * 删除评价
     * @param reviewId 评价ID
     * @param userId 用户ID
     */
    void deleteReview(Long reviewId, Long userId);
} 