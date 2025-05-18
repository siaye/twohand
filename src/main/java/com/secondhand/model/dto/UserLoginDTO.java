package com.secondhand.model.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String username;   // 用户名/手机号/邮箱
    private String password;   // 密码
    private String captcha;    // 验证码
} 