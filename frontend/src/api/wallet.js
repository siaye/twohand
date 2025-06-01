import request from '@/utils/request'

// 获取钱包余额
export function getWalletBalance() {
  return request({
    url: '/api/wallet/balance',
    method: 'get'
  })
}

// 充值
export function recharge(amount) {
  return request({
    url: '/api/wallet/recharge',
    method: 'post',
    data: { amount }
  })
}

// 获取交易记录
export function getTransactions(params) {
  return request({
    url: '/api/wallet/transactions',
    method: 'get',
    params
  })
}

// TODO: 获取交易记录接口
// export function getTransactions(token, params) {
//   return request({
//     url: '/wallet/transactions',
//     method: 'get',
//     headers: {
//       'Authorization': token
//     },
//     params
//   })
// } 