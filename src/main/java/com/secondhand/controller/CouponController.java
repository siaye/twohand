package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.entity.Coupon;
import com.secondhand.model.entity.UserCoupon;
import com.secondhand.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping("/{userId}/coupons/available")
    public Result<List<UserCoupon>> getAvailableCoupons(@PathVariable Long userId) {
        try {
            List<UserCoupon> coupons = couponService.getAvailableCoupons(userId);
            return Result.success(coupons);
        } catch (Exception e) {
            return Result.error("获取可用优惠券失败");
        }
    }

    @GetMapping("/coupons/{couponId}")
    public Result<UserCoupon> getCouponDetail(@PathVariable Long couponId) {
        try {
            UserCoupon coupon = couponService.getCouponDetail(couponId);
            return Result.success(coupon);
        } catch (Exception e) {
            return Result.error("获取优惠券详情失败");
        }
    }

    // 管理员接口
    @PostMapping("/admin/coupons")
    public Result<Coupon> createCoupon(@RequestBody Coupon coupon) {
        try {
            Coupon createdCoupon = couponService.createCoupon(coupon);
            return Result.success(createdCoupon);
        } catch (Exception e) {
            return Result.error("创建优惠券失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/coupons/issue")
    public Result<Void> issueCoupon(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long couponId = Long.valueOf(params.get("couponId").toString());
            couponService.issueCouponToUser(userId, couponId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("发放优惠券失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/coupons/issue/batch")
    public Result<Void> issueCouponsBatch(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> userIds = (List<Long>) params.get("userIds");
            Long couponId = Long.valueOf(params.get("couponId").toString());
            couponService.issueCouponsToUsers(userIds, couponId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("批量发放优惠券失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/coupons/issue/new-user/{userId}")
    public Result<Void> issueNewUserCoupon(@PathVariable Long userId) {
        try {
            couponService.issueNewUserCoupon(userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("发放新人优惠券失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/coupons/issue/birthday/{userId}")
    public Result<Void> issueBirthdayCoupon(@PathVariable Long userId) {
        try {
            couponService.issueBirthdayCoupon(userId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("发放生日优惠券失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/coupons/issue/points-reward/{userId}")
    public Result<Void> issuePointsRewardCoupon(
            @PathVariable Long userId,
            @RequestParam Integer points) {
        try {
            couponService.issuePointsRewardCoupon(userId, points);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("发放积分奖励优惠券失败：" + e.getMessage());
        }
    }

    /**
     * 获取今日可领取的优惠券列表
     */
    @GetMapping("/coupons/today")
    public Result<List<Coupon>> getTodayAvailableCoupons() {
        try {
            List<Coupon> coupons = couponService.getTodayAvailableCoupons();
            return Result.success(coupons);
        } catch (Exception e) {
            return Result.error("获取今日优惠券失败：" + e.getMessage());
        }
    }

    /**
     * 领取每日优惠券
     */
    @PostMapping("/coupons/today/{couponId}")
    public Result<UserCoupon> claimDailyCoupon(
            @RequestAttribute Long userId,
            @PathVariable Long couponId) {
        try {
            UserCoupon userCoupon = couponService.claimDailyCoupon(userId, couponId);
            return Result.success(userCoupon);
        } catch (Exception e) {
            return Result.error("领取优惠券失败：" + e.getMessage());
        }
    }
} 