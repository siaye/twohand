package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.common.exception.BusinessException;
import com.secondhand.model.entity.Product;
import com.secondhand.model.entity.ShoppingCart;
import com.secondhand.model.entity.User;
import com.secondhand.model.vo.ShoppingCartVO;
import com.secondhand.repository.ProductMapper;
import com.secondhand.repository.ShoppingCartMapper;
import com.secondhand.repository.UserMapper;
import com.secondhand.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public void addToCart(Long userId, Long productId, Integer quantity) {
        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        
        // 检查商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架或不可购买");
        }
        
        // 检查库存
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
        
        // 检查是否已在购物车中
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
               .eq(ShoppingCart::getProductId, productId);
        ShoppingCart existCart = this.getOne(wrapper);
        
        if (existCart != null) {
            // 更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            this.updateById(existCart);
        } else {
            // 新增购物车项
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setStatus(1);
            this.save(cart);
        }
    }

    @Override
    public List<ShoppingCartVO> getCartList(Long userId) {
        // 查询用户的购物车列表
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
               .orderByDesc(ShoppingCart::getCreatedAt);
        List<ShoppingCart> cartList = this.list(wrapper);
        
        // 转换为VO
        return cartList.stream().map(cart -> {
            ShoppingCartVO vo = new ShoppingCartVO();
            BeanUtils.copyProperties(cart, vo);
            
            // 获取商品信息
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                vo.setProductTitle(product.getTitle());
                // 处理图片，取第一张图片作为主图
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    String[] imageArray = product.getImages().split(",");
                    if (imageArray.length > 0) {
                        vo.setProductImage(imageArray[0]);
                    }
                }
                vo.setPrice(product.getPrice());
                vo.setSubtotal(product.getPrice().multiply(new java.math.BigDecimal(cart.getQuantity())));
                
                // 获取卖家信息
                User seller = userMapper.selectById(product.getSellerId());
                if (seller != null) {
                    vo.setSellerName(seller.getUsername());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        // 检查购物车项是否存在
        ShoppingCart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        
        // 检查商品库存
        Product product = productMapper.selectById(cart.getProductId());
        if (product == null || product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
        
        // 更新数量
        cart.setQuantity(quantity);
        this.updateById(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(Long userId, Long cartId) {
        // 检查购物车项是否存在
        ShoppingCart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        
        // 删除购物车项
        this.removeById(cartId);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        // 删除用户的所有购物车项
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        this.remove(wrapper);
    }

    @Override
    @Transactional
    public void updateStatus(Long userId, Long cartId, Integer status) {
        // 检查购物车项是否存在
        ShoppingCart cart = this.getById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车项不存在");
        }
        
        // 更新状态
        cart.setStatus(status);
        this.updateById(cart);
    }
} 