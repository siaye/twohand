import request from '@/utils/request'

export function getMerchantInfo() {
  return request({
    url: '/api/merchant/info',
    method: 'get'
  })
}

export function applyMerchant(data) {
  return request({
    url: '/api/merchant/apply',
    method: 'post',
    data
  })
}

export function getMerchantList(params) {
  return request({
    url: '/api/merchant/list',
    method: 'get',
    params
  })
}

export function auditMerchant(data) {
  return request({
    url: '/api/merchant/audit',
    method: 'post',
    data
  })
} 