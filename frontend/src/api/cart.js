import request from '@/utils/request'

// 获取购物车列表
export function getCartList() {
  return request({
    url: '/api/cart/list',
    method: 'get'
  })
}

// 添加商品到购物车
export function addToCart(data) {
  return request({
    url: '/api/cart/add',
    method: 'post',
    data
  })
}

// 更新购物车商品数量
export function updateCartItemQuantity(data) {
  return request({
    url: '/api/cart/update',
    method: 'put',
    data
  })
}

// 删除购物车商品
export function removeFromCart(cartItemId) {
  return request({
    url: `/api/cart/${cartItemId}`,
    method: 'delete'
  })
}

// 清空购物车
export function clearCart() {
  return request({
    url: '/api/cart/clear',
    method: 'delete'
  })
}

// 更新购物车商品状态
export function updateCartItemStatus(data) {
  return request({
    url: '/api/cart/status',
    method: 'put',
    data
  })
} 