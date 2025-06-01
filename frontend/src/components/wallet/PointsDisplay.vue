<template>
  <div class="points-display">
    <el-card class="points-card">
      <div class="points-content">
        <div class="points-icon">
          <el-icon><Medal /></el-icon>
        </div>
        <div class="points-info">
          <div class="points-label">我的积分</div>
          <div class="points-value">{{ points }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Medal } from '@element-plus/icons-vue'
import { getUserPoints } from '@/api/user'
import { ElMessage } from 'element-plus'

const points = ref(0)

// 获取用户积分
const fetchPoints = async () => {
  try {
    const res = await getUserPoints()
    if (res.code === 200) {
      points.value = res.data
    } else {
      ElMessage.error(res.message || '获取积分失败')
    }
  } catch (error) {
    console.error('获取积分失败:', error)
    ElMessage.error('获取积分失败')
  }
}

onMounted(() => {
  fetchPoints()
})
</script>

<style scoped>
.points-display {
  margin-bottom: 20px;
}

.points-card {
  background: linear-gradient(135deg, #409EFF 0%, #36D1DC 100%);
  color: white;
}

.points-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.points-icon {
  font-size: 48px;
  margin-right: 20px;
}

.points-info {
  flex: 1;
}

.points-label {
  font-size: 16px;
  margin-bottom: 8px;
  opacity: 0.9;
}

.points-value {
  font-size: 36px;
  font-weight: bold;
}
</style> 