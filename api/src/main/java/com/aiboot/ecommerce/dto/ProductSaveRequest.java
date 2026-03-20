package com.aiboot.ecommerce.dto;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductSaveRequest {

    private Long id;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;
    private String description;
    private String coverImage;
    private List<String> detailImages;

    @NotNull(message = "商品状态不能为空")
    private Integer status;

    @Valid
    @NotEmpty(message = "商品规格不能为空")
    private List<ProductSkuRequest> skuList;
}
