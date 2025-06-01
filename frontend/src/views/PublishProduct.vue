<template>
  <div class="publish-product">
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <h2>发布商品</h2>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="publish-form"
      >
        <!-- 基本信息 -->
        <el-form-item label="商品标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品标题" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择商品分类">
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品品牌" prop="brand">
          <el-input v-model="form.brand" placeholder="请输入商品品牌" />
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0.01"
            :precision="2"
            :step="0.1"
            placeholder="请输入商品价格"
          />
        </el-form-item>

        <el-form-item label="商品原价" prop="originalPrice">
          <el-input-number
            v-model="form.originalPrice"
            :min="0.01"
            :precision="2"
            :step="0.1"
            placeholder="请输入商品原价"
          />
        </el-form-item>

        <el-form-item label="商品尺寸" prop="size">
          <el-input v-model="form.size" placeholder="请输入商品尺寸" />
        </el-form-item>

        <el-form-item label="商品颜色" prop="color">
          <el-input v-model="form.color" placeholder="请输入商品颜色" />
        </el-form-item>

        <el-form-item label="商品成色" prop="condition">
          <el-select v-model="form.condition" placeholder="请选择商品成色">
            <el-option label="全新" value="全新" />
            <el-option label="九成新" value="九成新" />
            <el-option label="八成新" value="八成新" />
            <el-option label="七成新" value="七成新" />
            <el-option label="六成新及以下" value="六成新及以下" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入商品位置" />
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number
            v-model="form.stock"
            :min="0"
            :precision="0"
            placeholder="请输入商品库存"
          />
        </el-form-item>

        <el-form-item label="是否议价" prop="isNegotiable">
          <el-switch
            v-model="form.isNegotiable"
            active-text="允许议价"
            inactive-text="不允许议价"
          />
        </el-form-item>

        <el-form-item label="商品标签" prop="tags">
          <el-select
            v-model="form.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入商品标签"
          >
            <el-option
              v-for="tag in tagOptions"
              :key="tag"
              :label="tag"
              :value="tag"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>

        <el-form-item label="商品图片" prop="images">
          <el-upload
            class="product-uploader"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :data="{ type: 'product' }"
            :file-list="fileList"
            :before-upload="beforeUpload"
            :on-success="handleSuccess"
            :on-error="handleError"
            :on-remove="handleRemove"
            multiple
            list-type="picture-card"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <el-icon><Plus /></el-icon>
            <div class="upload-tip">点击上传</div>
          </el-upload>
          <div class="upload-tips">
            <p class="tip-title">上传要求：</p>
            <ul class="tip-list">
              <li>支持 jpg、jpeg、png、gif 格式</li>
              <li>图片大小不超过 4000KB</li>
              <li>建议尺寸：800x800 像素以上</li>
              <li>最多上传 9 张图片</li>
            </ul>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            发布商品
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createProduct, uploadProductImage } from '@/api/product'
import { getCategories } from '@/api/category'

const router = useRouter()
const store = useStore()
const formRef = ref(null)
const submitting = ref(false)
const fileList = ref([])
const categories = ref([])

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

// 表单数据
const form = reactive({
  title: '',
  categoryId: null,
  brand: '',
  price: null,
  originalPrice: null,
  size: '',
  color: '',
  condition: '',
  location: '',
  stock: 0,
  isNegotiable: false,
  tags: [],
  description: '',
  images: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入商品标题', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  brand: [
    { required: true, message: '请输入商品品牌', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  originalPrice: [
    { required: true, message: '请输入商品原价', trigger: 'blur' }
  ],
  size: [
    { required: true, message: '请输入商品尺寸', trigger: 'blur' }
  ],
  color: [
    { required: true, message: '请输入商品颜色', trigger: 'blur' }
  ],
  condition: [
    { required: true, message: '请选择商品成色', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入商品位置', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ],
  isNegotiable: [
    { required: true, message: '请选择是否允许议价', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入商品描述', trigger: 'blur' }
  ],
  images: [
    { required: true, message: '请上传商品图片', trigger: 'change' }
  ]
}

// 上传相关
const uploadUrl = '/api/product/upload'
const uploadHeaders = computed(() => ({
  Authorization: store.state.token
}))

// 上传前检查
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt4M = file.size / 1024 / 1024 < 4

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt4M) {
    ElMessage.error('图片大小不能超过 4MB！')
    return false
  }
  return true
}

// 上传成功处理
const handleSuccess = (response, file) => {
  if (response.code === 200) {
    form.images.push(response.data)
    fileList.value.push({
      name: file.name,
      url: response.data,
      response: response
    })
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败处理
const handleError = () => {
  ElMessage.error('上传失败')
}

// 移除图片
const handleRemove = (file) => {
  const index = form.images.indexOf(file.url)
  if (index !== -1) {
    form.images.splice(index, 1)
  }
}

// 获取商品分类
const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取商品分类失败:', error)
    ElMessage.error('获取商品分类失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 确保有图片
    if (form.images.length === 0) {
      ElMessage.error('请上传商品图片')
      return
    }
    
    const formData = {
      ...form,
      images: form.images,
      originalPrice: form.originalPrice || form.price, // 如果没有设置原价，使用当前价格
      isNegotiable: form.isNegotiable || false, // 确保有默认值
      tags: form.tags ? (Array.isArray(form.tags) ? form.tags.join(',') : form.tags) : '' // 确保标签格式正确
    }
    
    console.log('提交的表单数据：', formData)
    
    const response = await createProduct(formData)
    if (response.code === 200) {
      ElMessage.success('商品发布成功')
      router.push('/my-products')
    } else {
      ElMessage.error(response.message || '商品发布失败')
    }
  } catch (error) {
    console.error('表单验证失败：', error)
    ElMessage.error('请检查表单填写是否正确')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.publish-product {
  padding: 20px;
}

.publish-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.publish-form {
  margin-top: 20px;
}

.product-uploader {
  width: 100%;
}

.upload-tip {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.upload-tips {
  margin-top: 10px;
  color: #666;
}

.tip-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
}

.tip-list li {
  margin: 3px 0;
}
</style> 