package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.OrderService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    private final OrderService orderService;

    public StatisticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        AuthHelper.requireBackendUser();
        return ApiResponse.success(orderService.getOverviewStatistics());
    }

    @GetMapping("/top-products")
    public ApiResponse<List<Map<String, Object>>> topProducts() {
        AuthHelper.requireBackendUser();
        return ApiResponse.success(orderService.getTopSellingProducts());
    }
}
