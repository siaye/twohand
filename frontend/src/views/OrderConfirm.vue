<template>
  <div class="order-confirm">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>确认订单</span>
        </div>
      </template>
      
      <div v-loading="loading">
        <!-- 商品信息 -->
        <div class="section" v-if="product">
          <div class="section-title">商品信息</div>
          <div class="product-info">
            <el-image
              :src="getProductImage(product)"
              :preview-src-list="[getProductImage(product)]"
              fit="cover"
              class="product-image"
              @error="handleImageError">
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="product-details">
              <div class="product-name">{{ product.title || '商品标题' }}</div>
              <div class="product-price">
                <span>单价：¥{{ formatPrice(product.price) }}</span>
                <span>数量：{{ quantity }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <el-divider/>
        
        <!-- 收货地址 -->
        <div class="section">
          <div class="section-title">收货地址</div>
          <el-form :model="addressForm" label-width="80px">
            <el-form-item label="收货人">
              <el-input v-model="addressForm.receiver" placeholder="请输入收货人姓名"/>
            </el-form-item>
            <el-form-item label="手机号码">
              <el-input v-model="addressForm.phone" placeholder="请输入手机号码"/>
            </el-form-item>
            <el-form-item label="详细地址">
              <el-input
                v-model="addressForm.address"
                type="textarea"
                :rows="2"
                placeholder="请输入详细地址"/>
            </el-form-item>
          </el-form>
        </div>
        
        <el-divider/>
        
        <!-- 支付方式 -->
        <div class="section">
          <div class="section-title">支付方式</div>
          <el-radio-group v-model="paymentType" class="payment-options">
            <el-radio :value="1">
              <div class="payment-option">
                <span class="payment-icon">💰</span>
                <span class="payment-name">平台余额支付</span>
                <span class="payment-balance">(余额: ¥{{ formatPrice(userBalance) }})</span>
              </div>
            </el-radio>
            <el-radio :value="2">
              <div class="payment-option">
                <span class="payment-icon">💳</span>
                <span class="payment-name">线下交易</span>
              </div>
            </el-radio>
          </el-radio-group>
          
          <div class="payment-tips" v-if="paymentType === 1">
            <el-alert
              title="平台余额支付说明"
              type="info"
              :closable="false"
              show-icon>
              <p>1. 支付金额将暂时由平台保管</p>
              <p>2. 卖家发货后，买家确认收货</p>
              <p>3. 确认收货后，平台将货款转给卖家</p>
            </el-alert>
          </div>
          <div class="payment-tips" v-if="paymentType === 2">
            <el-alert
              title="线下交易说明"
              type="warning"
              :closable="false"
              show-icon>
              <p>1. 请与卖家协商线下交易方式</p>
              <p>2. 线下交易可能存在风险，请谨慎选择</p>
              <p>3. 建议选择平台担保交易</p>
            </el-alert>
          </div>
        </div>
        
        <el-divider/>
        
        <!-- 优惠券选择 -->
        <div class="section">
          <div class="section-title">优惠券</div>
          <el-select v-model="selectedCouponId" placeholder="请选择优惠券" clearable>
            <el-option
              v-for="coupon in availableCoupons"
              :key="coupon.id"
              :label="coupon.coupon.name + ' - ' + coupon.coupon.value + '元'"
              :value="coupon.id">
              <span style="float: left">{{ coupon.coupon.name }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px;">
                {{ coupon.coupon.value }}元 (满{{ coupon.coupon.minAmount }}可用)
              </span>
            </el-option>
          </el-select>
        </div>
        
        <el-divider/>
        
        <!-- 积分抵扣 -->
        <div class="section">
          <div class="section-title">积分抵扣</div>
          <div class="points-info">
            <span class="available-points">当前可用积分：{{ userPoints }} 积分</span>
            <el-form :inline="true" class="points-form">
              <el-form-item label="使用积分">
                <el-input-number
                  v-model="pointsToUse"
                  :min="0"
                  :max="maxPointsToUse"
                  :step="100"
                  @change="handlePointsChange"
                  placeholder="输入积分"
                  controls-position="right"
                />
              </el-form-item>
              <el-form-item>
                <span class="points-discount-text">可抵扣：¥{{ formatPrice(pointsDiscountAmount) }}</span>
              </el-form-item>
            </el-form>
            <div v-if="pointsToUse > 0" class="points-tips">
              <el-alert
                :title="`当前积分抵扣规则：${pointsExchangeRate} 积分 = 1 元，${pointsToUse} 积分可抵扣 ${formatPrice(pointsDiscountAmount)} 元`"
                type="info"
                :closable="false"
                show-icon>
              </el-alert>
            </div>
          </div>
        </div>
        
        <el-divider/>
        
        <!-- 订单金额 -->
        <div class="section" v-if="product">
          <div class="section-title">订单金额</div>
          <div class="amount-info">
            <div class="amount-item">
              <span class="label">商品总额：</span>
              <span class="value">¥{{ formatPrice(productTotalAmount) }}</span>
            </div>
            <div class="amount-item" v-if="selectedCouponId">
              <span class="label">优惠券抵扣：</span>
              <span class="value" style="color: #67C23A;">-¥{{ formatPrice(discountAmount) }}</span>
            </div>
            <div class="amount-item" v-if="pointsToUse > 0">
              <span class="label">积分抵扣：</span>
              <span class="value" style="color: #67C23A;">-¥{{ formatPrice(pointsDiscountAmount) }}</span>
            </div>
            <div class="amount-item total">
              <span class="label">应付总额：</span>
              <span class="value">¥{{ formatPrice(finalAmount) }}</span>
            </div>
          </div>
        </div>
        
        <!-- 提交订单 -->
        <div class="submit-section" v-if="product">
          <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
            提交订单
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct } from '@/api/product'
import { createOrder } from '@/api/order'
import { getWalletBalance } from '@/api/wallet'
import { getUserAvailableCoupons, getUserCouponById } from '@/api/coupon'
import { useStore } from 'vuex'
import { Picture } from '@element-plus/icons-vue'
import { formatPrice } from '@/utils/format'
import { getUserPoints } from '@/api/user'

const store = useStore()
const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const userBalance = ref(0)
const product = ref(null)  // 初始值设为 null
const quantity = ref(1)
const paymentType = ref(1)
const availableCoupons = ref([])
const selectedCouponId = ref(null)
const discountAmount = ref(0)
const pointsToUse = ref(0)
const maxPointsToUse = ref(0)
const pointsDiscountAmount = ref(0)
const pointsExchangeRate = ref(100) // 100积分 = 1元，暂时硬编码
const userPoints = ref(0)

const addressForm = ref({
  receiver: '',
  phone: '',
  address: ''
})

// 计算商品总金额
const productTotalAmount = computed(() => {
  if (!product.value) return 0
  return Number(product.value.price) * Number(quantity.value)
})

// 计算最终支付金额
const finalAmount = computed(() => {
  if (!product.value) return 0
  let amount = productTotalAmount.value
  if (discountAmount.value > 0) {
    amount -= discountAmount.value
  }
  if (pointsDiscountAmount.value > 0) {
    amount -= pointsDiscountAmount.value
  }
  // 确保最终金额不小于0
  return amount < 0 ? 0 : amount
})

// 计算积分抵扣金额
const calculatePointsDiscount = () => {
  if (pointsToUse.value > 0) {
    pointsDiscountAmount.value = pointsToUse.value / pointsExchangeRate.value;
  } else {
    pointsDiscountAmount.value = 0;
  }
}

// 计算可使用积分上限
watch([userPoints, productTotalAmount, discountAmount], () => {
  if (!product.value) {
    maxPointsToUse.value = 0
    pointsToUse.value = 0
    calculatePointsDiscount()
    return
  }
  
  let maxByPoints = userPoints.value;
  let remainingAmount = productTotalAmount.value;
  if (discountAmount.value > 0) {
    remainingAmount -= discountAmount.value;
  }
  const maxPointsByAmount = Math.floor(remainingAmount * pointsExchangeRate.value / 100) * 100;
  maxPointsToUse.value = Math.max(0, Math.min(maxByPoints, maxPointsByAmount));
  if (pointsToUse.value > maxPointsToUse.value) {
    pointsToUse.value = maxPointsToUse.value;
  }
  calculatePointsDiscount();
}, { immediate: true });

// 处理积分输入变化
const handlePointsChange = (value) => {
  pointsToUse.value = value || 0;
  calculatePointsDiscount();
};

// 获取商品信息
const fetchProductDetail = async () => {
  try {
    loading.value = true
    const productId = route.query.productId
    console.log('获取商品详情，商品ID:', productId)
    
    if (!productId) {
      ElMessage.error('商品信息不完整')
      router.push('/')
      return
    }

    const response = await getProduct(productId)
    console.log('商品详情响应:', response)
    
    if (response.code === 200 && response.data) {
      product.value = response.data
      console.log('处理后的商品信息:', {
        id: product.value.id,
        title: product.value.title,
        price: product.value.price,
        sellerId: product.value.sellerId,
        imageList: product.value.imageList
      })
      quantity.value = parseInt(route.query.quantity) || 1
    } else {
      ElMessage.error(response.message || '获取商品详情失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取商品详情失败：', error)
    ElMessage.error('获取商品详情失败')
    router.push('/')
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

// 获取用户可用优惠券
const fetchAvailableCoupons = async () => {
  try {
    const token = store.state.token
    if (!token) {
      console.error('获取token失败')
      return
    }

    // 从token中解析用户ID
    const tokenWithoutBearer = token.replace(/^Bearer\s+/i, '')
    const base64Url = tokenWithoutBearer.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))

    const { id } = JSON.parse(jsonPayload)
    if (!id) {
      console.error('从token中解析用户ID失败')
      return
    }
    
    console.log('获取用户优惠券，用户ID:', id)
    const res = await getUserAvailableCoupons(id)
    console.log('获取优惠券响应:', res)
    
    if (res.code === 200) {
      availableCoupons.value = res.data
      console.log('可用优惠券列表:', availableCoupons.value)
    } else {
      ElMessage.error(res.message || '获取可用优惠券失败')
    }
  } catch (error) {
    console.error('获取可用优惠券失败:', error)
    ElMessage.error('获取可用优惠券失败')
  }
}

// 获取用户积分
const fetchUserPoints = async () => {
  try {
    const res = await getUserPoints();
    if (res.code === 200) {
      userPoints.value = res.data;
    } else {
      ElMessage.error(res.message || '获取用户积分失败');
    }
  } catch (error) {
    console.error('获取用户积分失败:', error);
    ElMessage.error('获取用户积分失败');
  }
}

// 监听选中的优惠券变化，计算折扣金额
watch(selectedCouponId, async (newCouponId) => {
  if (newCouponId) {
    try {
      const res = await getUserCouponById(newCouponId)
      if (res.code === 200 && res.data && res.data.coupon) {
        const userCouponDetail = res.data
        const minAmount = Number(userCouponDetail.coupon.minAmount)
        if (productTotalAmount.value >= minAmount) {
          discountAmount.value = Number(userCouponDetail.coupon.value)
        } else {
          ElMessage.warning('订单金额未达到此优惠券的最低使用要求')
          selectedCouponId.value = null
          discountAmount.value = 0
        }
      } else {
        ElMessage.error(res.message || '获取优惠券详情失败')
        selectedCouponId.value = null
        discountAmount.value = 0
      }
    } catch (error) {
      console.error('获取优惠券详情失败:', error)
      ElMessage.error('获取优惠券详情失败')
      selectedCouponId.value = null
      discountAmount.value = 0
    }
  } else {
    discountAmount.value = 0
  }
}, { immediate: true })

// 获取商品图片
const getProductImage = (product) => {
  if (!product) {
    return ''
  }
  
  // 尝试从不同属性获取图片
  const image = product.image || 
               product.images?.[0] || 
               product.imageList?.[0]
  
  if (!image) {
    return ''
  }
  
  // 如果图片URL不是以http开头，添加服务器地址
  if (!image.startsWith('http')) {
    return `http://localhost:8080/api${image}`
  }
  
  return image
}

// 处理图片加载错误
const handleImageError = (e) => {
  console.error('图片加载失败:', e)
}

// 提交订单
const handleSubmit = async () => {
  // 验证表单
  if (!addressForm.value.receiver) {
    ElMessage.warning('请输入收货人姓名')
    return
  }
  if (!addressForm.value.phone) {
    ElMessage.warning('请输入手机号码')
    return
  }
  if (!addressForm.value.address) {
    ElMessage.warning('请输入详细地址')
    return
  }
  
  // 验证余额
  if (paymentType.value === 1 && userBalance.value < product.value.price * quantity.value) {
    ElMessage.error('余额不足，请充值或选择其他支付方式')
    return
  }
  
  console.log('当前商品信息:', product.value)
  console.log('商品ID:', product.value.id)
  console.log('卖家ID:', product.value.sellerId)
  
  if (!product.value.sellerId) {
    ElMessage.error('商品信息不完整')
    return
  }
  
  try {
    submitting.value = true
    const orderData = {
      address: `${addressForm.value.receiver},${addressForm.value.phone},${addressForm.value.address}`,
      paymentType: paymentType.value,
      sellerId: product.value.sellerId,
      totalAmount: productTotalAmount.value, // 直接用Number
      items: [{
        productId: product.value.id,
        quantity: quantity.value,
        price: Number(product.value.price)
      }],
      couponId: selectedCouponId.value,
      pointsToUse: pointsToUse.value
    }
    
    console.log('提交的订单数据:', orderData)
    
    const res = await createOrder(orderData)
    console.log('创建订单响应:', res)
    
    if (res.code === 200) {
      ElMessage.success('订单创建成功')
      if (paymentType.value === 1) {
        // 如果是平台余额支付，直接跳转到支付页面
        router.push(`/order/pay/${res.data.id}`)
      } else {
        // 如果是线下交易，跳转到订单详情页
        router.push(`/order/${res.data.id}`)
      }
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    console.log('OrderConfirm mounted:', {
      storeUser: store.state.user,
      isLoggedIn: store.getters.isLoggedIn,
      token: store.state.token,
      localStorageToken: localStorage.getItem('token'),
      localStorageUser: localStorage.getItem('userInfo')
    })

    if (!store.getters.isLoggedIn) {
      console.log('用户未登录，重定向到登录页')
      ElMessage.warning('请先登录后再进行购买')
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
      return
    }

    if (!route.query.productId) {
      ElMessage.error('商品信息不完整')
      router.push('/')
      return
    }

    // 并行获取所需数据
    await Promise.all([
      fetchProductDetail(),
      fetchUserBalance(),
      fetchAvailableCoupons(),
      fetchUserPoints()
    ])
  } catch (error) {
    console.error('初始化订单确认页面失败:', error)
    ElMessage.error('加载失败，请稍后重试')
    router.push('/')
  }
})
</script>

<style scoped>
.order-confirm {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
}

.product-info {
  display: flex;
  align-items: center;
  padding: 15px 0;
}

.product-image {
  width: 120px;
  height: 120px;
  margin-right: 20px;
  border-radius: 4px;
}

.product-details {
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

.amount-info {
  padding: 15px 0;
}

.amount-item {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.amount-item.total {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.amount-item .label {
  margin-right: 10px;
}

.submit-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 10px;
}

.payment-icon {
  font-size: 20px;
}

.payment-name {
  font-size: 16px;
  color: #303133;
}

.payment-balance {
  color: #909399;
  font-size: 14px;
}

.payment-tips {
  margin-top: 15px;
}

.payment-tips p {
  margin: 5px 0;
  font-size: 14px;
  color: #666;
}

.points-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.available-points {
  font-size: 14px;
  color: #606266;
}

.points-form .el-form-item {
  margin-bottom: 0;
}

.points-discount-text {
  font-size: 14px;
  color: #67C23A;
  margin-left: 10px;
}

.points-tips {
  margin-top: 10px;
}
</style> 