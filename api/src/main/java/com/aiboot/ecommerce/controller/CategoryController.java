package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.entity.Category;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.CategoryService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.success(categoryService.list());
    }

    @PostMapping
    public ApiResponse<Category> create(@Valid @RequestBody Category category) {
        AuthHelper.requireBackendUser();
        categoryService.save(category);
        return ApiResponse.success("分类创建成功", category);
    }

    @PutMapping("/{id}")
    public ApiResponse<Category> update(@PathVariable Long id, @Valid @RequestBody Category category) {
        AuthHelper.requireBackendUser();
        category.setId(id);
        categoryService.updateById(category);
        return ApiResponse.success("分类更新成功", category);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthHelper.requireBackendUser();
        categoryService.removeById(id);
        return ApiResponse.success("分类删除成功", null);
    }
}
