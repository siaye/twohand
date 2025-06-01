<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="refreshList">刷新</el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" border @row-click="handleRowClick">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getAuditStatusType(row.auditStatus)">
              {{ getAuditStatusText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button
              v-if="row.auditStatus === 0"
              type="success"
              size="small"
              @click.stop="handleAudit(row, 1)"
            >
              通过审核
            </el-button>
            <el-button
              v-if="row.auditStatus === 0"
              type="danger"
              size="small"
              @click.stop="handleAudit(row, 2)"
            >
              拒绝审核
            </el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click.stop="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="600px"
    >
      <div class="dialog-header">
        <el-button type="primary" @click="handleEdit" v-if="!isEditing">编辑</el-button>
        <div v-else class="edit-buttons">
          <el-button type="success" @click="handleSave" :loading="saving">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </div>
      </div>

      <el-form
        ref="formRef"
        :model="editForm"
        :rules="rules"
        label-width="120px"
        :disabled="!isEditing"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="editForm.city" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender">
            <el-option label="未知" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="银行账号" prop="bankAccount">
          <el-input v-model="editForm.bankAccount" />
        </el-form-item>
        <el-form-item label="账号状态" prop="status">
          <el-switch
            v-model="editForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="审核状态" prop="auditStatus">
          <el-tag :type="getAuditStatusType(editForm.auditStatus)">
            {{ getAuditStatusText(editForm.auditStatus) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="注册时间">
          <span>{{ editForm.createTime }}</span>
        </el-form-item>
        <el-form-item label="更新时间">
          <span>{{ editForm.updateTime }}</span>
        </el-form-item>
      </el-form>

      <!-- 头像信息 -->
      <div class="image-section">
        <h3>头像</h3>
        <el-image
          v-if="editForm.avatar"
          :src="`http://localhost:8080${editForm.avatar}`"
          :preview-src-list="[`http://localhost:8080${editForm.avatar}`]"
          fit="cover"
          style="width: 100px; height: 100px"
        />
        <span v-else>暂无</span>
      </div>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.auditStatus === 1 ? '通过审核' : '拒绝审核'"
      width="500px"
    >
      <el-form :model="auditForm" label-width="80px">
        <el-form-item label="审核原因" v-if="auditForm.auditStatus === 2">
          <el-input
            v-model="auditForm.reason"
            type="textarea"
            rows="3"
            placeholder="请输入拒绝原因"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllUsers, auditUser, updateUserStatus, updateUserInfo } from '@/api/admin'
import { formatTime } from '@/utils/format'

// 用户列表
const userList = ref([])
const loading = ref(false)

// 详情对话框
const detailDialogVisible = ref(false)
const currentUser = ref({})
const isEditing = ref(false)
const saving = ref(false)
const formRef = ref(null)

// 审核相关
const auditDialogVisible = ref(false)
const submitting = ref(false)
const auditForm = ref({
  userId: null,
  auditStatus: 1,
  reason: ''
})

// 编辑表单
const editForm = ref({
  username: '',
  realName: '',
  phone: '',
  email: '',
  city: '',
  gender: 0,
  bankAccount: '',
  status: 1,
  auditStatus: 0,
  createTime: '',
  updateTime: '',
  avatar: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取审核状态文本
const getAuditStatusText = (status) => {
  switch (status) {
    case 0: return '待审核'
    case 1: return '已通过'
    case 2: return '已拒绝'
    default: return '未知'
  }
}

// 获取审核状态类型
const getAuditStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'success'
    case 2: return 'danger'
    default: return 'info'
  }
}

// 获取用户列表
const fetchUserList = async () => {
  try {
    loading.value = true
    const res = await getAllUsers()
    console.log('【UserManagement】获取用户列表响应:', res)
    // 直接使用返回的数组数据
    if (Array.isArray(res)) {
      // 只显示普通用户（userType === 0）且不是管理员（role !== 2）
      const filteredUsers = res.filter(user => user.userType === 0 && user.role !== 2)
      console.log('【UserManagement】过滤后的用户列表:', filteredUsers)
      userList.value = filteredUsers
    } else {
      console.error('【UserManagement】获取用户列表失败: 数据格式错误')
      ElMessage.error('获取用户列表失败：数据格式错误')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  fetchUserList()
}

// 处理行点击
const handleRowClick = (row) => {
  currentUser.value = row
  Object.assign(editForm.value, row)
  detailDialogVisible.value = true
}

// 处理审核
const handleAudit = (row, status) => {
  auditForm.value = {
    userId: row.id,
    auditStatus: status,
    reason: ''
  }
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (auditForm.value.auditStatus === 2 && !auditForm.value.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  submitting.value = true
  try {
    const res = await auditUser(auditForm.value)
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditDialogVisible.value = false
      refreshList()
    } else {
      throw new Error(res.message || '审核失败')
    }
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error(error.message || '审核失败')
  } finally {
    submitting.value = false
  }
}

// 处理编辑
const handleEdit = () => {
  isEditing.value = true
}

// 处理保存
const handleSave = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    const res = await updateUserInfo({
      id: currentUser.value.id,
      ...editForm.value
    })

    if (res.code === 200) {
      ElMessage.success('保存成功')
      isEditing.value = false
      refreshList()
    } else {
      throw new Error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// 处理取消
const handleCancel = () => {
  Object.assign(editForm.value, currentUser.value)
  isEditing.value = false
}

// 处理状态切换
const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await updateUserStatus({
      id: row.id,
      status: row.status === 1 ? 0 : 1
    })

    if (res.code === 200) {
      ElMessage.success(`${row.status === 1 ? '禁用' : '启用'}成功`)
      refreshList()
    } else {
      throw new Error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.edit-buttons {
  display: flex;
  gap: 10px;
}

.image-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.image-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 