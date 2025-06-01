import request from '@/utils/request'

// 获取订单列表
export function getOrders(params) {
  return request({
    url: '/api/orders/list',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(orderId) {
  return request({
    url: `/api/orders/${orderId}`,
    method: 'get'
  })
}

// 创建订单
export function createOrder(data) {
  return request({
    url: '/api/orders',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 更新订单状态
export function updateOrderStatus(id, status) {
  return request({
    url: `/api/orders/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 删除订单
export function deleteOrder(id) {
  return request({
    url: `/api/orders/${id}`,
    method: 'delete'
  })
}

// 创建评价
export function createReview(data) {
  return request({
    url: '/api/reviews',
    method: 'post',
    data
  })
}

// 获取商品评价列表
export function getProductReviews(productId) {
  return request({
    url: `/api/reviews/product/${productId}`,
    method: 'get'
  })
}

// 获取用户评价列表
export function getUserReviews(userId) {
  return request({
    url: `/api/reviews/user/${userId}`,
    method: 'get'
  })
}

// 获取订单评价
export function getOrderReviews(orderId) {
  return request({
    url: `/api/reviews/order/${orderId}`,
    method: 'get'
  })
}

// 获取用户订单列表
export function getUserOrders(params) {
  return request({
    url: '/api/orders/user',
    method: 'get',
    params
  })
}

// 获取卖家订单列表
export function getSellerOrders(params) {
  return request({
    url: '/api/orders/seller',
    method: 'get',
    params
  })
}

// 支付订单
export function payOrder(orderId) {
  return request({
    url: `/api/orders/${orderId}/pay`,
    method: 'post'
  })
}

// 发货
export function shipOrder(orderId) {
  return request({
    url: `/api/orders/${orderId}/ship`,
    method: 'post'
  })
}

// 确认收货
export function confirmReceipt(orderId) {
  return request({
    url: `/api/orders/${orderId}/confirm`,
    method: 'post'
  })
}

// 申请退款
export function refundOrder(orderId) {
  return request({
    url: `/api/orders/${orderId}/refund`,
    method: 'post'
  })
}

// 确认退款
export function confirmRefund(orderId) {
  return request({
    url: `/api/orders/${orderId}/confirm-refund`,
    method: 'post'
  })
}

// 拒绝退款
export function rejectRefund(orderId) {
  return request({
    url: `/api/orders/${orderId}/reject-refund`,
    method: 'post'
  })
}

// 取消订单
export function cancelOrder(orderId) {
  return request({
    url: `/api/orders/${orderId}/cancel`,
    method: 'post'
  })
}

// 删除评价
export function deleteReview(reviewId) {
  return request({
    url: `/api/reviews/${reviewId}`,
    method: 'delete'
  })
} 