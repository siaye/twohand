<template>
  <div class="home-index">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索商品"
        class="search-input"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 商品列表 -->
    <div class="product-list" v-loading="loading">
      <el-empty v-if="products.length === 0" description="暂无商品" />
      <el-row :gutter="20" v-else>
        <el-col :span="6" v-for="product in products" :key="product.id">
          <el-card class="product-card" @click="goToDetail(product.id)">
            <el-image
              :src="getImageUrl(product.images ? product.images.split(',')[0] : null)"
              :data-original-src="product.images ? product.images.split(',')[0] : null"
              class="product-image"
              fit="cover"
              @error="handleImageError"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <p class="product-price">¥{{ product.price }}</p>
              <div class="product-meta">
                <span class="seller">{{ product.sellerName }}</span>
                <span class="time">{{ formatTime(product.createdAt) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 每日优惠券 -->
    <daily-coupons v-if="isLoggedIn" />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { Search, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProducts } from '@/api/product'
import DailyCoupons from '@/components/home/DailyCoupons.vue'

const router = useRouter()
const store = useStore()
const loading = ref(false)
const searchQuery = ref('')
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const pagination = ref({
  total: 0,
  pageSize: 12,
  currentPage: 1,
  totalPages: 0
})
const isLoggedIn = computed(() => store.getters.isLoggedIn)

// 获取商品列表
const fetchProducts = async () => {
  try {
    loading.value = true
    const res = await getProducts({
      current: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value
    })
    if (res.code === 200) {
      products.value = res.data.records
      total.value = res.data.total
      pagination.value = {
        total: res.data.total,
        pageSize: res.data.size,
        currentPage: res.data.current,
        totalPages: res.data.pages
      }
    } else {
      ElMessage.error(res.msg || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败：', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  fetchProducts()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 重置搜索
const resetSearch = () => {
  searchQuery.value = ''
  currentPage.value = 1
  fetchProducts()
}

// 跳转到商品详情页
const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  return date.toLocaleDateString()
}

// 处理图片URL
const getImageUrl = (url) => {
  console.log('处理图片URL:', url)
  if (!url) {
    console.log('图片URL为空')
    return ''
  }
  
  // 如果已经是完整URL，直接返回
  if (url.startsWith('http')) {
    console.log('图片URL已经是完整地址:', url)
    return url
  }
  
  // 处理相对路径
  let processedUrl = url
  // 如果URL不是以/api开头，添加/api前缀
  if (!processedUrl.startsWith('/api')) {
    processedUrl = '/api' + (processedUrl.startsWith('/') ? processedUrl : '/' + processedUrl)
  }
  
  const fullUrl = `http://localhost:8080${processedUrl}`
  console.log('处理后的完整图片URL:', fullUrl)
  return fullUrl
}

// 处理图片加载错误
const handleImageError = (e) => {
  console.error('图片加载失败:', {
    originalSrc: e.target.dataset.originalSrc,
    currentSrc: e.target.src,
    error: e
  })
}

// 初始化
onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.home-index {
  padding: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-input {
  width: 500px;
  max-width: 100%;
}

.product-list {
  margin-bottom: 20px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  padding: 10px;
}

.product-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin: 10px 0;
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}
</style> 