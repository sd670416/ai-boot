package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.CartCheckedRequest;
import com.aiboot.ecommerce.dto.CartQuantityUpdateRequest;
import com.aiboot.ecommerce.dto.CartRequest;
import com.aiboot.ecommerce.entity.CartItem;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.CartItemService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartItemService cartItemService;

    public CartController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping
    public ApiResponse<Void> addToCart(@Valid @RequestBody CartRequest request) {
        AuthHelper.requireSelfOrBackend(request.getUserId());
        cartItemService.addToCart(request);
        return ApiResponse.success("加入购物车成功", null);
    }

    @GetMapping
    public ApiResponse<List<CartItem>> list(@RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success(cartItemService.getUserCart(userId));
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success(cartItemService.getCartSummary(userId));
    }

    @PutMapping("/{id}/quantity")
    public ApiResponse<Void> updateQuantity(@PathVariable Long id,
                                            @RequestParam Long userId,
                                            @Valid @RequestBody CartQuantityUpdateRequest request) {
        AuthHelper.requireSelfOrBackend(userId);
        cartItemService.updateQuantity(id, request.getQuantity());
        return ApiResponse.success("购物车数量更新成功", null);
    }

    @PutMapping("/{id}/checked")
    public ApiResponse<Void> updateChecked(@PathVariable Long id,
                                           @RequestParam Long userId,
                                           @Valid @RequestBody CartCheckedRequest request) {
        AuthHelper.requireSelfOrBackend(userId);
        cartItemService.updateChecked(id, request.getChecked());
        return ApiResponse.success("购物车勾选状态更新成功", null);
    }

    @DeleteMapping("/user/{userId}")
    public ApiResponse<Void> clear(@PathVariable Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        cartItemService.clearUserCart(userId);
        return ApiResponse.success("购物车已清空", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        cartItemService.removeFromCart(id);
        return ApiResponse.success("购物车商品删除成功", null);
    }
}
