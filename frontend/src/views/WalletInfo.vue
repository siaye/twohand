<template>
  <div class="wallet-info-container">
    <h2>我的钱包</h2>
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <h2>我的钱包</h2>
        </div>
      </template>

      <!-- 积分显示 -->
      <points-display />

      <div class="balance-section">
        <div class="balance">¥ {{ balance }}</div>
        <el-button type="primary" @click="openRechargeDialog">充值</el-button>
      </div>
    </el-card>

    <el-card class="box-card transaction-card">
      <template #header>
        <div class="clearfix">
          <span>交易记录</span>
          <div class="search-section">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索订单号/金额"
              class="search-input"
              clearable
              @clear="handleSearch"
              @keyup.enter="handleSearch">
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
            <el-select v-model="transactionType" placeholder="交易类型" clearable @change="handleSearch">
              <el-option label="全部" value=""/>
              <el-option label="充值" :value="1"/>
              <el-option label="提现" :value="2"/>
              <el-option label="商品购买" :value="3"/>
              <el-option label="商品出售收入" :value="4"/>
              <el-option label="退款收入" :value="5"/>
              <el-option label="退款支出" :value="6"/>
              <el-option label="手续费收入" :value="7"/>
            </el-select>
          </div>
        </div>
      </template>
      <el-table :data="transactions" v-loading="loading" style="width: 100%">
        <el-table-column prop="typeText" label="交易类型" width="120"/>
        <el-table-column prop="amountText" label="金额" width="120">
          <template #default="{ row }">
            <span :class="{'income': row.amount > 0, 'expense': row.amount < 0}">
              {{ row.amount > 0 ? '+' : '-' }}¥{{ Math.abs(row.amount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单" width="120"/>
        <el-table-column prop="productName" label="商品名称" min-width="150"/>
        <el-table-column prop="remark" label="备注" min-width="150"/>
        <el-table-column prop="createdAtText" label="交易时间" width="180"/>
      </el-table>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalItems">
      </el-pagination>
    </el-card>

    <!-- 充值对话框 -->
    <el-dialog title="充值" v-model="rechargeDialogVisible" width="30%" @close="resetRechargeForm">
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="80px">
        <el-form-item label="充值金额" prop="amount">
          <el-input v-model.number="rechargeForm.amount" placeholder="请输入充值金额"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rechargeDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleRecharge">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWalletBalance, recharge, getTransactions } from '@/api/wallet'
import { formatTime } from '@/utils/format'
import PointsDisplay from '@/components/wallet/PointsDisplay.vue'

const balance = ref(0)
const transactions = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const totalItems = ref(0)
const searchKeyword = ref('')
const transactionType = ref('')
const rechargeDialogVisible = ref(false)
const rechargeFormRef = ref(null)
const rechargeForm = ref({
  amount: null
})

const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', message: '充值金额必须为数字' },
    { validator: (rule, value, callback) => {
        if (value <= 0) {
          callback(new Error('充值金额必须大于0'))
        } else {
          callback()
        }
      }, trigger: 'blur'
    }
  ]
}

// 获取钱包余额
const fetchBalance = async () => {
  try {
    const res = await getWalletBalance()
    if (res.code === 200) {
      balance.value = res.data
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取钱包余额失败: ' + error.message)
  }
}

// 获取交易记录
const fetchTransactions = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value,
      type: transactionType.value
    }
    const res = await getTransactions(params)
    if (res.code === 200) {
      transactions.value = res.data.records
      totalItems.value = res.data.total
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('获取交易记录失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 处理分页大小变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchTransactions()
}

// 处理页码变化
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  fetchTransactions()
}

// 打开充值对话框
const openRechargeDialog = () => {
  rechargeDialogVisible.value = true
}

// 重置充值表单
const resetRechargeForm = () => {
  rechargeFormRef.value?.resetFields()
  rechargeForm.value.amount = null
}

// 处理充值
const handleRecharge = async () => {
  if (!rechargeFormRef.value) return
  
  try {
    await rechargeFormRef.value.validate()
    const res = await recharge(rechargeForm.value.amount)
    if (res.code === 200) {
      ElMessage.success('充值成功')
      rechargeDialogVisible.value = false
      fetchBalance()
      fetchTransactions()
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('充值失败: ' + error.message)
    }
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchTransactions()
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    1: 'success',  // 成功
    2: 'danger',   // 失败
    3: 'warning'   // 处理中
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  fetchBalance()
  fetchTransactions()
})
</script>

<style scoped>
.wallet-info-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.balance-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
}

.balance {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.transaction-card .el-pagination {
  margin-top: 20px;
  text-align: right;
}

.search-section {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-input {
  width: 300px;
}

.income {
  color: #67C23A;
}

.expense {
  color: #F56C6C;
}
</style> 