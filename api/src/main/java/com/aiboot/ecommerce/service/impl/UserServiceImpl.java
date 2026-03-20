package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.dto.UserLoginRequest;
import com.aiboot.ecommerce.dto.UserRegisterRequest;
import com.aiboot.ecommerce.entity.User;
import com.aiboot.ecommerce.mapper.UserMapper;
import com.aiboot.ecommerce.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User register(UserRegisterRequest request) {
        User existing = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        save(user);
        return user;
    }

    @Override
    public User login(UserLoginRequest request) {
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("前台用户不存在");
        }
        if (!StringUtils.hasText(user.getPassword()) || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus().intValue() != 1) {
            throw new BusinessException("用户已被禁用");
        }
        return user;
    }
}
