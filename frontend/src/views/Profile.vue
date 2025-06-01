<template>
  <el-container class="profile-container">
    <el-aside width="200px" class="profile-sidebar">
      <div class="back-home">
        <el-button type="text" @click="handleBackToHome">
          <el-icon><HomeFilled /></el-icon>
          <span>返回主页</span>
        </el-button>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="profile-menu"
        router
      >
        <el-menu-item index="/profile/info">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="/profile/wallet">
          <el-icon><Wallet /></el-icon>
          <span>我的钱包</span>
        </el-menu-item>
        <el-menu-item index="/profile/password">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
        </el-menu-item>
        <el-menu-item index="/profile/coupons">
          <el-icon><Ticket /></el-icon>
          <span>我的优惠券</span>
        </el-menu-item>
        <!-- 其他个人中心相关的菜单项 -->
      </el-menu>
    </el-aside>
    <el-main class="profile-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Wallet, HomeFilled, Lock, Ticket } from '@element-plus/icons-vue'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'
import { formatTime } from '@/utils/format'
import { useRoute } from 'vue-router'

const store = useStore()
const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

// 上传相关配置
const uploadUrl = 'http://localhost:8080/api/user/upload'
const uploadHeaders = {
  Authorization: store.getters.token
}

// 表单数据
const form = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  userType: 1,
  createTime: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200 && res.data) {
      Object.assign(form, res.data)
      // 同时更新 store 中的用户信息
      store.commit('SET_USER', res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 监听路由变化，进入个人中心页面时刷新用户信息
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/profile') {
      fetchUserInfo()
    }
  }
)

// 组件挂载时获取用户信息
onMounted(() => {
  fetchUserInfo()
})

// 处理头像上传
const handleAvatarUpload = async (options) => {
  try {
    const res = await uploadAvatar(options.file)
    if (res.code === 200) {
      options.onSuccess(res)
    } else {
      options.onError(new Error(res.message || '上传失败'))
    }
  } catch (error) {
    options.onError(error)
  }
}

// 处理头像上传成功
const handleAvatarSuccess = (res) => {
  if (res.code === 200 && res.data) {
    form.avatar = res.data
    ElMessage.success('头像上传成功')
    // 更新 store 中的用户信息
    store.commit('UPDATE_USER_AVATAR', res.data)
    // 刷新用户信息
    fetchUserInfo()
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

// 处理头像上传失败
const handleAvatarError = (error) => {
  console.error('头像上传失败:', error)
  ElMessage.error('头像上传失败')
}

// 上传前处理
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt20M = file.size / 1024 / 1024 < 20

  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return false
  }
  if (!isLt20M) {
    ElMessage.error('上传头像图片大小不能超过 20MB!')
    return false
  }
  return true
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 1. 表单验证
    await formRef.value.validate()
    loading.value = true

    // 2. 获取当前用户信息
    const userInfo = store.getters.user
    if (!userInfo || !userInfo.id) {
      throw new Error('用户信息不完整，请重新登录')
    }

    // 3. 准备更新数据
    const updateData = {
      id: userInfo.id,
      realName: form.realName?.trim() || '',
      phone: form.phone?.trim() || '',
      email: form.email?.trim() || '',
      avatar: form.avatar || ''
    }

    // 4. 调用更新接口
    const res = await updateUserInfo(updateData)

    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      // 更新 store 中的用户信息
      store.commit('UPDATE_USER_INFO', {
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        avatar: form.avatar
      })
      // 刷新用户信息
      await fetchUserInfo()
    } else {
      throw new Error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error(error.message || '更新个人信息失败')
  } finally {
    loading.value = false
  }
}

// 返回首页
const handleBackToHome = () => {
  router.push('/')
}

// 根据当前路由设置默认激活的菜单项
const activeMenu = computed(() => route.path)

// 确保在路由变化时更新activeMenu（尽管router模式下el-menu会自动处理，但显式设置更保险）
watch(
  () => route.path,
  (newPath) => {
    // 可能会有子路径，匹配父路径或完整路径
    if (newPath.startsWith('/profile/info')) {
      activeMenu.value = '/profile/info'
    } else if (newPath.startsWith('/profile/wallet')) {
      activeMenu.value = '/profile/wallet'
    } else if (newPath.startsWith('/profile/password')) {
      activeMenu.value = '/profile/password'
    } else if (newPath.startsWith('/profile/coupons')) {
      activeMenu.value = '/profile/coupons'
    } else {
       activeMenu.value = newPath; // 匹配其他可能的子路由
    }
  },
  { immediate: true } // 立即执行一次，确保初始状态正确
)
</script>

<style scoped>
.profile-container {
  min-height: calc(100vh - 60px); /* 假设头部高度为60px */
}

.profile-sidebar {
  background-color: #fff;
  padding-top: 20px;
  border-right: 1px solid #e6e6e6;
}

.profile-menu {
  border-right: none;
}

.profile-main {
  background-color: #f5f7fa;
  padding: 20px;
}

.back-home {
  padding: 0 20px 20px;
  border-bottom: 1px solid #e6e6e6;
}

.back-home .el-button {
  width: 100%;
  text-align: left;
}

.back-home .el-icon {
  vertical-align: middle;
  margin-right: 5px;
}

.back-home span {
  vertical-align: middle;
}
</style> 