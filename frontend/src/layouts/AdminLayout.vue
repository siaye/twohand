<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><Shop /></el-icon>
        <span v-show="!isCollapse">二手交易平台</span>
      </div>
      
      <el-menu
        :default-active="route.path"
        class="menu"
        :collapse="isCollapse"
        router
      >
        <el-menu-item 
          v-for="item in menuItems" 
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主要内容区 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon 
            class="collapse-btn"
            @click="handleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              {{ userInfo.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Monitor, Shop, User, Document, Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useStore } from 'vuex'

const router = useRouter()
const route = useRoute()
const store = useStore()

// 获取用户信息
const userInfo = computed(() => store.getters.user)

// 菜单折叠状态
const isCollapse = ref(false)

// 处理菜单折叠
const handleCollapse = () => {
  isCollapse.value = !isCollapse.value
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
    store.dispatch('logout')
    
    // 跳转到登录页
    router.replace('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

// 菜单项
const menuItems = [
  {
    title: '控制台',
    icon: Monitor,
    path: '/admin/dashboard'
  },
  {
    title: '商家管理',
    icon: Shop,
    path: '/admin/merchants'
  },
  {
    title: '用户管理',
    icon: User,
    path: '/admin/users'
  },
  {
    title: '订单管理',
    icon: Document,
    path: '/admin/orders'
  }
]
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3649;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
  color: #409EFF;
}

.menu {
  border-right: none;
  background-color: #304156;
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

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
}

.user-info .el-icon {
  margin-left: 4px;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style> 