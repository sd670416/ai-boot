package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.dto.SystemParamSaveRequest;
import com.aiboot.ecommerce.entity.SystemParamConfig;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.service.SystemParamConfigService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system-params")
public class SystemParamConfigController {

    private final SystemParamConfigService systemParamConfigService;

    public SystemParamConfigController(SystemParamConfigService systemParamConfigService) {
        this.systemParamConfigService = systemParamConfigService;
    }

    @GetMapping
    public ApiResponse<List<SystemParamConfig>> list(@RequestParam(required = false) String paramType,
                                                     @RequestParam(required = false) String keyword) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success(systemParamConfigService.list(paramType, keyword));
    }

    @PostMapping
    public ApiResponse<SystemParamConfig> create(@Valid @RequestBody SystemParamSaveRequest request) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success("参数创建成功", systemParamConfigService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SystemParamConfig> update(@PathVariable Long id, @Valid @RequestBody SystemParamSaveRequest request) {
        AuthHelper.requireBackendUser();
        return ApiResponse.success("参数更新成功", systemParamConfigService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthHelper.requireBackendUser();
        systemParamConfigService.delete(id);
        return ApiResponse.success("参数删除成功", null);
    }
}
