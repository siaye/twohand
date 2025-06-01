package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.entity.ShoppingCart;
import com.secondhand.model.vo.ShoppingCartVO;
import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    // 添加商品到购物车
    void addToCart(Long userId, Long productId, Integer quantity);
    
    // 获取用户的购物车列表
    List<ShoppingCartVO> getCartList(Long userId);
    
    // 更新购物车商品数量
    void updateQuantity(Long userId, Long cartId, Integer quantity);
    
    // 删除购物车商品
    void removeFromCart(Long userId, Long cartId);
    
    // 清空购物车
    void clearCart(Long userId);
    
    // 更新购物车商品状态
    void updateStatus(Long userId, Long cartId, Integer status);
} 