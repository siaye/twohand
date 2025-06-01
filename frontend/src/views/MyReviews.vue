<template>
  <div class="my-reviews">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-button @click="handleBack" type="primary" plain>
              <el-icon><ArrowLeft /></el-icon>
              返回首页
            </el-button>
            <h2>我的评价</h2>
          </div>
        </div>
      </template>

      <div class="review-list" v-loading="loading">
        <el-empty v-if="reviews.length === 0" description="暂无评价" />
        <div v-else class="review-items">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="product-info">
              <el-image
                :src="getProductImage(review.productId)"
                fit="cover"
                class="product-image"
                @error="handleImageError"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-details">
                <h3 class="product-title">{{ review.productName }}</h3>
                <p class="review-time">评价时间：{{ formatTime(review.createdAt) }}</p>
              </div>
            </div>
            
            <div class="review-content">
              <div class="review-rating">
                <el-rate v-model="review.rating" disabled />
              </div>
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

            <div class="review-actions">
              <el-button 
                type="danger" 
                size="small"
                @click="handleDeleteReview(review)"
              >
                删除评价
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserReviews, deleteReview } from '@/api/order'
import { getProduct } from '@/api/product'
import { Picture, ArrowLeft } from '@element-plus/icons-vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const reviews = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const productImages = ref({})

// 获取我的评价列表
const fetchReviews = async () => {
  loading.value = true
  try {
    const user = store.getters.user
    console.log('【MyReviews】获取评价列表：', {
      userId: user.id
    })

    const res = await getUserReviews(user.id)
    console.log('【MyReviews】获取成功：', res)

    if (res.code === 200) {
      reviews.value = res.data
      total.value = res.data.length

      // 获取所有商品的图片
      const productIds = [...new Set(reviews.value.map(review => review.productId))]
      await Promise.all(
        productIds.map(async (productId) => {
          try {
            const productRes = await getProduct(productId)
            if (productRes.code === 200) {
              const product = productRes.data
              productImages.value[productId] = product.image || 
                                             product.images?.[0] || 
                                             product.imageList?.[0]
            }
          } catch (error) {
            console.error(`获取商品 ${productId} 详情失败:`, error)
          }
        })
      )
    } else {
      throw new Error(res.message || '获取评价列表失败')
    }
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error(error.message || '获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// 获取商品图片
const getProductImage = (productId) => {
  const image = productImages.value[productId]
  if (!image) return ''
  if (image.startsWith('http')) return image
  return `http://localhost:8080${image}`
}

// 处理图片加载错误
const handleImageError = (e) => {
  console.error('图片加载失败:', e)
}

// 处理删除评价
const handleDeleteReview = async (review) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？此操作不可恢复。', '提示', {
      type: 'warning'
    })
    
    const res = await deleteReview(review.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchReviews()
    } else {
      throw new Error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      ElMessage.error('删除评价失败')
    }
  }
}

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchReviews()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchReviews()
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  return date.toLocaleString()
}

// 处理返回
const handleBack = () => {
  router.push('/')
}

// 处理查看商品详情
const handleViewProduct = (productId) => {
  router.push(`/product/${productId}`)
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.my-reviews {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.review-list {
  margin-bottom: 20px;
}

.review-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.product-info {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.product-details {
  flex: 1;
}

.product-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #303133;
}

.review-time {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.review-content {
  margin-bottom: 20px;
}

.review-rating {
  margin-bottom: 10px;
}

.review-text {
  margin: 10px 0;
  color: #303133;
  line-height: 1.6;
}

.review-images {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.review-actions {
  display: flex;
  justify-content: flex-end;
}

.pagination {
  display: flex;
  justify-content: flex-end;
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