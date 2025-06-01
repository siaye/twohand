import request from '@/utils/request'

// 获取待审核商家列表
export function getPendingMerchants() {
  return request.get('/admin/merchants/pending')
}

// 审核商家
export function auditMerchant(data) {
  return request.post('/admin/merchants/audit', data)
}

// 用户登录
export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

// 更新用户信息
export function updateUserInfo(data) {
  return request({
    url: '/api/user/update',
    method: 'put',
    data
  })
}

// 获取验证码
export function getCaptcha() {
  return request({
    url: '/user/captcha',
    method: 'get',
    responseType: 'blob'
  })
}

// 完善用户信息
export function completeProfile(data) {
  const { userId, ...profileData } = data
  return request({
    url: '/user/complete-profile',
    method: 'post',
    data: profileData,
    headers: {
      'X-User-ID': userId
    }
  })
}

// 上传图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 上传头像
export function uploadAvatar(data) {
  return request({
    url: '/api/user/avatar',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

// 修改密码
export function changePassword(data) {
  return request({
    url: '/api/user/password',
    method: 'put',
    data
  })
}

// 获取用户积分
export function getUserPoints() {
  return request({
    url: '/api/user/points',
    method: 'get'
  })
} 