package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.secondhand.model.entity.Product;
import com.secondhand.model.entity.User;
import com.secondhand.model.entity.Order;
import com.secondhand.service.AdminService;
import com.secondhand.service.ProductService;
import com.secondhand.service.UserService;
import com.secondhand.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        // 获取今日和昨日的开始和结束时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime yesterdayStart = todayStart.minusDays(1);
        LocalDateTime yesterdayEnd = todayEnd.minusDays(1);

        // 1. 用户统计
        long totalUsers = userService.count();
        long todayNewUsers = userService.count(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, todayStart, todayEnd));
        long yesterdayNewUsers = userService.count(new LambdaQueryWrapper<User>()
                .between(User::getCreateTime, yesterdayStart, yesterdayEnd));
        
        double userGrowth = yesterdayNewUsers == 0 ? 100 : 
                ((double) (todayNewUsers - yesterdayNewUsers) / yesterdayNewUsers) * 100;

        // 2. 商品统计
        long totalProducts = productService.count();
        long todayNewProducts = productService.count(new LambdaQueryWrapper<Product>()
                .between(Product::getCreatedAt, todayStart, todayEnd));
        long yesterdayNewProducts = productService.count(new LambdaQueryWrapper<Product>()
                .between(Product::getCreatedAt, yesterdayStart, yesterdayEnd));
        
        double productGrowth = yesterdayNewProducts == 0 ? 100 :
                ((double) (todayNewProducts - yesterdayNewProducts) / yesterdayNewProducts) * 100;

        // 3. 订单统计
        long totalOrders = orderService.count();
        long todayNewOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .between(Order::getCreatedAt, todayStart, todayEnd));
        long yesterdayNewOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .between(Order::getCreatedAt, yesterdayStart, yesterdayEnd));
        
        double orderGrowth = yesterdayNewOrders == 0 ? 100 :
                ((double) (todayNewOrders - yesterdayNewOrders) / yesterdayNewOrders) * 100;

        // 4. 交易额统计
        BigDecimal totalAmount = orderService.getTotalAmount();
        BigDecimal todayAmount = orderService.getTotalAmount(new LambdaQueryWrapper<Order>()
                .between(Order::getCreatedAt, todayStart, todayEnd));
        BigDecimal yesterdayAmount = orderService.getTotalAmount(new LambdaQueryWrapper<Order>()
                .between(Order::getCreatedAt, yesterdayStart, yesterdayEnd));
        
        double amountGrowth = yesterdayAmount.compareTo(BigDecimal.ZERO) == 0 ? 100 :
                todayAmount.subtract(yesterdayAmount).divide(yesterdayAmount, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal("100")).doubleValue();

        // 5. 用户增长趋势（最近7天）
        List<Map<String, Object>> userTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MAX);
            long count = userService.count(new LambdaQueryWrapper<User>()
                    .between(User::getCreateTime, start, end));
            
            Map<String, Object> point = new HashMap<>();
            point.put("date", start.toLocalDate().toString());
            point.put("value", count);
            userTrend.add(point);
        }

        // 6. 商品增长趋势（最近7天）
        List<Map<String, Object>> productTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime start = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(LocalDate.now().minusDays(i), LocalTime.MAX);
            long count = productService.count(new LambdaQueryWrapper<Product>()
                    .between(Product::getCreatedAt, start, end));
            
            Map<String, Object> point = new HashMap<>();
            point.put("date", start.toLocalDate().toString());
            point.put("value", count);
            productTrend.add(point);
        }

        // 组装统计数据
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", totalUsers);
        statistics.put("userGrowth", userGrowth);
        statistics.put("totalProducts", totalProducts);
        statistics.put("productGrowth", productGrowth);
        statistics.put("totalOrders", totalOrders);
        statistics.put("orderGrowth", orderGrowth);
        statistics.put("totalAmount", totalAmount);
        statistics.put("amountGrowth", amountGrowth);

        // 组装图表数据
        Map<String, Object> charts = new HashMap<>();
        charts.put("userTrend", userTrend);
        charts.put("productTrend", productTrend);

        data.put("statistics", statistics);
        data.put("charts", charts);

        return data;
    }
} 