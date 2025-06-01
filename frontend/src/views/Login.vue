<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>用户登录</h2>
        </div>
      </template>
      
      <el-form 
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-container">
            <el-input 
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              :prefix-icon="Key"
              style="width: 200px"
            />
            <img 
              :src="captchaUrl" 
              @click="handleCaptchaClick" 
              class="captcha-img" 
              alt="验证码"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            native-type="submit"
            :loading="loading"
            class="submit-btn"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { login } from '@/api/user'
import { getCaptcha } from '@/api/auth'
import store from '@/store'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref(null)
const loading = ref(false)
const captchaUrl = ref('')

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

// 刷新验证码
const refreshCaptcha = () => {
  // 添加时间戳防止缓存
  const timestamp = new Date().getTime()
  captchaUrl.value = `http://localhost:8080/api/user/captcha?t=${timestamp}`
}

// 处理验证码点击
const handleCaptchaClick = () => {
  refreshCaptcha()
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    console.log('【Login】开始登录请求:', {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha
    })
    
    // 使用 store 的 login action 处理登录
    const res = await store.dispatch('login', {
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha
    })
    
    console.log('【Login】登录响应详情:', {
      code: res.code,
      data: res.data,
      role: res.data?.role,
      userType: res.data?.userType,
      username: res.data?.username,
      storeUser: store.getters.user,
      isAdmin: store.getters.isAdmin
    })
    
    if (res.code === 200 && res.data) {
      // 确保用户信息已经正确保存到 store
      const userInfo = store.getters.user
      console.log('【Login】Store 中的用户信息:', {
        userInfo,
        role: userInfo?.role,
        isAdmin: store.getters.isAdmin,
        localStorageUser: JSON.parse(localStorage.getItem('userInfo') || 'null')
      })
      
      if (!userInfo) {
        console.error('【Login】用户信息未正确保存到 store')
        throw new Error('登录状态异常，请重试')
      }
      
      ElMessage.success('登录成功')
      
      // 等待下一个 tick，确保 store 更新完成
      await nextTick()
      
      // 根据用户角色跳转到不同页面
      if (store.getters.isAdmin) {
        console.log('【Login】检测到管理员角色，准备跳转到管理后台')
        // 检查是否有重定向地址
        const redirect = route.query.redirect
        if (redirect && redirect.startsWith('/admin')) {
          console.log('【Login】检测到管理后台重定向地址:', redirect)
          await router.push(redirect)
        } else {
          console.log('【Login】无重定向地址或非管理后台地址，跳转到管理后台首页')
          await router.push('/admin/dashboard')
        }
      } else {
        // 普通用户跳转逻辑
        console.log('【Login】检测到普通用户角色，准备跳转')
        const redirect = route.query.redirect
        if (redirect && !redirect.startsWith('/admin')) {
          console.log('【Login】检测到重定向地址:', redirect)
          await router.push(redirect)
        } else {
          console.log('【Login】无重定向地址或管理后台地址，跳转到首页')
          await router.push('/')
        }
      }
    } else {
      throw new Error(res.message || '登录失败')
    }
  } catch (error) {
    console.error('【Login】登录失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '登录失败，请检查用户名和密码')
    // 登录失败时刷新验证码
    refreshCaptcha()
    loginForm.captcha = ''
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取验证码
onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 480px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.submit-btn {
  width: 100%;
}

.form-footer {
  margin-top: 16px;
  text-align: center;
}

.form-footer a {
  color: #409EFF;
  text-decoration: none;
}

.form-footer a:hover {
  color: #66b1ff;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.captcha-img {
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
}
</style> 