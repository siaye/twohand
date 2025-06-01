import request from '@/utils/request'

export function getMerchantInfo() {
  return request({
    url: '/merchant/info',
    method: 'get'
  })
}

export function applyMerchant(data) {
  return request({
    url: '/merchant/apply',
    method: 'post',
    data
  })
}

export function getMerchantList(params) {
  return request({
    url: '/merchant/list',
    method: 'get',
    params
  })
}

export function auditMerchant(data) {
  return request({
    url: '/merchant/audit',
    method: 'post',
    data
  })
} 