import request from '@/utils/request'

// 获取商品列表
export function getProducts(params) {
  return request({
    url: '/api/product/list',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProduct(id) {
  return request({
    url: `/api/product/${id}`,
    method: 'get'
  })
}

// 创建商品
export function createProduct(data) {
  return request({
    url: '/api/product/create',
    method: 'post',
    data
  })
}

// 更新商品
export function updateProduct(id, data) {
  return request({
    url: `/api/product/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteProduct(id) {
  return request({
    url: `/api/product/${id}`,
    method: 'delete'
  })
}

// 获取用户的商品列表
export function getUserProducts() {
  return request({
    url: '/api/product/user',
    method: 'get'
  })
}

// 更新商品状态
export function updateProductStatus(id, status) {
  return request({
    url: '/api/product/status',
    method: 'put',
    params: { id, status }
  })
}

// 发布商品
export function publishProduct(data) {
  return request({
    url: '/api/product/create',
    method: 'post',
    data
  })
}

// 获取我的商品列表
export function getMyProducts(params) {
  return request({
    url: '/api/product/user',
    method: 'get',
    params
  })
}

// 上传商品图片
export function uploadProductImage(data) {
  return request({
    url: '/product/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

/**
 * 获取待审核商品列表
 * @param {number} current 当前页码
 * @param {number} size 每页数量
 */
export function getPendingProducts(current, size) {
  return request({
    url: '/api/product/pending',
    method: 'get',
    params: { current, size }
  })
}

/**
 * 审核商品
 * @param {number} id 商品ID
 * @param {number} auditStatus 审核状态：1-通过，2-不通过
 * @param {string} auditReason 审核原因
 */
export function auditProduct(id, auditStatus, auditReason) {
  return request({
    url: `/api/product/audit/${id}`,
    method: 'post',
    data: { auditStatus, auditReason }
  })
} 