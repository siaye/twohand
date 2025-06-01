package com.secondhand.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.secondhand.common.response.Result;
import com.secondhand.model.dto.OrderCreateDTO;
import com.secondhand.model.vo.OrderVO;
import com.secondhand.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public Result<OrderVO> createOrder(@RequestBody OrderCreateDTO orderDTO) {
        logger.info("创建订单，订单信息：{}", orderDTO);
        OrderVO order = orderService.createOrder(orderDTO);
        return Result.success(order);
    }
    
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(orderService.getOrderDetail(id));
    }
    
    @GetMapping("/user")
    public Result<Page<OrderVO>> getUserOrders(
            @RequestAttribute Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        logger.info("获取用户订单列表，用户ID：{}", userId);
        Page<OrderVO> orders = orderService.getUserOrders(userId, status, page, size);
        return Result.success(orders);
    }
    
    @GetMapping("/seller")
    public Result<Page<OrderVO>> getSellerOrders(
            @RequestAttribute Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        logger.info("获取卖家订单列表，卖家ID：{}", userId);
        Page<OrderVO> orders = orderService.getSellerOrders(userId, status, page, size);
        return Result.success(orders);
    }
    
    @PostMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId) {
        logger.info("支付订单，订单ID：{}", orderId);
        orderService.payOrder(orderId);
        return Result.success(null);
    }
    
    @PostMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        logger.info("发货订单，订单ID：{}", orderId);
        orderService.shipOrder(orderId);
        return Result.success(null);
    }
    
    @PostMapping("/{orderId}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Long orderId) {
        logger.info("确认收货，订单ID：{}", orderId);
        orderService.confirmReceipt(orderId);
        return Result.success(null);
    }
    
    @PostMapping("/{orderId}/refund")
    public Result<Void> refundOrder(@PathVariable Long orderId) {
        logger.info("申请退款，订单ID：{}", orderId);
        orderService.refundOrder(orderId);
        return Result.success(null);
    }
    
    @PostMapping("/{orderId}/confirm-refund")
    public Result<Void> confirmRefund(@PathVariable Long orderId) {
        logger.info("确认退款，订单ID：{}", orderId);
        orderService.confirmRefund(orderId);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelOrder(@PathVariable Long id) {
        return Result.success(orderService.cancelOrder(id));
    }
} 