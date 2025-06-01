package com.secondhand.model.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class UserUpdateDTO {
    private Long id;

    @Size(min = 2, max = 50, message = "真实姓名长度必须在2-50个字符之间")
    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(max = 50, message = "城市名称不能超过50个字符")
    private String city;

    private Integer gender;

    @Pattern(regexp = "^\\d{16,20}$", message = "银行账号必须是16-20位数字")
    private String bankAccount;

    @Size(max = 255, message = "营业执照号不能超过255个字符")
    private String businessLicense;

    @Size(max = 255, message = "头像URL不能超过255个字符")
    private String avatar;

    private Integer merchantLevel;    // 商家等级 1-5
    private BigDecimal merchantFee;   // 商家手续费率
} 