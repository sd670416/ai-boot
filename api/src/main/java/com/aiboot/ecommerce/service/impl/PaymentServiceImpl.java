package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.entity.Order;
import com.aiboot.ecommerce.entity.Payment;
import com.aiboot.ecommerce.mapper.PaymentMapper;
import com.aiboot.ecommerce.service.OrderService;
import com.aiboot.ecommerce.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final OrderService orderService;

    public PaymentServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPayment(Long orderId, String payType) {
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不允许发起支付");
        }

        Payment existing = getByOrderId(orderId);
        if (existing != null && "SUCCESS".equals(existing.getStatus())) {
            throw new BusinessException("该订单已支付");
        }

        Payment payment = existing == null ? new Payment() : existing;
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(order.getUserId());
        payment.setPayNo(generatePayNo());
        payment.setPayAmount(order.getTotalAmount());
        payment.setPayType(payType);
        payment.setStatus("WAITING");
        payment.setPayTime(null);
        if (existing == null) {
            save(payment);
        } else {
            updateById(payment);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("payment", payment);
        result.put("payUrl", "https://mock-pay.local/pay/" + payment.getPayNo());
        result.put("message", "模拟支付单创建成功");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> paySuccess(Long paymentId, String thirdPartyTradeNo) {
        Payment payment = getById(paymentId);
        if (payment == null) {
            throw new BusinessException("支付单不存在");
        }
        if ("SUCCESS".equals(payment.getStatus())) {
            Map<String, Object> repeat = new HashMap<String, Object>();
            repeat.put("payment", payment);
            repeat.put("order", orderService.getById(payment.getOrderId()));
            return repeat;
        }

        payment.setStatus("SUCCESS");
        payment.setPayTime(LocalDateTime.now());
        if (StringUtils.hasText(thirdPartyTradeNo)) {
            payment.setPayNo(thirdPartyTradeNo);
        }
        updateById(payment);

        orderService.updateStatus(payment.getOrderId(), "PAID");
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("payment", getById(paymentId));
        result.put("order", orderService.getById(payment.getOrderId()));
        return result;
    }

    @Override
    public Payment getByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, orderId).last("limit 1"));
    }

    private String generatePayNo() {
        return "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
