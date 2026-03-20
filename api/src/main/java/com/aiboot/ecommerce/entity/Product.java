package com.aiboot.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("products")
public class Product extends BaseEntity {

    private Long categoryId;
    private String name;
    private String subtitle;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String coverImage;

    @JsonIgnore
    @TableField("detail_images")
    private String detailImagesJson;

    @TableField(exist = false)
    private List<String> detailImages;

    private Integer status;

    @TableField(exist = false)
    private List<ProductSku> skuList;
}
