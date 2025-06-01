package com.secondhand.model.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserVO {
    private Long id;                  // 主键ID
    private String username;          // 用户名
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
    private String createTime;        // 创建时间
    private String updateTime;        // 更新时间
    private String auditTime;         // 审核时间
    private Long auditorId;           // 审核人ID
} 