package com.aiboot.ecommerce.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserSaveRequest {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String username;

    private String password;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    @NotBlank(message = "用户类型不能为空")
    private String userType;

    private String roleCode;

    private Integer status;
}
