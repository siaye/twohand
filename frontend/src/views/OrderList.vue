<template>
  <div class="order-list">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>我的订单</span>
          <el-radio-group v-model="orderType" @change="handleTypeChange">
            <el-radio-button label="buy">我买到的</el-radio-button>
            <el-radio-button label="sell">我卖出的</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="orderList"
        style="width: 100%">
        <el-table-column prop="id" label="订单号" width="180"/>
        <el-table-column label="商品信息" min-width="300">
          <template #default="{ row }">
            <div v-for="item in row.items" :key="item.id" class="order-item">
              <el-image
                :src="item.productImage"
                :preview-src-list="[item.productImage]"
                fit="cover"
                class="product-image"/>
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-price">
                  <span>¥{{ item.price }}</span>
                  <span>x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              size="small"
              @click="handlePay(row)">
              立即支付
            </el-button>
            <el-button
              v-if="row.status === 0"
              type="danger"
              size="small"
              @click="handleCancel(row)">
              取消订单
            </el-button>
            <el-button
              v-if="row.status === 1 && orderType === 'sell'"
              type="success"
              size="small"
              @click="handleShip(row)">
              发货
            </el-button>
            <el-button
              v-if="row.status === 2 && orderType === 'buy'"
              type="success"
              size="small"
              @click="handleConfirm(row)">
              确认收货
            </el-button>
            <el-button
              v-if="(row.status === 2 || row.status === 3) && orderType === 'buy'"
              type="warning"
              size="small"
              @click="handleRefund(row)">
              申请退款
            </el-button>
            <el-button
              v-if="row.status === 5 && orderType === 'sell'"
              type="primary"
              size="small"
              @click="handleConfirmRefund(row)">
              确认退货
            </el-button>
            <el-button
              v-if="row.status === 5 && orderType === 'sell'"
              type="info"
              size="small"
              @click="handleRejectRefund(row)">
              拒绝退货
            </el-button>
            <el-button
              v-if="row.status === 4"
              type="danger"
              size="small"
              @click="handleDelete(row)">
              删除订单
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 添加分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="30%">
      <el-form :model="refundForm" label-width="80px">
        <el-form-item label="退款原因">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入退款原因"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRefund">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserOrders, getSellerOrders, payOrder, shipOrder, confirmOrder, applyRefund, cancelOrder } from '@/api/order'
import { formatDate } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const orderList = ref([])
const orderType = ref('buy')
const refundDialogVisible = ref(false)
const refundForm = ref({
  orderId: null,
  reason: ''
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取订单列表
const getOrderList = async () => {
  loading.value = true
  try {
    const res = orderType.value === 'buy' ? await getUserOrders() : await getSellerOrders()
    orderList.value = res.data
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 处理页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  getOrderList()
}

// 处理每页条数改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getOrderList()
}

// 切换订单类型
const handleTypeChange = () => {
  getOrderList()
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',   // 待付款
    1: 'info',      // 待发货
    2: 'primary',   // 待收货
    3: 'success',   // 已完成
    4: 'danger',    // 已取消
    5: 'warning',   // 退款中
    6: 'success'    // 已退款
  }
  return typeMap[status] || 'info'
}

// 支付订单
const handlePay = async (row) => {
  try {
    await payOrder(row.id)
    ElMessage.success('支付成功')
    getOrderList()
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  }
}

// 发货
const handleShip = async (row) => {
  try {
    await shipOrder(row.id)
    ElMessage.success('发货成功')
    getOrderList()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  }
}

// 确认收货
const handleConfirm = async (row) => {
  try {
    await confirmOrder(row.id)
    ElMessage.success('确认收货成功')
    getOrderList()
  } catch (error) {
    console.error('确认收货失败:', error)
    ElMessage.error('确认收货失败')
  }
}

// 申请退款
const handleRefund = (row) => {
  refundForm.value = {
    orderId: row.id,
    reason: ''
  }
  refundDialogVisible.value = true
}

// 提交退款申请
const submitRefund = async () => {
  if (!refundForm.value.reason) {
    ElMessage.warning('请输入退款原因')
    return
  }
  
  try {
    await applyRefund(refundForm.value.orderId, refundForm.value.reason)
    ElMessage.success('退款申请已提交')
    refundDialogVisible.value = false
    getOrderList()
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error('申请退款失败')
  }
}

// 取消订单
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelOrder(row.id)
      ElMessage.success('订单已取消')
      getOrderList()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  })
}

// 查看订单详情
const handleDetail = (row) => {
  router.push(`/order/${row.id}`)
}

onMounted(() => {
  getOrderList()
})
</script>

<style scoped>
.order-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  margin-right: 10px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 5px;
}

.product-price {
  font-size: 12px;
  color: #999;
}

.product-price span {
  margin-right: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 