package com.secondhand.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectRoot = System.getProperty("user.dir");
        
        // 商品图片映射
        registry.addResourceHandler("/api/uploads/products/**")
                .addResourceLocations("file:" + projectRoot + "/uploads/products/");
        
        // 用户头像映射
        registry.addResourceHandler("/api/uploads/avatar/**")
                .addResourceLocations("file:" + projectRoot + "/uploads/avatar/");
        
        // 身份证照片映射
        registry.addResourceHandler("/api/uploads/idcard/**")
                .addResourceLocations("file:" + projectRoot + "/uploads/idcard/");
        
        // 营业执照映射
        registry.addResourceHandler("/api/uploads/license/**")
                .addResourceLocations("file:" + projectRoot + "/uploads/license/");
    }
} 