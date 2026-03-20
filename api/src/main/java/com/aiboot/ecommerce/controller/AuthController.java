package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.UserProfileResponse;
import com.aiboot.ecommerce.entity.BackendUser;
import com.aiboot.ecommerce.entity.User;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.security.AuthUser;
import com.aiboot.ecommerce.service.BackendUserService;
import com.aiboot.ecommerce.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final BackendUserService backendUserService;

    public AuthController(UserService userService, BackendUserService backendUserService) {
        this.userService = userService;
        this.backendUserService = backendUserService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        AuthUser authUser = AuthHelper.requireLogin();
        if ("BACKEND".equals(authUser.getUserType())) {
            BackendUser user = backendUserService.getById(authUser.getUserId());
            return ApiResponse.success(toProfile(user));
        }
        User user = userService.getById(authUser.getUserId());
        return ApiResponse.success(toProfile(user));
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

    private UserProfileResponse toProfile(BackendUser user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setUserType("BACKEND");
        response.setRoleCode(user.getRoleCode());
        response.setStatus(user.getStatus());
        return response;
    }
}
