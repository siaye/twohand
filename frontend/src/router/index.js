import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/merchant-status',
    name: 'MerchantStatus',
    component: () => import('@/views/MerchantStatus.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/merchant-audit',
    name: 'MerchantAudit',
    component: () => import('@/views/admin/MerchantAudit.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  }
  // 其它路由可在此添加
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 