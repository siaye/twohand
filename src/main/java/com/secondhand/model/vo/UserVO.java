package com.secondhand.model.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String city;
    private Integer gender;
    private String bankAccount;
    private String avatar;
    private Integer userType;
    private Integer auditStatus;
    private String businessLicense;
    private Integer role;
    private Integer status;
} 