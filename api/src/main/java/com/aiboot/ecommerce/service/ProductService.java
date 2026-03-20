package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.ProductQueryRequest;
import com.aiboot.ecommerce.dto.ProductSaveRequest;
import com.aiboot.ecommerce.entity.Product;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ProductService extends IService<Product> {

    IPage<Product> search(ProductQueryRequest request);

    Product getDetail(Long id);

    Product create(ProductSaveRequest request);

    Product update(Long id, ProductSaveRequest request);

    void deleteProduct(Long id);
}
