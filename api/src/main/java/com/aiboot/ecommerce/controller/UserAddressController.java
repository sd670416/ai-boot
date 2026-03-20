package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.UserAddressRequest;
import com.aiboot.ecommerce.entity.UserAddress;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.UserAddressService;
import java.util.List;
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
@RequestMapping("/api/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @PostMapping
    public ApiResponse<UserAddress> create(@Valid @RequestBody UserAddressRequest request) {
        AuthHelper.requireSelfOrBackend(request.getUserId());
        return ApiResponse.success("地址创建成功", userAddressService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserAddress> update(@PathVariable Long id, @Valid @RequestBody UserAddressRequest request) {
        AuthHelper.requireSelfOrBackend(request.getUserId());
        return ApiResponse.success("地址更新成功", userAddressService.update(id, request));
    }

    @GetMapping
    public ApiResponse<List<UserAddress>> list(@RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        return ApiResponse.success(userAddressService.listByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        AuthHelper.requireSelfOrBackend(userId);
        userAddressService.deleteAddress(id);
        return ApiResponse.success("地址删除成功", null);
    }
}
