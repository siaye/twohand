package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
    
    @Select("SELECT * FROM user_coupon WHERE user_id = #{userId} AND status = 1")
    List<UserCoupon> selectAvailableByUserId(Long userId);
} 