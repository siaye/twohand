<template>
  <div class="order-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>订单管理</h2>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="filter-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="订单号">
            <el-input
              v-model="searchForm.orderNo"
              placeholder="请输入订单号"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="商品名称">
            <el-input
              v-model="searchForm.productTitle"
              placeholder="请输入商品名称"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="买家">
            <el-input
              v-model="searchForm.buyerName"
              placeholder="请输入买家用户名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="卖家">
            <el-input
              v-model="searchForm.sellerName"
              placeholder="请输入卖家用户名"
              clearable
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="订单状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 200px">
              <el-option label="待付款" value="0" />
              <el-option label="待发货" value="1" />
              <el-option label="待收货" value="2" />
              <el-option label="已完成" value="3" />
              <el-option label="退款中" value="4" />
              <el-option label="已取消" value="5" />
              <el-option label="已退款" value="6" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 订单列表 -->
      <el-table
        v-loading="loading"
        :data="orders"
        style="width: 100%"
      >
        <el-table-column prop="id" label="订单号" width="180" />
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              <div class="product-info">
                <div class="product-name">{{ item.productTitle }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="buyerName" label="买家" width="120" />
        <el-table-column prop="sellerName" label="卖家" width="120" />
        <el-table-column prop="totalAmount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentType" label="支付方式" width="100">
          <template #default="{ row }">
            {{ getPaymentTypeText(row.paymentType) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                size="small"
                type="primary"
                @click="handleView(row)"
              >
                查看
              </el-button>
              <el-button
                v-if="row.status === 1"
                size="small"
                type="success"
                @click="handleShip(row)"
              >
                发货
              </el-button>
              <el-button
                v-if="row.status === 2"
                size="small"
                type="warning"
                @click="handleConfirmReceipt(row)"
              >
                确认收货
              </el-button>
              <el-button
                v-if="row.status === 4"
                size="small"
                type="danger"
                @click="handleConfirmRefund(row)"
              >
                确认退款
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder?.id }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getOrderStatusType(currentOrder?.status)">
            {{ getOrderStatusText(currentOrder?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="买家">{{ currentOrder?.buyerName }}</el-descriptions-item>
        <el-descriptions-item label="卖家">{{ currentOrder?.sellerName }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ currentOrder?.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ getPaymentTypeText(currentOrder?.paymentType) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatTime(currentOrder?.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatTime(currentOrder?.updatedAt) }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ currentOrder?.address }}</el-descriptions-item>
      </el-descriptions>

      <div class="order-items">
        <h3>商品信息</h3>
        <el-table :data="currentOrder?.items" border>
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <el-image
                :src="row.productImage"
                :preview-src-list="[row.productImage]"
                fit="cover"
                class="product-image"
                @error="(e) => handleImageError(e, row)"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="productTitle" label="商品名称" />
          <el-table-column prop="price" label="单价" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column label="小计" width="120">
            <template #default="{ row }">
              ¥{{ (row.price * row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminOrders, getAdminOrderDetail, updateAdminOrderStatus } from '@/api/admin'
import { Picture } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  productTitle: '',
  buyerName: '',
  sellerName: '',
  status: '',
  timeRange: []
})

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 只有当值不为空时才添加到参数中
    if (searchForm.orderNo) params.orderNo = searchForm.orderNo
    if (searchForm.productTitle) params.productTitle = searchForm.productTitle
    if (searchForm.buyerName) params.buyerName = searchForm.buyerName
    if (searchForm.sellerName) params.sellerName = searchForm.sellerName
    if (searchForm.status !== '') params.status = parseInt(searchForm.status)
    if (searchForm.timeRange && searchForm.timeRange.length === 2) {
      params.startTime = searchForm.timeRange[0]
      params.endTime = searchForm.timeRange[1]
    }
    
    console.log('发送请求参数:', params)
    const res = await getAdminOrders(params)
    console.log('收到响应数据:', res)
    
    if (res.code === 200 && res.data) {
      console.log('响应数据解析成功')
      // 确保list存在，如果不存在则使用空数组
      const list = res.data.list || []
      console.log('订单列表数据:', list)
      
      orders.value = list.map(order => {
        console.log('处理订单数据:', order)
        return {
          ...order,
          // 商品信息
          items: order.items?.map(item => ({
            id: item.id,
            productTitle: item.productTitle || '未知商品'
          })) || [],
          // 买家信息
          buyerName: order.buyerName || '未知买家',
          // 卖家信息
          sellerName: order.sellerName || '未知卖家',
          // 其他信息
          totalAmount: order.totalAmount || 0,
          status: order.status || 0,
          paymentType: order.paymentType || 1,
          createdAt: order.createdAt || '',
          updatedAt: order.updatedAt || ''
        }
      })
      total.value = res.data.total || 0
      console.log('处理后的订单数据:', orders.value)
    } else {
      console.error('响应数据异常:', res)
      throw new Error(res.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error(error.message || '获取订单列表失败')
    // 发生错误时清空数据
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  console.log('开始搜索，搜索条件：', searchForm)
  currentPage.value = 1
  fetchOrders()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'timeRange' ? [] : ''
  })
  handleSearch()
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

// 查看订单详情
const handleView = async (row) => {
  try {
    const res = await getAdminOrderDetail(row.id)
    if (res.code === 200) {
      // 处理商品图片
      if (res.data.items) {
        res.data.items = res.data.items.map(item => {
          // 处理图片URL
          let productImage = item.productImage
          if (productImage) {
            if (!productImage.startsWith('http')) {
              // 添加/api前缀
              productImage = `http://localhost:8080/api${productImage}`
            }
          } else {
            // 如果没有图片，使用默认图片
            productImage = '/placeholder.png'
          }
          return {
            ...item,
            productImage
          }
        })
      }
      currentOrder.value = res.data
      detailDialogVisible.value = true
    } else {
      throw new Error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error(error.message || '获取订单详情失败')
  }
}

// 发货
const handleShip = async (row) => {
  try {
    await ElMessageBox.confirm('确认发货该订单吗？', '提示', {
      type: 'warning'
    })
    const res = await updateAdminOrderStatus(row.id, 2)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      fetchOrders()
    } else {
      throw new Error(res.message || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error(error.message || '发货失败')
    }
  }
}

// 确认收货
const handleConfirmReceipt = async (row) => {
  try {
    await ElMessageBox.confirm('确认已收到该订单吗？', '提示', {
      type: 'warning'
    })
    const res = await updateAdminOrderStatus(row.id, 3)
    if (res.code === 200) {
      ElMessage.success('确认收货成功')
      fetchOrders()
    } else {
      throw new Error(res.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error(error.message || '确认收货失败')
    }
  }
}

// 确认退款
const handleConfirmRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确认退款该订单吗？', '提示', {
      type: 'warning'
    })
    const res = await updateAdminOrderStatus(row.id, 6)
    if (res.code === 200) {
      ElMessage.success('退款成功')
      fetchOrders()
    } else {
      throw new Error(res.message || '退款失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error(error.message || '退款失败')
    }
  }
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '退款中',
    5: '已取消',
    6: '已退款'
  }
  return statusMap[status] || '未知状态'
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'warning',
    5: 'danger',
    6: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取支付方式文本
const getPaymentTypeText = (type) => {
  const typeMap = {
    1: '平台余额',
    2: '线下交易'
  }
  return typeMap[type] || '未知方式'
}

// 处理图片加载错误
const handleImageError = (e, item) => {
  console.error('图片加载失败:', {
    itemId: item.id,
    productImage: item.productImage,
    error: e
  })
  // 如果图片加载失败，尝试添加/api前缀
  if (item.productImage && !item.productImage.includes('/api/')) {
    item.productImage = item.productImage.replace('/uploads/', '/api/uploads/')
  } else {
    item.productImage = '/placeholder.png'
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-management {
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

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 10px;
  margin-right: 10px;
}

.search-form :deep(.el-input),
.search-form :deep(.el-select),
.search-form :deep(.el-date-editor) {
  width: 200px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.product-price {
  font-size: 12px;
  color: #909399;
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

.order-items {
  margin-top: 20px;
}

.order-items h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

:deep(.el-descriptions) {
  margin: 20px 0;
}

:deep(.el-descriptions__label) {
  width: 120px;
  justify-content: flex-end;
}
</style> 