package com.secondhand.task;

import com.secondhand.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DailyCouponTask {

    @Autowired
    private CouponService couponService;

    /**
     * 每天凌晨0点生成新的优惠券
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyCoupons() {
        log.info("开始执行每日优惠券生成任务");
        try {
            couponService.generateDailyCoupons();
            log.info("每日优惠券生成任务执行完成");
        } catch (Exception e) {
            log.error("每日优惠券生成任务执行失败", e);
        }
    }
} 