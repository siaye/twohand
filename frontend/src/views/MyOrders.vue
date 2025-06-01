<template>
  <div class="my-orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
          <el-radio-group v-model="orderType" @change="handleOrderTypeChange">
            <el-radio-button label="all">全部订单</el-radio-button>
            <el-radio-button label="buy">我买的</el-radio-button>
            <el-radio-button label="sell">我卖的</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 订单状态筛选 -->
      <div class="filter-container">
        <el-radio-group v-model="statusFilter" @change="handleSearch">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">待付款</el-radio-button>
          <el-radio-button label="1">待发货</el-radio-button>
          <el-radio-button label="2">待收货</el-radio-button>
          <el-radio-button label="3">已完成</el-radio-button>
          <el-radio-button label="5">已取消</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 订单列表 -->
      <div class="order-list" v-loading="loading">
        <el-empty v-if="orders.length === 0" description="暂无订单" />
        <div v-else class="order-items">
          <el-card v-for="order in orders" :key="order.id" class="order-item">
            <template #header>
              <div class="order-header">
                <div class="order-info">
                  <span class="order-id">订单号：{{ order.id }}</span>
                  <el-tag size="small" :type="orderType === 'buy' ? 'success' : 'warning'">
                    {{ orderType === 'buy' ? '我买的' : '我卖的' }}
                  </el-tag>
                </div>
                <el-tag :type="getOrderStatusType(order.status)">
                  {{ order.statusText || getOrderStatusText(order.status) }}
                </el-tag>
              </div>
            </template>

            <div class="order-content">
              <div v-for="item in order.items" :key="item.id" class="product-info">
                <el-image
                  :src="item.productImage"
                  :preview-src-list="[item.productImage]"
                  fit="cover"
                  class="product-image"
                  @error="(e) => handleImageError(e, item)"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="product-details">
                  <h3 class="product-title">{{ item.productName }}</h3>
                  <p class="product-price">¥{{ item.price }} x {{ item.quantity }}</p>
                  <p class="product-subtotal">小计：¥{{ item.subtotal }}</p>
                </div>
              </div>

              <div class="order-summary">
                <p class="total-amount">订单总额：¥{{ order.totalAmount }}</p>
                <p class="payment-type">支付方式：{{ order.paymentTypeText }}</p>
              </div>

              <div class="order-actions">
                <template v-if="order.status === 0 && (orderType === 'buy' || order.orderType === 'buy')">
                  <el-button type="primary" @click="handlePay(order)">立即付款</el-button>
                  <el-button @click="handleCancel(order)">取消订单</el-button>
                </template>
                <template v-else-if="order.status === 1 && (orderType === 'sell' || order.orderType === 'sell')">
                  <el-button type="primary" @click="handleShip(order)">发货</el-button>
                </template>
                <template v-else-if="order.status === 2 && (orderType === 'buy' || order.orderType === 'buy')">
                  <el-button type="primary" @click="handleConfirmReceipt(order)">确认收货</el-button>
                  <el-button 
                    v-if="canRefund(order)" 
                    type="warning" 
                    @click="handleRefund(order)"
                  >
                    申请退货
                  </el-button>
                </template>
                <template v-else-if="order.status === 3 && (orderType === 'buy' || order.orderType === 'buy')">
                  <el-button type="primary" @click="openReviewDialog(order)">评价</el-button>
                  <el-button 
                    v-if="canRefund(order)" 
                    type="warning" 
                    @click="handleRefund(order)"
                  >
                    申请退货
                  </el-button>
                </template>
                <template v-else-if="order.status === 5">
                  <template v-if="orderType === 'sell' || order.orderType === 'sell'">
                    <el-button type="primary" @click="handleConfirmRefund(order)">确认退货</el-button>
                    <el-button type="info" @click="handleRejectRefund(order)">拒绝退货</el-button>
                  </template>
                  <template v-else>
                    <el-button type="info" disabled>等待卖家处理退货申请</el-button>
                  </template>
                </template>
                <template v-else-if="order.status === 4">
                  <el-button type="danger" @click="handleDelete(order)">删除订单</el-button>
                </template>
                <template v-else-if="order.status === 6">
                  <el-button type="info" disabled>已退款</el-button>
                </template>
              </div>
            </div>
          </el-card>
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

    <!-- 评价对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="商品评价"
      width="500px"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="80px"
      >
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入评价内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="reviewDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview" :loading="reviewLoading">
            提交评价
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserOrders, getSellerOrders, updateOrderStatus, deleteOrder, createReview, cancelOrder, confirmReceipt, refundOrder, shipOrder, confirmRefund, payOrder } from '@/api/order'
import { getProduct } from '@/api/product'
import { getWalletBalance } from '@/api/wallet'
import { useStore } from 'vuex'
import { Picture } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()
const loading = ref(false)
const reviewLoading = ref(false)
const reviewDialogVisible = ref(false)
const reviewFormRef = ref(null)
const currentOrder = ref(null)

const orderType = ref('all') // 新增：订单类型（全部/我买的/我卖的）
const statusFilter = ref('')
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 判断是否为卖家
const isSeller = computed(() => {
  return store.state.user?.role === 1 // 假设 1 表示卖家角色
})

// 评价表单
const reviewForm = ref({
  orderId: null,
  productId: null,
  rating: 5,
  content: '',
  images: ''
})

// 评价表单验证规则
const reviewRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 检查订单是否可以申请退货
const canRefund = (order) => {
  // 只有待收货或已完成状态可以申请退货
  if (order.status !== 2 && order.status !== 3) return false
  const orderTime = new Date(order.createdAt).getTime()
  const now = new Date().getTime()
  const hours = (now - orderTime) / (1000 * 60 * 60)
  return hours <= 24
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    let res
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value || undefined
    }

    if (orderType.value === 'all') {
      // 获取所有订单
      const [buyRes, sellRes] = await Promise.all([
        getUserOrders(params),
        getSellerOrders(params)
      ])
      console.log('【MyOrders】买家订单数据:', buyRes.data.records)
      console.log('【MyOrders】卖家订单数据:', sellRes.data.records)
      
      res = {
        code: 200,
        data: {
          records: [
            ...buyRes.data.records.map(order => ({ ...order, orderType: 'buy' })),
            ...sellRes.data.records.map(order => ({ ...order, orderType: 'sell' }))
          ],
          total: buyRes.data.total + sellRes.data.total
        }
      }
    } else if (orderType.value === 'buy') {
      res = await getUserOrders(params)
    } else {
      res = await getSellerOrders(params)
    }

    if (res.code === 200) {
      // 获取所有订单项的商品详情
      const orderItems = res.data.records.flatMap(order => order.items)
      const productIds = [...new Set(orderItems.map(item => item.productId))]
      
      // 获取所有商品的详细信息
      const productDetails = await Promise.all(
        productIds.map(async (productId) => {
          try {
            const productRes = await getProduct(productId)
            return productRes.data
          } catch (error) {
            console.error(`获取商品 ${productId} 详情失败:`, error)
            return null
          }
        })
      )
      
      // 创建商品ID到详情的映射
      const productMap = productDetails.reduce((map, product) => {
        if (product) {
          map[product.id] = product
        }
        return map
      }, {})

      orders.value = res.data.records.map(order => {
        return {
          ...order,
          statusText: getOrderStatusText(order.status),
          status: Number(order.status),
          items: order.items.map(item => {
            // 获取商品详情
            const product = productMap[item.productId]
            console.log('【MyOrders】商品详情:', {
              itemId: item.id,
              productId: item.productId,
              product
            })
            
            // 从商品详情中获取图片
            let productImage = null
            if (product) {
              productImage = product.image || 
                           product.images?.[0] || 
                           product.imageList?.[0]
            }
            
            // 如果商品详情中没有图片，尝试从订单项中获取
            if (!productImage) {
              productImage = item.image || 
                           item.productImage || 
                           item.images?.[0] || 
                           item.imageList?.[0]
            }
            
            // 如果图片URL不是以http开头，添加服务器地址
            if (productImage && !productImage.startsWith('http')) {
              productImage = `http://localhost:8080${productImage}`
            }
            
            console.log('【MyOrders】最终使用的图片URL:', {
              itemId: item.id,
              productImage,
              productName: product?.name || item.productName
            })
            
            return {
              ...item,
              productImage,
              productName: product?.name || item.productName
            }
          })
        }
      })
      total.value = res.data.total
    } else {
      throw new Error(res.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 处理订单类型切换
const handleOrderTypeChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrders()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrders()
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const statusMap = {
    0: 'warning',    // 待付款
    1: 'primary',    // 待发货
    2: 'success',    // 待收货
    3: 'info',       // 已完成
    4: 'danger',     // 已取消
    5: 'warning',    // 退款中
    6: 'success'     // 已退款
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消',
    5: '退款中',
    6: '已退款'
  }
  return statusMap[status] || '未知状态'
}

// 处理付款
const handlePay = async (order) => {
  try {
    console.log('【支付流程】开始处理支付，订单信息:', {
      orderId: order.id,
      status: order.status,
      totalAmount: order.totalAmount,
      paymentType: order.paymentType,
      userId: order.userId,
      sellerId: order.sellerId
    })
    
    // 检查订单状态
    if (order.status !== 0) {
      console.warn('【支付流程】订单状态不正确:', order.status)
      ElMessage.warning('该订单状态不正确，无法支付')
      return
    }
    
    // 获取用户余额
    console.log('【支付流程】获取用户余额...')
    const balanceRes = await getWalletBalance()
    console.log('【支付流程】余额查询结果:', balanceRes)
    
    if (balanceRes.code !== 200) {
      console.error('【支付流程】获取余额失败:', balanceRes)
      throw new Error('获取余额失败')
    }
    
    const userBalance = balanceRes.data
    console.log('【支付流程】用户余额:', userBalance, '订单金额:', order.totalAmount)
    
    if (userBalance < order.totalAmount) {
      console.warn('【支付流程】余额不足:', {
        userBalance,
        orderAmount: order.totalAmount,
        difference: userBalance - order.totalAmount
      })
      ElMessage.error('余额不足，请先充值')
      return
    }
    
    // 确认支付
    console.log('【支付流程】显示支付确认对话框')
    await ElMessageBox.confirm(
      `确认支付 ¥${order.totalAmount}？`,
      '支付确认',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用支付接口
    console.log('【支付流程】调用支付接口，订单ID:', order.id)
    const res = await payOrder(order.id)
    console.log('【支付流程】支付接口响应:', res)
    
    if (res.code === 200) {
      console.log('【支付流程】支付成功，准备跳转到订单详情页')
      ElMessage.success('支付成功')
      // 支付成功后跳转到订单详情页
      router.push(`/order/${order.id}`)
    } else {
      console.error('【支付流程】支付失败:', res)
      throw new Error(res.message || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('【支付流程】支付失败，详细错误:', {
        error,
        message: error.message,
        response: error.response?.data,
        stack: error.stack
      })
      ElMessage.error(error.message || '支付失败')
    }
  }
}

// 处理发货
const handleShip = async (order) => {
  try {
    await ElMessageBox.confirm('确认已发货？', '提示', {
      type: 'warning'
    })
    await shipOrder(order.id)
    ElMessage.success('发货成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败')
    }
  }
}

// 处理确认收货
const handleConfirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm('确认买家已收货？', '提示', {
      type: 'warning'
    })
    await confirmReceipt(order.id)
    ElMessage.success('确认收货成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

// 处理取消订单
const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      type: 'warning'
    })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 处理删除订单
const handleDelete = async (order) => {
  try {
    await ElMessageBox.confirm('确定要删除这个订单吗？此操作不可恢复。', '提示', {
      type: 'warning'
    })
    await deleteOrder(order.id)
    ElMessage.success('删除成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除订单失败:', error)
      ElMessage.error('删除订单失败')
    }
  }
}

// 打开评价对话框
const openReviewDialog = (order) => {
  currentOrder.value = order
  reviewForm.value = {
    orderId: order.id,
    productId: order.items[0].productId,
    rating: 5,
    content: '',
    images: ''
  }
  reviewDialogVisible.value = true
}

// 提交评价
const submitReview = async () => {
  if (!reviewFormRef.value) return
  
  try {
    await reviewFormRef.value.validate()
    reviewLoading.value = true
    
    await createReview(reviewForm.value)
    ElMessage.success('评价成功')
    reviewDialogVisible.value = false
    // 刷新订单列表
    fetchOrders()
  } catch (error) {
    console.error('评价失败:', error)
    ElMessage.error('评价失败: ' + (error.response?.data?.message || error.message))
  } finally {
    reviewLoading.value = false
  }
}

// 处理退货申请
const handleRefund = async (order) => {
  try {
    await ElMessageBox.confirm('确认申请退货？', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    
    await refundOrder(order.id)
    ElMessage.success('退货申请已提交，等待卖家确认')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交退货申请失败:', error)
      ElMessage.error('提交退货申请失败')
    }
  }
}

// 处理卖家确认退货
const handleConfirmRefund = async (order) => {
  try {
    await ElMessageBox.confirm('确认同意买家的退货申请？', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    
    await confirmRefund(order.id)
    ElMessage.success('已确认退货申请，退款已完成')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认退货失败:', error)
      ElMessage.error('确认退货失败')
    }
  }
}

// 处理卖家拒绝退货
const handleRejectRefund = async (order) => {
  try {
    await ElMessageBox.confirm('确认拒绝买家的退货申请？', '提示', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    })
    
    await rejectRefund(order.id)
    ElMessage.success('已拒绝退货申请')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝退货失败:', error)
      ElMessage.error('拒绝退货失败')
    }
  }
}

// 处理图片 URL
const getImageUrl = (url) => {
  console.log('【MyOrders】处理图片URL:', {
    originalUrl: url,
    type: typeof url,
    isArray: Array.isArray(url)
  })

  if (!url) {
    console.log('【MyOrders】图片URL为空')
    return ''
  }
  if (url.startsWith('http')) {
    console.log('【MyOrders】图片URL已经是完整地址:', url)
    return url
  }
  // 如果图片URL是数组，取第一张图片
  if (Array.isArray(url)) {
    url = url[0]
    console.log('【MyOrders】从数组中获取第一张图片:', url)
  }
  // 如果图片URL是字符串且包含逗号，取第一张图片
  if (typeof url === 'string' && url.includes(',')) {
    url = url.split(',')[0]
    console.log('【MyOrders】从逗号分隔的字符串中获取第一张图片:', url)
  }
  const fullUrl = `http://localhost:8080${url}`
  console.log('【MyOrders】处理后的完整图片URL:', fullUrl)
  return fullUrl
}

// 处理图片加载错误
const handleImageError = (e, item) => {
  console.error('【MyOrders】图片加载失败:', {
    itemId: item.id,
    productImage: item.productImage,
    error: e
  })
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.my-orders {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.filter-container {
  margin-bottom: 20px;
}

.order-list {
  margin-bottom: 20px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-item {
  margin-bottom: 0;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  display: flex;
  gap: 20px;
  color: #606266;
  align-items: center;
}

.order-time {
  color: #909399;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.product-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.product-title {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.product-price {
  margin: 0;
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.order-summary {
  margin: 10px 0;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.total-amount {
  font-size: 16px;
  color: #f56c6c;
  font-weight: bold;
  margin: 5px 0;
}

.payment-type {
  color: #606266;
  margin: 5px 0;
}

.product-subtotal {
  color: #909399;
  margin: 5px 0;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

.el-select {
  width: 100%;
}
</style> 