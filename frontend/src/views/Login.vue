<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const isRegister = ref(false)
const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
})

const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (isRegister.value && form.value.password !== form.value.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    if (isRegister.value) {
      await authStore.register({
        username: form.value.username,
        password: form.value.password,
        email: form.value.email,
      })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
    } else {
      await authStore.login({
        username: form.value.username,
        password: form.value.password,
      })
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch (e: any) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <h1>🌊 南海海洋数据可视化系统</h1>
      <p class="subtitle">{{ isRegister ? '注册新账号' : '用户登录' }}</p>

      <el-form @keyup.enter="handleSubmit" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-form-item v-if="isRegister" label="确认密码">
          <el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>

        <el-form-item v-if="isRegister" label="邮箱（选填）">
          <el-input v-model="form.email" size="large" placeholder="请输入邮箱" />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" block @click="handleSubmit">
          {{ isRegister ? '注 册' : '登 录' }}
        </el-button>
      </el-form>

      <p class="toggle">
        {{ isRegister ? '已有账号？' : '没有账号？' }}
        <a href="javascript:void(0)" @click="isRegister = !isRegister">
          {{ isRegister ? '去登录' : '去注册' }}
        </a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0a3d62 0%, #3c6382 50%, #0c2461 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-card h1 {
  text-align: center;
  font-size: 22px;
  color: #0a3d62;
  margin-bottom: 6px;
}
.subtitle {
  text-align: center;
  color: #666;
  margin-bottom: 24px;
}
.toggle {
  text-align: center;
  margin-top: 16px;
  color: #999;
}
.toggle a {
  color: #0a3d62;
  text-decoration: none;
  font-weight: 600;
}
</style>
