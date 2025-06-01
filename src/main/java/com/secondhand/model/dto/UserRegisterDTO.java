package com.secondhand.model.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;         // 用户名
    private String password;         // 密码
    private String confirmPassword;  // 确认密码
    private String realName;         // 真实姓名
    private String idCard;           // 身份证号
    private String idCardImage;      // 身份证照片（Base64）
    private String phone;            // 手机号
    private String email;            // 邮箱
    private String city;             // 城市
    private Integer gender;          // 性别
    private String bankAccount;      // 银行账号
    private Integer userType;        // 用户类型 0-个人 1-商家
    private String businessLicense;  // 商家营业执照号
    private String businessLicenseImage; // 营业执照照片（Base64）
    private String captcha;          // 验证码
} 