package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.entity.Payment;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

public interface PaymentService extends IService<Payment> {

    Map<String, Object> createPayment(Long orderId, String payType);

    Map<String, Object> paySuccess(Long paymentId, String thirdPartyTradeNo);

    Payment getByOrderId(Long orderId);
}
