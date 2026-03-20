package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.dto.UserAddressRequest;
import com.aiboot.ecommerce.entity.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    UserAddress create(UserAddressRequest request);

    UserAddress update(Long id, UserAddressRequest request);

    List<UserAddress> listByUserId(Long userId);

    void deleteAddress(Long id);
}
