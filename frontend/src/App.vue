<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <keep-alive>
        <component :is="Component" />
      </keep-alive>
    </router-view>
    <el-dropdown v-if="isLoggedIn" trigger="click" @command="handleCommand">
      <span class="user-dropdown">
        <el-avatar :size="32" :src="userInfo?.avatar || defaultAvatar" />
        <span class="username">{{ userInfo?.username }}</span>
      </span>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="profile">个人中心</el-dropdown-item>
          <el-dropdown-item command="my-products">我的商品</el-dropdown-item>
          <el-dropdown-item command="cart">购物车</el-dropdown-item>
          <el-dropdown-item command="orders">我的订单</el-dropdown-item>
          <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
          <el-dropdown-item v-if="isAdmin" command="admin">管理后台</el-dropdown-item>
          <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import store from '@/store'
import { useRouter } from 'vue-router'

const router = useRouter()

// 检查登录状态
onMounted(() => {
  // 从 localStorage 恢复登录状态
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')
  
  if (token && userInfo) {
    try {
      // 验证 token 是否过期
      const tokenWithoutBearer = token.replace(/^Bearer\s+/i, '')
      const base64Url = tokenWithoutBearer.split('.')[1]
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
      }).join(''))

      const { exp } = JSON.parse(jsonPayload)
      const currentTime = Math.floor(Date.now() / 1000)
      
      if (exp > currentTime) {
        store.commit('SET_TOKEN', token)
        store.commit('SET_USER', JSON.parse(userInfo))
        console.log('【App】项目启动，已恢复登录状态')
      } else {
        console.log('【App】项目启动，token已过期')
        store.commit('CLEAR_USER')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    } catch (error) {
      console.error('【App】恢复登录状态失败:', error)
      store.commit('CLEAR_USER')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  } else {
    console.log('【App】项目启动，未发现登录状态')
    store.commit('CLEAR_USER')
  }
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'my-products':
      router.push('/my-products')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'orders':
      router.push('/my-orders')
      break
    case 'wallet':
      router.push('/profile/wallet')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}
</script>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
}

#app {
  width: 100%;
  height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style> 