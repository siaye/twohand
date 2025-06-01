package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.entity.Coupon;
import com.secondhand.model.entity.UserCoupon;
import java.math.BigDecimal;
import java.util.List;

public interface CouponService extends IService<Coupon> {

    /**
     * 获取用户可用的优惠券列表
     * @param userId 用户ID
     * @return 可用优惠券列表
     */
    List<UserCoupon> getAvailableCoupons(Long userId);

    /**
     * 获取优惠券详情
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    UserCoupon getCouponDetail(Long couponId);

    /**
     * 验证优惠券并计算折扣金额
     * @param couponId 用户优惠券ID (UserCoupon的ID)
     * @param userId 用户ID
     * @param orderAmount 订单总金额
     * @return 优惠券面值，如果无效返回 BigDecimal.ZERO
     */
    BigDecimal validateAndCalculateDiscount(Long couponId, Long userId, BigDecimal orderAmount);

    /**
     * 使用优惠券
     * @param userCouponId 用户优惠券ID (UserCoupon的ID)
     */
    void useCoupon(Long userCouponId);

    /**
     * 根据用户优惠券ID获取详细信息（包括Coupon信息）
     * @param userCouponId 用户优惠券ID
     * @return 用户优惠券实体（包含关联Coupon信息）
     */
    UserCoupon getUserCouponById(Long userCouponId);

    /**
     * 创建优惠券模板
     * @param coupon 优惠券实体
     * @return 创建后的优惠券实体
     */
    Coupon createCoupon(Coupon coupon);

    /**
     * 发放优惠券给用户
     * @param userId 用户ID
     * @param couponId 优惠券ID
     */
    void issueCouponToUser(Long userId, Long couponId);

    /**
     * 批量发放优惠券给用户
     * @param userIds 用户ID列表
     * @param couponId 优惠券ID
     */
    void issueCouponsToUsers(List<Long> userIds, Long couponId);

    /**
     * 发放新人优惠券
     * @param userId 用户ID
     */
    void issueNewUserCoupon(Long userId);

    /**
     * 发放生日优惠券
     * @param userId 用户ID
     */
    void issueBirthdayCoupon(Long userId);

    /**
     * 发放积分奖励优惠券
     * @param userId 用户ID
     * @param points 积分数量
     */
    void issuePointsRewardCoupon(Long userId, Integer points);

    /**
     * 获取今日可领取的优惠券列表
     * @return 可领取的优惠券列表
     */
    List<Coupon> getTodayAvailableCoupons();

    /**
     * 领取每日优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 领取结果
     */
    UserCoupon claimDailyCoupon(Long userId, Long couponId);

    /**
     * 生成每日优惠券
     * 该方法由定时任务调用
     */
    void generateDailyCoupons();
} 