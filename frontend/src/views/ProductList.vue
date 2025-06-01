<template>
  <div class="product-list">
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" label-width="80px" class="filter-form">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="分类">
              <el-select v-model="filterForm.categoryId" placeholder="选择分类" clearable>
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="品牌">
              <el-select v-model="filterForm.brand" placeholder="选择品牌" clearable>
                <el-option
                  v-for="brand in brandOptions"
                  :key="brand"
                  :label="brand"
                  :value="brand"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="成色">
              <el-select v-model="filterForm.condition" placeholder="选择成色" clearable>
                <el-option label="全新" value="全新" />
                <el-option label="九成新" value="九成新" />
                <el-option label="八成新" value="八成新" />
                <el-option label="七成新" value="七成新" />
                <el-option label="六成新及以下" value="六成新及以下" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="价格区间">
              <el-input-number
                v-model="filterForm.minPrice"
                :min="0"
                :precision="2"
                placeholder="最低价"
                style="width: 45%"
              />
              <span class="price-separator">-</span>
              <el-input-number
                v-model="filterForm.maxPrice"
                :min="0"
                :precision="2"
                placeholder="最高价"
                style="width: 45%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="标签">
              <el-select
                v-model="filterForm.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入标签"
              >
                <el-option
                  v-for="tag in tagOptions"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="位置">
              <el-input v-model="filterForm.location" placeholder="输入位置" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="排序">
              <el-select v-model="filterForm.sortBy" placeholder="排序方式">
                <el-option label="最新发布" value="createTime" />
                <el-option label="价格从低到高" value="priceAsc" />
                <el-option label="价格从高到低" value="priceDesc" />
                <el-option label="销量优先" value="sales" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">搜索</el-button>
              <el-button @click="resetFilter">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 商品列表 -->
    <div class="product-grid">
      <el-row :gutter="20">
        <el-col
          v-for="product in productList"
          :key="product.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
          :xl="4"
        >
          <el-card class="product-card" @click="goToDetail(product.id)">
            <div class="product-image">
              <el-image
                :src="product.imageList[0]"
                fit="cover"
                :preview-src-list="product.imageList"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-tags" v-if="product.tags">
                <el-tag
                  v-for="tag in product.tags.split(',')"
                  :key="tag"
                  size="small"
                  class="tag"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-title">{{ product.title }}</h3>
              <div class="product-price">
                <span class="current-price">¥{{ product.price }}</span>
                <span class="original-price" v-if="product.originalPrice">
                  ¥{{ product.originalPrice }}
                </span>
              </div>
              <div class="product-meta">
                <span class="condition">{{ product.condition }}</span>
                <span class="location">{{ product.location }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'
import { getCategories } from '@/api/category'

const router = useRouter()
const productList = ref([])
const categories = ref([])
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 品牌选项
const brandOptions = ref([
  'Apple',
  'Samsung',
  'Huawei',
  'Xiaomi',
  'Nike',
  'Adidas',
  '其他'
])

// 标签选项
const tagOptions = ref([
  '热门',
  '新品',
  '促销',
  '限时',
  '包邮',
  '正品',
  '二手',
  '闲置'
])

// 筛选表单
const filterForm = reactive({
  categoryId: null,
  brand: null,
  condition: null,
  minPrice: null,
  maxPrice: null,
  tags: [],
  location: '',
  sortBy: 'createTime'
})

// 获取商品列表
const fetchProductList = async () => {
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...filterForm
    }
    const res = await getProductList(params)
    if (res.code === 200) {
      productList.value = res.data.records
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取商品列表失败')
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
    ElMessage.error('获取商品列表失败')
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 200) {
      categories.value = res.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProductList()
}

// 重置筛选
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    if (Array.isArray(filterForm[key])) {
      filterForm[key] = []
    } else {
      filterForm[key] = null
    }
  })
  filterForm.sortBy = 'createTime'
  handleSearch()
}

// 处理分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchProductList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchProductList()
}

// 跳转到商品详情
const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  fetchCategories()
  fetchProductList()
})
</script>

<style scoped>
.product-list {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: 0;
}

.price-separator {
  margin: 0 5px;
  color: #666;
}

.product-grid {
  margin-bottom: 20px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.product-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.product-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
}

.tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.product-info {
  padding: 10px;
}

.product-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-bottom: 10px;
}

.current-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 12px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 
</style> 