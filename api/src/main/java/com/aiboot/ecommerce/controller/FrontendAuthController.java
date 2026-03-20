package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.UserLoginRequest;
import com.aiboot.ecommerce.dto.UserProfileResponse;
import com.aiboot.ecommerce.dto.UserRegisterRequest;
import com.aiboot.ecommerce.entity.User;
import com.aiboot.ecommerce.security.JwtTokenProvider;
import com.aiboot.ecommerce.security.dto.LoginResponse;
import com.aiboot.ecommerce.service.UserService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/frontend/auth")
public class FrontendAuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public FrontendAuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ApiResponse<UserProfileResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return ApiResponse.success("注册成功", toProfile(userService.register(request)));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        User user = userService.login(request);
        LoginResponse response = new LoginResponse();
        response.setToken(jwtTokenProvider.createFrontendToken(user));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpireSeconds());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        return ApiResponse.success("前台登录成功", response);
    }

    private UserProfileResponse toProfile(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setUserType("FRONTEND");
        response.setRoleCode(null);
        response.setStatus(user.getStatus());
        return response;
    }
}
