package com.secondhand.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("user_points")
@Schema(description = "用户积分表")
public class UserPoints implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "积分数量")
    private Integer points;

    @Schema(description = "创建时间")
    private LocalDateTime created_at;

    @Schema(description = "修改时间")
    private LocalDateTime updated_at;

    public void setUpdatedAt(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
} 