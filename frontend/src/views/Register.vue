<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>用户注册</h2>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio :label="0">个人用户</el-radio>
            <el-radio :label="1">商家用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-container">
            <el-input v-model="form.captcha" placeholder="请输入验证码" style="width: 200px"></el-input>
            <img :src="captchaUrl" @click="refreshCaptcha" class="captcha-img" alt="验证码">
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
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
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref(null)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  captcha: '',
  userType: 0  // 默认选择个人用户
})

const captchaUrl = ref('/api/user/captcha')

const refreshCaptcha = () => {
  captchaUrl.value = `/api/user/captcha?t=${new Date().getTime()}`
}

onMounted(() => {
  refreshCaptcha()
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (form.confirmPassword !== '') {
      formRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await register({
          username: form.username,
          password: form.password,
          confirmPassword: form.confirmPassword,
          phone: form.phone,
          captcha: form.captcha,
          userType: form.userType
        })
        ElMessage.success('注册成功')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
        refreshCaptcha()
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f5f5;
}

.register-card {
  width: 500px;
}

.register-card :deep(.el-card__header) {
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