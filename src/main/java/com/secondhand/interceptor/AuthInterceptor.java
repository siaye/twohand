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
        // 放行所有 OPTIONS 请求
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        // 白名单路径直接放行
        String path = request.getRequestURI();
        if (path.startsWith("/api/user/captcha") || 
            path.startsWith("/api/user/login") || 
            path.startsWith("/api/user/register") ||
            path.startsWith("/api/user/complete-profile") ||
            path.startsWith("/api/user/upload") ||
            path.startsWith("/api/product/upload") ||
            path.equals("/api/product/list") ||  // 商品列表
            path.startsWith("/api/product/") ||  // 商品详情
            path.startsWith("/api/static/") ||   // 静态资源
            path.startsWith("/api/uploads/")) {  // 上传文件
            return true;
        }

        // 验证token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                // 从token中获取用户ID并设置到请求属性中
                Long userId = jwtUtil.getUserIdFromToken(token);
                request.setAttribute("userId", userId);
                
                // 对于管理员接口，验证用户角色
                if (path.startsWith("/api/admin/")) {
                    Integer role = jwtUtil.getRoleFromToken(token);
                    if (role != null && role == 2) {  // 2 表示管理员角色
                        return true;
                    } else {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        try {
                            response.getWriter().write("{\"code\":403,\"message\":\"需要管理员权限\"}");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                }
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