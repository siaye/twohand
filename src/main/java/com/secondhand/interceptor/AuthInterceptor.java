package com.secondhand.interceptor;

import com.secondhand.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 白名单路径直接放行
        String path = request.getRequestURI();
        if (path.startsWith("/api/user/captcha") || 
            path.startsWith("/api/user/login") || 
            path.startsWith("/api/user/register")) {
            return true;
        }

        // 验证token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return true;
            }
        }

        // 未认证返回401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write("{\"code\":401,\"message\":\"未认证或登录已过期\"}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
} 