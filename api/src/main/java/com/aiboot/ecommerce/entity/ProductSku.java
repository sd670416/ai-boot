package com.aiboot.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_skus")
public class ProductSku extends BaseEntity {

    private Long productId;
    private String skuCode;
    private String skuName;
    private String specValues;
    private String image;
    private BigDecimal salePrice;
    private Integer stock;
    private Integer status;
}
