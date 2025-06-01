package com.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.secondhand.model.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {
} 