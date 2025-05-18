<template>
  <div class="home-container">
    <!-- 顶部区域 -->
    <header class="header">
      <div class="site-title">二手交易市场</div>
      <nav class="user-area">
        <template v-if="isLoggedIn">
          <span>你好，{{ username }}</span>
          <el-button type="text" @click="handleLogout">退出登录</el-button>
        </template>
        <template v-else>
          <el-button type="text" @click="goToLogin">登录</el-button>
          <el-button type="text" @click="goToRegister">注册</el-button>
        </template>
      </nav>
    </header>

    <!-- 商品展示区域 -->
    <main class="main-content">
      <h2>最新商品</h2>
      <!-- 这里将展示商品列表 -->
      <div class="product-list">
        <!-- 示例商品卡片 -->
        <el-card class="product-card">
          <img src="https://via.placeholder.com/150" class="product-image" alt="示例商品">
          <div style="padding: 14px;">
            <span>示例商品名称</span>
            <div class="bottom">
              <time class="time">2023-10-27</time>
              <el-button type="text" class="button">查看详情</el-button>
            </div>
          </div>
        </el-card>
        <!-- 更多商品卡片... -->
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useStore } from 'vuex'; // 假设使用 Vuex 进行状态管理

const router = useRouter();
const store = useStore(); // 假设使用 Vuex

// 判断是否登录的计算属性 (需要根据实际的 Vuex store 状态进行调整)
const isLoggedIn = computed(() => store.getters.isLoggedIn); // 假设 Vuex store 有 isLoggedIn getter
const username = computed(() => store.getters.username); // 假设 Vuex store 有 username getter

// 跳转到登录页
const goToLogin = () => {
  router.push('/login');
};

// 跳转到注册页
const goToRegister = () => {
  router.push('/register');
};

// 处理退出登录 (需要根据实际的退出登录逻辑进行调整)
const handleLogout = () => {
  // 执行退出登录操作，例如清除 token，更新 Vuex store 状态
  console.log('执行退出登录');
  // store.dispatch('logout'); // 假设 Vuex store 有 logout action
  // router.push('/login'); // 退出登录后跳转到登录页
};

// 页面加载时的一些操作，例如获取商品列表
onMounted(() => {
  console.log('Home 页面加载');
  // 获取商品列表的 API 调用...
});
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.site-title {
  font-size: 24px;
  font-weight: bold;
}

.user-area button {
  margin-left: 10px;
}

.main-content {
  flex-grow: 1;
  padding: 20px;
  background-color: #f5f5f5;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.product-card {
  width: 100%;
}

.product-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.bottom {
  margin-top: 10px;
  line-height: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time {
  font-size: 13px;
  color: #999;
}

.button {
  padding: 0;
  min-height: auto;
}
</style> 