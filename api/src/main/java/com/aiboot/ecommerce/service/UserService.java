package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.UserLoginRequest;
import com.aiboot.ecommerce.dto.UserRegisterRequest;
import com.aiboot.ecommerce.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    User register(UserRegisterRequest request);

    User login(UserLoginRequest request);
}
