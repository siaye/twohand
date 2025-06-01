package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface UserRepository extends BaseMapper<User> {
    
    @Select("SELECT * FROM user WHERE username = #{username}")
    Optional<User> findByUsername(String username);

    @Select("SELECT COUNT(*) FROM user WHERE phone = #{phone}")
    boolean existsByPhone(String phone);

    @Select("SELECT COUNT(*) FROM user WHERE email = #{email}")
    boolean existsByEmail(String email);
} 