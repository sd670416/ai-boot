package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.UserLoginRequest;
import com.aiboot.ecommerce.entity.BackendUser;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BackendUserService extends IService<BackendUser> {

    BackendUser login(UserLoginRequest request);
}
