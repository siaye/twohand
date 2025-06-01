<template>
  <div class="shopping-cart">
    <div class="cart-header">
      <h2>我的购物车</h2>
      <el-button type="primary" plain @click="$router.push('/')">
        <el-icon><ArrowLeft /></el-icon>
        继续购物
      </el-button>
    </div>
    
    <div v-if="cartList.length === 0" class="empty-cart">
      <el-empty description="购物车是空的">
        <template #image>
          <el-icon :size="60" class="empty-icon"><ShoppingCart /></el-icon>
        </template>
        <el-button type="primary" @click="$router.push('/')">去购物</el-button>
      </el-empty>
    </div>
    
    <div v-else class="cart-content">
      <el-card class="cart-card">
        <el-table 
          :data="cartList" 
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa' }"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          
          <el-table-column label="商品信息" min-width="400">
            <template #default="{ row }">
              <div class="product-info">
                <el-image 
                  :src="row.productImage" 
                  :preview-src-list="row.productImage ? [row.productImage] : []"
                  fit="cover"
                  class="product-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div class="product-details">
                  <h3 class="product-name" @click="handleProductClick(row.productId)">
                    {{ row.productTitle }}
                  </h3>
                  <p class="seller">卖家：{{ row.sellerName || '未知卖家' }}</p>
                  <p class="price">¥{{ row.price }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="数量" width="150">
            <template #default="{ row }">
              <el-input-number 
                v-model="row.quantity" 
                :min="1" 
                :max="row.stock"
                size="small"
                :disabled="row.stock <= 0"
                @change="(value) => handleQuantityChange(row.id, value)"
              />
              <div class="stock-info" v-if="row.stock <= 5 && row.stock > 0">
                仅剩 {{ row.stock }} 件
              </div>
              <div class="stock-info error" v-if="row.stock <= 0">
                已售罄
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="小计" width="150">
            <template #default="{ row }">
              <span class="subtotal">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button 
                type="danger" 
                size="small"
                :icon="Delete"
                circle
                @click="handleRemove(row.id)"
              />
            </template>
          </el-table-column>
        </el-table>
        
        <div class="cart-footer">
          <div class="left">
            <el-button type="danger" plain @click="handleClear">
              <el-icon><Delete /></el-icon>
              清空购物车
            </el-button>
            <span class="selected-count">
              已选择 {{ selectedCount }} 件商品
            </span>
          </div>
          <div class="right">
            <div class="price-info">
              <span class="total-label">总计：</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button 
              type="primary" 
              size="large"
              :disabled="selectedCount === 0"
              @click="handleCheckout"
            >
              结算 ({{ selectedCount }})
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, ShoppingCart, Delete, Picture } from '@element-plus/icons-vue'
import { getCartList, updateCartItemQuantity, removeFromCart, clearCart } from '@/api/cart'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartList = ref([])
const selectedItems = ref([])

// 计算总价
const totalPrice = computed(() => {
  return selectedItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

// 计算选中商品数量
const selectedCount = computed(() => {
  return selectedItems.value.length
})

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}

// 获取购物车列表
const fetchCartList = async () => {
  try {
    const res = await getCartList()
    if (res.code === 200) {
      cartList.value = res.data.map(item => {
        let productImage = item.productImage
        if (productImage) {
          if (!productImage.startsWith('http')) {
            if (!productImage.startsWith('/api')) {
              productImage = '/api' + (productImage.startsWith('/') ? productImage : '/' + productImage)
            }
            productImage = 'http://localhost:8080' + productImage
          }
        }
        return {
          ...item,
          productImage,
          sellerName: item.sellerName || '未知卖家'
        }
      })
    }
  } catch (error) {
    console.error('获取购物车列表失败:', error)
    ElMessage.error('获取购物车列表失败')
  }
}

// 更新商品数量
const handleQuantityChange = async (cartId, quantity) => {
  try {
    const res = await updateCartItemQuantity({
      cartId,
      quantity
    })
    if (res.code === 200) {
      ElMessage.success('更新成功')
      fetchCartList()
    }
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新数量失败')
  }
}

// 删除商品
const handleRemove = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const res = await removeFromCart(cartId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCartList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 清空购物车
const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    
    const res = await clearCart()
    if (res.code === 200) {
      ElMessage.success('清空成功')
      fetchCartList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败:', error)
      ElMessage.error('清空失败')
    }
  }
}

// 点击商品名称跳转到商品详情
const handleProductClick = (productId) => {
  router.push(`/product/${productId}`)
}

// 结算
const handleCheckout = () => {
  // TODO: 实现结算功能
  ElMessage.info('结算功能开发中...')
}

onMounted(() => {
  fetchCartList()
})
</script>

<style scoped>
.shopping-cart {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.empty-cart {
  margin-top: 100px;
  text-align: center;
}

.empty-icon {
  color: #909399;
}

.cart-content {
  margin-top: 20px;
}

.cart-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.product-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.product-details {
  flex: 1;
}

.product-name {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
  cursor: pointer;
  transition: color 0.3s;
}

.product-name:hover {
  color: #409EFF;
}

.seller {
  margin: 5px 0;
  font-size: 14px;
  color: #909399;
}

.price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.stock-info {
  margin-top: 5px;
  font-size: 12px;
  color: #e6a23c;
}

.stock-info.error {
  color: #f56c6c;
}

.subtotal {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.cart-footer {
  margin-top: 20px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.selected-count {
  color: #606266;
  font-size: 14px;
}

.right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  color: #606266;
}

.total-price {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style> 