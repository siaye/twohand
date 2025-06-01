<template>
  <div class="order-detail">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-button @click="router.back()">返回</el-button>
        </div>
      </template>
      
      <div v-loading="loading">
        <div class="order-info">
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ order.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">订单状态：</span>
            <el-tag :type="getStatusType(order.status)">{{ order.statusText }}</el-tag>
          </div>
          <div class="info-item">
            <span class="label">创建时间：</span>
            <span class="value">{{ formatDate(order.createdAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ order.paymentTypeText }}</span>
          </div>
          <div class="info-item">
            <span class="label">收货地址：</span>
            <span class="value">{{ order.address }}</span>
          </div>
        </div>
        
        <el-divider/>
        
        <div class="order-items">
          <div class="section-title">商品信息</div>
          <div v-for="item in order.items" :key="item.id" class="order-item">
            <el-image
              :src="item.productImage"
              :preview-src-list="[item.productImage]"
              fit="cover"
              class="product-image"/>
            <div class="product-info">
              <div class="product-name">{{ item.productName }}</div>
              <div class="product-price">
                <span>单价：¥{{ item.price }}</span>
                <span>数量：{{ item.quantity }}</span>
                <span>小计：¥{{ item.subtotal }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <el-divider/>
        
        <div class="order-amount">
          <div class="section-title">金额信息</div>
          <div class="amount-item">
            <span class="label">商品总额：</span>
            <span class="value">¥{{ order.totalAmount }}</span>
          </div>
        </div>
        
        <el-divider/>
        
        <div class="order-actions" v-if="order.status !== 3 && order.status !== 6">
          <el-button
            v-if="order.status === 0"
            type="primary"
            @click="handlePay">
            立即支付
          </el-button>
          <el-button
            v-if="order.status === 0"
            type="danger"
            @click="handleCancel">
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 1 && isSeller"
            type="success"
            @click="handleShip">
            发货
          </el-button>
          <el-button
            v-if="order.status === 2 && !isSeller"
            type="success"
            @click="handleConfirm">
            确认收货
          </el-button>
          <el-button
            v-if="(order.status === 2 || order.status === 3) && !isSeller"
            type="warning"
            @click="handleRefund">
            申请退款
          </el-button>
          <el-button
            v-if="order.status === 5 && isSeller"
            type="primary"
            @click="handleConfirmRefund">
            确认退货
          </el-button>
          <el-button
            v-if="order.status === 5 && isSeller"
            type="info"
            @click="handleRejectRefund">
            拒绝退货
          </el-button>
          <el-button
            v-if="order.status === 4"
            type="danger"
            @click="handleDelete">
            删除订单
          </el-button>
        </div>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, payOrder, shipOrder, confirmReceipt, refundOrder, cancelOrder } from '@/api/order'
import { formatDate } from '@/utils/format'
import { useStore } from 'vuex'

const router = useRouter()
const route = useRoute()
const store = useStore()
const loading = ref(false)
const order = ref({})
const refundDialogVisible = ref(false)
const refundForm = ref({
  reason: ''
})

// 判断是否为卖家
const isSeller = computed(() => {
  return order.value.sellerId === store.state.user?.id
})

// 获取订单详情
const getOrder = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200) {
      // 处理商品图片URL
      if (res.data.items) {
        res.data.items = res.data.items.map(item => {
          if (item.productImage && !item.productImage.startsWith('http')) {
            item.productImage = `http://localhost:8080/api${item.productImage}`
          }
          return item
        })
      }
      order.value = res.data
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
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
const handlePay = async () => {
  try {
    await payOrder(order.value.id)
    ElMessage.success('支付成功')
    getOrder()
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败')
  }
}

// 发货
const handleShip = async () => {
  try {
    await shipOrder(order.value.id)
    ElMessage.success('发货成功')
    getOrder()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  }
}

// 确认收货
const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '提示', {
      type: 'warning'
    })
    await confirmReceipt(order.value.id)
    ElMessage.success('确认收货成功')
    getOrder()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

// 申请退款
const handleRefund = () => {
  refundForm.value.reason = ''
  refundDialogVisible.value = true
}

// 提交退款申请
const submitRefund = async () => {
  if (!refundForm.value.reason) {
    ElMessage.warning('请输入退款原因')
    return
  }
  
  try {
    await refundOrder(order.value.id)
    ElMessage.success('退款申请已提交')
    refundDialogVisible.value = false
    getOrder()
  } catch (error) {
    console.error('申请退款失败:', error)
    ElMessage.error('申请退款失败')
  }
}

// 取消订单
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelOrder(order.value.id)
      ElMessage.success('订单已取消')
      getOrder()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  })
}

onMounted(() => {
  getOrder()
})
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  margin-bottom: 20px;
}

.info-item {
  margin-bottom: 10px;
}

.info-item .label {
  color: #666;
  margin-right: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  border-bottom: none;
}

.product-image {
  width: 100px;
  height: 100px;
  margin-right: 15px;
  border-radius: 4px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.product-price {
  font-size: 14px;
  color: #666;
}

.product-price span {
  margin-right: 20px;
}

.order-amount {
  margin: 20px 0;
}

.amount-item {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
}

.amount-item .label {
  color: #666;
  margin-right: 10px;
}

.amount-item .value {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.order-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 