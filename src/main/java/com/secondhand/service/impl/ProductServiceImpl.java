package com.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.secondhand.model.entity.Product;
import com.secondhand.model.entity.User;
import com.secondhand.model.vo.ProductVO;
import com.secondhand.repository.ProductMapper;
import com.secondhand.repository.UserMapper;
import com.secondhand.service.ProductService;
import com.secondhand.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.secondhand.model.dto.ProductQueryDTO;
import com.secondhand.service.CategoryService;
import com.secondhand.model.entity.Category;
import com.secondhand.model.dto.ProductCreateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    @Override
    public IPage<ProductVO> getProductList(ProductQueryDTO queryDTO) {
        try {
            log.info("【ProductService】获取商品列表：");
            log.info("- 查询参数：{}", queryDTO);
            
            // 构建查询条件
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            
            // 只查询上架状态的商品
            wrapper.eq(Product::getStatus, 1);  // 上架状态
            
            // 关键字搜索
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                wrapper.and(w -> w
                    .like(Product::getTitle, queryDTO.getKeyword())
                    .or()
                    .like(Product::getDescription, queryDTO.getKeyword())
                );
            }
            
            // 分类筛选
            if (queryDTO.getCategoryId() != null) {
                wrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
            }
            
            // 品牌筛选
            if (StringUtils.hasText(queryDTO.getBrand())) {
                wrapper.eq(Product::getBrand, queryDTO.getBrand());
            }
            
            // 成色筛选
            if (StringUtils.hasText(queryDTO.getProductCondition())) {
                wrapper.eq(Product::getProductCondition, queryDTO.getProductCondition());
            }
            
            // 价格区间筛选
            if (queryDTO.getMinPrice() != null) {
                wrapper.ge(Product::getPrice, queryDTO.getMinPrice());
            }
            if (queryDTO.getMaxPrice() != null) {
                wrapper.le(Product::getPrice, queryDTO.getMaxPrice());
            }
            
            // 标签筛选
            if (queryDTO.getTags() != null && !queryDTO.getTags().isEmpty()) {
                wrapper.and(w -> {
                    for (String tag : queryDTO.getTags()) {
                        w.or().like(Product::getTags, tag);
                    }
                });
            }
            
            // 位置筛选
            if (StringUtils.hasText(queryDTO.getLocation())) {
                wrapper.like(Product::getLocation, queryDTO.getLocation());
            }
            
            // 排序
            if (StringUtils.hasText(queryDTO.getSortBy())) {
                switch (queryDTO.getSortBy()) {
                    case "priceAsc":
                        wrapper.orderByAsc(Product::getPrice);
                        break;
                    case "priceDesc":
                        wrapper.orderByDesc(Product::getPrice);
                        break;
                    case "sales":
                        wrapper.orderByDesc(Product::getSalesCount);
                        break;
                    default:
                        wrapper.orderByDesc(Product::getCreatedAt);
                }
            } else {
                wrapper.orderByDesc(Product::getCreatedAt);
            }
            
            log.info("- 查询条件：{}", wrapper.getCustomSqlSegment());
            
            // 创建分页对象
            Page<Product> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            
            // 执行查询
            IPage<Product> result = productMapper.selectPage(page, wrapper);
            
            // 转换为VO对象
            IPage<ProductVO> voPage = result.convert(product -> {
                ProductVO vo = new ProductVO();
                BeanUtils.copyProperties(product, vo);
                
                // 获取卖家信息
                User seller = userService.getById(product.getSellerId());
                if (seller != null) {
                    vo.setSellerId(seller.getId());
                    vo.setSellerName(seller.getUsername());
                    String sellerAvatar = seller.getAvatar();
                    if (sellerAvatar != null && !sellerAvatar.isEmpty()) {
                        if (!sellerAvatar.startsWith("http")) {
                            sellerAvatar = "http://localhost:8080" + sellerAvatar;
                        }
                        vo.setSellerAvatar(sellerAvatar);
                    } else {
                        vo.setSellerAvatar("");
                    }
                    log.info("【ProductService】设置卖家信息: sellerId={}, sellerName={}, sellerAvatar={}", 
                        seller.getId(), seller.getUsername(), sellerAvatar);
                } else {
                    log.warn("【ProductService】未找到卖家信息: sellerId={}", product.getSellerId());
                }
                
                // 获取分类名称
                Category category = categoryService.getById(product.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getName());
                }
                
                // 处理图片列表
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    String[] imageArray = product.getImages().split(",");
                    List<String> images = new ArrayList<>();
                    for (String image : imageArray) {
                        if (!image.isEmpty()) {
                            if (image.startsWith("http")) {
                                images.add(image);
                            } else {
                                // 确保URL以/api开头
                                if (!image.startsWith("/api")) {
                                    image = "/api" + (image.startsWith("/") ? image : "/" + image);
                                }
                                images.add("http://localhost:8080" + image);
                            }
                        }
                    }
                    vo.setImageList(images);
                    if (!images.isEmpty()) {
                        vo.setImage(images.get(0));
                    }
                }
                
                log.info("【ProductService】转换商品信息: id={}, title={}, sellerId={}", vo.getId(), vo.getTitle(), vo.getSellerId());
                return vo;
            });
            
            return voPage;
        } catch (Exception e) {
            log.error("【ProductService】获取商品列表失败：", e);
            throw e;
        }
    }

    @Override
    public long getProductCount(String keyword) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getStatus, 1);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(wrapper -> wrapper
                .like(Product::getTitle, keyword)
                .or()
                .like(Product::getDescription, keyword)
            );
        }
        
        return productMapper.selectCount(query);
    }

    @Override
    public ProductVO getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return convertToVO(product);
    }

    private ProductVO convertToVO(Product product) {
        try {
            log.info("【ProductService】转换商品信息: id={}, title={}", product.getId(), product.getTitle());
            
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(product, vo);
            
            // 获取卖家信息
            User seller = userService.getById(product.getSellerId());
            if (seller != null) {
                vo.setSellerId(seller.getId());
                vo.setSellerName(seller.getUsername());
                String sellerAvatar = seller.getAvatar();
                if (sellerAvatar != null && !sellerAvatar.isEmpty()) {
                    if (!sellerAvatar.startsWith("http")) {
                        sellerAvatar = "http://localhost:8080" + sellerAvatar;
                    }
                    vo.setSellerAvatar(sellerAvatar);
                } else {
                    vo.setSellerAvatar("");
                }
                log.info("【ProductService】设置卖家信息: sellerId={}, sellerName={}, sellerAvatar={}", 
                    seller.getId(), seller.getUsername(), sellerAvatar);
            } else {
                log.warn("【ProductService】未找到卖家信息: sellerId={}", product.getSellerId());
            }
            
            // 获取分类名称
            Category category = categoryService.getById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            
            // 处理图片列表
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                String[] imageArray = product.getImages().split(",");
                List<String> images = new ArrayList<>();
                for (String image : imageArray) {
                    if (!image.isEmpty()) {
                        if (image.startsWith("http")) {
                            images.add(image);
                        } else {
                            // 确保URL以/api开头
                            if (!image.startsWith("/api")) {
                                image = "/api" + (image.startsWith("/") ? image : "/" + image);
                            }
                            images.add("http://localhost:8080" + image);
                        }
                    }
                }
                vo.setImageList(images);
                if (!images.isEmpty()) {
                    vo.setImage(images.get(0));
                }
            }
            
            log.info("【ProductService】转换完成: id={}, title={}, sellerId={}", vo.getId(), vo.getTitle(), vo.getSellerId());
            return vo;
        } catch (Exception e) {
            log.error("【ProductService】转换商品信息失败: id={}", product.getId(), e);
            throw new RuntimeException("转换商品信息失败: " + e.getMessage());
        }
    }

    @Override
    public Product createProduct(Product product) {
        try {
            System.out.println("【ProductService】创建商品：");
            System.out.println("- 商品数据：" + product);
            
            // 设置初始状态
            product.setStatus(0);  // 待审核状态
            product.setAuditStatus(0);  // 待审核
            product.setSalesCount(0);  // 初始销量为0
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            
            System.out.println("- 插入数据库...");
            int rows = productMapper.insert(product);
            System.out.println("- 插入结果：" + rows + " 行");
            
            if (rows > 0) {
                System.out.println("- 创建成功，ID：" + product.getId());
                return product;
            } else {
                System.out.println("- 创建失败：插入行数为0");
                throw new RuntimeException("创建商品失败");
            }
        } catch (Exception e) {
            System.out.println("- 创建失败：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Product updateProduct(Product product) {
        Product existingProduct = productMapper.selectById(product.getId());
        if (existingProduct == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 更新商品信息
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 将商品状态改为下架
        product.setStatus(2);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public List<ProductVO> getUserProducts(Long userId) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        query.eq(Product::getSellerId, userId)
             .orderByDesc(Product::getCreatedAt);
        
        List<Product> products = productMapper.selectList(query);
        return products.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public void updateProductStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
    }

    @Override
    public ProductVO createProduct(ProductCreateDTO productDTO) {
        try {
            System.out.println("【ProductService】创建商品：");
            System.out.println("- 商品数据：" + productDTO);
            
            // 创建商品实体
            Product product = new Product();
            BeanUtils.copyProperties(productDTO, product);
            
            // 设置初始状态
            product.setStatus(1);  // 正常状态
            product.setSalesCount(0);  // 初始销量为0
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            
            // 处理图片列表
            if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
                product.setImages(String.join(",", productDTO.getImages()));
            }
            
            System.out.println("- 插入数据库...");
            int rows = productMapper.insert(product);
            System.out.println("- 插入结果：" + rows + " 行");
            
            if (rows > 0) {
                System.out.println("- 创建成功，ID：" + product.getId());
                return convertToVO(product);
            } else {
                System.out.println("- 创建失败：插入行数为0");
                throw new RuntimeException("创建商品失败");
            }
        } catch (Exception e) {
            System.out.println("- 创建失败：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void auditProduct(Long id, Integer auditStatus, String auditReason) {
        try {
            log.info("【ProductService】审核商品：");
            log.info("- 商品ID：{}", id);
            log.info("- 审核状态：{}", auditStatus);
            log.info("- 审核原因：{}", auditReason);
            
            // 获取商品信息
            Product product = getById(id);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            
            // 更新审核状态
            product.setAuditStatus(auditStatus);
            product.setAuditReason(auditReason);
            product.setAuditTime(LocalDateTime.now());
            
            // 如果审核通过，将商品状态设置为上架
            if (auditStatus == 1) {
                product.setStatus(1);
            } else {
                // 如果审核不通过，将商品状态设置为下架
                product.setStatus(2);
            }
            
            // 保存更新
            if (!updateById(product)) {
                throw new RuntimeException("更新商品审核状态失败");
            }
            
            log.info("- 审核完成");
        } catch (Exception e) {
            log.error("【ProductService】审核商品失败：", e);
            throw new RuntimeException("审核商品失败：" + e.getMessage());
        }
    }
    
    @Override
    public IPage<ProductVO> getPendingProducts(Integer current, Integer size) {
        try {
            log.info("【ProductService】获取待审核商品列表：");
            log.info("- 页码：{}", current);
            log.info("- 每页数量：{}", size);
            
            // 构建查询条件
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Product::getAuditStatus, 0)  // 待审核状态
                  .orderByDesc(Product::getCreatedAt);
            
            // 执行分页查询
            Page<Product> page = new Page<>(current, size);
            IPage<Product> productPage = page(page, wrapper);
            
            // 转换为VO对象
            IPage<ProductVO> voPage = productPage.convert(this::convertToVO);
            
            log.info("- 查询完成，总记录数：{}", voPage.getTotal());
            return voPage;
        } catch (Exception e) {
            log.error("【ProductService】获取待审核商品列表失败：", e);
            throw new RuntimeException("获取待审核商品列表失败：" + e.getMessage());
        }
    }
} 