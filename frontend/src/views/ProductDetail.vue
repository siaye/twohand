<template>
  <div class="product-detail">
    <el-card class="detail-card" v-if="product">
      <!-- 商品基本信息 -->
      <div class="product-info">
        <!-- 商品图片展示 -->
        <div class="product-gallery">
          <el-carousel :interval="4000" type="card" height="400px">
            <el-carousel-item v-for="(image, index) in product.imageList" :key="index">
              <img :src="image" :alt="product.title" class="carousel-image" />
            </el-carousel-item>
          </el-carousel>
        </div>

        <!-- 商品信息 -->
        <div class="product-content">
          <h1 class="product-title">{{ product.title }}</h1>
          
          <!-- 价格信息 -->
          <div class="price-info">
            <div class="current-price">
              <span class="label">当前价格：</span>
              <span class="price">¥{{ product.price }}</span>
            </div>
            <div class="original-price" v-if="product.originalPrice">
              <span class="label">原价：</span>
              <span class="price">¥{{ product.originalPrice }}</span>
              <span class="discount">({{ getDiscountRate }}折)</span>
            </div>
          </div>

          <!-- 商品标签 -->
          <div class="product-tags" v-if="product.tags">
            <el-tag
              v-for="tag in product.tags.split(',')"
              :key="tag"
              class="tag"
              size="small"
            >
              {{ tag }}
            </el-tag>
          </div>

          <!-- 商品属性 -->
          <div class="product-attributes">
            <div class="attribute-item">
              <span class="label">品牌：</span>
              <span class="value">{{ product.brand || '暂无' }}</span>
            </div>
            <div class="attribute-item">
              <span class="label">分类：</span>
              <span class="value">{{ product.categoryName || '暂无' }}</span>
            </div>
            <div class="attribute-item">
              <span class="label">尺寸：</span>
              <span class="value">{{ product.size || '暂无' }}</span>
            </div>
            <div class="attribute-item">
              <span class="label">颜色：</span>
              <span class="value">{{ product.color || '暂无' }}</span>
            </div>
            <div class="attribute-item">
              <span class="label">成色：</span>
              <span class="value">{{ product.productCondition || '暂无' }}</span>
            </div>
            <div class="attribute-item">
              <span class="label">位置：</span>
              <span class="value">{{ product.location || '暂无' }}</span>
            </div>
            <div class="attribute-item">
              <span class="label">库存：</span>
              <span class="value">{{ product.stock || 0 }}件</span>
            </div>
            <div class="attribute-item">
              <span class="label">销量：</span>
              <span class="value">{{ product.salesCount || 0 }}件</span>
            </div>
            <div class="attribute-item">
              <span class="label">议价：</span>
              <span class="value">
                <el-tag :type="product.isNegotiable ? 'success' : 'info'" size="small">
                  {{ product.isNegotiable ? '允许议价' : '不允许议价' }}
                </el-tag>
              </span>
            </div>
          </div>

          <!-- 购买操作 -->
          <div class="purchase-actions">
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="Math.max(1, product.stock)"
              :disabled="product.stock <= 0"
              size="large"
            />
            <el-button
              type="primary"
              size="large"
              :disabled="product.stock <= 0"
              @click="handlePurchase"
            >
              {{ product.stock > 0 ? '立即购买' : '暂时缺货' }}
            </el-button>
            <el-button
              type="success"
              size="large"
              :disabled="product.stock <= 0"
              @click="handleAddToCart"
            >
              加入购物车
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="product-description">
        <h2>商品描述</h2>
        <div class="description-content" v-html="product.description"></div>
      </div>

      <!-- 卖家信息 -->
      <div class="seller-info">
        <h2>卖家信息</h2>
        <div class="seller-content">
          <el-avatar :size="64" :src="getAvatarUrl(product.sellerAvatar)">
            {{ product.sellerName?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="seller-details">
            <h3>{{ product.sellerName }}</h3>
            <div class="seller-stats">
              <span>商品数：{{ sellerStats.productCount }}</span>
              <span>好评率：{{ sellerStats.positiveRate }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品评价部分 -->
      <el-card class="review-section">
        <template #header>
          <div class="review-header">
            <h3>商品评价</h3>
            <div class="review-stats">
              <span class="total-reviews">共 {{ totalReviews }} 条评价</span>
              <div class="average-rating">
                <span>平均评分：</span>
                <el-rate
                  v-model="averageRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
              </div>
            </div>
          </div>
        </template>

        <div v-loading="reviewsLoading">
          <el-empty v-if="reviews.length === 0" description="暂无评价" />
          <div v-else class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-user">
                <el-avatar :size="40" :src="getAvatarUrl(review.userAvatar)">
                  {{ review.userName?.charAt(0)?.toUpperCase() }}
                </el-avatar>
                <div class="user-info">
                  <span class="username">{{ review.userName }}</span>
                  <span class="review-time">{{ formatTime(review.createdAt) }}</span>
                </div>
              </div>
              <div class="review-content">
                <el-rate v-model="review.rating" disabled />
                <p class="review-text">{{ review.content }}</p>
                <div v-if="review.images" class="review-images">
                  <el-image
                    v-for="(image, index) in review.images.split(',')"
                    :key="index"
                    :src="image"
                    :preview-src-list="review.images.split(',')"
                    fit="cover"
                    class="review-image"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </el-card>
    <el-card v-else class="loading-card">
      <el-skeleton :rows="10" animated />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onActivated } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct } from '@/api/product'
import { getProductReviews } from '@/api/order'
import { addToCart } from '@/api/cart'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const product = ref(null)
const quantity = ref(1)
const reviews = ref([])
const reviewsLoading = ref(false)
const totalReviews = ref(0)
const averageRating = ref(0)
const sellerStats = ref({
  productCount: 0,
  positiveRate: 0
})

// 获取折扣率
const getDiscountRate = computed(() => {
  if (!product.value?.originalPrice || !product.value?.price) return 0
  return ((product.value.price / product.value.originalPrice) * 10).toFixed(1)
})

// 获取商品详情
const fetchProductDetail = async () => {
  try {
    const productId = route.params.id
    const res = await getProduct(productId)
    if (res.code === 200) {
      product.value = res.data
      // 处理图片URL
      if (product.value.imageList) {
        product.value.imageList = product.value.imageList.map(img => {
          if (!img) return ''
          if (img.startsWith('http')) return img
          return `http://localhost:8080${img}`
        })
      }
    }
  } catch (error) {
    console.error('获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  }
}

// 获取商品评价
const fetchReviews = async () => {
  try {
    reviewsLoading.value = true
    const productId = route.params.id
    const res = await getProductReviews(productId)
    if (res.code === 200) {
      reviews.value = res.data
      totalReviews.value = res.data.length
      // 计算平均评分
      if (reviews.value.length > 0) {
        const sum = reviews.value.reduce((acc, review) => acc + review.rating, 0)
        averageRating.value = sum / reviews.value.length
      }
    }
  } catch (error) {
    console.error('获取评价失败:', error)
    ElMessage.error('获取评价失败')
  } finally {
    reviewsLoading.value = false
  }
}

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

// 处理购买
const handlePurchase = () => {
  if (!product.value) {
    ElMessage.error('商品信息不完整')
    return
  }
  
  if (product.value.stock <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  if (quantity.value <= 0) {
    ElMessage.warning('请选择有效的购买数量')
    return
  }
  
  try {
    router.push({
      path: '/order/confirm',
      query: {
        productId: product.value.id,
        quantity: quantity.value
      }
    })
  } catch (error) {
    console.error('跳转订单确认页面失败:', error)
    ElMessage.error('跳转失败，请稍后重试')
  }
}

// 处理加入购物车
const handleAddToCart = async () => {
  if (!product.value) return
  try {
    const res = await addToCart({
      productId: product.value.id,
      quantity: quantity.value
    })
    if (res.code === 200) {
      ElMessage.success('已加入购物车')
    }
  } catch (error) {
    console.error('加入购物车失败:', error)
    ElMessage.error('加入购物车失败')
  }
}

// 监听路由参数变化
watch(
  () => route.params.id,
  (newId, oldId) => {
    if (newId && newId !== oldId) {
      fetchProductDetail()
      fetchReviews()
    }
  }
)

// 组件被激活时刷新数据
onActivated(() => {
  if (route.params.id) {
    fetchProductDetail()
  }
})

// 初始化数据
onMounted(() => {
  if (route.params.id) {
    fetchProductDetail()
    fetchReviews()
  }
})
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.detail-card {
  max-width: 1200px;
  margin: 0 auto;
}

.product-info {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

.product-gallery {
  flex: 1;
  max-width: 600px;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-content {
  flex: 1;
}

.product-title {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
}

.price-info {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.current-price {
  margin-bottom: 10px;
}

.current-price .price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.original-price .price {
  text-decoration: line-through;
  color: #999;
}

.discount {
  color: #f56c6c;
  margin-left: 10px;
}

.product-tags {
  margin-bottom: 20px;
}

.tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.product-attributes {
  margin-bottom: 30px;
}

.attribute-item {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.attribute-item .label {
  width: 80px;
  color: #666;
}

.attribute-item .value {
  color: #333;
}

.purchase-actions {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}

.product-description {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.product-description h2 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.description-content {
  line-height: 1.6;
  color: #666;
}

.seller-info {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.seller-info h2 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.seller-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 4px;
}

.seller-details h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.seller-stats {
  display: flex;
  gap: 20px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.review-section {
  margin-top: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.review-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-reviews {
  color: #909399;
}

.average-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-list {
  margin-top: 20px;
}

.review-item {
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.review-item:last-child {
  border-bottom: none;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 500;
  color: #303133;
}

.review-time {
  font-size: 12px;
  color: #909399;
}

.review-content {
  margin-left: 52px;
}

.review-text {
  margin: 8px 0;
  color: #303133;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}
</style> 