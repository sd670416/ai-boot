package com.aiboot.ecommerce.dto;

import lombok.Data;

@Data
public class PaymentCallbackRequest {

    private String thirdPartyTradeNo;
    private String status;
}
