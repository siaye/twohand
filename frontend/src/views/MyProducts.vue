<template>
  <div class="my-products">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>我的商品</h2>
          <el-button type="primary" @click="$router.push('/publish')">
            发布商品
          </el-button>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="filter-container">
        <el-input
          v-model="searchQuery"
          placeholder="搜索商品"
          class="search-input"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>

        <el-select v-model="statusFilter" placeholder="商品状态" @change="handleSearch">
          <el-option label="全部" :value="null" />
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </div>

      <!-- 商品列表 -->
      <el-table
        v-loading="loading"
        :data="products"
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.image"
              :preview-src-list="[row.image]"
              fit="cover"
              class="product-image"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品标题" min-width="200" />
        <el-table-column prop="price" label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button-group>
              <el-button
                size="small"
                type="primary"
                @click="handleEdit(row)"
              >
                编辑
              </el-button>
              <el-button
                size="small"
                :type="row.status === 1 ? 'warning' : 'success'"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
    >
      <span>确定要删除这个商品吗？此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">
            确定删除
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyProducts, deleteProduct, updateProduct } from '@/api/product'

const router = useRouter()
const loading = ref(false)
const deleteLoading = ref(false)
const deleteDialogVisible = ref(false)
const currentProduct = ref(null)

const searchQuery = ref('')
const statusFilter = ref(null)
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    console.log('【MyProducts】获取商品列表：', {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      status: statusFilter.value
    })
    
    const res = await getMyProducts({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value,
      status: statusFilter.value
    })
    
    console.log('【MyProducts】获取成功：', res)
    
    if (res.code === 200) {
     // 如果后端返回的是分页数据
     if (res.data.records) {
       products.value = res.data.records
       total.value = res.data.total
     } 
     // 如果后端返回的是数组
     else {
       products.value = res.data
       total.value = res.data.length
     }
   }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchProducts()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchProducts()
}

// 处理编辑
const handleEdit = (row) => {
  router.push(`/publish?id=${row.id}`)
}

// 处理状态切换
const handleToggleStatus = async (row) => {
  try {
    await updateProduct(row.id, {
      ...row,
      status: row.status === 1 ? 0 : 1
    })
    ElMessage.success('状态更新成功')
    fetchProducts()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  }
}

// 处理删除
const handleDelete = (row) => {
  currentProduct.value = row
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!currentProduct.value) return

  deleteLoading.value = true
  try {
    await deleteProduct(currentProduct.value.id)
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    fetchProducts()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  } finally {
    deleteLoading.value = false
  }
}

// 格式化时间
const formatTime = (time) => {
  const date = new Date(time)
  return date.toLocaleString()
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'  // 待审核
    case 1: return 'success'  // 在售
    case 2: return 'info'     // 已下架
    case 3: return 'danger'   // 已售罄
    default: return 'info'
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.my-products {
  padding: 20px;
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

.filter-container {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.search-input {
  width: 300px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
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