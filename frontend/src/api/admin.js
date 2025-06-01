import request from '@/utils/request'

// 获取仪表盘数据
export function getDashboardData() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}

// 获取待审核商品列表
export function getPendingProducts(current = 1, size = 10) {
  return request({
    url: '/api/product/pending',
    method: 'get',
    params: { current, size }
  })
}

// 获取待处理订单列表
export function getPendingOrders() {
  return request({
    url: '/admin/order/list',
    method: 'get',
    params: {
      status: 1  // 待发货状态
    }
  })
}

// 审核商品
export function auditProduct(id, data) {
  return request({
    url: `/api/product/audit/${id}`,
    method: 'post',
    data
  })
}

// 获取用户列表
export function getUsers(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 更新用户状态
export function updateUserStatus(data) {
  return request({
    url: '/admin/user/status',
    method: 'post',
    params: data
  })
}

// 获取商家列表
export function getMerchants(params) {
  return request({
    url: '/admin/merchants',
    method: 'get',
    params
  })
}

// 审核商家
export function auditMerchant(data) {
  return request({
    url: '/admin/merchant/audit',
    method: 'post',
    params: data
  })
}

// 获取所有订单列表
export function getAdminOrders(params) {
  return request({
    url: '/api/admin/order/list',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getAdminOrderDetail(id) {
  return request({
    url: `/api/admin/order/${id}`,
    method: 'get'
  })
}

// 更新订单状态
export function updateAdminOrderStatus(id, status) {
  return request({
    url: `/api/admin/order/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取所有用户列表
export function getAllUsers() {
  return request({
    url: '/admin/users',
    method: 'get'
  }).then(res => {
    if (res.code === 200) {
      return res.data
    } else {
      throw new Error(res.message || '获取用户列表失败')
    }
  })
}

// 获取待审核用户列表
export function getPendingUsers() {
  return request({
    url: '/admin/users/pending',
    method: 'get'
  })
}

// 审核用户
export function auditUser(data) {
  const { userId, auditStatus } = data
  return request({
    url: '/admin/merchant/audit',
    method: 'post',
    params: {
      userId,
      auditStatus
    }
  })
}

// 删除用户
export function deleteUser(userId) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'delete'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/admin/user/update',
    method: 'post',
    data
  })
} 