<template>
  <div class="seller-orders">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的订单</h2>
          <el-radio-group v-model="statusFilter" @change="handleSearch">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="1">待发货</el-radio-button>
            <el-radio-button label="2">待收货</el-radio-button>
            <el-radio-button label="3">已完成</el-radio-button>
            <el-radio-button label="4">退款中</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 订单列表 -->
      <div class="order-list" v-loading="loading">
        <el-empty v-if="orders.length === 0" description="暂无订单" />
        <div v-else class="order-items">
          <el-card v-for="order in orders" :key="order.id" class="order-item">
            <template #header>
              <div class="order-header">
                <div class="order-info">
                  <span class="order-id">订单号：{{ order.id }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
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
                />
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
                <template v-if="order.status === 1">
                  <el-button type="primary" @click="handleShip(order)">发货</el-button>
                </template>
                <template v-if="order.status === 4">
                  <el-button type="warning" @click="handleRefund(order)">处理退款</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSellerOrders, shipOrder, handleRefund } from '@/api/order'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true
    const res = await getSellerOrders({
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value
    })
    if (res.code === 200) {
      orders.value = res.data.records.map(order => ({
        ...order,
        statusText: getOrderStatusText(order.status)
      }))
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
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
    1: 'primary',
    2: 'success',
    3: 'info',
    4: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '退款中'
  }
  return statusMap[status] || '未知状态'
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

// 处理退款
const handleRefund = async (order) => {
  try {
    await ElMessageBox.confirm('确认同意退款？', '提示', {
      type: 'warning'
    })
    await handleRefund(order.id)
    ElMessage.success('退款处理成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退款处理失败:', error)
      ElMessage.error('退款处理失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  return date.toLocaleString()
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.seller-orders {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
</style> 