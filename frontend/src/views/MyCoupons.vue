<template>
  <div class="my-coupons">
    <div class="page-header">
      <h2>我的优惠券</h2>
    </div>

    <div class="coupons-container" v-loading="loading">
      <div v-if="coupons.length === 0" class="no-coupons">
        暂无优惠券
      </div>
      
      <div v-else class="coupons-list">
        <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card">
          <div class="coupon-info">
            <h3>{{ coupon.coupon.name }}</h3>
            <div class="coupon-value">
              <template v-if="coupon.coupon.type === 1">
                <span class="amount">¥{{ coupon.coupon.value }}</span>
                <span class="condition">满{{ coupon.coupon.minAmount }}可用</span>
              </template>
              <template v-else>
                <span class="amount">{{ (coupon.coupon.value * 10).toFixed(1) }}折</span>
                <span class="condition">满{{ coupon.coupon.minAmount }}可用</span>
              </template>
            </div>
            <div class="coupon-time">
              有效期至：{{ formatDate(coupon.coupon.endTime) }}
            </div>
          </div>
          <div class="coupon-status">
            <el-tag :type="getStatusType(coupon.status)">
              {{ getStatusText(coupon.status) }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserAvailableCoupons } from '@/api/coupon'
import { formatDate } from '@/utils/format'
import { useStore } from 'vuex'

const store = useStore()
const loading = ref(false)
const coupons = ref([])

// 获取用户优惠券
const fetchCoupons = async () => {
  loading.value = true
  try {
    const userId = store.getters.user.id
    const res = await getUserAvailableCoupons(userId)
    if (res.code === 200) {
      coupons.value = res.data
    } else {
      ElMessage.error(res.message || '获取优惠券失败')
    }
  } catch (error) {
    console.error('获取优惠券失败:', error)
    ElMessage.error('获取优惠券失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 1:
      return 'success'
    case 2:
      return 'info'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 1:
      return '未使用'
    case 2:
      return '已使用'
    case 3:
      return '已过期'
    default:
      return '未知状态'
  }
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.my-coupons {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.coupons-container {
  min-height: 200px;
}

.no-coupons {
  text-align: center;
  color: #909399;
  padding: 40px 0;
}

.coupons-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.coupon-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.coupon-info {
  flex: 1;
}

.coupon-info h3 {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
}

.coupon-value {
  margin-bottom: 10px;
}

.coupon-value .amount {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  margin-right: 10px;
}

.coupon-value .condition {
  color: #909399;
  font-size: 14px;
}

.coupon-time {
  font-size: 12px;
  color: #909399;
}

.coupon-status {
  margin-left: 20px;
}
</style> 