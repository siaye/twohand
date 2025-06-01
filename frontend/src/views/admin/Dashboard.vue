<template>
  <div class="dashboard">
    <!-- 数据概览卡片 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
              <el-tag size="small" type="success">实时</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ statistics.totalUsers }}</div>
            <div class="trend">
              <span>较昨日</span>
              <span :class="statistics.userGrowth >= 0 ? 'up' : 'down'">
                {{ statistics.userGrowth >= 0 ? '+' : '' }}{{ statistics.userGrowth }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>总商品数</span>
              <el-tag size="small" type="warning">实时</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ statistics.totalProducts }}</div>
            <div class="trend">
              <span>较昨日</span>
              <span :class="statistics.productGrowth >= 0 ? 'up' : 'down'">
                {{ statistics.productGrowth >= 0 ? '+' : '' }}{{ statistics.productGrowth }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>总订单数</span>
              <el-tag size="small" type="info">实时</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="number">{{ statistics.totalOrders }}</div>
            <div class="trend">
              <span>较昨日</span>
              <span :class="statistics.orderGrowth >= 0 ? 'up' : 'down'">
                {{ statistics.orderGrowth >= 0 ? '+' : '' }}{{ statistics.orderGrowth }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <template #header>
            <div class="card-header">
              <span>总交易额</span>
              <el-tag size="small" type="danger">实时</el-tag>
            </div>
          </template>
          <div class="card-content">
            <div class="number">¥{{ statistics.totalAmount }}</div>
            <div class="trend">
              <span>较昨日</span>
              <span :class="statistics.amountGrowth >= 0 ? 'up' : 'down'">
                {{ statistics.amountGrowth >= 0 ? '+' : '' }}{{ statistics.amountGrowth }}%
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-container">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
              <el-radio-group v-model="userChartType" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart" ref="userChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>商品增长趋势</span>
              <el-radio-group v-model="orderChartType" size="small">
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
                <el-radio-button label="year">全年</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart" ref="orderChartRef"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 待办事项 -->
    <el-row :gutter="20" class="todo-container">
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待审核商品</span>
              <el-button type="primary" link @click="$router.push('/admin/audit')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="pendingProducts" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column label="商品图片" width="120">
              <template #default="{ row }">
                <el-image 
                  :src="row.image" 
                  :preview-src-list="row.imageList"
                  fit="cover"
                  style="width: 80px; height: 80px"
                />
              </template>
            </el-table-column>
            <el-table-column prop="title" label="商品标题" min-width="200" />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                ¥{{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="120" />
            <el-table-column prop="sellerName" label="卖家" width="120" />
            <el-table-column prop="createdAt" label="发布时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleAudit(row)">审核</el-button>
                <el-button type="primary" link @click="handleView(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="todo-card">
          <template #header>
            <div class="card-header">
              <span>订单管理</span>
              <el-button type="primary" link @click="$router.push('/admin/orders')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="pendingOrders" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="productTitle" label="商品" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleOrder(row)">
                  处理
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="商品审核"
      width="500px"
    >
      <el-form :model="auditForm" label-width="100px">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核原因" v-if="auditForm.auditStatus === 2">
          <el-input
            v-model="auditForm.auditReason"
            type="textarea"
            :rows="3"
            placeholder="请输入审核不通过的原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getDashboardData, getPendingProducts, getPendingOrders, auditProduct } from '@/api/admin'
import { formatDate } from '@/utils/format'

const router = useRouter()
const userChartRef = ref(null)
const orderChartRef = ref(null)
let userChart = null
let orderChart = null

const userChartType = ref('week')
const orderChartType = ref('week')

// 统计数据
const statistics = ref({
  totalUsers: 0,
  userGrowth: 0,
  totalProducts: 0,
  productGrowth: 0
})

// 待办数据
const pendingProducts = ref([])
const pendingOrders = ref([])
const loading = ref(false)
const submitting = ref(false)
const pendingCount = ref(0)
const todayNewCount = ref(0)
const totalCount = ref(0)
const userCount = ref(0)

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = ref({
  id: null,
  auditStatus: 1,
  auditReason: ''
})

// 获取仪表盘数据
const fetchDashboardData = async () => {
  try {
    const res = await getDashboardData()
    statistics.value = res.data.statistics
    
    // 初始化图表
    initCharts(res.data.charts)
  } catch (error) {
    console.error('获取仪表盘数据失败:', error)
    ElMessage.error('获取仪表盘数据失败')
  }
}

// 获取待审核商品
const fetchPendingProducts = async () => {
  try {
    loading.value = true
    const res = await getPendingProducts(1, 5)  // 只获取前5条
    pendingProducts.value = res.data.records
    pendingCount.value = res.data.total
  } catch (error) {
    ElMessage.error('获取待审核商品失败')
  } finally {
    loading.value = false
  }
}

// 获取待处理订单
const fetchPendingOrders = async () => {
  try {
    const res = await getPendingOrders()
    if (res.code === 200) {
      pendingOrders.value = res.data
    } else {
      throw new Error(res.message || '获取待处理订单失败')
    }
  } catch (error) {
    console.error('获取待处理订单失败:', error)
    ElMessage.error(error.message || '获取待处理订单失败')
  }
}

// 初始化图表
const initCharts = (data) => {
  // 确保 DOM 元素已经渲染
  nextTick(async () => {
    try {
      // 用户增长趋势图
      if (userChartRef.value) {
        // 销毁旧的图表实例
        if (userChart) {
          userChart.dispose()
          userChart = null
        }
        
        // 创建新的图表实例
        userChart = echarts.init(userChartRef.value)
        const userOption = {
          tooltip: {
            trigger: 'axis',
            formatter: '{b}: {c} 人'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: data.userTrend.map(item => item.date),
            boundaryGap: false
          },
          yAxis: {
            type: 'value',
            name: '用户数'
          },
          series: [{
            name: '用户增长',
            data: data.userTrend.map(item => item.value),
            type: 'line',
            smooth: true,
            areaStyle: {
              opacity: 0.3
            },
            itemStyle: {
              color: '#409EFF'
            }
          }]
        }
        userChart.setOption(userOption)
      }

      // 商品增长趋势图
      if (orderChartRef.value) {
        // 销毁旧的图表实例
        if (orderChart) {
          orderChart.dispose()
          orderChart = null
        }
        
        // 创建新的图表实例
        orderChart = echarts.init(orderChartRef.value)
        const orderOption = {
          tooltip: {
            trigger: 'axis',
            formatter: '{b}: {c} 件'
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          xAxis: {
            type: 'category',
            data: data.productTrend.map(item => item.date)
          },
          yAxis: {
            type: 'value',
            name: '商品数'
          },
          series: [{
            name: '商品增长',
            data: data.productTrend.map(item => item.value),
            type: 'bar',
            itemStyle: {
              color: '#67C23A'
            }
          }]
        }
        orderChart.setOption(orderOption)
      }
    } catch (error) {
      console.error('初始化图表失败:', error)
      ElMessage.error('初始化图表失败')
    }
  })
}

// 监听图表类型变化
watch([userChartType, orderChartType], () => {
  console.log('【Dashboard】图表类型变化，重新获取数据')
  fetchDashboardData()
})

// 处理审核
const handleAudit = (row) => {
  auditForm.value = {
    id: row.id,
    auditStatus: 1,
    auditReason: ''
  }
  auditDialogVisible.value = true
}

// 处理订单
const handleOrder = (order) => {
  router.push(`/admin/orders?id=${order.id}`)
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const statusMap = {
    PENDING_PAYMENT: 'warning',
    PENDING_SHIPMENT: 'primary',
    PENDING_RECEIPT: 'success',
    COMPLETED: 'info',
    CANCELLED: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const statusMap = {
    PENDING_PAYMENT: '待付款',
    PENDING_SHIPMENT: '待发货',
    PENDING_RECEIPT: '待收货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

// 提交审核
const submitAudit = async () => {
  if (auditForm.value.auditStatus === 2 && !auditForm.value.auditReason) {
    ElMessage.warning('请输入审核不通过的原因')
    return
  }

  try {
    submitting.value = true
    await auditProduct(auditForm.value.id, auditForm.value.auditStatus, auditForm.value.auditReason)
    ElMessage.success('审核成功')
    auditDialogVisible.value = false
    fetchPendingProducts()
  } catch (error) {
    ElMessage.error('审核失败')
  } finally {
    submitting.value = false
  }
}

// 查看商品详情
const handleView = (row) => {
  router.push(`/product/${row.id}`)
}

// 监听窗口大小变化
const handleResize = () => {
  console.log('【Dashboard】窗口大小变化，调整图表大小')
  userChart?.resize()
  orderChart?.resize()
}

onMounted(() => {
  console.log('【Dashboard】组件挂载，初始化数据')
  fetchDashboardData()
  fetchPendingProducts()
  fetchPendingOrders()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  console.log('【Dashboard】组件卸载，清理资源')
  window.removeEventListener('resize', handleResize)
  if (userChart) {
    userChart.dispose()
    userChart = null
  }
  if (orderChart) {
    orderChart.dispose()
    orderChart = null
  }
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.data-overview {
  margin-bottom: 20px;
}

.data-card {
  height: 160px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100px;
}

.number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.trend {
  font-size: 14px;
  color: #909399;
}

.trend .up {
  color: #67c23a;
  margin-left: 5px;
}

.trend .down {
  color: #f56c6c;
  margin-left: 5px;
}

.charts-container {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart {
  height: 300px;
}

.todo-container {
  margin-bottom: 20px;
}

.todo-card {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 