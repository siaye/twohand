package com.secondhand.model.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateDTO {
    @NotBlank(message = "商品标题不能为空")
    @Size(max = 200, message = "商品标题不能超过200个字符")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    private String description;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "商品原价不能为空")
    @DecimalMin(value = "0.01", message = "商品原价必须大于0")
    private BigDecimal originalPrice;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品品牌不能为空")
    private String brand;

    @NotBlank(message = "商品尺寸不能为空")
    private String size;

    @NotBlank(message = "商品颜色不能为空")
    private String color;

    @NotBlank(message = "商品成色不能为空")
    private String condition;

    @NotBlank(message = "商品位置不能为空")
    private String location;

    @NotEmpty(message = "商品图片不能为空")
    private List<String> images;

    @NotNull(message = "商品库存不能为空")
    @Min(value = 0, message = "商品库存不能小于0")
    private Integer stock;

    @NotNull(message = "是否允许议价不能为空")
    private Boolean isNegotiable;

    private String tags;
} 