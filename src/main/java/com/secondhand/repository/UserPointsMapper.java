package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPointsMapper extends BaseMapper<UserPoints> {

    @Select("SELECT id, user_id, points, created_at, updated_at FROM user_points WHERE user_id = #{userId}")
    UserPoints selectByUserId(Long userId);

} 