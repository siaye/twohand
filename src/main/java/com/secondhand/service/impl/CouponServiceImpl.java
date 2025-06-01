package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.common.exception.BusinessException;
import com.secondhand.model.entity.Coupon;
import com.secondhand.model.entity.UserCoupon;
import com.secondhand.repository.CouponMapper;
import com.secondhand.repository.UserCouponMapper;
import com.secondhand.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.secondhand.utils.SecurityUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private CouponMapper couponMapper;

    @Override
    public List<UserCoupon> getAvailableCoupons(Long userId) {
        try {
            // 获取用户所有未使用的优惠券（status = 1）
            List<UserCoupon> userCoupons = userCouponMapper.selectAvailableByUserId(userId);

            // 过滤掉已过期的优惠券
            LocalDateTime now = LocalDateTime.now();
            return userCoupons.stream()
                .filter(userCoupon -> {
                    Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
                    return coupon != null && 
                           coupon.getStartTime().isBefore(now) && 
                           coupon.getEndTime().isAfter(now);
                })
                .peek(userCoupon -> {
                    // 设置优惠券信息
                    userCoupon.setCoupon(couponMapper.selectById(userCoupon.getCouponId()));
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new BusinessException("获取可用优惠券失败：" + e.getMessage());
        }
    }

    @Override
    public UserCoupon getCouponDetail(Long couponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(couponId);
        if (userCoupon != null) {
            userCoupon.setCoupon(couponMapper.selectById(userCoupon.getCouponId()));
        }
        return userCoupon;
    }

    @Override
    public UserCoupon getUserCouponById(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon != null) {
            userCoupon.setCoupon(couponMapper.selectById(userCoupon.getCouponId()));
        }
        return userCoupon;
    }

    @Override
    public BigDecimal validateAndCalculateDiscount(Long userCouponId, Long userId, BigDecimal orderAmount) {
        // 1. 获取用户优惠券信息
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
            throw new BusinessException("优惠券无效或不属于当前用户");
        }

        // 2. 验证优惠券状态
        if (userCoupon.getStatus() != 1) {
            throw new BusinessException("优惠券已被使用或不可用");
        }

        // 3. 获取优惠券模板信息
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        if (coupon == null) {
            throw new BusinessException("优惠券模板不存在");
        }

        // 4. 验证优惠券有效期
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
            throw new BusinessException("优惠券不在有效期内");
        }

        // 5. 验证最低使用金额
        if (coupon.getMinAmount() != null && orderAmount.compareTo(coupon.getMinAmount()) < 0) {
            throw new BusinessException("订单金额未达到优惠券最低使用要求");
        }

        // 6. 计算折扣金额
        if (coupon.getType() == 1) {
            // 满减券
            return coupon.getValue();
        } else if (coupon.getType() == 2) {
            // 折扣券
            return orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getValue()));
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void useCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon != null && userCoupon.getStatus() == 1) {
            userCoupon.setStatus(2); // 状态2表示已使用
            userCoupon.setUsedAt(LocalDateTime.now());
            userCouponMapper.updateById(userCoupon);
        }
    }

    @Override
    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        // 设置创建时间
        coupon.setCreatedAt(LocalDateTime.now());
        // 保存优惠券模板
        save(coupon);
        return coupon;
    }

    @Override
    @Transactional
    public void issueCouponToUser(Long userId, Long couponId) {
        // 检查优惠券是否存在
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 检查是否已经发放过该优惠券
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .eq(UserCoupon::getCouponId, couponId)
               .eq(UserCoupon::getStatus, 1);
        if (userCouponMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户已拥有该优惠券");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(1); // 1：未使用
        userCoupon.setCreatedAt(LocalDateTime.now());
        userCouponMapper.insert(userCoupon);
    }

    @Override
    @Transactional
    public void issueCouponsToUsers(List<Long> userIds, Long couponId) {
        for (Long userId : userIds) {
            try {
                issueCouponToUser(userId, couponId);
            } catch (BusinessException e) {
                // 记录日志，继续处理下一个用户
                log.error("发放优惠券失败：userId={}, couponId={}, error={}", userId, couponId, e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void issueNewUserCoupon(Long userId) {
        // 获取新人优惠券模板
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getName, "新人优惠券")
               .eq(Coupon::getType, 1) // 满减券
               .orderByDesc(Coupon::getCreatedAt)
               .last("LIMIT 1");
        Coupon newUserCoupon = getOne(wrapper);

        if (newUserCoupon != null) {
            issueCouponToUser(userId, newUserCoupon.getId());
        }
    }

    @Override
    @Transactional
    public void issueBirthdayCoupon(Long userId) {
        // 获取生日优惠券模板
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getName, "生日优惠券")
               .eq(Coupon::getType, 2) // 折扣券
               .orderByDesc(Coupon::getCreatedAt)
               .last("LIMIT 1");
        Coupon birthdayCoupon = getOne(wrapper);

        if (birthdayCoupon != null) {
            issueCouponToUser(userId, birthdayCoupon.getId());
        }
    }

    @Override
    @Transactional
    public void issuePointsRewardCoupon(Long userId, Integer points) {
        // 根据积分数量选择不同的优惠券
        String couponName;
        if (points >= 1000) {
            couponName = "积分奖励优惠券(1000)";
        } else if (points >= 500) {
            couponName = "积分奖励优惠券(500)";
        } else {
            couponName = "积分奖励优惠券(100)";
        }

        // 获取对应的优惠券模板
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Coupon::getName, couponName)
               .orderByDesc(Coupon::getCreatedAt)
               .last("LIMIT 1");
        Coupon rewardCoupon = getOne(wrapper);

        if (rewardCoupon != null) {
            issueCouponToUser(userId, rewardCoupon.getId());
        }
    }

    @Override
    public List<Coupon> getTodayAvailableCoupons() {
        // 获取今天的开始时间和结束时间
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(23, 59, 59);

        // 查询今日可领取的优惠券
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(Coupon::getCreatedAt, startOfDay, endOfDay)
               .orderByDesc(Coupon::getCreatedAt);
        
        List<Coupon> coupons = list(wrapper);
        
        // 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId != null) {
            // 查询用户已领取的优惠券ID列表
            LambdaQueryWrapper<UserCoupon> userCouponWrapper = new LambdaQueryWrapper<>();
            userCouponWrapper.eq(UserCoupon::getUserId, userId)
                           .select(UserCoupon::getCouponId);
            List<UserCoupon> userCoupons = userCouponMapper.selectList(userCouponWrapper);
            List<Long> claimedCouponIds = userCoupons.stream()
                                                   .map(UserCoupon::getCouponId)
                                                   .collect(Collectors.toList());
            
            // 设置优惠券是否已领取的状态
            coupons.forEach(coupon -> {
                coupon.setClaimed(claimedCouponIds.contains(coupon.getId()));
            });
        }
        
        return coupons;
    }

    @Override
    @Transactional
    public UserCoupon claimDailyCoupon(Long userId, Long couponId) {
        // 检查优惠券是否存在
        Coupon coupon = getById(couponId);
        if (coupon == null) {
            throw new BusinessException("优惠券不存在");
        }

        // 检查是否已经领取过该优惠券
        LambdaQueryWrapper<UserCoupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCoupon::getUserId, userId)
               .eq(UserCoupon::getCouponId, couponId);
        if (userCouponMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已经领取过该优惠券");
        }

        // 创建用户优惠券记录
        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(1); // 1：未使用
        userCoupon.setCreatedAt(LocalDateTime.now());
        userCouponMapper.insert(userCoupon);

        // 设置优惠券信息
        userCoupon.setCoupon(coupon);
        return userCoupon;
    }

    @Override
    @Transactional
    public void generateDailyCoupons() {
        log.info("开始生成每日优惠券");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(7); // 优惠券有效期7天

        // 生成满减券
        Coupon discountCoupon = new Coupon();
        discountCoupon.setName("每日优惠券");
        discountCoupon.setType(1); // 满减券
        discountCoupon.setValue(new BigDecimal("10")); // 满减10元
        discountCoupon.setMinAmount(new BigDecimal("100")); // 满100可用
        discountCoupon.setStartTime(now);
        discountCoupon.setEndTime(endTime);
        discountCoupon.setCreatedAt(now);
        save(discountCoupon);

        // 生成折扣券
        Coupon percentageCoupon = new Coupon();
        percentageCoupon.setName("每日优惠券");
        percentageCoupon.setType(2); // 折扣券
        percentageCoupon.setValue(new BigDecimal("0.9")); // 9折
        percentageCoupon.setMinAmount(new BigDecimal("200")); // 满200可用
        percentageCoupon.setStartTime(now);
        percentageCoupon.setEndTime(endTime);
        percentageCoupon.setCreatedAt(now);
        save(percentageCoupon);

        log.info("每日优惠券生成完成");
    }
} 