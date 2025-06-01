package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.repository.CategoryMapper;
import com.secondhand.model.entity.Category;
import com.secondhand.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    
    @Override
    public List<Category> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
               .orderByAsc(Category::getSort);
        return list(wrapper);
    }

    @Override
    public Category getCategoryById(Long id) {
        return getById(id);
    }

    @Override
    public Category createCategory(Category category) {
        save(category);
        return category;
    }

    @Override
    public Category updateCategory(Category category) {
        updateById(category);
        return category;
    }

    @Override
    public boolean deleteCategory(Long id) {
        return removeById(id);
    }
} 