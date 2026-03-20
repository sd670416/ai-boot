package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.UserLoginRequest;
import com.aiboot.ecommerce.entity.BackendUser;
import com.aiboot.ecommerce.security.JwtTokenProvider;
import com.aiboot.ecommerce.security.dto.LoginResponse;
import com.aiboot.ecommerce.service.BackendUserService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final BackendUserService backendUserService;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminAuthController(BackendUserService backendUserService, JwtTokenProvider jwtTokenProvider) {
        this.backendUserService = backendUserService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        BackendUser user = backendUserService.login(request);
        LoginResponse response = new LoginResponse();
        response.setToken(jwtTokenProvider.createBackendToken(user));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpireSeconds());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        return ApiResponse.success("后台登录成功", response);
    }
}
