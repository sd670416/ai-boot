package com.aiboot.ecommerce.dto;

import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSkuRequest {

    private Long id;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;

    @NotBlank(message = "SKU名称不能为空")
    private String skuName;

    @NotBlank(message = "规格值不能为空")
    private String specValues;

    private String image;

    @NotNull(message = "SKU售价不能为空")
    private BigDecimal salePrice;

    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "SKU库存不能小于0")
    private Integer stock;

    @NotNull(message = "SKU状态不能为空")
    private Integer status;
}
