package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.dto.ProductQueryRequest;
import com.aiboot.ecommerce.dto.ProductSaveRequest;
import com.aiboot.ecommerce.dto.ProductSkuRequest;
import com.aiboot.ecommerce.entity.Product;
import com.aiboot.ecommerce.entity.ProductSku;
import com.aiboot.ecommerce.mapper.ProductMapper;
import com.aiboot.ecommerce.mapper.ProductSkuMapper;
import com.aiboot.ecommerce.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductSkuMapper productSkuMapper;
    private final ObjectMapper objectMapper;

    public ProductServiceImpl(ProductSkuMapper productSkuMapper, ObjectMapper objectMapper) {
        this.productSkuMapper = productSkuMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public IPage<Product> search(ProductQueryRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>();
        wrapper.eq(request.getCategoryId() != null, Product::getCategoryId, request.getCategoryId())
                .eq(request.getStatus() != null, Product::getStatus, request.getStatus())
                .and(StringUtils.hasText(request.getKeyword()), q -> q.like(Product::getName, request.getKeyword())
                        .or().like(Product::getSubtitle, request.getKeyword())
                        .or().like(Product::getDescription, request.getKeyword()))
                .orderByDesc(Product::getUpdateTime);
        IPage<Product> pageResult = page(new Page<Product>(pageNum, pageSize), wrapper);
        fillSkuList(pageResult.getRecords());
        normalizeProducts(pageResult.getRecords());
        return pageResult;
    }

    @Override
    public Product getDetail(Long id) {
        Product product = getById(id);
        if (product != null) {
            fillSkuList(product);
            normalizeProduct(product);
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product create(ProductSaveRequest request) {
        Product product = new Product();
        fillProduct(product, request);
        save(product);
        saveSkuList(product.getId(), request.getSkuList());
        fillSkuList(product);
        normalizeProduct(product);
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Product update(Long id, ProductSaveRequest request) {
        Product product = getById(id);
        if (product == null) {
            return null;
        }
        fillProduct(product, request);
        updateById(product);
        productSkuMapper.delete(new QueryWrapper<ProductSku>().eq("product_id", id));
        saveSkuList(id, request.getSkuList());
        fillSkuList(product);
        normalizeProduct(product);
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        productSkuMapper.delete(new QueryWrapper<ProductSku>().eq("product_id", id));
        removeById(id);
    }

    private void fillProduct(Product product, ProductSaveRequest request) {
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName());
        product.setSubtitle(request.getSubtitle());
        product.setDescription(request.getDescription());
        product.setCoverImage(request.getCoverImage());
        product.setDetailImagesJson(serializeDetailImages(request.getDetailImages()));
        product.setDetailImages(request.getDetailImages());
        product.setStatus(request.getStatus());
        product.setPrice(calculateMinPrice(request.getSkuList()));
        product.setStock(calculateTotalStock(request.getSkuList()));
    }

    private BigDecimal calculateMinPrice(List<ProductSkuRequest> skuList) {
        BigDecimal minPrice = null;
        for (ProductSkuRequest sku : skuList) {
            if (minPrice == null || sku.getSalePrice().compareTo(minPrice) < 0) {
                minPrice = sku.getSalePrice();
            }
        }
        return minPrice == null ? BigDecimal.ZERO : minPrice;
    }

    private Integer calculateTotalStock(List<ProductSkuRequest> skuList) {
        int total = 0;
        for (ProductSkuRequest sku : skuList) {
            total += sku.getStock();
        }
        return total;
    }

    private void saveSkuList(Long productId, List<ProductSkuRequest> skuList) {
        for (ProductSkuRequest item : skuList) {
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSkuCode(item.getSkuCode());
            sku.setSkuName(item.getSkuName());
            sku.setSpecValues(item.getSpecValues());
            sku.setImage(item.getImage());
            sku.setSalePrice(item.getSalePrice());
            sku.setStock(item.getStock());
            sku.setStatus(item.getStatus());
            productSkuMapper.insert(sku);
        }
    }

    private void fillSkuList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        List<Long> productIds = new ArrayList<Long>();
        for (Product product : products) {
            productIds.add(product.getId());
        }
        List<ProductSku> skus = productSkuMapper.selectList(new QueryWrapper<ProductSku>().in("product_id", productIds).orderByAsc("id"));
        Map<Long, List<ProductSku>> grouped = new HashMap<Long, List<ProductSku>>();
        for (ProductSku sku : skus) {
            if (!grouped.containsKey(sku.getProductId())) {
                grouped.put(sku.getProductId(), new ArrayList<ProductSku>());
            }
            grouped.get(sku.getProductId()).add(sku);
        }
        for (Product product : products) {
            product.setSkuList(grouped.get(product.getId()));
        }
    }

    private void fillSkuList(Product product) {
        List<Product> products = new ArrayList<Product>();
        products.add(product);
        fillSkuList(products);
    }

    private void normalizeProducts(List<Product> products) {
        if (products == null) {
            return;
        }
        for (Product product : products) {
            normalizeProduct(product);
        }
    }

    private void normalizeProduct(Product product) {
        product.setDetailImages(parseDetailImages(product.getDetailImagesJson()));
        if (product.getSkuList() == null) {
            product.setSkuList(Collections.<ProductSku>emptyList());
        }
    }

    private String serializeDetailImages(List<String> detailImages) {
        try {
            return objectMapper.writeValueAsString(detailImages == null ? Collections.<String>emptyList() : detailImages);
        } catch (Exception exception) {
            throw new RuntimeException("商品详情图序列化失败", exception);
        }
    }

    private List<String> parseDetailImages(String detailImagesJson) {
        if (!StringUtils.hasText(detailImagesJson)) {
            return new ArrayList<String>();
        }
        try {
            return objectMapper.readValue(detailImagesJson, new TypeReference<List<String>>() {
            });
        } catch (Exception exception) {
            throw new RuntimeException("商品详情图解析失败", exception);
        }
    }
}
