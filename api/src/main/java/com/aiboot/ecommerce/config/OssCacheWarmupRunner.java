package com.aiboot.ecommerce.config;

import com.aiboot.ecommerce.common.constant.SystemParamConstants;
import com.aiboot.ecommerce.service.SystemParamConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OssCacheWarmupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(OssCacheWarmupRunner.class);

    private final SystemParamConfigService systemParamConfigService;

    public OssCacheWarmupRunner(SystemParamConfigService systemParamConfigService) {
        this.systemParamConfigService = systemParamConfigService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            systemParamConfigService.warmupGroupCache(SystemParamConstants.GROUP_OSS);
        } catch (Exception exception) {
            log.warn("OSS 参数缓存预热失败", exception);
        }
    }
}
