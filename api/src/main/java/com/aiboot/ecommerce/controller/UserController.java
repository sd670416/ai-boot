package com.aiboot.ecommerce.controller;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.aiboot.ecommerce.common.exception.BusinessException;
import com.aiboot.ecommerce.dto.AdminUserSaveRequest;
import com.aiboot.ecommerce.dto.UserProfileResponse;
import com.aiboot.ecommerce.entity.BackendUser;
import com.aiboot.ecommerce.entity.User;
import com.aiboot.ecommerce.security.AuthHelper;
import com.aiboot.ecommerce.security.AuthUser;
import com.aiboot.ecommerce.service.BackendUserService;
import com.aiboot.ecommerce.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.validation.Valid;
import org.springframework.util.StringUtils;
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
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;
    private final BackendUserService backendUserService;

    public UserController(UserService userService, BackendUserService backendUserService) {
        this.userService = userService;
        this.backendUserService = backendUserService;
    }

    @GetMapping
    public ApiResponse<List<UserProfileResponse>> list(@RequestParam(required = false) String userType) {
        AuthHelper.requireBackendUser();
        List<UserProfileResponse> result = new ArrayList<UserProfileResponse>();
        if (!StringUtils.hasText(userType) || "FRONTEND".equalsIgnoreCase(userType)) {
            for (User user : userService.list()) {
                result.add(toFrontendProfile(user));
            }
        }
        if (!StringUtils.hasText(userType) || "BACKEND".equalsIgnoreCase(userType)) {
            for (BackendUser user : backendUserService.list()) {
                result.add(toBackendProfile(user));
            }
        }
        result.sort(Comparator.comparing(UserProfileResponse::getUserType).reversed().thenComparing(UserProfileResponse::getId));
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserProfileResponse> detail(@PathVariable Long id, @RequestParam String userType) {
        AuthHelper.requireBackendUser();
        if ("BACKEND".equalsIgnoreCase(userType)) {
            return ApiResponse.success(toBackendProfile(getBackendUser(id)));
        }
        return ApiResponse.success(toFrontendProfile(getFrontendUser(id)));
    }

    @PostMapping
    public ApiResponse<UserProfileResponse> create(@Valid @RequestBody AdminUserSaveRequest request) {
        AuthHelper.requireBackendRole("SUPER_ADMIN");
        if ("BACKEND".equalsIgnoreCase(request.getUserType())) {
            ensureBackendUsernameUnique(request.getUsername(), null);
            BackendUser user = new BackendUser();
            user.setUsername(request.getUsername());
            user.setPassword(normalizePassword(request.getPassword(), null));
            user.setNickname(request.getNickname());
            user.setPhone(request.getPhone());
            user.setEmail(request.getEmail());
            user.setAvatar(request.getAvatar());
            user.setRoleCode(normalizeBackendRole(request.getRoleCode()));
            user.setStatus(normalizeStatus(request.getStatus()));
            backendUserService.save(user);
            return ApiResponse.success("后台用户创建成功", toBackendProfile(user));
        }
        ensureFrontendUsernameUnique(request.getUsername(), null);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(normalizePassword(request.getPassword(), null));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(normalizeStatus(request.getStatus()));
        userService.save(user);
        return ApiResponse.success("前台用户创建成功", toFrontendProfile(user));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserProfileResponse> update(@PathVariable Long id,
                                                   @RequestParam String userType,
                                                   @Valid @RequestBody AdminUserSaveRequest request) {
        AuthUser currentAuth = AuthHelper.requireLogin();
        if ("BACKEND".equalsIgnoreCase(userType)) {
            BackendUser existing = getBackendUser(id);
            if (!"SUPER_ADMIN".equals(currentAuth.getRoleCode()) && !id.equals(currentAuth.getUserId())) {
                throw new BusinessException("无权修改其他后台用户");
            }
            if (!"SUPER_ADMIN".equals(currentAuth.getRoleCode())) {
                request.setRoleCode(existing.getRoleCode());
                request.setStatus(existing.getStatus());
            }
            ensureBackendUsernameUnique(request.getUsername(), id);
            existing.setUsername(request.getUsername());
            existing.setPassword(normalizePassword(request.getPassword(), existing.getPassword()));
            existing.setNickname(request.getNickname());
            existing.setPhone(request.getPhone());
            existing.setEmail(request.getEmail());
            existing.setAvatar(request.getAvatar());
            existing.setRoleCode(normalizeBackendRole(request.getRoleCode()));
            existing.setStatus(normalizeStatus(request.getStatus()));
            backendUserService.updateById(existing);
            return ApiResponse.success("后台用户更新成功", toBackendProfile(existing));
        }
        AuthHelper.requireBackendUser();
        User existing = getFrontendUser(id);
        ensureFrontendUsernameUnique(request.getUsername(), id);
        existing.setUsername(request.getUsername());
        existing.setPassword(normalizePassword(request.getPassword(), existing.getPassword()));
        existing.setNickname(request.getNickname());
        existing.setPhone(request.getPhone());
        existing.setEmail(request.getEmail());
        existing.setStatus(normalizeStatus(request.getStatus()));
        userService.updateById(existing);
        return ApiResponse.success("前台用户更新成功", toFrontendProfile(existing));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam String userType) {
        AuthHelper.requireBackendRole("SUPER_ADMIN");
        if ("BACKEND".equalsIgnoreCase(userType)) {
            backendUserService.removeById(id);
            return ApiResponse.success("后台用户删除成功", null);
        }
        userService.removeById(id);
        return ApiResponse.success("前台用户删除成功", null);
    }

    private User getFrontendUser(Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException("前台用户不存在");
        }
        return user;
    }

    private BackendUser getBackendUser(Long id) {
        BackendUser user = backendUserService.getById(id);
        if (user == null) {
            throw new BusinessException("后台用户不存在");
        }
        return user;
    }

    private void ensureFrontendUsernameUnique(String username, Long currentId) {
        User existing = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException("前台用户名已存在");
        }
    }

    private void ensureBackendUsernameUnique(String username, Long currentId) {
        BackendUser existing = backendUserService.getOne(new LambdaQueryWrapper<BackendUser>().eq(BackendUser::getUsername, username));
        if (existing != null && (currentId == null || !existing.getId().equals(currentId))) {
            throw new BusinessException("后台用户名已存在");
        }
    }

    private String normalizePassword(String password, String existingPassword) {
        if (StringUtils.hasText(password)) {
            return password;
        }
        if (StringUtils.hasText(existingPassword)) {
            return existingPassword;
        }
        throw new BusinessException("密码不能为空");
    }

    private Integer normalizeStatus(Integer status) {
        return status == null ? 1 : status;
    }

    private String normalizeBackendRole(String roleCode) {
        return StringUtils.hasText(roleCode) ? roleCode : "OPERATOR";
    }

    private UserProfileResponse toFrontendProfile(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(null);
        response.setUserType("FRONTEND");
        response.setRoleCode(null);
        response.setStatus(user.getStatus());
        return response;
    }

    private UserProfileResponse toBackendProfile(BackendUser user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setPhone(user.getPhone());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setUserType("BACKEND");
        response.setRoleCode(user.getRoleCode());
        response.setStatus(user.getStatus());
        return response;
    }
}
