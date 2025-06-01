package com.secondhand.controller;

import com.secondhand.common.response.Result;
import com.secondhand.model.entity.Product;
import com.secondhand.model.entity.User;
import com.secondhand.model.entity.Category;
import com.secondhand.model.vo.ProductVO;
import com.secondhand.service.ProductService;
import com.secondhand.service.UserService;
import com.secondhand.service.CategoryService;
import com.secondhand.common.utils.JwtUtil;
import com.secondhand.common.response.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.secondhand.model.dto.ProductQueryDTO;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取商品列表
    @GetMapping("/list")
    public Result<IPage<ProductVO>> getProductList(ProductQueryDTO queryDTO) {
        try {
            log.info("【Product】获取商品列表：");
            log.info("- 查询参数：{}", queryDTO);
            
            IPage<ProductVO> page = productService.getProductList(queryDTO);
            
            log.info("- 查询结果：");
            log.info("  - 总记录数：{}", page.getTotal());
            log.info("  - 当前页记录数：{}", page.getRecords().size());
            
            return Result.success(page);
        } catch (Exception e) {
            log.error("【Product】获取商品列表失败：", e);
            return Result.error("获取商品列表失败：" + e.getMessage());
        }
    }

    // 获取商品详情
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        try {
            log.info("【Product】获取商品详情: id={}", id);
            ProductVO product = productService.getProductDetail(id);
            log.info("【Product】商品详情: id={}, title={}, sellerId={}", product.getId(), product.getTitle(), product.getSellerId());
            return Result.success(product);
        } catch (Exception e) {
            log.error("【Product】获取商品详情失败: id={}, error={}", id, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    // 创建商品
    @PostMapping("/create")
    public Result<Product> createProduct(@RequestBody Map<String, Object> requestData, @RequestHeader("Authorization") String token) {
        try {
            System.out.println("【Product】创建商品：");
            System.out.println("- 请求数据：" + requestData);
            
            // 从 token 中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            System.out.println("- 用户信息：" + user.getUsername());
            
            // 创建商品对象
            Product product = new Product();
            
            // 基本信息
            product.setTitle((String) requestData.get("title"));
            product.setDescription((String) requestData.get("description"));
            product.setPrice(new BigDecimal(requestData.get("price").toString()));
            product.setSize((String) requestData.get("size"));
            product.setColor((String) requestData.get("color"));
            product.setProductCondition((String) requestData.get("condition"));
            product.setLocation((String) requestData.get("location"));
            product.setBrand((String) requestData.get("brand"));
            
            // 设置原价
            Object originalPriceObj = requestData.get("originalPrice");
            if (originalPriceObj != null) {
                product.setOriginalPrice(new BigDecimal(originalPriceObj.toString()));
            }
            
            // 设置是否允许议价
            Object isNegotiableObj = requestData.get("isNegotiable");
            if (isNegotiableObj != null) {
                product.setIsNegotiable((Boolean) isNegotiableObj);
            } else {
                product.setIsNegotiable(false);
            }
            
            // 设置标签
            Object tagsObj = requestData.get("tags");
            if (tagsObj != null) {
                if (tagsObj instanceof List) {
                    product.setTags(String.join(",", (List<String>) tagsObj));
                } else if (tagsObj instanceof String) {
                    product.setTags((String) tagsObj);
                }
            }
            
            // 获取分类ID
            Object categoryIdObj = requestData.get("categoryId");
            if (categoryIdObj != null) {
                Long categoryId;
                if (categoryIdObj instanceof Integer) {
                    categoryId = ((Integer) categoryIdObj).longValue();
                } else if (categoryIdObj instanceof String) {
                    categoryId = Long.parseLong((String) categoryIdObj);
                } else if (categoryIdObj instanceof Long) {
                    categoryId = (Long) categoryIdObj;
                } else {
                    return Result.error("无效的商品分类ID");
                }
                
                // 验证分类是否存在
                Category category = categoryService.getById(categoryId);
                if (category == null) {
                    return Result.error("商品分类不存在");
                }
                product.setCategoryId(categoryId);
            } else {
                return Result.error("请选择商品分类");
            }
            
            // 处理图片数组
            List<String> images = (List<String>) requestData.get("images");
            if (images != null && !images.isEmpty()) {
                product.setImages(String.join(",", images));
            }
            
            // 库存和销量
            Object stockObj = requestData.get("stock");
            System.out.println("- 库存数据（原始）：" + stockObj);
            System.out.println("- 库存数据类型：" + (stockObj != null ? stockObj.getClass().getName() : "null"));
            Integer stock = null;
            try {
                if (stockObj instanceof Integer) {
                    stock = (Integer) stockObj;
                } else if (stockObj instanceof String) {
                    stock = Integer.parseInt((String) stockObj);
                } else if (stockObj instanceof Number) {
                    stock = ((Number) stockObj).intValue();
                }
            } catch (Exception e) {
                System.out.println("- 库存数据转换失败：" + e.getMessage());
            }
            System.out.println("- 库存数据（转换后）：" + stock);
            if (stock != null && stock >= 0) {
                product.setStock(stock);
            } else {
                product.setStock(0);  // 默认库存为0
            }
            product.setSalesCount(0);  // 初始销量为0
            
            // 商品状态
            Integer status = (Integer) requestData.get("status");
            if (status != null && (status == 0 || status == 1)) {
                product.setStatus(status);
            } else {
                product.setStatus(1);  // 默认状态为1（上架）
            }
            
            // 验证必填字段
            if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
                return Result.error("商品标题不能为空");
            }
            if (product.getTitle().length() > 200) {
                return Result.error("商品标题不能超过200个字符");
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("商品价格必须大于0");
            }
            if (product.getCategoryId() == null) {
                return Result.error("请选择商品分类");
            }
            if (product.getImages() == null || product.getImages().trim().isEmpty()) {
                return Result.error("请上传商品图片");
            }
            if (product.getSize() != null && product.getSize().length() > 50) {
                return Result.error("商品尺寸不能超过50个字符");
            }
            
            // 设置卖家ID
            product.setSellerId(user.getId());
            System.out.println("- 设置卖家ID：" + user.getId());
            
            // 设置时间
            LocalDateTime now = LocalDateTime.now();
            product.setCreatedAt(now);
            product.setUpdatedAt(now);
            
            System.out.println("- 商品对象：" + product);
            
            Product createdProduct = productService.createProduct(product);
            System.out.println("- 创建成功：" + createdProduct.getId());
            return Result.success(createdProduct);
        } catch (Exception e) {
            System.out.println("- 创建失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 根据分类名称获取分类ID
    private Long getCategoryIdByName(String categoryName) {
        // 这里应该从数据库或缓存中获取分类ID
        // 暂时使用硬编码的方式
        Map<String, Long> categoryMap = new HashMap<>();
        categoryMap.put("books", 1L);
        categoryMap.put("clothes", 2L);
        categoryMap.put("electronics", 3L);
        categoryMap.put("others", 4L);
        
        return categoryMap.get(categoryName.toLowerCase());
    }

    // 更新商品
    @PutMapping("/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> requestData, @RequestHeader("Authorization") String token) {
        try {
            System.out.println("【Product】更新商品：");
            System.out.println("- 商品ID：" + id);
            System.out.println("- 请求数据：" + requestData);
            
            // 验证权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            
            // 检查是否是商品所有者或管理员
            ProductVO existingProduct = productService.getProductDetail(id);
            if (!existingProduct.getSellerId().equals(user.getId()) && user.getRole() != 2) {
                return Result.error("无权修改此商品");
            }
            
            // 创建商品对象
            Product product = new Product();
            product.setId(id);
            
            // 基本信息
            product.setTitle((String) requestData.get("title"));
            product.setDescription((String) requestData.get("description"));
            product.setPrice(new BigDecimal(requestData.get("price").toString()));
            product.setSize((String) requestData.get("size"));
            
            // 获取分类ID
            String category = (String) requestData.get("category");
            if (category != null) {
                Long categoryId = getCategoryIdByName(category);
                if (categoryId != null) {
                    product.setCategoryId(categoryId);
                } else {
                    return Result.error("无效的商品分类");
                }
            }
            
            // 处理图片数组
            List<String> images = (List<String>) requestData.get("images");
            if (images != null && !images.isEmpty()) {
                product.setImages(String.join(",", images));
            }
            
            // 库存和销量
            Integer stock = (Integer) requestData.get("stock");
            if (stock != null && stock >= 0) {
                product.setStock(stock);
            }
            
            // 商品状态
            Integer status = (Integer) requestData.get("status");
            if (status != null && (status == 0 || status == 1)) {
                product.setStatus(status);
            }
            
            // 验证必填字段
            if (product.getTitle() == null || product.getTitle().trim().isEmpty()) {
                return Result.error("商品标题不能为空");
            }
            if (product.getTitle().length() > 200) {
                return Result.error("商品标题不能超过200个字符");
            }
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return Result.error("商品价格必须大于0");
            }
            if (product.getCategoryId() == null) {
                return Result.error("请选择商品分类");
            }
            if (product.getImages() == null || product.getImages().trim().isEmpty()) {
                return Result.error("请上传商品图片");
            }
            if (product.getSize() != null && product.getSize().length() > 50) {
                return Result.error("商品尺寸不能超过50个字符");
            }
            
            // 设置更新时间
            product.setUpdatedAt(LocalDateTime.now());
            
            Product updatedProduct = productService.updateProduct(product);
            return Result.success(updatedProduct);
        } catch (Exception e) {
            System.out.println("- 更新失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 删除商品（下架）
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            
            // 检查是否是商品所有者或管理员
            ProductVO product = productService.getProductDetail(id);
            if (!product.getSellerId().equals(user.getId()) && user.getRole() != 2) {
                return Result.error("无权删除此商品");
            }
            
            productService.deleteProduct(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取用户的商品列表
    @GetMapping("/user")
    public Result<List<ProductVO>> getUserProducts(@RequestHeader("Authorization") String token) {
        try {
            System.out.println("【Product】获取用户商品列表：");
            
            // 从 token 中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            System.out.println("- 用户信息：" + user.getUsername());
            
            List<ProductVO> products = productService.getUserProducts(user.getId());
            System.out.println("- 获取到 " + products.size() + " 个商品");
            
            return Result.success(products);
        } catch (Exception e) {
            System.out.println("- 获取失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 更新商品状态
    @PutMapping("/status")
    public Result<Void> updateProductStatus(
            @RequestParam Long id,
            @RequestParam Integer status,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            
            // 检查是否是商品所有者或管理员
            ProductVO product = productService.getProductDetail(id);
            if (!product.getSellerId().equals(user.getId()) && user.getRole() != 2) {
                return Result.error("无权修改此商品状态");
            }
            
            productService.updateProductStatus(id, status);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/upload")
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("【Product】上传商品图片：");

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                System.out.println("- 错误：只能上传图片文件");
                return Result.error("只能上传图片文件");
            }

            // 验证文件大小（4MB）
            if (file.getSize() > 4 * 1024 * 1024) {
                System.out.println("- 错误：图片大小不能超过4MB");
                return Result.error("图片大小不能超过4MB");
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            // 创建上传目录
            String uploadDir = projectRoot + File.separator + "uploads" + File.separator + "products";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File destFile = new File(dir, filename);
            file.transferTo(destFile);
            System.out.println("- 文件保存成功：" + destFile.getAbsolutePath());

            // 返回文件URL（使用相对路径）
            String imageUrl = "/api/uploads/products/" + filename;
            System.out.println("- 返回URL：" + imageUrl);
            return Result.success(imageUrl);
        } catch (Exception e) {
            System.out.println("- 上传失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    // 获取我的商品列表
    @GetMapping("/my")
    public Result<Map<String, Object>> getMyProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        try {
            // 从 token 中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));
            User user = userService.getUserByUsername(username);
            
            // 获取用户的商品列表
            List<ProductVO> products = productService.getUserProducts(user.getId());
            
            // 根据关键字和状态过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = products.stream()
                    .filter(p -> p.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
            }
            
            if (status != null) {
                products = products.stream()
                    .filter(p -> p.getStatus().equals(status))
                    .collect(Collectors.toList());
            }
            
            // 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, products.size());
            List<ProductVO> pagedProducts = products.subList(start, end);
            
            Map<String, Object> result = new HashMap<>();
            result.put("records", pagedProducts);
            result.put("total", products.size());
            result.put("page", page);
            result.put("size", size);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核商品
     */
    @PostMapping("/audit/{id}")
    public Result<Void> auditProduct(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            log.info("【ProductController】审核商品：");
            log.info("- 商品ID：{}", id);
            log.info("- 审核数据：{}", requestData);
            
            // 获取审核状态和原因
            Integer auditStatus = (Integer) requestData.get("auditStatus");
            String auditReason = (String) requestData.get("auditReason");
            
            if (auditStatus == null) {
                return Result.error("审核状态不能为空");
            }
            
            // 调用服务层进行审核
            productService.auditProduct(id, auditStatus, auditReason);
            
            return Result.success(null);
        } catch (Exception e) {
            log.error("【ProductController】审核商品失败：", e);
            return Result.error("审核商品失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取待审核商品列表
     */
    @GetMapping("/pending")
    public Result<IPage<ProductVO>> getPendingProducts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 创建分页对象
            Page<Product> page = new Page<>(current, size);
            
            // 创建查询条件
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getStatus, 0)  // 待审核状态
                    .orderByDesc(Product::getCreatedAt);
            
            // 执行查询
            IPage<Product> productPage = productService.page(page, queryWrapper);
            
            // 转换为VO对象
            IPage<ProductVO> voPage = productPage.convert(product -> {
                ProductVO vo = new ProductVO();
                BeanUtils.copyProperties(product, vo);
                
                // 设置分类名称
                Category category = categoryService.getById(product.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
                
                // 设置卖家信息
                User seller = userService.getById(product.getSellerId());
                if (seller != null) {
                    vo.setSellerName(seller.getUsername());
                }
                
                // 设置图片列表
                if (StringUtils.hasText(product.getImages())) {
                    List<String> imageList = Arrays.asList(product.getImages().split(","));
                    // 添加完整的URL前缀
                    imageList = imageList.stream()
                            .map(image -> image.startsWith("http") ? image : "http://localhost:8080" + image)
                            .collect(Collectors.toList());
                    vo.setImageList(imageList);
                    if (!vo.getImageList().isEmpty()) {
                        vo.setImage(vo.getImageList().get(0));
                    }
                }
                
                return vo;
            });
            
            return Result.success(voPage);
        } catch (Exception e) {
            log.error("获取待审核商品列表失败", e);
            return Result.error("获取待审核商品列表失败");
        }
    }
} 