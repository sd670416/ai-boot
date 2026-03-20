package com.aiboot.ecommerce.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentCreateRequest {

    @NotBlank(message = "支付方式不能为空")
    private String payType;
}
