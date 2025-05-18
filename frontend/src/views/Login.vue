<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>用户登录</h2>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-container">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 200px"></el-input>
            <img 
              :src="captchaUrl" 
              @click="refreshCaptcha" 
              class="captcha-img" 
              alt="验证码"
              crossorigin="use-credentials"
            >
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="$router.push('/register')">注册账号</el-button>
          <el-button @click="$router.push('/')">返回主页</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const captchaUrl = ref('')

const refreshCaptcha = () => {
  captchaUrl.value = `http://localhost:8080/api/user/captcha?t=${new Date().getTime()}`
}

onMounted(() => {
  refreshCaptcha()
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await login({
          username: form.username,
          password: form.password,
          captcha: form.captcha
        })
        localStorage.setItem('token', res)
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
        refreshCaptcha()
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.login-card {
  width: 500px;
}

.login-card :deep(.el-card__header) {
  text-align: center;
}

.captcha-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.captcha-img {
  height: 40px;
  cursor: pointer;
}
</style> 