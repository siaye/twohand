package com.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> getCategoryList();
    Category getCategoryById(Long id);
    Category createCategory(Category category);
    Category updateCategory(Category category);
    boolean deleteCategory(Long id);
} 