package com.aiboot.ecommerce.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderDeliveryRequest {

    @NotBlank(message = "物流公司不能为空")
    private String deliveryCompany;

    @NotBlank(message = "物流单号不能为空")
    private String deliveryNo;
}
