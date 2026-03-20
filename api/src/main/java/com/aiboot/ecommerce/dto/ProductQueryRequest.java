package com.aiboot.ecommerce.dto;

import lombok.Data;

@Data
public class ProductQueryRequest {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private Long categoryId;
    private Integer status;
}
