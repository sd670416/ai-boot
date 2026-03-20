package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.OrderCreateRequest;
import com.aiboot.ecommerce.entity.Order;
import com.aiboot.ecommerce.entity.OrderItem;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface OrderService extends IService<Order> {

    Map<String, Object> createOrder(OrderCreateRequest request);

    Map<String, Object> getOrderDetail(Long orderId);

    List<Order> listByUserId(Long userId, String status);

    void updateStatus(Long orderId, String status);

    void cancelOrder(Long orderId, String reason);

    void deliverOrder(Long orderId, String deliveryCompany, String deliveryNo);

    void confirmReceipt(Long orderId);

    void refundOrder(Long orderId, String reason);

    Map<String, Object> getOverviewStatistics();

    List<Map<String, Object>> getTopSellingProducts();

    List<OrderItem> listItemsByOrderId(Long orderId);
}
