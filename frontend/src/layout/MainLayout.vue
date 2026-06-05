<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  { path: '/dashboard', title: '数据大屏', icon: 'DataAnalysis' },
  { path: '/data-query', title: '数据查询', icon: 'Search' },
  { path: '/monitor-map', title: '监测地图', icon: 'MapLocation' },
  { path: '/ai-chat', title: 'AI 助手', icon: 'ChatDotRound' },
  { path: '/knowledge', title: '科普知识', icon: 'Reading' },
  { path: '/export', title: '数据导出', icon: 'Download' },
]

const adminMenus = [
  { path: '/alerts', title: '预警管理', icon: 'Warning' },
  { path: '/admin', title: '系统管理', icon: 'Setting' },
]

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    authStore.logout()
    router.push('/login')
  })
}
</script>

<template>
  <el-container class="app-shell">
    <!-- 侧边栏 -->
    <el-aside width="240px" class="app-sidebar">
      <!-- Logo 区 -->
      <div class="sidebar-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 40 40" fill="none">
            <circle cx="20" cy="20" r="18" fill="rgba(255,255,255,.1)"/>
            <path d="M12 28 Q20 10 28 28" stroke="white" stroke-width="1.8" fill="none" opacity=".8"/>
            <path d="M8 30 Q20 16 32 30" stroke="white" stroke-width="1.2" fill="none" opacity=".5"/>
          </svg>
        </div>
        <div class="brand-text">
          <span class="brand-title">南海海洋数据平台</span>
          <span class="brand-sub">South China Sea Data</span>
        </div>
      </div>

      <!-- 导航菜单 -->
      <div class="sidebar-nav">
        <div class="nav-section-label">主菜单</div>
        <div
          v-for="item in menuItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: route.path === item.path }"
          @click="router.push(item.path)"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
          <div v-if="route.path === item.path" class="active-indicator" />
        </div>

        <template v-if="authStore.role === 'ADMIN'">
          <div class="nav-section-label" style="margin-top:16px">管理</div>
          <div
            v-for="item in adminMenus"
            :key="item.path"
            class="nav-item"
            :class="{ active: route.path === item.path }"
            @click="router.push(item.path)"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
            <div v-if="route.path === item.path" class="active-indicator" />
          </div>
        </template>
      </div>

      <!-- 底部波浪装饰 -->
      <div class="sidebar-wave">
        <svg viewBox="0 0 240 40" preserveAspectRatio="none">
          <path d="M0 20 Q30 5 60 20 Q90 35 120 20 Q150 5 180 20 Q210 35 240 20 L240 40 L0 40 Z" fill="rgba(255,255,255,.04)"/>
          <path d="M0 28 Q40 15 80 28 Q120 41 160 28 Q200 15 240 28 L240 40 L0 40 Z" fill="rgba(255,255,255,.02)"/>
        </svg>
      </div>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="app-main">
      <!-- 顶栏 -->
      <el-header class="app-header">
        <div class="header-left">
          <span class="header-greeting">👋 欢迎回来，</span>
          <span class="header-username">{{ authStore.username }}</span>
          <el-tag
            v-if="authStore.role === 'ADMIN'"
            size="small"
            class="role-tag"
          >管理员</el-tag>
        </div>
        <div class="header-right">
          <div class="header-time">{{ new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' }) }}</div>
          <el-button class="logout-btn" @click="handleLogout" text>
            <el-icon><component is="SwitchButton" /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="app-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* ========== Shell ========== */
.app-shell { height: 100vh; overflow: hidden; }

/* ========== 侧边栏 ========== */
.app-sidebar {
  background: linear-gradient(180deg, #06203a 0%, #0a3d62 40%, #0d5e8a 100%);
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: 4px 0 24px rgba(6, 32, 58, .25);
}

/* Logo */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 24px 20px 20px;
  border-bottom: 1px solid rgba(255,255,255,.08);
}
.brand-icon svg { width: 42px; height: 42px; display: block; }
.brand-text { display: flex; flex-direction: column; }
.brand-title {
  font-size: 15px; font-weight: 700; color: #fff; letter-spacing: .5px;
}
.brand-sub {
  font-size: 10px; color: rgba(255,255,255,.45); letter-spacing: 1px; text-transform: uppercase;
}

/* 导航 */
.sidebar-nav { flex: 1; padding: 12px 12px 0; overflow-y: auto; }
.nav-section-label {
  font-size: 11px; color: rgba(255,255,255,.35); text-transform: uppercase;
  letter-spacing: 1px; padding: 8px 12px 6px;
}
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 11px 16px; margin-bottom: 2px;
  border-radius: 10px;
  color: rgba(255,255,255,.65); font-size: 14px;
  cursor: pointer; transition: all .2s;
  position: relative;
}
.nav-item:hover {
  background: rgba(255,255,255,.08); color: rgba(255,255,255,.9);
}
.nav-item.active {
  background: rgba(255,255,255,.12); color: #fff; font-weight: 600;
}
.active-indicator {
  position: absolute; left: 0; top: 50%; transform: translateY(-50%);
  width: 3px; height: 24px; background: #06b6d4; border-radius: 0 2px 2px 0;
}

/* 波浪装饰 */
.sidebar-wave {
  height: 50px; overflow: hidden; flex-shrink: 0;
}
.sidebar-wave svg { width: 100%; height: 100%; }

/* ========== 顶栏 ========== */
.app-header {
  display: flex; align-items: center; justify-content: space-between;
  height: 60px; padding: 0 28px;
  background: rgba(255,255,255,.85); backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(6,182,212,.12);
  box-shadow: 0 1px 8px rgba(6,32,58,.04);
}
.header-left { display: flex; align-items: center; gap: 8px; }
.header-greeting { color: #64748b; font-size: 14px; }
.header-username { color: #0a3d62; font-weight: 700; font-size: 15px; }
.role-tag {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: #fff; border: none; font-size: 11px; padding: 2px 10px; border-radius: 10px;
}
.header-right { display: flex; align-items: center; gap: 20px; }
.header-time { color: #94a3b8; font-size: 13px; }
.logout-btn { color: #94a3b8; font-size: 13px; }
.logout-btn:hover { color: #ef4444; }

/* ========== 内容区 ========== */
.app-content {
  background: linear-gradient(160deg, #f0f6fb 0%, #e8f4fd 30%, #f5f9fc 70%, #eef5fa 100%);
  padding: 24px;
  overflow-y: auto;
}
</style>
