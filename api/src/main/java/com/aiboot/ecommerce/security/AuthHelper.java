package com.aiboot.ecommerce.security;

import com.aiboot.ecommerce.common.exception.BusinessException;

public final class AuthHelper {

    private AuthHelper() {
    }

    public static AuthUser requireLogin() {
        AuthUser authUser = AuthContext.get();
        if (authUser == null) {
            throw new BusinessException("未登录");
        }
        return authUser;
    }

    public static void requireBackendUser() {
        AuthUser authUser = requireLogin();
        if (!"BACKEND".equals(authUser.getUserType())) {
            throw new BusinessException("无后台访问权限");
        }
    }

    public static void requireBackendRole(String... roleCodes) {
        AuthUser authUser = requireLogin();
        if (!"BACKEND".equals(authUser.getUserType())) {
            throw new BusinessException("无后台访问权限");
        }
        if (roleCodes == null || roleCodes.length == 0) {
            return;
        }
        for (String roleCode : roleCodes) {
            if (roleCode.equals(authUser.getRoleCode())) {
                return;
            }
        }
        throw new BusinessException("无角色权限");
    }

    public static void requireSelfOrBackend(Long userId) {
        AuthUser authUser = requireLogin();
        if ("BACKEND".equals(authUser.getUserType())) {
            return;
        }
        if (userId == null || !userId.equals(authUser.getUserId())) {
            throw new BusinessException("无权访问其他用户数据");
        }
    }
}
