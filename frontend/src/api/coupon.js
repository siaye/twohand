import request from '@/utils/request'

// TODO: Implement coupon related api calls 

// 获取用户可用优惠券
export function getUserAvailableCoupons(userId, params) {
  return request({
    url: `/api/user/${userId}/coupons/available`,
    method: 'get',
    params
  })
}

// 获取优惠券详情
export function getUserCouponById(couponId) {
  return request({
    url: `/api/user/coupons/${couponId}`,
    method: 'get'
  })
}

// 获取今日可领取的优惠券
export function getTodayAvailableCoupons() {
  return request({
    url: '/api/user/coupons/today',
    method: 'get'
  })
}

// 领取每日优惠券
export function claimDailyCoupon(couponId) {
  return request({
    url: `/api/user/coupons/today/${couponId}`,
    method: 'post'
  })
} 