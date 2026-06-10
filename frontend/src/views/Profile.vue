<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useAuthStore } from '@/store/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'

const authStore = useAuthStore()

// 当前 Tab
const activeTab = ref<'profile' | 'password'>('profile')

// 编辑资料弹窗
const editDialog = ref(false)
const editForm = reactive({ email: '', phone: '' })
const editSubmitting = ref(false)

// 改密表单
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const passwordSubmitting = ref(false)

// 加载个人资料
onMounted(async () => {
  try {
    await authStore.fetchProfile()
  } catch {
    // 错误已在拦截器处理
  }
})

// 打开编辑弹窗
const openEdit = () => {
  editForm.email = authStore.email
  editForm.phone = authStore.phone
  editDialog.value = true
}

// 保存资料
const saveProfile = async () => {
  editSubmitting.value = true
  try {
    await authStore.updateProfile({ email: editForm.email, phone: editForm.phone })
    ElMessage.success('个人资料已更新')
    editDialog.value = false
  } catch {
    // 错误已在拦截器处理
  } finally {
    editSubmitting.value = false
  }
}

// 修改密码
const submitPassword = async () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('新密码至少6位')
    return
  }
  passwordSubmitting.value = true
  try {
    await authStore.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码已修改，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    // 2秒后跳转登录页
    setTimeout(() => {
      authStore.logout()
      window.location.href = '/login'
    }, 1500)
  } catch {
    // 错误已在拦截器处理
  } finally {
    passwordSubmitting.value = false
  }
}

// 角色标签样式
const roleTagType = (role: string) => (role === 'ADMIN' ? 'danger' : 'primary')

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  return new Date(time).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}
</script>

<template>
  <div class="profile-page">
    <div class="profile-container">
      <!-- 左侧菜单 -->
      <aside class="profile-sidebar">
        <div class="sidebar-title">个人中心</div>
        <nav class="sidebar-nav">
          <button
            class="sidebar-tab"
            :class="{ active: activeTab === 'profile' }"
            @click="activeTab = 'profile'"
          >
            <SvgIcon name="user" :size="16" />
            <span>个人资料</span>
          </button>
          <button
            class="sidebar-tab"
            :class="{ active: activeTab === 'password' }"
            @click="activeTab = 'password'"
          >
            <SvgIcon name="lock" :size="16" />
            <span>修改密码</span>
          </button>
        </nav>
      </aside>

      <!-- 右侧内容 -->
      <section class="profile-content">
        <!-- Tab 1: 个人资料 -->
        <div v-if="activeTab === 'profile'" class="content-card">
          <div class="card-header">
            <h3>基本信息</h3>
            <button class="btn-edit" @click="openEdit">
              <SvgIcon name="edit" :size="14" />
              编辑资料
            </button>
          </div>

          <div class="profile-info">
            <!-- 头像区 -->
            <div class="info-avatar">
              <div class="avatar-placeholder">
                <SvgIcon name="user" :size="40" />
              </div>
              <span class="avatar-username">{{ authStore.username }}</span>
              <span class="avatar-role" :class="authStore.role === 'ADMIN' ? 'role-admin' : 'role-user'">
                {{ authStore.role === 'ADMIN' ? '管理员' : '普通用户' }}
              </span>
            </div>

            <!-- 详情 -->
            <div class="info-details">
              <div class="info-row">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ authStore.username }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ authStore.email || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ authStore.phone || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">注册时间</span>
                <span class="info-value">-</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Tab 2: 修改密码 -->
        <div v-if="activeTab === 'password'" class="content-card">
          <div class="card-header">
            <h3>修改密码</h3>
          </div>
          <div class="password-form">
            <div class="form-group">
              <label>旧密码</label>
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入旧密码"
                show-password
                size="large"
              />
            </div>
            <div class="form-group">
              <label>新密码</label>
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
                size="large"
              />
            </div>
            <div class="form-group">
              <label>确认密码</label>
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                size="large"
              />
            </div>
            <div class="form-hint">
              <SvgIcon name="info" :size="12" />
              密码长度至少 6 位，建议包含字母和数字
            </div>
            <button class="btn-submit" :disabled="passwordSubmitting" @click="submitPassword">
              {{ passwordSubmitting ? '提交中...' : '确认修改' }}
            </button>
          </div>
        </div>
      </section>
    </div>

    <!-- 编辑资料弹窗 -->
    <el-dialog v-model="editDialog" title="编辑个人资料" width="440px" :close-on-click-modal="false">
      <div class="edit-dialog-body">
        <div class="form-group">
          <label>邮箱</label>
          <el-input v-model="editForm.email" placeholder="请输入邮箱" size="large" />
        </div>
        <div class="form-group">
          <label>手机号</label>
          <el-input v-model="editForm.phone" placeholder="请输入手机号" size="large" />
        </div>
      </div>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 28px 24px 48px;
}

.profile-container {
  display: flex;
  gap: 28px;
}

/* ========== 左侧菜单 ========== */
.profile-sidebar {
  width: 200px;
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 13px;
  font-weight: 700;
  color: #0a3d62;
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-bottom: 12px;
  padding: 0 12px;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-tab {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 11px 14px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all .15s;
  text-align: left;
}

.sidebar-tab:hover {
  background: #f1f5f9;
  color: #0a3d62;
}

.sidebar-tab.active {
  background: #e8f4fd;
  color: #0ea5e9;
  font-weight: 600;
}

/* ========== 右侧内容 ========== */
.profile-content {
  flex: 1;
  min-width: 0;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 6px rgba(6, 32, 58, .06);
  padding: 28px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8edf4;
}

.card-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0a3d62;
}

.btn-edit {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border: 1px solid #d1d9e6;
  border-radius: 8px;
  background: #fff;
  color: #475569;
  font-size: 13px;
  cursor: pointer;
  transition: all .15s;
}

.btn-edit:hover {
  border-color: #0ea5e9;
  color: #0ea5e9;
  background: #f0f9ff;
}

/* ========== 个人资料 ========== */
.profile-info {
  display: flex;
  gap: 40px;
}

.info-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e8f4fd 0%, #bae6fd 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0ea5e9;
}

.avatar-username {
  font-size: 16px;
  font-weight: 700;
  color: #0a3d62;
}

.avatar-role {
  font-size: 12px;
  padding: 3px 12px;
  border-radius: 12px;
  font-weight: 500;
}

.role-admin {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fecaca;
}

.role-user {
  background: #e8f4fd;
  color: #0ea5e9;
  border: 1px solid #bae6fd;
}

.info-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 13px 16px;
  border-radius: 8px;
  transition: background .15s;
}

.info-row:hover {
  background: #f8fafc;
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1e293b;
}

/* ========== 改密表单 ========== */
.password-form {
  max-width: 400px;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

.form-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 20px;
}

.btn-submit {
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
}

.btn-submit:hover {
  box-shadow: 0 4px 14px rgba(14, 165, 233, .35);
  transform: translateY(-1px);
}

.btn-submit:disabled {
  opacity: .6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* ========== 弹窗 ========== */
.edit-dialog-body .form-group {
  margin-bottom: 16px;
}

.edit-dialog-body .form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
  }
  .profile-sidebar {
    width: 100%;
  }
  .sidebar-nav {
    flex-direction: row;
  }
  .profile-info {
    flex-direction: column;
    align-items: center;
  }
}
</style>
