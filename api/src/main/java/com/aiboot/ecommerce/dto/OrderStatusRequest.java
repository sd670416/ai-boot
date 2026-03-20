package com.aiboot.ecommerce.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusRequest {

    @NotBlank(message = "订单状态不能为空")
    private String status;
}
