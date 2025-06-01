package com.secondhand.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.dto.OrderCreateDTO;
import com.secondhand.model.entity.Order;
import com.secondhand.model.vo.OrderVO;

import java.math.BigDecimal;

public interface OrderService extends IService<Order> {
    /**
     * 创建订单
     * @param orderDTO 订单创建DTO
     * @return 创建的订单
     */
    OrderVO createOrder(OrderCreateDTO orderDTO);
    
    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单详情VO
     */
    OrderVO getOrderDetail(Long orderId);
    
    /**
     * 支付订单
     * @param orderId 订单ID
     */
    void payOrder(Long orderId);
    
    /**
     * 发货
     * @param orderId 订单ID
     */
    void shipOrder(Long orderId);
    
    /**
     * 确认收货
     * @param orderId 订单ID
     */
    void confirmReceipt(Long orderId);
    
    /**
     * 申请退款
     * @param orderId 订单ID
     */
    void refundOrder(Long orderId);
    
    /**
     * 确认退款
     * @param orderId 订单ID
     */
    void confirmRefund(Long orderId);
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 是否取消成功
     */
    boolean cancelOrder(Long orderId);
    
    /**
     * 获取卖家订单列表
     * @param sellerId 卖家ID
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderVO> getSellerOrders(Long sellerId, Integer status, Integer page, Integer size);
    
    /**
     * 获取买家订单列表
     * @param userId 用户ID
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 订单列表
     */
    Page<OrderVO> getUserOrders(Long userId, Integer status, Integer page, Integer size);

    /**
     * 统计订单数量
     * @param wrapper 查询条件
     * @return 订单数量
     */
    long count(Wrapper<Order> wrapper);

    /**
     * 获取订单总金额
     * @param wrapper 查询条件
     * @return 订单总金额
     */
    BigDecimal getTotalAmount(Wrapper<Order> wrapper);

    /**
     * 获取所有订单总金额
     * @return 订单总金额
     */
    BigDecimal getTotalAmount();
} 