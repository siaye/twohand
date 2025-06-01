package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.vo.OrderVO;
import com.secondhand.model.vo.PageResult;
import com.secondhand.service.AdminOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    @Autowired
    private AdminOrderService adminOrderService;

    // 获取订单列表
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> getOrderList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String productTitle,
            @RequestParam(required = false) String buyerName,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<OrderVO> result = adminOrderService.getOrderList(page, size, orderNo, productTitle, buyerName, sellerName, status, startTime, endTime);
        return Result.success(result);
    }

    // 获取订单详情
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO order = adminOrderService.getOrderDetail(id);
        return Result.success(order);
    }

    // 更新订单状态
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        adminOrderService.updateOrderStatus(id, status);
        return Result.success(null);
    }

    // 发货
    @PostMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id) {
        adminOrderService.updateOrderStatus(id, 2);
        return Result.success(null);
    }

    // 确认收货
    @PostMapping("/{id}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Long id) {
        adminOrderService.updateOrderStatus(id, 3);
        return Result.success(null);
    }

    // 确认退款
    @PostMapping("/{id}/confirm-refund")
    public Result<Void> confirmRefund(@PathVariable Long id) {
        adminOrderService.updateOrderStatus(id, 6);
        return Result.success(null);
    }
} 