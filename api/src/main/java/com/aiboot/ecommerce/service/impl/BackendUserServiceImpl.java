package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.dto.UserLoginRequest;
import com.aiboot.ecommerce.entity.BackendUser;
import com.aiboot.ecommerce.mapper.BackendUserMapper;
import com.aiboot.ecommerce.service.BackendUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BackendUserServiceImpl extends ServiceImpl<BackendUserMapper, BackendUser> implements BackendUserService {

    @Override
    public BackendUser login(UserLoginRequest request) {
        BackendUser user = getOne(new LambdaQueryWrapper<BackendUser>().eq(BackendUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException("后台用户不存在");
        }
        if (!StringUtils.hasText(user.getPassword()) || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus().intValue() != 1) {
            throw new BusinessException("用户已被禁用");
        }
        if (!StringUtils.hasText(user.getRoleCode())) {
            user.setRoleCode("OPERATOR");
        }
        return user;
    }
}
