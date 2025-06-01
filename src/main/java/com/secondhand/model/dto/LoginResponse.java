package com.secondhand.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;        // JWT token
    private String username;     // 用户名
    private Integer role;        // 角色 0-普通用户 1-商家 2-管理员
    private Integer userType;    // 用户类型 0-个人 1-商家
    private String realName;     // 真实姓名
    private String avatar;       // 头像
} 