<template>
  <div class="complete-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>{{ isMerchant ? '商家信息完善' : '个人信息完善' }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="profile-form"
      >
        <!-- 基本信息 -->
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="form.phone" 
            placeholder="请输入手机号"
            maxlength="11"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入所在城市" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="银行账号" prop="bankAccount">
          <el-input 
            v-model="form.bankAccount" 
            placeholder="请输入16位银行账号"
            maxlength="16"
            show-word-limit
          />
        </el-form-item>

        <!-- 商家特有信息 -->
        <template v-if="isMerchant">
          <el-form-item label="身份证号" prop="idCard">
            <el-input 
              v-model="form.idCard" 
              placeholder="请输入身份证号"
              maxlength="18"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="身份证照片" prop="idCardImage">
            <el-upload
              class="id-card-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ type: 'idCard', userId: userId }"
              :show-file-list="false"
              :before-upload="(file) => beforeUpload(file, 'idCard')"
              :on-success="(response, file) => handleUploadSuccess(response, file, 'idCard')"
              :on-error="handleUploadError"
              accept=".jpg,.jpeg,.png,.gif"
              name="file"
            >
              <div class="upload-content">
                <img v-if="form.idCardImage" :src="form.idCardImage" class="uploaded-image" />
                <template v-else>
                  <el-icon class="uploader-icon"><Plus /></el-icon>
                  <div class="upload-text">点击上传</div>
                </template>
              </div>
            </el-upload>
            <div class="upload-tips">
              <p class="tip-title">上传要求：</p>
              <ul class="tip-list">
                <li>支持 jpg、jpeg、png、gif 格式</li>
                <li>图片大小不超过 4000KB</li>
                <li>请上传清晰的身份证正反面照片</li>
                <li>图片内容必须清晰可见，无遮挡</li>
              </ul>
            </div>
          </el-form-item>

          <el-form-item label="营业执照号" prop="businessLicense">
            <el-input 
              v-model="form.businessLicense" 
              placeholder="请输入营业执照号"
            />
          </el-form-item>

          <el-form-item label="营业执照照片" prop="businessLicenseImage">
            <el-upload
              class="license-uploader"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :data="{ type: 'businessLicense', userId: userId }"
              :show-file-list="false"
              :before-upload="(file) => beforeUpload(file, 'businessLicense')"
              :on-success="(response, file) => handleUploadSuccess(response, file, 'businessLicense')"
              :on-error="handleUploadError"
              accept=".jpg,.jpeg,.png,.gif"
              name="file"
            >
              <div class="upload-content">
                <img v-if="form.businessLicenseImage" :src="form.businessLicenseImage" class="uploaded-image" />
                <template v-else>
                  <el-icon class="uploader-icon"><Plus /></el-icon>
                  <div class="upload-text">点击上传</div>
                </template>
              </div>
            </el-upload>
            <div class="upload-tips">
              <p class="tip-title">上传要求：</p>
              <ul class="tip-list">
                <li>支持 jpg、jpeg、png、gif 格式</li>
                <li>图片大小不超过 4000KB</li>
                <li>请上传清晰的营业执照照片</li>
                <li>图片内容必须清晰可见，无遮挡</li>
              </ul>
            </div>
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            提交审核
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { completeProfile } from '@/api/user'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const store = useStore()
const formRef = ref(null)
const submitting = ref(false)

// 从sessionStorage获取用户ID和类型
const userId = computed(() => sessionStorage.getItem('tempUserId'))
const userType = computed(() => Number(sessionStorage.getItem('tempUserType')) || 0)

// 判断是否是商家用户
const isMerchant = computed(() => userType.value === 1)

// 上传相关
const uploadUrl = computed(() => 'http://localhost:8080/api/user/upload')
const baseUrl = 'http://localhost:8080'  // 添加基础URL

const uploadHeaders = computed(() => {
  // 使用临时用户ID作为认证
  const tempUserId = sessionStorage.getItem('tempUserId')
  console.log('【CompleteProfile】临时用户ID:', tempUserId)
  return {
    'X-User-ID': Number(tempUserId)
  }
})

// 上传配置
const uploadConfig = {
  acceptTypes: '.jpg,.jpeg,.png',
  maxSize: 4096, // 4MB
  action: uploadUrl,
  headers: uploadHeaders
}

// 表单数据
const form = ref({
  realName: '',
  phone: '',
  email: '',
  city: '',
  gender: 1,
  bankAccount: '',
  idCard: '',
  idCardImage: '',
  businessLicense: '',
  businessLicenseImage: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入所在城市', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  bankAccount: [
    { required: true, message: '请输入银行账号', trigger: 'blur' },
    { pattern: /^\d{16}$/, message: '请输入16位数字的银行账号', trigger: 'blur' }
  ],
  idCard: [
    { required: isMerchant.value, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  idCardImage: [
    { required: isMerchant.value, message: '请上传身份证照片', trigger: 'change' }
  ],
  businessLicense: [
    { required: isMerchant.value, message: '请输入营业执照号', trigger: 'blur' }
  ],
  businessLicenseImage: [
    { required: isMerchant.value, message: '请上传营业执照照片', trigger: 'change' }
  ]
}

// 上传前检查
const beforeUpload = (file, type) => {
  // 检查文件类型
  const allowedTypes = uploadConfig.acceptTypes.split(',').map(type => `image/${type.replace('.', '')}`)
  const isImage = allowedTypes.includes(file.type)
  const isLtMaxSize = file.size / 1024 < uploadConfig.maxSize

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLtMaxSize) {
    ElMessage.error('图片大小不能超过 4MB!')
    return false
  }
  return true
}

// 处理上传成功
const handleUploadSuccess = (response, uploadFile, type) => {
  console.log('【CompleteProfile】上传成功:', response)
  if (response.code === 200 && response.data) {
    ElMessage.success('上传成功')
    console.log('【CompleteProfile】上传类型:', type)
    
    // 根据type更新对应的表单字段，添加基础URL
    if (type === 'idCard') {
      form.value.idCardImage = baseUrl + response.data
    } else if (type === 'businessLicense') {
      form.value.businessLicenseImage = baseUrl + response.data
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 处理上传失败
const handleUploadError = (error) => {
  console.error('【CompleteProfile】上传失败:', error)
  let errorMsg = '上传失败，请重试'
  
  if (error.status === 413) {
    errorMsg = '图片太大，请压缩后重试'
  } else if (error.status === 415) {
    errorMsg = '不支持的图片格式'
  } else if (error.status === 401) {
    errorMsg = '请先完成注册'
    router.replace('/register')
  } else if (error.status === 0) {
    errorMsg = '网络连接失败，请检查网络设置'
  }
  
  ElMessage.error(errorMsg)
}

// 获取用户信息
const getUserProfile = async () => {
  try {
    // 从sessionStorage获取临时用户ID
    const tempUserId = sessionStorage.getItem('tempUserId')
    console.log('【CompleteProfile】获取用户信息 - 临时用户ID:', tempUserId)
    
    if (!tempUserId) {
      ElMessage.warning('请先完成注册')
      router.replace('/register')
      return
    }

    // 确保userId是数字
    const userId = parseInt(tempUserId)
    if (isNaN(userId)) {
      ElMessage.error('无效的用户ID')
      return
    }

    const response = await request({
      url: '/user/profile',
      method: 'get',
      headers: {
        'X-User-ID': Number(userId)
      }
    })
    if (response.code === 200 && response.data) {
      // 更新表单数据
      form.value = {
        ...form.value,
        realName: response.data.realName || '',
        phone: response.data.phone || '',
        email: response.data.email || '',
        city: response.data.city || '',
        gender: response.data.gender || 1,
        bankAccount: response.data.bankAccount || '',
        idCard: response.data.idCard || '',
        idCardImage: response.data.idCardImage ? baseUrl + response.data.idCardImage : '',
        businessLicense: response.data.businessLicense || '',
        businessLicenseImage: response.data.businessLicenseImage ? baseUrl + response.data.businessLicenseImage : ''
      }
    }
  } catch (error) {
    console.error('【CompleteProfile】获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    // 从sessionStorage获取临时用户ID
    const tempUserId = sessionStorage.getItem('tempUserId')
    if (!tempUserId) {
      ElMessage.warning('请先完成注册')
      router.replace('/register')
      return
    }

    // 确保userId是数字
    const userId = parseInt(tempUserId)
    if (isNaN(userId)) {
      ElMessage.error('无效的用户ID')
      return
    }
    
    // 添加详细的日志输出
    console.log('【CompleteProfile】表单数据:', {
      form: form.value,
      userId,
      userType: userType.value,
      isMerchant: isMerchant.value
    })
    
    // 提交时带上用户ID
    const submitData = {
      ...form.value,
      userId: userId
    }
    console.log('【CompleteProfile】提交数据:', submitData)
    
    // 使用request直接调用接口，带上X-User-ID头
    const response = await request({
      url: '/user/complete-profile',
      method: 'post',
      headers: {
        'X-User-ID': Number(userId)
      },
      data: submitData
    })
    
    if (response.code === 200) {
      ElMessage.success('信息提交成功，请等待管理员审核')
      // 提交成功后跳转到登录页
      router.replace('/login')
    } else {
      throw new Error(response.message || '提交失败')
    }
  } catch (error) {
    console.error('【CompleteProfile】提交失败:', error)
    ElMessage.error(error.message || '提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  // 从sessionStorage获取临时用户ID
  const tempUserId = sessionStorage.getItem('tempUserId')
  console.log('【CompleteProfile】临时用户ID:', tempUserId)
  
  if (!tempUserId) {
    ElMessage.warning('请先完成注册')
    router.replace('/register')
    return
  }

  // 获取用户信息
  await getUserProfile()
})
</script>

<style scoped>
.complete-profile {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
}

.profile-card {
  width: 100%;
  max-width: 800px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-form {
  margin-top: 20px;
}

.upload-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  position: relative;
}

.upload-text {
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.upload-tips {
  margin-top: 12px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.tip-title {
  font-size: 13px;
  color: #606266;
  margin: 0 0 8px 0;
  font-weight: bold;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}

.tip-list li {
  margin-bottom: 4px;
}

.id-card-uploader,
.license-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 240px;
  height: 160px;
  transition: border-color 0.3s;
}

.id-card-uploader:hover,
.license-uploader:hover {
  border-color: #409eff;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #f5f7fa;
}

/* 添加图片预览遮罩 */
.upload-content:hover .uploaded-image {
  opacity: 0.8;
}

.upload-content:hover::after {
  content: '点击预览';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style> 