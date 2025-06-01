package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.model.entity.UserPoints;
import com.secondhand.repository.UserPointsMapper;
import com.secondhand.service.UserPointsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPointsServiceImpl extends ServiceImpl<UserPointsMapper, UserPoints> implements UserPointsService {

    private static final Logger logger = LoggerFactory.getLogger(UserPointsServiceImpl.class);

    @Autowired
    private UserPointsMapper userPointsMapper;

    @Override
    @Transactional
    public void increasePoints(Long userId, Integer points) {
        if (points <= 0) {
            // 增加的积分必须大于0
            return;
        }

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );

        if (userPoints == null) {
            // 如果用户积分记录不存在，创建一个新的
            userPoints = new UserPoints();
            userPoints.setUserId(userId);
            userPoints.setPoints(points);
            userPointsMapper.insert(userPoints);
        } else {
            // 如果用户积分记录存在，更新积分
            userPoints.setPoints(userPoints.getPoints() + points);
            userPoints.setUpdatedAt(java.time.LocalDateTime.now());
            userPointsMapper.updateById(userPoints);
        }
    }

    @Override
    @Transactional
    public boolean decreasePoints(Long userId, Integer points) {
        if (points <= 0) {
            // 减少的积分必须大于0
            logger.warn("减少积分失败：积分数量必须大于0, userId={}, points={}", userId, points);
            return false;
        }

        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );

        if (userPoints != null && userPoints.getPoints() >= points) {
            // 如果用户积分足够，减少积分
            logger.info("开始减少用户积分: userId={}, currentPoints={}, decreasePoints={}", 
                userId, userPoints.getPoints(), points);
            
            userPoints.setPoints(userPoints.getPoints() - points);
            userPoints.setUpdatedAt(java.time.LocalDateTime.now());
            userPointsMapper.updateById(userPoints);
            
            logger.info("用户积分减少成功: userId={}, remainingPoints={}", 
                userId, userPoints.getPoints());
            return true; // 减少成功
        } else {
            // 用户积分不足
            logger.warn("减少积分失败：用户积分不足, userId={}, currentPoints={}, requiredPoints={}", 
                userId, userPoints != null ? userPoints.getPoints() : 0, points);
            return false; // 减少失败
        }
    }

    @Override
    public Integer getUserPoints(Long userId) {
        UserPoints userPoints = userPointsMapper.selectOne(
                new LambdaQueryWrapper<UserPoints>().eq(UserPoints::getUserId, userId)
        );
        Integer points = userPoints != null ? userPoints.getPoints() : 0;
        logger.info("获取用户积分: userId={}, points={}", userId, points);
        return points;
    }
} 