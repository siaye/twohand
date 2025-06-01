package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {
    
    @Select("SELECT * FROM wallet WHERE user_id = #{userId}")
    Wallet selectByUserId(Long userId);
} 