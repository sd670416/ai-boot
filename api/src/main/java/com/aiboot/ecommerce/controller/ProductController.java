package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.ProductQueryRequest;
import com.aiboot.ecommerce.dto.ProductSaveRequest;
import com.aiboot.ecommerce.entity.Product;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.ProductService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ApiResponse<IPage<Product>> list(@RequestParam(required = false) Integer pageNum,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        ProductQueryRequest request = new ProductQueryRequest();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setKeyword(keyword);
        request.setCategoryId(categoryId);
        request.setStatus(status);
        return ApiResponse.success(productService.search(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> detail(@PathVariable Long id) {
        return ApiResponse.success(productService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Product> create(@Valid @RequestBody ProductSaveRequest request) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success("商品创建成功", productService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Product> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success("商品更新成功", productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthHelper.requireBackendUser();
        productService.deleteProduct(id);
        return ApiResponse.success("商品删除成功", null);
    }
}
