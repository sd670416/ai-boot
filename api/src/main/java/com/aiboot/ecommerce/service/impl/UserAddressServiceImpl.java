package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.dto.UserAddressRequest;
import com.aiboot.ecommerce.entity.UserAddress;
import com.aiboot.ecommerce.mapper.UserAddressMapper;
import com.aiboot.ecommerce.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public UserAddress create(UserAddressRequest request) {
        if (request.getIsDefault() != null && request.getIsDefault().intValue() == 1) {
            clearDefault(request.getUserId());
        }
        UserAddress address = new UserAddress();
        fill(address, request);
        address.setStatus(1);
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault().intValue() == 1 ? 1 : 0);
        save(address);
        return address;
    }

    @Override
    public UserAddress update(Long id, UserAddressRequest request) {
        UserAddress address = getById(id);
        if (address == null) {
            return null;
        }
        if (request.getIsDefault() != null && request.getIsDefault().intValue() == 1) {
            clearDefault(request.getUserId());
        }
        fill(address, request);
        address.setIsDefault(request.getIsDefault() != null && request.getIsDefault().intValue() == 1 ? 1 : 0);
        updateById(address);
        return address;
    }

    @Override
    public List<UserAddress> listByUserId(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getStatus, 1)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getUpdateTime));
    }

    @Override
    public void deleteAddress(Long id) {
        removeById(id);
    }

    private void clearDefault(Long userId) {
        update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
    }

    private void fill(UserAddress address, UserAddressRequest request) {
        address.setUserId(request.getUserId());
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        address.setPostalCode(request.getPostalCode());
    }
}
