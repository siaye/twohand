<template>
  <el-card class="profile-card">
    <template #header>
      <div class="card-header">
        <h2>个人信息</h2>
      </div>
    </template>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="profile-form"
    >
      <!-- 头像 -->
      <el-form-item label="头像">
        <div class="avatar-container">
          <el-avatar
            :size="100"
            :src="form.avatar ? `http://localhost:8080/api${form.avatar}` : ''"
            :icon="User"
            class="avatar"
          />
          <el-upload
            class="avatar-uploader"
            action="http://localhost:8080/api/user/upload"
            :headers="uploadHeaders"
            :data="{ type: 'avatar' }"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :on-success="handleAvatarSuccess"
            :on-error="handleAvatarError"
          >
            <el-button type="primary" size="small">更换头像</el-button>
          </el-upload>
        </div>
      </el-form-item>

      <!-- 用户名 -->
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" disabled />
      </el-form-item>

      <!-- 真实姓名 -->
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="请输入真实姓名" />
      </el-form-item>

      <!-- 手机号 -->
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" placeholder="请输入手机号" />
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>

      <!-- 用户类型 -->
      <el-form-item label="用户类型">
        <el-tag :type="form.userType === 0 ? 'success' : 'warning'">
          {{ form.userType === 0 ? '普通用户' : '商家用户' }}
        </el-tag>
      </el-form-item>

      <!-- 注册时间 -->
      <el-form-item label="注册时间">
        <span>{{ form.createTime ? formatTime(form.createTime) : '暂无' }}</span>
      </el-form-item>

      <!-- 提交按钮 -->
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          保存修改
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'
import { formatTime } from '@/utils/format'

const store = useStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  userType: 0,
  createTime: '',
  avatar: '',
})

// 表单验证规则
const rules = reactive({
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
})

// 文件上传头部
const uploadHeaders = reactive({
  Authorization: store.state.token
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo(localStorage.getItem('token'))
    if (res.code === 200) {
      Object.assign(form, res.data)
      // 确保上传头部有最新的token
      uploadHeaders.Authorization = store.state.token
    } else {
      ElMessage.error(res.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息异常:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 处理头像上传前
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('头像图片只能是 JPG 或 PNG 格式!')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  loading.value = true // 上传时显示加载状态
  return true
}

// 处理头像上传成功
const handleAvatarSuccess = (res, file) => {
  loading.value = false // 隐藏加载状态
  if (res.code === 200) {
    form.avatar = res.data // 更新头像URL
    ElMessage.success('头像上传成功')
    // 考虑更新store中的用户信息，如果需要实时反映头像变化
    // store.dispatch('fetchUserInfo') 
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

// 处理头像上传失败
const handleAvatarError = (error) => {
  loading.value = false // 隐藏加载状态
  console.error('头像上传异常:', error)
  ElMessage.error('头像上传失败')
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 过滤掉不需要提交的字段，或者使用DTO
        const updateData = { ...form }
        // 移除不需要的字段，例如 username, userType, createTime 等
        delete updateData.username;
        delete updateData.userType;
        delete updateData.createTime;
        // 确保ID存在，后端需要ID来更新
        updateData.id = store.state.user.id; // 从store获取用户ID
        
        const res = await updateUserInfo(updateData, localStorage.getItem('token'))
        if (res.code === 200) {
          ElMessage.success('用户信息更新成功')
          // 考虑更新store中的用户信息，如果需要实时反映变化
          // store.dispatch('fetchUserInfo')
        } else {
          ElMessage.error(res.message || '用户信息更新失败')
        }
      } catch (error) {
        console.error('用户信息更新异常:', error)
        ElMessage.error('用户信息更新失败')
      } finally {
        loading.value = false
      }
    } else {
      console.log('表单验证失败')
    }
  })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.profile-form {
  max-width: 600px;
  margin: 0 auto;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar {
  background-color: #f0f2f5;
}

.avatar-uploader {
  display: flex;
  align-items: center;
}
</style> 