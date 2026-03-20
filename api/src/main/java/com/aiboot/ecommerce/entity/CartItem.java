package com.aiboot.ecommerce.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart_items")
public class CartItem extends BaseEntity {

    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer checked;
}
