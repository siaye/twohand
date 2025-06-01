import request from '@/utils/request'

// 获取分类列表
export function getCategories() {
  return request({
    url: '/api/category/list',
    method: 'get'
  })
}

// 获取分类详情
export function getCategory(id) {
  return request({
    url: `/api/category/${id}`,
    method: 'get'
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/api/category/create',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/api/category/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/api/category/${id}`,
    method: 'delete'
  })
} 