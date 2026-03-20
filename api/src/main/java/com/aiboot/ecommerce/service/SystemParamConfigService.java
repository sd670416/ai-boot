package com.aiboot.ecommerce.service;

import com.aiboot.ecommerce.config.oss.OssRuntimeConfig;
import com.aiboot.ecommerce.dto.SystemParamSaveRequest;
import com.aiboot.ecommerce.entity.SystemParamConfig;
import java.util.List;

public interface SystemParamConfigService {

    List<SystemParamConfig> list(String paramType, String keyword);

    SystemParamConfig create(SystemParamSaveRequest request);

    SystemParamConfig update(Long id, SystemParamSaveRequest request);

    void delete(Long id);

    String getValueByKey(String paramKey);

    OssRuntimeConfig getOssConfig();

    void warmupGroupCache(String paramGroup);
}
