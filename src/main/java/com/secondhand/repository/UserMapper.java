package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
 
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 可扩展自定义方法
} 