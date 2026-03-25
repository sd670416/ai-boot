package com.aiboot.ecommerce.dto;

import lombok.Data;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String userType;
    private String roleCode;
    private Integer status;
}
