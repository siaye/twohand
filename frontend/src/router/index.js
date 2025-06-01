import { createRouter, createWebHistory } from 'vue-router'
import store from '@/store'
import ProductDetail from '@/views/ProductDetail.vue'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { 
      title: '首页',
      requiresAuth: false 
    },
    children: [
      {
        path: '',
        name: 'HomeIndex',
        component: () => import('@/views/HomeIndex.vue'),
        meta: { 
          title: '首页',
          requiresAuth: false 
        }
      },
      {
        path: 'publish',
        name: 'Publish',
        component: () => import('@/views/PublishProduct.vue'),
        meta: { 
          title: '发布商品',
          requiresAuth: true 
        }
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: () => import('@/views/MyProducts.vue'),
        meta: { 
          title: '我的商品',
          requiresAuth: true 
        }
      },
      {
        path: 'my-orders',
        name: 'MyOrders',
        component: () => import('@/views/MyOrders.vue'),
        meta: { 
          title: '我的订单',
          requiresAuth: true 
        }
      },
      {
        path: 'my-reviews',
        name: 'MyReviews',
        component: () => import('@/views/MyReviews.vue'),
        meta: { 
          title: '我的评价',
          requiresAuth: true 
        }
      },
      {
        path: 'seller-orders',
        name: 'SellerOrders',
        component: () => import('@/views/SellerOrders.vue'),
        meta: { 
          title: '卖家订单',
          requiresAuth: true,
          requiresSeller: true
        }
      }
    ]
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { 
      title: '注册',
      requiresAuth: false 
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { 
      title: '登录',
      requiresAuth: false 
    }
  },
  {
    path: '/merchant-status',
    name: 'MerchantStatus',
    component: () => import('@/views/MerchantStatus.vue'),
    meta: { 
      title: '商家状态',
      requiresAuth: true 
    }
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { 
      requiresAuth: true, 
      requiresAdmin: true 
    },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { 
          title: '控制台',
          requiresAdmin: true 
        }
      },
      {
        path: 'audit',
        name: 'AuditManagement',
        component: () => import('@/views/admin/AuditManagement.vue'),
        meta: { 
          title: '商品审核',
          requiresAdmin: true 
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { 
          title: '用户管理',
          requiresAdmin: true 
        }
      },
      {
        path: 'merchants',
        name: 'MerchantManagement',
        component: () => import('@/views/admin/MerchantManagement.vue'),
        meta: { 
          title: '商家管理',
          requiresAdmin: true 
        }
      },
      {
        path: 'orders',
        name: 'OrderManagement',
        component: () => import('@/views/admin/OrderManagement.vue'),
        meta: { 
          title: '订单管理',
          requiresAdmin: true 
        }
      }
    ]
  },
  {
    path: '/profile',
    component: () => import('@/views/Profile.vue'),
    meta: {
      title: '个人中心',
      requiresAuth: true
    },
    children: [
      {
        path: '',
        redirect: '/profile/info'
      },
      {
        path: 'info',
        name: 'PersonalInfo',
        component: () => import('@/views/PersonalInfo.vue'),
        meta: {
          title: '个人信息',
          requiresAuth: true
        }
      },
      {
        path: 'wallet',
        name: 'WalletInfo',
        component: () => import('@/views/WalletInfo.vue'),
        meta: {
          title: '我的钱包',
          requiresAuth: true
        }
      },
      {
        path: 'password',
        name: 'ChangePassword',
        component: () => import('@/views/ChangePassword.vue'),
        meta: {
          title: '修改密码',
          requiresAuth: true
        }
      },
      {
        path: 'coupons',
        name: 'MyCoupons',
        component: () => import('@/views/MyCoupons.vue'),
        meta: {
          title: '我的优惠券',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/reviews',
    name: 'MyReviews',
    component: () => import('@/views/MyReviews.vue'),
    meta: {
      title: '我的评价',
      requiresAuth: true
    }
  },
  {
    path: '/complete-profile',
    name: 'CompleteProfile',
    component: () => import('@/views/CompleteProfile.vue'),
    meta: {
      requiresAuth: false,
      title: '完善信息'
    }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: {
      title: '商品详情'
    }
  },
  {
    path: '/product-audit',
    name: 'ProductAudit',
    component: () => import('@/views/ProductAudit.vue'),
    meta: {
      title: '商品审核',
      requiresAuth: true,
      roles: ['admin']  // 只有管理员可以访问
    }
  },
  {
    path: '/cart',
    name: 'ShoppingCart',
    component: () => import('@/views/ShoppingCart.vue'),
    meta: {
      requiresAuth: true,
      title: '购物车'
    }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/OrderList.vue'),
    meta: {
      requiresAuth: true,
      title: '我的订单'
    }
  },
  {
    path: '/order',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { 
      requiresAuth: true 
    },
    children: [
      {
        path: 'confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/OrderConfirm.vue'),
        meta: { 
          title: '确认订单',
          requiresAuth: true 
        }
      },
      {
        path: 'pay/:id',
        name: 'OrderPay',
        component: () => import('@/views/OrderPay.vue'),
        meta: { 
          title: '订单支付',
          requiresAuth: true 
        }
      },
      {
        path: ':id',
        name: 'OrderDetail',
        component: () => import('@/views/OrderDetail.vue'),
        meta: { 
          title: '订单详情',
          requiresAuth: true 
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  console.log('【Router】路由跳转详情:', {
    to: {
      path: to.path,
      name: to.name,
      meta: to.meta,
      query: to.query
    },
    from: {
      path: from.path,
      name: from.name,
      meta: from.meta
    }
  });
  
  // 获取登录状态
  const isLoggedIn = store.getters.isLoggedIn;
  const user = store.getters.user;
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 二手交易平台` : '二手交易平台';
  
  // 如果是首页或登录/注册页面，直接允许访问
  if (to.path === '/' || to.path === '/login' || to.path === '/register') {
    console.log('【Router】访问公开页面，允许访问');
    next();
    return;
  }
  
  // 需要登录的页面
  if (to.meta.requiresAuth === true) {
    console.log('【Router】检查需要登录的页面:', {
      isLoggedIn,
      user,
      path: to.path
    });
    
    if (!isLoggedIn) {
      console.log('【Router】未登录，重定向到首页');
      next('/');
      return;
    }
    
    // 检查管理员权限
    if (to.meta.requiresAdmin === true && !store.getters.isAdmin) {
      console.log('【Router】非管理员访问管理页面，重定向到首页');
      next('/');
      return;
    }
    
    // 检查商家权限
    if (to.meta.requiresSeller === true && user?.userType !== 1) {
      console.log('【Router】非商家访问商家页面，重定向到首页');
      next('/');
      return;
    }
  }
  
  // 已登录用户访问登录/注册页面，重定向到首页
  if (isLoggedIn && (to.path === '/login' || to.path === '/register')) {
    console.log('【Router】已登录用户访问登录/注册页面，重定向到首页');
    next('/');
    return;
  }
  
  next();
})

export default router 