package com.secondhand.service;

import com.secondhand.model.vo.OrderVO;
import com.secondhand.model.vo.PageResult;
import com.secondhand.model.entity.User;

public interface AdminOrderService {
    /**
     * 获取订单列表
     * @param page 页码
     * @param size 每页大小
     * @param orderNo 订单号
     * @param productTitle 商品名称
     * @param buyerName 买家名称
     * @param sellerName 卖家名称
     * @param status 订单状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    PageResult<OrderVO> getOrderList(int page, int size, String orderNo, String productTitle, String buyerName, String sellerName, Integer status, String startTime, String endTime);

    /**
     * 获取订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    OrderVO getOrderDetail(Long id);

    /**
     * 更新订单状态
     * @param id 订单ID
     * @param status 订单状态
     */
    void updateOrderStatus(Long id, Integer status);

    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);
} 