package com.secondhand.utils;

import com.secondhand.common.exception.BusinessException;
import com.secondhand.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SecurityUtils {
    
    private static JwtUtil jwtUtil;
    
    @Autowired
    public void setJwtUtil(JwtUtil jwtUtil) {
        SecurityUtils.jwtUtil = jwtUtil;
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                throw new BusinessException("无法获取当前请求");
            }
            String token = attributes.getRequest().getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                throw new BusinessException("用户未登录");
            }
            return jwtUtil.getUserIdFromToken(token.substring(7));
        } catch (Exception e) {
            throw new BusinessException("用户未登录");
        }
    }
    
    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public static String getCurrentUsername() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                throw new BusinessException("无法获取当前请求");
            }
            String token = attributes.getRequest().getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                throw new BusinessException("用户未登录");
            }
            return jwtUtil.getUsernameFromToken(token.substring(7));
        } catch (Exception e) {
            throw new BusinessException("用户未登录");
        }
    }
} 