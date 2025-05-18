import request from '@/utils/request'

// 获取待审核商家列表
export function getPendingMerchants() {
  return request.get('/api/user/pending-merchants')
}

// 审核商家
export function auditMerchant(userId, auditStatus, reason) {
  return request.post('/api/user/audit', null, {
    params: { userId, auditStatus, reason }
  })
}

export function register(data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
}

export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
} 