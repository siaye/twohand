package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
} 