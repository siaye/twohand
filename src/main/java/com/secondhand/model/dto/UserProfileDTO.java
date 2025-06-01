package com.secondhand.model.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class UserProfileDTO {
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度必须在2-20个字符之间")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotNull(message = "性别不能为空")
    @Min(value = 1, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;

    @NotBlank(message = "银行账号不能为空")
    @Pattern(regexp = "^\\d{16}$", message = "银行账号必须是16位数字")
    private String bankAccount;

    // 商家特有字段，可以为空
    private String idCard;
    private String idCardImage;
    private String businessLicense;
    private String businessLicenseImage;
} 