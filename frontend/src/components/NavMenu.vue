<template>
  <el-menu
    :default-active="activeIndex"
    class="nav-menu"
    mode="horizontal"
    router
  >
    <el-menu-item index="/">首页</el-menu-item>
    
    <!-- 用户菜单 -->
    <el-sub-menu index="user" v-if="isLoggedIn">
      <template #title>
        <el-avatar :size="32" :src="userAvatar">{{ userInitials }}</el-avatar>
        <span class="username">{{ username }}</span>
      </template>
      
      <el-menu-item index="/profile/info">
        <el-icon><User /></el-icon>个人中心
      </el-menu-item>
      
      <el-menu-item index="/my-orders">
        <el-icon><List /></el-icon>我的订单
      </el-menu-item>
      
      <el-menu-item index="/my-products" v-if="isSeller">
        <el-icon><Goods /></el-icon>我的商品
      </el-menu-item>
      
      <el-menu-item index="/wallet">
        <el-icon><Wallet /></el-icon>我的钱包
      </el-menu-item>
      
      <el-menu-item index="/reviews">
        <el-icon><ChatDotRound /></el-icon>我的评价
      </el-menu-item>
      
      <el-menu-item @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>退出登录
      </el-menu-item>
    </el-sub-menu>

    <!-- 未登录时显示登录注册按钮 -->
    <div class="login-buttons" v-else>
      <el-button type="primary" @click="$router.push('/login')">登录</el-button>
      <el-button @click="$router.push('/register')">注册</el-button>
    </div>
  </el-menu>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  List,
  Goods,
  Wallet,
  SwitchButton,
  ChatDotRound
} from '@element-plus/icons-vue'

const store = useStore()
const router = useRouter()
const activeIndex = ref('/')

// 计算属性
const isLoggedIn = computed(() => store.getters.isLoggedIn)
const username = computed(() => store.state.user?.username || '')
const isSeller = computed(() => store.state.user?.role === 1)
const userAvatar = computed(() => store.state.user?.avatar || '')
const userInitials = computed(() => {
  const name = username.value
  return name ? name.charAt(0).toUpperCase() : ''
})

// 退出登录
const handleLogout = async () => {
  try {
    await store.dispatch('logout')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    console.error('退出失败:', error)
    ElMessage.error('退出失败')
  }
}
</script>

<style scoped>
.nav-menu {
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.username {
  margin-left: 8px;
}

.login-buttons {
  display: flex;
  gap: 10px;
}

:deep(.el-menu-item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-sub-menu__title) {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style> 