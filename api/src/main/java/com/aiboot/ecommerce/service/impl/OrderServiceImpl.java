package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.dto.OrderCreateRequest;
import com.aiboot.ecommerce.dto.OrderItemRequest;
import com.aiboot.ecommerce.entity.Order;
import com.aiboot.ecommerce.entity.OrderItem;
import com.aiboot.ecommerce.entity.Payment;
import com.aiboot.ecommerce.entity.Product;
import com.aiboot.ecommerce.entity.UserAddress;
import com.aiboot.ecommerce.mapper.OrderItemMapper;
import com.aiboot.ecommerce.mapper.OrderMapper;
import com.aiboot.ecommerce.mapper.PaymentMapper;
import com.aiboot.ecommerce.service.OrderService;
import com.aiboot.ecommerce.service.ProductService;
import com.aiboot.ecommerce.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final String STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_SHIPPED = "SHIPPED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_REFUND_REQUESTED = "REFUND_REQUESTED";
    private static final String STATUS_REFUNDED = "REFUNDED";

    private final ProductService productService;
    private final OrderItemMapper orderItemMapper;
    private final UserAddressService userAddressService;
    private final PaymentMapper paymentMapper;

    public OrderServiceImpl(ProductService productService,
                            OrderItemMapper orderItemMapper,
                            UserAddressService userAddressService,
                            PaymentMapper paymentMapper) {
        this.productService = productService;
        this.orderItemMapper = orderItemMapper;
        this.userAddressService = userAddressService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createOrder(OrderCreateRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        String orderNo = generateOrderNo();
        ReceiverInfo receiverInfo = buildReceiverInfo(request);

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setStatus(STATUS_PENDING_PAYMENT);
        order.setReceiverName(receiverInfo.getReceiverName());
        order.setReceiverPhone(receiverInfo.getReceiverPhone());
        order.setReceiverAddress(receiverInfo.getReceiverAddress());
        order.setRemark(request.getRemark());
        save(order);

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productService.getById(itemRequest.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus().intValue() != 1) {
                throw new BusinessException("下单商品不存在或已下架");
            }
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException("商品[" + product.getName() + "]库存不足");
            }

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getCoverImage());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setTotalAmount(itemTotal);
            orderItemMapper.insert(orderItem);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            productService.updateById(product);
        }

        order.setTotalAmount(totalAmount);
        updateById(order);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("order", order);
        result.put("items", listItemsByOrderId(order.getId()));
        return result;
    }

    @Override
    public Map<String, Object> getOrderDetail(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        Map<String, Object> detail = new HashMap<String, Object>();
        detail.put("order", order);
        detail.put("items", listItemsByOrderId(orderId));
        return detail;
    }

    @Override
    public List<Order> listByUserId(Long userId, String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        wrapper.eq(StringUtils.hasText(status), Order::getStatus, status);
        return list(wrapper);
    }

    @Override
    public void updateStatus(Long orderId, String status) {
        Order order = getRequiredOrder(orderId);
        order.setStatus(status);
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        Order order = getRequiredOrder(orderId);
        if (!STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许取消");
        }

        rollbackStock(orderId);
        order.setStatus(STATUS_CANCELLED);
        appendRemark(order, reason, "取消原因:");
        updateById(order);
    }

    @Override
    public void deliverOrder(Long orderId, String deliveryCompany, String deliveryNo) {
        Order order = getRequiredOrder(orderId);
        if (!STATUS_PAID.equals(order.getStatus())) {
            throw new BusinessException("只有已支付订单才能发货");
        }
        order.setStatus(STATUS_SHIPPED);
        order.setDeliveryCompany(deliveryCompany);
        order.setDeliveryNo(deliveryNo);
        order.setDeliveryTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void confirmReceipt(Long orderId) {
        Order order = getRequiredOrder(orderId);
        if (!STATUS_SHIPPED.equals(order.getStatus())) {
            throw new BusinessException("只有已发货订单才能确认收货");
        }
        order.setStatus(STATUS_COMPLETED);
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId, String reason) {
        Order order = getRequiredOrder(orderId);
        if (!(STATUS_PAID.equals(order.getStatus()) || STATUS_SHIPPED.equals(order.getStatus()) || STATUS_COMPLETED.equals(order.getStatus()))) {
            throw new BusinessException("当前订单状态不允许退款");
        }

        order.setStatus(STATUS_REFUNDED);
        order.setRefundTime(LocalDateTime.now());
        appendRemark(order, reason, "退款原因:");
        updateById(order);

        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, orderId)
                .orderByDesc(Payment::getId)
                .last("limit 1"));
        if (payment != null) {
            payment.setStatus(STATUS_REFUNDED);
            paymentMapper.updateById(payment);
        }

        rollbackStock(orderId);
    }

    @Override
    public Map<String, Object> getOverviewStatistics() {
        List<Order> orders = list();
        BigDecimal totalSales = BigDecimal.ZERO;
        int totalOrders = orders.size();
        int paidOrders = 0;
        int pendingOrders = 0;
        int refundOrders = 0;

        for (Order order : orders) {
            if (STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
                pendingOrders++;
            }
            if (STATUS_REFUNDED.equals(order.getStatus()) || STATUS_REFUND_REQUESTED.equals(order.getStatus())) {
                refundOrders++;
            }
            if (STATUS_PAID.equals(order.getStatus()) || STATUS_SHIPPED.equals(order.getStatus()) || STATUS_COMPLETED.equals(order.getStatus())) {
                paidOrders++;
                totalSales = totalSales.add(order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount());
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalOrders", totalOrders);
        result.put("paidOrders", paidOrders);
        result.put("pendingPaymentOrders", pendingOrders);
        result.put("refundOrders", refundOrders);
        result.put("totalSales", totalSales);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts() {
        List<Order> orders = list();
        Set<String> effectiveOrderNos = new HashSet<String>();
        for (Order order : orders) {
            if (STATUS_PAID.equals(order.getStatus()) || STATUS_SHIPPED.equals(order.getStatus()) || STATUS_COMPLETED.equals(order.getStatus())) {
                effectiveOrderNos.add(order.getOrderNo());
            }
        }

        Map<Long, Map<String, Object>> aggregated = new HashMap<Long, Map<String, Object>>();
        for (OrderItem item : orderItemMapper.selectList(null)) {
            if (!effectiveOrderNos.contains(item.getOrderNo())) {
                continue;
            }
            Map<String, Object> row = aggregated.get(item.getProductId());
            if (row == null) {
                row = new HashMap<String, Object>();
                row.put("productId", item.getProductId());
                row.put("productName", item.getProductName());
                row.put("salesQuantity", 0);
                row.put("salesAmount", BigDecimal.ZERO);
                aggregated.put(item.getProductId(), row);
            }
            row.put("salesQuantity", ((Integer) row.get("salesQuantity")) + item.getQuantity());
            row.put("salesAmount", ((BigDecimal) row.get("salesAmount")).add(item.getTotalAmount()));
        }

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(aggregated.values());
        result.sort((a, b) -> ((Integer) b.get("salesQuantity")).compareTo((Integer) a.get("salesQuantity")));
        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    @Override
    public List<OrderItem> listItemsByOrderId(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId));
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private ReceiverInfo buildReceiverInfo(OrderCreateRequest request) {
        if (request.getAddressId() != null) {
            UserAddress address = userAddressService.getById(request.getAddressId());
            if (address == null || address.getStatus() == null || address.getStatus().intValue() != 1) {
                throw new BusinessException("收货地址不存在");
            }
            if (!request.getUserId().equals(address.getUserId())) {
                throw new BusinessException("该地址不属于当前用户");
            }
            ReceiverInfo info = new ReceiverInfo();
            info.setReceiverName(address.getReceiverName());
            info.setReceiverPhone(address.getReceiverPhone());
            info.setReceiverAddress(joinAddress(address));
            return info;
        }

        if (!StringUtils.hasText(request.getReceiverName())
                || !StringUtils.hasText(request.getReceiverPhone())
                || !StringUtils.hasText(request.getReceiverAddress())) {
            throw new BusinessException("请提供完整收货信息或地址ID");
        }

        ReceiverInfo info = new ReceiverInfo();
        info.setReceiverName(request.getReceiverName());
        info.setReceiverPhone(request.getReceiverPhone());
        info.setReceiverAddress(request.getReceiverAddress());
        return info;
    }

    private String joinAddress(UserAddress address) {
        StringBuilder builder = new StringBuilder();
        append(builder, address.getProvince());
        append(builder, address.getCity());
        append(builder, address.getDistrict());
        append(builder, address.getDetailAddress());
        return builder.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (StringUtils.hasText(value)) {
            builder.append(value);
        }
    }

    private Order getRequiredOrder(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private void rollbackStock(Long orderId) {
        List<OrderItem> items = listItemsByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() + item.getQuantity());
                productService.updateById(product);
            }
        }
    }

    private void appendRemark(Order order, String reason, String prefix) {
        if (StringUtils.hasText(reason)) {
            String currentRemark = order.getRemark();
            order.setRemark(StringUtils.hasText(currentRemark) ? currentRemark + " | " + prefix + reason : prefix + reason);
        }
    }

    private static class ReceiverInfo {
        private String receiverName;
        private String receiverPhone;
        private String receiverAddress;

        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public void setReceiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
        }

        public String getReceiverAddress() {
            return receiverAddress;
        }

        public void setReceiverAddress(String receiverAddress) {
            this.receiverAddress = receiverAddress;
        }
    }
}
