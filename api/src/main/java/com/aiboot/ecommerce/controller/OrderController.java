package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.OrderCancelRequest;
import com.aiboot.ecommerce.dto.OrderCreateRequest;
import com.aiboot.ecommerce.dto.OrderDeliveryRequest;
import com.aiboot.ecommerce.dto.OrderRefundRequest;
import com.aiboot.ecommerce.dto.OrderStatusRequest;
import com.aiboot.ecommerce.entity.Order;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.OrderService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody OrderCreateRequest request) {
        AuthHelper.requireSelfOrBackend(request.getUserId());
        return ApiResponse.success("订单创建成功", orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id, @RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success(orderService.getOrderDetail(id));
    }

    @GetMapping
    public ApiResponse<List<Order>> listByUserId(@RequestParam Long userId,
                                                 @RequestParam(required = false) String status) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success(orderService.listByUserId(userId, status));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        AuthHelper.requireBackendUser();
        orderService.updateStatus(id, request.getStatus());
        return ApiResponse.success("订单状态更新成功", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id,
                                    @RequestParam Long userId,
                                    @RequestBody(required = false) OrderCancelRequest request) {
        AuthHelper.requireSelfOrBackend(userId);
        orderService.cancelOrder(id, request == null ? null : request.getReason());
        return ApiResponse.success("订单取消成功", null);
    }

    @PostMapping("/{id}/deliver")
    public ApiResponse<Void> deliver(@PathVariable Long id, @Valid @RequestBody OrderDeliveryRequest request) {
        AuthHelper.requireBackendUser();
        orderService.deliverOrder(id, request.getDeliveryCompany(), request.getDeliveryNo());
        return ApiResponse.success("订单发货成功", null);
    }

    @PostMapping("/{id}/receive")
    public ApiResponse<Void> confirmReceipt(@PathVariable Long id, @RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        orderService.confirmReceipt(id);
        return ApiResponse.success("确认收货成功", null);
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<Void> refund(@PathVariable Long id,
                                    @RequestParam Long userId,
                                    @RequestBody(required = false) OrderRefundRequest request) {
        AuthHelper.requireSelfOrBackend(userId);
        orderService.refundOrder(id, request == null ? null : request.getReason());
        return ApiResponse.success("订单退款成功", null);
    }
}
