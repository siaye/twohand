package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
} 