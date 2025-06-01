package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.dto.ReviewCreateDTO;
import com.secondhand.model.vo.ReviewVO;
import com.secondhand.service.ReviewService;
import com.secondhand.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    public Result<ReviewVO> createReview(@RequestBody ReviewCreateDTO reviewDTO) {
        logger.info("创建评价，评价信息：{}", reviewDTO);
        return Result.success(reviewService.createReview(reviewDTO));
    }
    
    @GetMapping("/product/{productId}")
    public Result<List<ReviewVO>> getProductReviews(@PathVariable Long productId) {
        return Result.success(reviewService.getProductReviews(productId));
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<ReviewVO>> getUserReviews(@PathVariable Long userId) {
        logger.info("获取用户评价列表，用户ID：{}", userId);
        
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        logger.info("当前登录用户ID：{}", currentUserId);
        
        // 验证权限：只能查看自己的评价
        if (!currentUserId.equals(userId)) {
            logger.warn("用户 {} 尝试查看用户 {} 的评价列表", currentUserId, userId);
            return Result.error("无权查看其他用户的评价");
        }
        
        return Result.success(reviewService.getUserReviews(userId));
    }
    
    @GetMapping("/order/{orderId}")
    public Result<List<ReviewVO>> getOrderReviews(@PathVariable Long orderId) {
        return Result.success(reviewService.getOrderReviews(orderId));
    }
    
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long reviewId) {
        logger.info("删除评价，评价ID：{}", reviewId);
        
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        logger.info("当前登录用户ID：{}", currentUserId);
        
        // 验证权限：只能删除自己的评价
        reviewService.deleteReview(reviewId, currentUserId);
        return Result.success(null);
    }
} 