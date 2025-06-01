package com.secondhand.config;

import com.secondhand.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    // 公开接口
                    "/api/user/captcha/**",    // 验证码
                    "/api/user/login",         // 登录
                    "/api/user/register",      // 注册
                    "/api/user/send-code",     // 发送验证码
                    "/api/user/verify-code",   // 验证验证码
                    "/api/product/list",       // 商品列表
                    "/api/product/*",          // 商品详情
                    "/api/error",              // 错误页面
                    "/api/home/**",            // 首页相关接口
                    "/api/category/**",        // 分类相关接口
                    "/api/search/**",          // 搜索相关接口
                    "/api/notice/**",          // 公告相关接口
                    // 静态资源
                    "/api/static/**",          // 静态资源
                    "/api/uploads/**"          // 上传文件
                )
                .order(1);  // 设置拦截器顺序
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
} 