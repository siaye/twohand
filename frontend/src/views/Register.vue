<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册</h2>
        </div>
      </template>
      
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="form.phone"
            placeholder="请输入手机号"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item label="用户类型" prop="userType">
          <el-radio-group v-model="form.userType">
            <el-radio :label="0">个人用户</el-radio>
            <el-radio :label="1">商家用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-container">
            <el-input 
              v-model="form.captcha"
              placeholder="请输入验证码"
              :prefix-icon="Key"
              style="width: 200px"
            />
            <img 
              :src="captchaUrl" 
              @click="refreshCaptcha" 
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
            注册
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <router-link to="/login">已有账号？立即登录</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Key } from '@element-plus/icons-vue'
import { register, getCaptcha } from '@/api/auth'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()
const formRef = ref(null)
const loading = ref(false)
const captchaUrl = ref('')

// 表单数据
const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  userType: 0,  // 默认选择个人用户
  captcha: ''
})

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (form.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
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

// 刷新验证码
const refreshCaptcha = () => {
  // 添加时间戳防止缓存
  const timestamp = new Date().getTime()
  captchaUrl.value = `http://localhost:8080/api/user/captcha?t=${timestamp}`
}

// 处理注册
const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    console.log('【Register】开始注册:', form)
    const res = await register(form)
    console.log('【Register】注册成功:', res)
    
    if (res.code === 200 && res.data) {
      // 注册成功后，保存用户ID和类型到sessionStorage
      const userId = res.data.userId
      const userType = res.data.userType
      
      console.log('【Register】保存临时用户信息:', { userId, userType })
      sessionStorage.setItem('tempUserId', userId)
      sessionStorage.setItem('tempUserType', userType)
      
      ElMessage.success('注册成功，请完善个人信息')
      
      // 跳转到完善信息页面
      router.replace('/complete-profile')
    } else {
      throw new Error(res.message || '注册失败，请重试')
    }
  } catch (error) {
    console.error('【Register】注册失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || '注册失败，请重试')
    refreshCaptcha()
    form.captcha = ''
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
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.register-card {
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