package com.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.secondhand.model.dto.ProductCreateDTO;
import com.secondhand.model.dto.ProductQueryDTO;
import com.secondhand.model.entity.Product;
import com.secondhand.model.vo.ProductVO;
import java.util.List;

public interface ProductService extends IService<Product> {
    // 获取商品列表（分页）
    IPage<ProductVO> getProductList(ProductQueryDTO queryDTO);
    
    // 获取商品总数
    long getProductCount(String keyword);
    
    // 获取商品详情
    ProductVO getProductDetail(Long id);
    
    // 创建商品
    Product createProduct(Product product);
    
    // 更新商品
    Product updateProduct(Product product);
    
    // 删除商品（下架）
    void deleteProduct(Long id);
    
    // 获取用户的商品列表
    List<ProductVO> getUserProducts(Long userId);
    
    // 更新商品状态
    void updateProductStatus(Long id, Integer status);

    ProductVO createProduct(ProductCreateDTO productDTO);

    /**
     * 审核商品
     * @param id 商品ID
     * @param auditStatus 审核状态：0-待审核，1-审核通过，2-审核不通过
     * @param auditReason 审核原因
     */
    void auditProduct(Long id, Integer auditStatus, String auditReason);
    
    /**
     * 获取待审核商品列表
     * @param current 当前页码
     * @param size 每页数量
     * @return 分页结果
     */
    IPage<ProductVO> getPendingProducts(Integer current, Integer size);
} 