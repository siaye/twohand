package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 根据用户ID查询订单列表
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    @Select("<script>" +
            "SELECT id, order_sn, user_id, seller_id, total_amount, pay_amount, status, address, receiver_address, " +
            "note, confirm_status, delete_status, payment_time, delivery_time, receive_time, comment_time, " +
            "payment_type, coupon_id, points_used, created_at, updated_at " +
            "FROM `order` " +
            "WHERE user_id = #{userId} " +
            "<if test='status != null'>" +
            "AND `status` = #{status} " +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<Order> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 根据卖家ID查询订单列表
     * @param sellerId 卖家ID
     * @param status 订单状态
     * @return 订单列表
     */
    @Select("<script>" +
            "SELECT id, order_sn, user_id, seller_id, total_amount, pay_amount, status, address, receiver_address, " +
            "note, confirm_status, delete_status, payment_time, delivery_time, receive_time, comment_time, " +
            "payment_type, coupon_id, points_used, created_at, updated_at " +
            "FROM `order` " +
            "WHERE seller_id = #{sellerId} " +
            "<if test='status != null'>" +
            "AND `status` = #{status} " +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    List<Order> selectBySellerId(@Param("sellerId") Long sellerId, @Param("status") Integer status);
} 