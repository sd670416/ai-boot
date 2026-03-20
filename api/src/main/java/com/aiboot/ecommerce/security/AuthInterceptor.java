package com.aiboot.ecommerce.security;

import com.aiboot.ecommerce.common.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicRequest(request)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或Token缺失");
            return false;
        }

        try {
            String token = authorization.substring(7);
            Claims claims = jwtTokenProvider.parseToken(token);
            Object roleCodeClaim = claims.get("roleCode");
            AuthUser authUser = new AuthUser();
            authUser.setUserId(Long.valueOf(claims.getSubject()));
            authUser.setUsername(String.valueOf(claims.get("username")));
            authUser.setNickname(String.valueOf(claims.get("nickname")));
            authUser.setUserType(String.valueOf(claims.get("userType")));
            authUser.setRoleCode(roleCodeClaim == null ? null : String.valueOf(roleCodeClaim));
            AuthContext.set(authUser);
            return true;
        } catch (Exception exception) {
            writeUnauthorized(response, "Token无效或已过期");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if ("/api/admin/auth/login".equals(uri)
                || "/api/frontend/auth/login".equals(uri)
                || "/api/frontend/auth/register".equals(uri)) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method) && ("/api/products".equals(uri) || uri.startsWith("/api/products/") || "/api/categories".equals(uri))) {
            return true;
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(message)));
    }
}
