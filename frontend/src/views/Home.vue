<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span class="title">二手交易平台</span>
      </div>
      
      <div class="header-right">
        <template v-if="isLoggedIn">
          <el-button type="primary" @click="handlePublish">发布商品</el-button>
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar 
                :size="32" 
                :src="getAvatarUrl(userInfo?.avatar)" 
                :icon="User"
                class="user-avatar"
              />
              <span class="username">{{ userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleMyProducts">我的商品</el-dropdown-item>
                <el-dropdown-item @click="handleCart">购物车</el-dropdown-item>
                <el-dropdown-item @click="handleMyOrders">我的订单</el-dropdown-item>
                <el-dropdown-item @click="handleMyReviews">我的评价</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" divided @click="handleAdmin">后台管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="handleRegister">注册</el-button>
        </template>
      </div>
    </el-header>

    <!-- 主要内容区 -->
    <el-main class="main">
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Shop, User, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'

const router = useRouter()
const store = useStore()

// 获取用户信息
const userInfo = computed(() => store.getters.user)
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const isAdmin = computed(() => store.getters.isAdmin)

// 处理头像URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  // 如果URL不是以/api开头，添加/api前缀
  if (!avatar.startsWith('/api')) {
    avatar = '/api' + (avatar.startsWith('/') ? avatar : '/' + avatar)
  }
  return `http://localhost:8080${avatar}`
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 清除所有登录状态
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    store.commit('CLEAR_USER')
    
    // 跳转到首页
    router.replace('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

// 处理发布商品
const handlePublish = () => {
  router.push('/publish')
}

// 处理查看我的商品
const handleMyProducts = () => {
  router.push('/my-products')
}

// 处理查看我的订单
const handleMyOrders = () => {
  router.push('/my-orders')
}

// 处理登录
const handleLogin = () => {
  router.replace('/login')
}

// 处理注册
const handleRegister = () => {
  router.replace('/register')
}

// 处理个人中心
const handleProfile = () => {
  router.push('/profile/info')
}

// 处理后台管理
const handleAdmin = () => {
  if (!isAdmin.value) {
    ElMessageBox.alert('您没有访问后台管理的权限', '提示', {
      confirmButtonText: '确定',
      type: 'warning'
    })
    return
  }
  router.push('/admin/dashboard')
}

// 处理购物车
const handleCart = () => {
  router.push('/cart')
}

// 处理查看我的评价
const handleMyReviews = () => {
  router.push('/reviews')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #409EFF;
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  gap: 8px;
}

.user-avatar {
  background-color: #f0f2f5;
}

.username {
  font-size: 14px;
  margin: 0 4px;
}

.main {
  flex: 1;
  background-color: #f5f7fa;
  padding: 20px;
}
</style> 