package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.entity.Category;
import com.aiboot.ecommerce.mapper.CategoryMapper;
import com.aiboot.ecommerce.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
