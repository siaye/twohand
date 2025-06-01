package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("user_coupon")
@Schema(description = "用户优惠券表")
public class UserCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户优惠券ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "状态（1：未使用，2：已使用，3：已过期）")
    private Integer status;

    @Schema(description = "使用时间")
    private LocalDateTime usedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Coupon coupon;
} 