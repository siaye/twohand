<template>
  <div class="change-password-container">
    <h2>修改密码</h2>
    <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px">
      <el-form-item label="当前密码" prop="currentPassword">
        <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="请输入当前密码"></el-input>
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码"></el-input>
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">修改密码</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import { changePassword } from '@/api/user'; // 假设用户 API 中有修改密码的接口

// 将表单数据属性改为 ref
const currentPassword = ref('');
const newPassword = ref('');
const confirmPassword = ref('');

// 将 ref 组合成一个对象，供 :model 使用
const passwordForm = reactive({
  currentPassword,
  newPassword,
  confirmPassword,
});

const passwordRules = reactive({
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== newPassword.value) { // 注意这里使用 .value
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    }, trigger: 'blur' },
  ],
});

const passwordFormRef = ref(null);

const submitForm = () => {
  if (!passwordFormRef.value) return;
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await changePassword({
          currentPassword: currentPassword.value, // 注意这里使用 .value
          newPassword: newPassword.value,     // 注意这里使用 .value
        });
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录');
          // TODO: 修改密码成功后，可能需要强制用户重新登录，清除本地 token
          // router.push('/login');
        } else {
          ElMessage.error(res.message || '密码修改失败');
        }
      } catch (error) {
        console.error('修改密码请求失败:', error);
        ElMessage.error('密码修改失败');
      }
    } else {
      console.log('表单验证失败');
      return false;
    }
  });
};
</script>

<style scoped>
.change-password-container {
  padding: 20px;
  max-width: 500px;
}
</style> 