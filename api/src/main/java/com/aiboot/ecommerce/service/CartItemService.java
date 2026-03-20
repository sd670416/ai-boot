package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.CartCheckedRequest;
import com.aiboot.ecommerce.dto.CartRequest;
import com.aiboot.ecommerce.entity.CartItem;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface CartItemService extends IService<CartItem> {

    void addToCart(CartRequest request);

    List<CartItem> getUserCart(Long userId);

    Map<String, Object> getCartSummary(Long userId);

    void updateQuantity(Long id, Integer quantity);

    void updateChecked(Long id, Integer checked);

    void clearUserCart(Long userId);

    void removeFromCart(Long id);
}
