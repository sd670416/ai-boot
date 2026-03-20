package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.dto.CartRequest;
import com.aiboot.ecommerce.entity.CartItem;
import com.aiboot.ecommerce.entity.Product;
import com.aiboot.ecommerce.mapper.CartItemMapper;
import com.aiboot.ecommerce.service.CartItemService;
import com.aiboot.ecommerce.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartItemService {

    private final ProductService productService;

    public CartItemServiceImpl(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void addToCart(CartRequest request) {
        Product product = productService.getById(request.getProductId());
        validateProduct(product, request.getQuantity());

        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, request.getUserId())
                .eq(CartItem::getProductId, request.getProductId());
        CartItem existing = getOne(wrapper);
        if (existing == null) {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(request.getUserId());
            cartItem.setProductId(request.getProductId());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setChecked(1);
            save(cartItem);
            return;
        }

        int targetQuantity = existing.getQuantity() + request.getQuantity();
        validateProduct(product, targetQuantity);
        existing.setQuantity(targetQuantity);
        updateById(existing);
    }

    @Override
    public List<CartItem> getUserCart(Long userId) {
        return list(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getUpdateTime));
    }

    @Override
    public Map<String, Object> getCartSummary(Long userId) {
        List<CartItem> items = getUserCart(userId);
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        int checkedCount = 0;

        for (CartItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (item.getChecked() != null && item.getChecked().intValue() == 1 && product != null) {
                totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                totalQuantity += item.getQuantity();
                checkedCount++;
            }
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("items", items);
        result.put("totalAmount", totalAmount);
        result.put("totalQuantity", totalQuantity);
        result.put("checkedCount", checkedCount);
        return result;
    }

    @Override
    public void updateQuantity(Long id, Integer quantity) {
        CartItem cartItem = getById(id);
        if (cartItem == null) {
            throw new BusinessException("购物车记录不存在");
        }
        Product product = productService.getById(cartItem.getProductId());
        validateProduct(product, quantity);
        cartItem.setQuantity(quantity);
        updateById(cartItem);
    }

    @Override
    public void updateChecked(Long id, Integer checked) {
        CartItem cartItem = getById(id);
        if (cartItem == null) {
            throw new BusinessException("购物车记录不存在");
        }
        cartItem.setChecked(checked != null && checked.intValue() == 1 ? 1 : 0);
        updateById(cartItem);
    }

    @Override
    public void clearUserCart(Long userId) {
        remove(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
    }

    @Override
    public void removeFromCart(Long id) {
        removeById(id);
    }

    private void validateProduct(Product product, Integer quantity) {
        if (product == null || product.getStatus() == null || product.getStatus().intValue() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (quantity == null || quantity.intValue() < 1) {
            throw new BusinessException("商品数量至少为1");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException("商品库存不足");
        }
    }
}
