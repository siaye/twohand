<template>
  <div class="order-pay">
    <el-card class="pay-card">
      <template #header>
        <div class="card-header">
          <h2>订单支付</h2>
        </div>
      </template>
      
      <div v-loading="loading">
        <div class="order-info">
          <div class="info-item">
            <span class="label">订单号：</span>
            <span class="value">{{ order.id }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付金额：</span>
            <span class="value price">¥{{ order.totalAmount }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ order.paymentTypeText }}</span>
          </div>
        </div>
        
        <div class="payment-info">
          <div class="balance-info">
            <span>当前余额：</span>
            <span class="balance">¥{{ userBalance }}</span>
          </div>
          
          <div class="payment-tips">
            <el-alert
              title="支付说明"
              type="info"
              :closable="false"
              show-icon>
              <p>1. 支付金额将暂时由平台保管</p>
              <p>2. 卖家发货后，买家确认收货</p>
              <p>3. 确认收货后，平台将货款转给卖家</p>
            </el-alert>
          </div>
        </div>
        
        <div class="action-buttons">
          <el-button @click="handleCancel">取消支付</el-button>
          <el-button type="primary" @click="handlePay" :loading="paying">
            确认支付
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail } from '@/api/order'
import { getWalletBalance } from '@/api/wallet'
import { payOrder } from '@/api/order'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const paying = ref(false)
const order = ref({})
const userBalance = ref(0)

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    loading.value = true
    const orderId = route.params.id
    console.log('获取订单详情，订单ID:', orderId)
    
    const res = await getOrderDetail(orderId)
    console.log('订单详情响应:', res)
    
    if (res.code === 200) {
      order.value = res.data
      // 检查订单状态
      if (order.value.status !== 0) {
        ElMessage.warning('该订单状态不正确，无法支付')
        router.push('/my-orders')
        return
      }
    } else {
      throw new Error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    if (error.response?.status === 403) {
      ElMessage.error('无权查看该订单')
    } else if (error.response?.status === 404) {
      ElMessage.error('订单不存在')
    } else {
      ElMessage.error(error.message || '获取订单详情失败')
    }
    router.push('/my-orders')
  } finally {
    loading.value = false
  }
}

// 获取用户余额
const fetchUserBalance = async () => {
  try {
    const res = await getWalletBalance()
    if (res.code === 200) {
      userBalance.value = res.data
    }
  } catch (error) {
    console.error('获取用户余额失败:', error)
  }
}

// 处理支付
const handlePay = async () => {
  if (userBalance.value < order.value.totalAmount) {
    ElMessage.error('余额不足，请先充值')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确认支付 ¥${order.value.totalAmount}？`,
      '支付确认',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    paying.value = true
    const res = await payOrder(order.value.id)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      // 支付成功后跳转到订单详情页
      await router.push({
        path: `/order/${order.value.id}`,
        replace: true
      })
    } else {
      throw new Error(res.message || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error(error.message || '支付失败')
    }
  } finally {
    paying.value = false
  }
}

// 处理取消
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消支付吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    // 取消支付后跳转到订单列表页
    router.push('/my-orders')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消支付失败:', error)
      ElMessage.error('取消支付失败')
    }
  }
}

onMounted(() => {
  fetchOrderDetail()
  fetchUserBalance()
})
</script>

<style scoped>
.order-pay {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
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

.order-info {
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  margin-bottom: 15px;
  font-size: 16px;
}

.info-item .label {
  width: 100px;
  color: #606266;
}

.info-item .value {
  color: #303133;
}

.info-item .value.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.payment-info {
  margin-bottom: 30px;
}

.balance-info {
  margin-bottom: 15px;
  font-size: 16px;
}

.balance-info .balance {
  color: #f56c6c;
  font-weight: bold;
  margin-left: 10px;
}

.payment-tips {
  margin-top: 15px;
}

.payment-tips p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}
</style> 