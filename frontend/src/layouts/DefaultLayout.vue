<template>
  <div class="default-layout">
    <el-container>
      <el-header>
        <div class="header-content">
          <div class="logo" @click="$router.push('/')">
            二手交易平台
          </div>
          <div class="nav-menu">
            <el-menu
              mode="horizontal"
              :router="true"
              :default-active="$route.path">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/publish">发布商品</el-menu-item>
              <el-menu-item index="/my-products">我的商品</el-menu-item>
              <el-menu-item index="/my-orders">我的订单</el-menu-item>
            </el-menu>
          </div>
          <div class="user-menu">
            <el-dropdown v-if="isLoggedIn" @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="userAvatar"/>
                {{ username }}
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="wallet">我的钱包</el-dropdown-item>
                  <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <template v-else>
              <el-button type="text" @click="$router.push('/login')">登录</el-button>
              <el-button type="text" @click="$router.push('/register')">注册</el-button>
            </template>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <router-view></router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const store = useStore()
const router = useRouter()

const isLoggedIn = computed(() => store.getters.isLoggedIn)
const username = computed(() => store.getters.user?.username || '')

// 处理用户头像URL
const userAvatar = computed(() => {
  const avatar = store.getters.user?.avatar
  if (!avatar) return ''
  
  // 如果头像URL不是以http开头，添加服务器地址
  if (!avatar.startsWith('http')) {
    return `http://localhost:8080${avatar}`
  }
  
  return avatar
})

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'wallet':
      router.push('/profile/wallet')
      break
    case 'logout':
      try {
        await store.dispatch('logout')
        ElMessage.success('退出成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('退出失败')
      }
      break
  }
}
</script>

<style scoped>
.default-layout {
  min-height: 100vh;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: fixed;
  width: 100%;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
  cursor: pointer;
}

.nav-menu {
  flex: 1;
  margin: 0 20px;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.el-main {
  padding-top: 80px;
  max-width: 1200px;
  margin: 0 auto;
}
</style> 