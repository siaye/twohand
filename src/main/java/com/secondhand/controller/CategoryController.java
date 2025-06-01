package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.entity.Category;
import com.secondhand.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@CrossOrigin
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> getCategoryList() {
        try {
            List<Category> categories = categoryService.getCategoryList();
            return Result.success(categories);
        } catch (Exception e) {
            return Result.error("获取分类列表失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Category> getCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return Result.success(category);
        } catch (Exception e) {
            return Result.error("获取分类详情失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        try {
            Category newCategory = categoryService.createCategory(category);
            return Result.success(newCategory);
        } catch (Exception e) {
            return Result.error("创建分类失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            category.setId(id);
            Category updatedCategory = categoryService.updateCategory(category);
            return Result.success(updatedCategory);
        } catch (Exception e) {
            return Result.error("更新分类失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除分类失败：" + e.getMessage());
        }
    }
} 