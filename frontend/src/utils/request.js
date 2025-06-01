import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import store from '@/store'

// 创建axios实例
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  withCredentials: true // 允许跨域请求携带cookie
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 验证码请求特殊处理
    if (config.url === '/user/captcha') {
      config.responseType = 'blob'
      return config
    }

    // 如果 URL 不以 /api 开头，则添加 /api 前缀
    if (!config.url.startsWith('/api')) {
      config.url = '/api' + config.url
    }

    // 从store获取token
    const token = store.getters.token
    console.log('【Request】请求拦截器 - Token:', {
      token,
      hasToken: !!token,
      url: config.url
    })
    
    if (token) {
      // 确保 token 格式正确
      const cleanToken = token.replace(/^Bearer\s+/i, '').trim()
      if (cleanToken) {
        config.headers['Authorization'] = `Bearer ${cleanToken}`
      }
    }

    // 从sessionStorage获取临时用户ID
    const tempUserId = sessionStorage.getItem('tempUserId')
    if (tempUserId) {
      config.headers['X-User-ID'] = Number(tempUserId)
      console.log('【Request】添加X-User-ID:', Number(tempUserId))
    }

    // 处理请求数据
    if (config.data && typeof config.data === 'object') {
      console.log('【Request】原始请求数据:', {
        url: config.url,
        method: config.method,
        data: JSON.stringify(config.data)
      })

      // 如果是 FormData，不做处理
      if (!(config.data instanceof FormData)) {
        // 处理普通对象数据
        Object.keys(config.data).forEach(key => {
          if (typeof config.data[key] === 'string') {
            const originalValue = config.data[key]
            config.data[key] = config.data[key].trim()
            console.log(`【Request】处理字段 ${key}:`, {
              original: originalValue,
              trimmed: config.data[key],
              hasSpaces: originalValue.includes(' ')
            })
          }
        })
      }

      console.log('【Request】处理后的请求数据:', {
        url: config.url,
        method: config.method,
        data: JSON.stringify(config.data)
      })
    }

    return config
  },
  error => {
    console.error('【Request】请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('【Response】响应成功:', response.data)
    return response.data
  },
  error => {
    console.log('【Response】响应错误:', error)
    console.log('【Response】错误详情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      config: {
        url: error.config?.url,
        method: error.config?.method,
        data: error.config?.data,
        headers: error.config?.headers
      }
    })
    
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // 检查是否是完善信息页面的请求
          const isCompleteProfile = error.config?.url?.includes('/user/profile') || 
                                  error.config?.url?.includes('/user/complete-profile')
          if (isCompleteProfile) {
            // 如果是完善信息页面的请求，不跳转登录页
            ElMessage.error('请先完成注册')
            return Promise.reject(error)
          }
          // 其他401错误跳转到登录页
          ElMessage.error('登录已过期，请重新登录')
          router.replace('/login')
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

export default service 