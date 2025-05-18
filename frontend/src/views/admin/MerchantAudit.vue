<template>
  <div class="merchant-audit-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>商家审核</h2>
          <el-radio-group v-model="statusFilter" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="0">待审核</el-radio-button>
            <el-radio-button label="1">已通过</el-radio-button>
            <el-radio-button label="2">已拒绝</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="merchantList" style="width: 100%" v-loading="loading">
        <el-table-column prop="name" label="商家名称" />
        <el-table-column prop="licenseNumber" label="营业执照号" />
        <el-table-column prop="applyTime" label="申请时间">
          <template #default="scope">
            {{ formatDate(scope.row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="small"
              @click="handleAudit(scope.row, 1)"
            >
              通过
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              type="danger"
              size="small"
              @click="handleAudit(scope.row, 2)"
            >
              拒绝
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="showDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="商家详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="商家名称">
          {{ currentMerchant?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="营业执照号">
          {{ currentMerchant?.licenseNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDate(currentMerchant?.applyTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentMerchant?.status)">
            {{ getStatusText(currentMerchant?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" v-if="currentMerchant?.reviewComment">
          {{ currentMerchant.reviewComment }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="auditDialogVisible" :title="auditType === 1 ? '通过审核' : '拒绝审核'" width="500px">
      <el-form :model="auditForm" :rules="auditRules" ref="auditFormRef" label-width="100px">
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="auditForm.reviewComment"
            type="textarea"
            :rows="3"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantList, auditMerchant } from '@/api/merchant'

const loading = ref(false)
const merchantList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')
const dialogVisible = ref(false)
const auditDialogVisible = ref(false)
const currentMerchant = ref(null)
const auditFormRef = ref(null)
const auditType = ref(1)

const auditForm = reactive({
  reviewComment: ''
})

const auditRules = {
  reviewComment: [
    { required: true, message: '请输入审核意见', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const types = {
    0: 'info',    // 待审核
    1: 'success', // 已通过
    2: 'danger'   // 已拒绝
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return texts[status] || '未知状态'
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const loadMerchantList = async () => {
  loading.value = true
  try {
    const res = await getMerchantList({
      page: currentPage.value,
      size: pageSize.value,
      status: statusFilter.value
    })
    merchantList.value = res.list
    total.value = res.total
  } catch (error) {
    ElMessage.error('获取商家列表失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadMerchantList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  loadMerchantList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadMerchantList()
}

const showDetail = (merchant) => {
  currentMerchant.value = merchant
  dialogVisible.value = true
}

const handleAudit = (merchant, type) => {
  currentMerchant.value = merchant
  auditType.value = type
  auditForm.reviewComment = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (!auditFormRef.value) return
  
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await auditMerchant({
          merchantId: currentMerchant.value.id,
          status: auditType.value,
          reviewComment: auditForm.reviewComment
        })
        ElMessage.success('审核操作成功')
        auditDialogVisible.value = false
        loadMerchantList()
      } catch (error) {
        ElMessage.error(error.message || '审核操作失败')
      }
    }
  })
}

onMounted(() => {
  loadMerchantList()
})
</script>

<style scoped>
.merchant-audit-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 