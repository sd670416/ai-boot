package com.aiboot.ecommerce.service.impl;

import com.aiboot.ecommerce.common.constant.SystemParamConstants;
import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.config.oss.OssRuntimeConfig;
import com.aiboot.ecommerce.dto.SystemParamSaveRequest;
import com.aiboot.ecommerce.entity.SystemParamConfig;
import com.aiboot.ecommerce.mapper.SystemParamConfigMapper;
import com.aiboot.ecommerce.service.SystemParamConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SystemParamConfigServiceImpl extends ServiceImpl<SystemParamConfigMapper, SystemParamConfig>
        implements SystemParamConfigService {

    private static final Logger log = LoggerFactory.getLogger(SystemParamConfigServiceImpl.class);

    private final StringRedisTemplate stringRedisTemplate;

    public SystemParamConfigServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public List<SystemParamConfig> list(String paramType, String keyword) {
        LambdaQueryWrapper<SystemParamConfig> wrapper = new LambdaQueryWrapper<SystemParamConfig>()
                .eq(StringUtils.hasText(paramType), SystemParamConfig::getParamType, paramType)
                .and(StringUtils.hasText(keyword), q -> q.like(SystemParamConfig::getParamName, keyword)
                        .or().like(SystemParamConfig::getParamKey, keyword)
                        .or().like(SystemParamConfig::getParamGroup, keyword))
                .orderByAsc(SystemParamConfig::getParamType)
                .orderByAsc(SystemParamConfig::getParamGroup)
                .orderByAsc(SystemParamConfig::getId);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemParamConfig create(SystemParamSaveRequest request) {
        checkParamKeyUnique(null, request.getParamKey());
        SystemParamConfig entity = new SystemParamConfig();
        fillEntity(entity, request);
        save(entity);
        syncCache(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemParamConfig update(Long id, SystemParamSaveRequest request) {
        SystemParamConfig entity = getById(id);
        if (entity == null) {
            throw new BusinessException("参数不存在");
        }
        String oldParamKey = entity.getParamKey();
        checkParamKeyUnique(id, request.getParamKey());
        fillEntity(entity, request);
        updateById(entity);
        if (!oldParamKey.equals(entity.getParamKey())) {
            deleteCache(oldParamKey);
        }
        syncCache(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SystemParamConfig entity = getById(id);
        if (entity == null) {
            return;
        }
        removeById(id);
        deleteCache(entity.getParamKey());
    }

    @Override
    public String getValueByKey(String paramKey) {
        String cacheKey = buildCacheKey(paramKey);
        try {
            String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
            if (cachedValue != null) {
                return cachedValue;
            }
        } catch (DataAccessException exception) {
            log.warn("读取 Redis 参数缓存失败, key={}", paramKey, exception);
        }

        SystemParamConfig entity = getOne(new LambdaQueryWrapper<SystemParamConfig>()
                .eq(SystemParamConfig::getParamKey, paramKey)
                .eq(SystemParamConfig::getStatus, 1)
                .last("limit 1"));
        if (entity == null) {
            return null;
        }
        syncCache(entity);
        return entity.getParamValue();
    }

    @Override
    public OssRuntimeConfig getOssConfig() {
        OssRuntimeConfig config = new OssRuntimeConfig();
        config.setEndpoint(getRequiredValue(SystemParamConstants.OSS_ENDPOINT, "OSS Endpoint"));
        config.setAccessKeyId(getRequiredValue(SystemParamConstants.OSS_ACCESS_KEY_ID, "OSS AccessKeyId"));
        config.setAccessKeySecret(getRequiredValue(SystemParamConstants.OSS_ACCESS_KEY_SECRET, "OSS AccessKeySecret"));
        config.setBucketName(getRequiredValue(SystemParamConstants.OSS_BUCKET_NAME, "OSS Bucket"));
        config.setDomain(getValueByKey(SystemParamConstants.OSS_DOMAIN));
        config.setBasePath(getValueByKey(SystemParamConstants.OSS_BASE_PATH));
        return config;
    }

    @Override
    public void warmupGroupCache(String paramGroup) {
        List<SystemParamConfig> params = list(new LambdaQueryWrapper<SystemParamConfig>()
                .eq(SystemParamConfig::getParamGroup, paramGroup)
                .eq(SystemParamConfig::getStatus, 1));
        for (SystemParamConfig param : params) {
            syncCache(param);
        }
    }

    private void fillEntity(SystemParamConfig entity, SystemParamSaveRequest request) {
        entity.setParamType(request.getParamType());
        entity.setParamGroup(request.getParamGroup());
        entity.setParamName(request.getParamName());
        entity.setParamKey(request.getParamKey());
        entity.setParamValue(request.getParamValue());
        entity.setValueType(request.getValueType());
        entity.setRemark(request.getRemark());
        entity.setStatus(request.getStatus());
    }

    private void checkParamKeyUnique(Long id, String paramKey) {
        SystemParamConfig existing = getOne(new LambdaQueryWrapper<SystemParamConfig>()
                .eq(SystemParamConfig::getParamKey, paramKey)
                .last("limit 1"));
        if (existing != null && (id == null || !existing.getId().equals(id))) {
            throw new BusinessException("参数键已存在");
        }
    }

    private void syncCache(SystemParamConfig entity) {
        if (entity == null || !StringUtils.hasText(entity.getParamKey())) {
            return;
        }
        if (entity.getStatus() == null || entity.getStatus() != 1) {
            deleteCache(entity.getParamKey());
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(buildCacheKey(entity.getParamKey()), entity.getParamValue() == null ? "" : entity.getParamValue());
        } catch (DataAccessException exception) {
            log.warn("写入 Redis 参数缓存失败, key={}", entity.getParamKey(), exception);
        }
    }

    private void deleteCache(String paramKey) {
        try {
            stringRedisTemplate.delete(buildCacheKey(paramKey));
        } catch (DataAccessException exception) {
            log.warn("删除 Redis 参数缓存失败, key={}", paramKey, exception);
        }
    }

    private String buildCacheKey(String paramKey) {
        return SystemParamConstants.REDIS_KEY_PREFIX + paramKey;
    }

    private String getRequiredValue(String paramKey, String displayName) {
        String value = getValueByKey(paramKey);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(displayName + " 未配置，请先在系统参数中维护");
        }
        return value;
    }
}
