package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("coupon")
@Schema(description = "优惠券表")
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "优惠券ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "优惠券名称")
    private String name;

    @Schema(description = "优惠券类型（1：满减券，2：折扣券）")
    private Integer type;

    @Schema(description = "优惠券面值")
    private BigDecimal value;

    @Schema(description = "最低使用金额")
    private BigDecimal minAmount;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "是否已领取")
    @TableField(exist = false)
    private Boolean claimed;
} 