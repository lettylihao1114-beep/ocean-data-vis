<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()

const isRegister = ref(false)
const form = ref({ username: '', password: '', confirmPassword: '', email: '' })
const loading = ref(false)

const handleSubmit = async () => {
  if (!form.value.username || !form.value.password) return ElMessage.warning('请填写用户名和密码')
  if (isRegister.value && form.value.password !== form.value.confirmPassword) return ElMessage.warning('两次密码不一致')
  if (isRegister.value && form.value.password.length < 8) return ElMessage.warning('密码至少8位')
  loading.value = true
  try {
    if (isRegister.value) {
      await authStore.register({ username: form.value.username, password: form.value.password, email: form.value.email })
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
    } else {
      await authStore.login({ username: form.value.username, password: form.value.password })
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || '操作失败，请重试'
    ElMessage.error(msg)
  } finally { loading.value = false }
}
</script>

<template>
  <div class="login-wrap">
    <div class="bubbles">
      <span v-for="i in 20" :key="i" class="bubble" :style="{ left: `${Math.random() * 100}%`, animationDelay: `${Math.random() * 10}s`, animationDuration: `${6 + Math.random() * 8}s`, width: `${8 + Math.random() * 20}px`, height: `${8 + Math.random() * 20}px` }"></span>
    </div>

    <div class="login-card">
      <div class="logo-icon">🌊</div>
      <h1>南海海洋环境数据可视化</h1>
      <p class="subtitle">{{ isRegister ? '创建账号开始探索' : '登录海洋数据平台' }}</p>

      <el-form @keyup.enter="handleSubmit" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" size="large" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" prefix-icon="Lock" />
        </el-form-item>
        <el-form-item v-if="isRegister" label="确认密码">
          <el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item v-if="isRegister" label="邮箱（选填）">
          <el-input v-model="form.email" size="large" placeholder="请输入邮箱" prefix-icon="Message" />
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" block round @click="handleSubmit" class="login-btn">
          {{ isRegister ? '注 册' : '登 录' }}
        </el-button>
      </el-form>

      <p class="toggle">
        {{ isRegister ? '已有账号？' : '没有账号？' }}
        <a href="javascript:void(0)" @click="isRegister = !isRegister">{{ isRegister ? '去登录' : '去注册' }}</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #071526 0%, #0d2847 35%, #0f3a5c 65%, #0c2340 100%);
  overflow: hidden;
  position: relative;
}
.bubbles { position: absolute; inset: 0; pointer-events: none; }
.bubble {
  position: absolute;
  bottom: -40px;
  background: radial-gradient(circle, rgba(0, 210, 255, 0.3), transparent);
  border: 1px solid rgba(0, 210, 255, 0.1);
  border-radius: 50%;
  animation: rise linear infinite;
}
@keyframes rise { 0% { transform: translateY(0) scale(1); opacity: 0.5; } 100% { transform: translateY(-110vh) scale(0.2); opacity: 0; } }
.login-card {
  width: 440px;
  padding: 48px 44px;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 20px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5), 0 0 40px rgba(0, 180, 255, 0.1);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(20px);
}
.logo-icon { text-align: center; font-size: 48px; margin-bottom: 8px; }
.login-card h1 { text-align: center; font-size: 20px; color: #0a3d62; margin-bottom: 4px; font-weight: 700; }
.subtitle { text-align: center; color: #888; margin-bottom: 28px; font-size: 14px; }
.login-btn { background: linear-gradient(135deg, #0984e3, #00b894); border: none; height: 48px; font-size: 16px; letter-spacing: 4px; }
.login-btn:hover { background: linear-gradient(135deg, #0773c5, #00a381); }
.toggle { text-align: center; margin-top: 20px; color: #aaa; font-size: 13px; }
.toggle a { color: #0984e3; text-decoration: none; font-weight: 600; }
</style>
