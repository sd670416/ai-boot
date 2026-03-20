package com.aiboot.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("payments")
public class Payment extends BaseEntity {

    private Long orderId;
    private String orderNo;
    private Long userId;
    private String payNo;
    private BigDecimal payAmount;
    private String payType;
    private String status;
    private LocalDateTime payTime;
}
