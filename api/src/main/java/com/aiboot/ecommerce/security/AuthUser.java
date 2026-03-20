package com.aiboot.ecommerce.security;

import lombok.Data;

@Data
public class AuthUser {

    private Long userId;
    private String username;
    private String nickname;
    private String userType;
    private String roleCode;
}
