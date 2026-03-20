package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.PaymentCallbackRequest;
import com.aiboot.ecommerce.dto.PaymentCreateRequest;
import com.aiboot.ecommerce.entity.Payment;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.PaymentService;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/orders/{orderId}")
    public ApiResponse<Map<String, Object>> create(@PathVariable Long orderId,
                                                   @RequestParam Long userId,
                                                   @Valid @RequestBody PaymentCreateRequest request) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success("支付单创建成功", paymentService.createPayment(orderId, request.getPayType()));
    }

    @PostMapping("/{paymentId}/success")
    public ApiResponse<Map<String, Object>> paySuccess(@PathVariable Long paymentId,
                                                       @RequestBody(required = false) PaymentCallbackRequest request) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success("支付成功",
                paymentService.paySuccess(paymentId, request == null ? null : request.getThirdPartyTradeNo()));
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<Payment> getByOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success(paymentService.getByOrderId(orderId));
    }
}
