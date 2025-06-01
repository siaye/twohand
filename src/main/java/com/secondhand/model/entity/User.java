package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;                  // 主键ID
    private String username;          // 用户名
    private String password;          // 密码
    private String realName;          // 真实姓名
    private String idCard;            // 身份证号
    private String idCardImage;       // 身份证照片URL
    private String phone;             // 手机号
    private String email;             // 邮箱
    private String city;              // 城市
    private Integer gender;           // 性别 0-未知 1-男 2-女
    private String bankAccount;       // 银行账号
    private String avatar;            // 头像
    private Integer userType;         // 用户类型 0-个人 1-商家
    private Integer auditStatus;      // 审核状态 0-待审核 1-通过 2-拒绝
    private String auditReason;       // 审核拒绝原因
    private String businessLicense;   // 商家营业执照号
    private String businessLicenseImage; // 营业执照照片URL
    private Integer merchantLevel;    // 商家等级 1-5
    private BigDecimal merchantFee;   // 商家手续费率
    private Integer role;             // 角色 0-普通用户 1-商家 2-管理员
    private Integer status;           // 状态 0-禁用 1-正常
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
    private LocalDateTime auditTime;  // 审核时间
    private Long auditorId;           // 审核人ID

    // 判断账号是否可用
    public boolean isEnabled() {
        return status == 1 && auditStatus == 1;
    }

    // 判断账号是否被锁定
    public boolean isLocked() {
        return status != 1;
    }

    // 获取角色名称
    public String getRoleName() {
        return switch (role) {
            case 0 -> "USER";
            case 1 -> "MERCHANT";
            case 2 -> "ADMIN";
            default -> "USER";
        };
    }
} 