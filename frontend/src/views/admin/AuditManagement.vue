<template>
  <div class="audit-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>商品审核管理</span>
        </div>
      </template>
      
      <!-- 商品列表 -->
      <el-table :data="products" style="width: 100%" v-loading="loading">
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
      
      <!-- 分页 -->
      <div class="pagination">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPendingProducts, auditProduct } from '@/api/product'
import { formatDate } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = ref({
  id: null,
  auditStatus: 1,
  auditReason: ''
})

// 获取待审核商品列表
const fetchProducts = async () => {
  try {
    loading.value = true
    const res = await getPendingProducts(currentPage.value, pageSize.value)
    products.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    ElMessage.error('获取待审核商品列表失败')
  } finally {
    loading.value = false
  }
}

// 处理审核
const handleAudit = (row) => {
  auditForm.value = {
    id: row.id,
    auditStatus: 1,
    auditReason: ''
  }
  auditDialogVisible.value = true
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
    fetchProducts()
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

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchProducts()
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.audit-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
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