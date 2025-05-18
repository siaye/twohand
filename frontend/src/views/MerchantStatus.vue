<template>
  <div class="merchant-status-container">
    <el-card class="status-card">
      <template #header>
        <h2>商家状态</h2>
      </template>
      <div v-if="merchantInfo">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="商家名称">
            {{ merchantInfo.name }}
          </el-descriptions-item>
          <el-descriptions-item label="营业执照号">
            {{ merchantInfo.licenseNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(merchantInfo.status)">
              {{ getStatusText(merchantInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" v-if="merchantInfo.reviewComment">
            {{ merchantInfo.reviewComment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <div v-else class="no-merchant">
        <p>您还不是商家</p>
        <el-button type="primary" @click="showApplyDialog">申请成为商家</el-button>
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="申请成为商家" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="商家名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商家名称"></el-input>
        </el-form-item>
        <el-form-item label="营业执照号" prop="licenseNumber">
          <el-input v-model="form.licenseNumber" placeholder="请输入营业执照号"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApply">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMerchantInfo, applyMerchant } from '@/api/merchant'

const merchantInfo = ref(null)
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  name: '',
  licenseNumber: ''
})

const rules = {
  name: [
    { required: true, message: '请输入商家名称', trigger: 'blur' }
  ],
  licenseNumber: [
    { required: true, message: '请输入营业执照号', trigger: 'blur' }
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

const showApplyDialog = () => {
  dialogVisible.value = true
}

const handleApply = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await applyMerchant(form)
        ElMessage.success('申请提交成功')
        dialogVisible.value = false
        loadMerchantInfo()
      } catch (error) {
        ElMessage.error(error.message || '申请提交失败')
      }
    }
  })
}

const loadMerchantInfo = async () => {
  try {
    const res = await getMerchantInfo()
    merchantInfo.value = res
  } catch (error) {
    console.error('获取商家信息失败:', error)
  }
}

onMounted(() => {
  loadMerchantInfo()
})
</script>

<style scoped>
.merchant-status-container {
  padding: 20px;
}

.status-card {
  max-width: 800px;
  margin: 0 auto;
}

.status-card :deep(.el-card__header) {
  text-align: center;
}

.no-merchant {
  text-align: center;
  padding: 40px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 