package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author secondhand
 * @since 2023-11-16
 */
@Getter
@Setter
@TableName("`order`")
@Schema(description = "订单表")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "卖家ID")
    private Long sellerId;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付方式（1：平台余额，2：线下交易）")
    private Integer paymentType;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "使用的积分数量")
    private Integer pointsUsed;

    @Schema(description = "订单状态（0：待付款，1：待发货，2：待收货，3：已完成，4：已取消，5：退款中，6：已退款）")
    @TableField("`status`") // 使用反引号包围状态，因为status是SQL关键字
    private Integer status;

    @Schema(description = "收货人姓名+电话+地址")
    @TableField("address")
    private String receiverAddress;

    @Schema(description = "订单备注")
    private String note;

    @Schema(description = "确认收货状态（0：未确认，1：已确认）")
    private Integer confirmStatus;

    @Schema(description = "删除状态（0：未删除，1：已删除）")
    private Integer deleteStatus;

    @Schema(description = "支付时间")
    private LocalDateTime paymentTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "确认收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "评价时间")
    private LocalDateTime commentTime;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "修改时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<OrderItem> orderItems;
    
    @TableField(exist = false)
    private String orderNo;
    
    @TableField(exist = false)
    private String buyerName;
    
    @TableField(exist = false)
    private String sellerName;

    public void setAddress(String address) {
        this.receiverAddress = address;
    }

    public String getAddress() {
        return this.receiverAddress;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 获取状态文本
    public String getStatusText() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待付款";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "已完成";
            case 4: return "已取消";
            case 5: return "退款中";
            case 6: return "已退款";
            default: return "未知";
        }
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }
} 