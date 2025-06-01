package com.secondhand.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantAuditDTO {
    private Long userId;           // 用户ID
    private Integer auditStatus;   // 审核状态 1-通过 2-拒绝
    private String auditReason;    // 审核拒绝原因
    private Integer merchantLevel; // 商家等级 1-5
    private BigDecimal merchantFee; // 商家手续费率
} 