package com.secondhand.service.impl;

import com.secondhand.common.exception.BusinessException;
import com.secondhand.model.dto.ReviewCreateDTO;
import com.secondhand.model.entity.Order;
import com.secondhand.model.entity.Product;
import com.secondhand.model.entity.ProductReview;
import com.secondhand.model.entity.User;
import com.secondhand.model.vo.ReviewVO;
import com.secondhand.repository.OrderMapper;
import com.secondhand.repository.ProductMapper;
import com.secondhand.repository.ProductReviewMapper;
import com.secondhand.repository.UserMapper;
import com.secondhand.service.ReviewService;
import com.secondhand.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    
    @Autowired
    private ProductReviewMapper reviewMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public ReviewVO createReview(ReviewCreateDTO reviewDTO) {
        // 1. 验证订单状态
        Order order = orderMapper.selectById(reviewDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("只能评价已完成的订单");
        }
        
        // 2. 验证是否已评价
        List<ProductReview> existingReviews = reviewMapper.selectByOrderId(reviewDTO.getOrderId());
        if (!existingReviews.isEmpty()) {
            throw new BusinessException("该订单已评价");
        }
        
        // 3. 创建评价
        ProductReview review = new ProductReview();
        review.setProductId(reviewDTO.getProductId());
        review.setUserId(SecurityUtils.getCurrentUserId());
        review.setOrderId(reviewDTO.getOrderId());
        review.setRating(reviewDTO.getRating());
        review.setContent(reviewDTO.getContent());
        review.setImages(reviewDTO.getImages());
        
        reviewMapper.insert(review);
        
        return convertToVO(review);
    }
    
    @Override
    public List<ReviewVO> getProductReviews(Long productId) {
        List<ProductReview> reviews = reviewMapper.selectByProductId(productId);
        return reviews.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewVO> getUserReviews(Long userId) {
        List<ProductReview> reviews = reviewMapper.selectByUserId(userId);
        return reviews.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public List<ReviewVO> getOrderReviews(Long orderId) {
        List<ProductReview> reviews = reviewMapper.selectByOrderId(orderId);
        return reviews.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 1. 验证评价是否存在
        ProductReview review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        
        // 2. 验证权限：只能删除自己的评价
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人的评价");
        }
        
        // 3. 删除评价
        reviewMapper.deleteById(reviewId);
    }
    
    private ReviewVO convertToVO(ProductReview review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);
        
        // 获取商品信息
        Product product = productMapper.selectById(review.getProductId());
        if (product != null) {
            vo.setProductName(product.getTitle());
            // 获取卖家信息
            User seller = userMapper.selectById(product.getSellerId());
            if (seller != null) {
                vo.setSellerName(seller.getUsername());
                vo.setSellerAvatar(seller.getAvatar());
            }
        }
        
        // 获取用户信息
        User user = userMapper.selectById(review.getUserId());
        if (user != null) {
            vo.setUserName(user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
        
        return vo;
    }
} 