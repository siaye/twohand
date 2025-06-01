<template>
  <div class="daily-coupons" v-if="isLoggedIn">
    <div class="section-title">
      <h2>每日优惠券</h2>
      <span class="refresh-btn" @click="refreshCoupons">
        <el-icon><Refresh /></el-icon>
        刷新
      </span>
    </div>
    
    <div class="coupons-container" v-loading="loading">
      <div v-if="coupons.length === 0" class="no-coupons">
        暂无可用优惠券
      </div>
      
      <div v-else class="coupons-list">
        <div v-for="coupon in coupons" :key="coupon.id" class="coupon-card">
          <div class="coupon-info">
            <h3>{{ coupon.name }}</h3>
            <div class="coupon-value">
              <template v-if="coupon.type === 1">
                <span class="amount">¥{{ coupon.value }}</span>
                <span class="condition">满{{ coupon.minAmount }}可用</span>
              </template>
              <template v-else>
                <span class="amount">{{ (coupon.value * 10).toFixed(1) }}折</span>
                <span class="condition">满{{ coupon.minAmount }}可用</span>
              </template>
            </div>
            <div class="coupon-time">
              有效期至：{{ formatDate(coupon.endTime) }}
            </div>
          </div>
          <div class="coupon-action">
            <el-button 
              :type="coupon.claimed ? 'info' : 'primary'"
              :disabled="coupon.claimed || loading"
              @click="claimCoupon(coupon.id)"
            >
              {{ coupon.claimed ? '已领取' : '立即领取' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getTodayAvailableCoupons, claimDailyCoupon } from '@/api/coupon'
import { formatDate } from '@/utils/format'
import { Refresh } from '@element-plus/icons-vue'
import { useStore } from 'vuex'

const store = useStore()
const loading = ref(false)
const coupons = ref([])

// 检查登录状态
const isLoggedIn = computed(() => store.getters.isLoggedIn)

// 获取今日优惠券
const fetchCoupons = async () => {
  loading.value = true
  try {
    const res = await getTodayAvailableCoupons()
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

// 领取优惠券
const claimCoupon = async (couponId) => {
  loading.value = true
  try {
    const res = await claimDailyCoupon(couponId)
    if (res.code === 200) {
      ElMessage.success('领取成功')
      // 刷新优惠券列表
      await fetchCoupons()
    } else {
      ElMessage.error(res.message || '领取失败')
    }
  } catch (error) {
    console.error('领取优惠券失败:', error)
    ElMessage.error('领取失败')
  } finally {
    loading.value = false
  }
}

// 刷新优惠券列表
const refreshCoupons = () => {
  fetchCoupons()
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.daily-coupons {
  margin: 20px 0;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.refresh-btn {
  display: flex;
  align-items: center;
  color: #409EFF;
  cursor: pointer;
}

.refresh-btn .el-icon {
  margin-right: 4px;
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

.coupon-action {
  margin-left: 20px;
}
</style> 