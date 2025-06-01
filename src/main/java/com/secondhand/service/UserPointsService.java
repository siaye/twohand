package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.entity.UserPoints;

public interface UserPointsService extends IService<UserPoints> {

    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param points 增加的积分数量
     */
    void increasePoints(Long userId, Integer points);

    /**
     * 减少用户积分（用于积分抵扣等）
     * @param userId 用户ID
     * @param points 减少的积分数量
     * @return 是否成功减少（积分是否足够）
     */
    boolean decreasePoints(Long userId, Integer points);

    /**
     * 获取用户当前积分
     * @param userId 用户ID
     * @return 用户当前积分
     */
    Integer getUserPoints(Long userId);
} 