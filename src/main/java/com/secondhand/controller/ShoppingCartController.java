package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.model.vo.ShoppingCartVO;
import com.secondhand.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody Map<String, Object> params) {
        Long userId = jwtUtil.getCurrentUserId();
        Long productId = Long.valueOf(params.get("productId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        
        shoppingCartService.addToCart(userId, productId, quantity);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<ShoppingCartVO>> getCartList() {
        Long userId = jwtUtil.getCurrentUserId();
        List<ShoppingCartVO> cartList = shoppingCartService.getCartList(userId);
        return Result.success(cartList);
    }

    @PutMapping("/update")
    public Result<Void> updateQuantity(@RequestBody Map<String, Object> params) {
        Long userId = jwtUtil.getCurrentUserId();
        Long cartId = Long.valueOf(params.get("cartId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        
        shoppingCartService.updateQuantity(userId, cartId, quantity);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> removeFromCart(@PathVariable Long id) {
        Long userId = jwtUtil.getCurrentUserId();
        shoppingCartService.removeFromCart(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        Long userId = jwtUtil.getCurrentUserId();
        shoppingCartService.clearCart(userId);
        return Result.success(null);
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long userId = jwtUtil.getCurrentUserId();
        Long cartId = Long.valueOf(params.get("cartId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        
        shoppingCartService.updateStatus(userId, cartId, status);
        return Result.success(null);
    }
} 