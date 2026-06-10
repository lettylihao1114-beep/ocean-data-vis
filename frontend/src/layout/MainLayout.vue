<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessageBox } from 'element-plus'
import { computed } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const goProfile = () => router.push('/profile')

// Banner 背景图：将 banner.jpg/png 放到 src/assets/，不存在则回退到 CSS 渐变
const images = import.meta.glob<{ default: string }>('@/assets/banner.*', { eager: true })
const bannerUrl: string = Object.values(images)[0]?.default || ''

const isHome = computed(() => route.path === '/' || route.path === '/home')

const navItems = [
  { path: '/home', title: '首页', icon: 'HomeFilled' },
  { path: '/dashboard', title: '数据大屏', icon: 'DataAnalysis' },
  { path: '/data-query', title: '数据查询', icon: 'Search' },
  { path: '/monitor-map', title: '监测地图', icon: 'MapLocation' },
  { path: '/ai-chat', title: 'AI 助手', icon: 'ChatDotRound' },
  { path: '/knowledge', title: '科普知识', icon: 'Reading' },
  { path: '/export', title: '数据导出', icon: 'Download' },
]

const adminNavItems = [
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
  <div class="app-shell">
    <!-- Banner 头部 -->
    <header class="app-banner" :style="bannerUrl ? { backgroundImage: `url(${bannerUrl})` } : {}">
      <div class="banner-overlay"></div>
      <div class="banner-bg">
        <div class="banner-wave"></div>
      </div>
      <div class="banner-content">
        <div class="banner-topbar">
          <el-dropdown trigger="click" @command="(cmd: string) => cmd === 'profile' ? goProfile() : handleLogout()">
            <span class="banner-topbar-welcome banner-user-dropdown">
              <SvgIcon name="user" :size="14"/> {{ authStore.username }}
              <span v-if="authStore.role === 'ADMIN'" class="banner-admin-badge">管理员</span>
              <span class="dropdown-arrow">▾</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <SvgIcon name="user" :size="13" /> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <SvgIcon name="trash" :size="13" /> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <span class="banner-topbar-date">{{ new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' }) }}</span>
        </div>
        <div class="banner-hero">
          <div class="banner-logo">
            <svg viewBox="0 0 48 48" fill="none" width="56" height="56">
              <circle cx="24" cy="24" r="22" stroke="rgba(255,255,255,.25)" stroke-width="1.5"/>
              <path d="M14 32 Q24 10 34 32" stroke="white" stroke-width="2" fill="none" opacity=".9"/>
              <path d="M10 34 Q24 16 38 34" stroke="white" stroke-width="1" fill="none" opacity=".5"/>
            </svg>
          </div>
          <div class="banner-title-group">
            <h1 class="banner-title">南海海洋数据可视化平台</h1>
            <p class="banner-sub">South China Sea Environmental Data Visualization · 国家地球系统科学数据中心</p>
          </div>
        </div>
      </div>
    </header>

    <!-- 横向导航栏 -->
    <nav class="app-navbar">
      <div class="navbar-inner">
        <div class="navbar-links">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="navbar-link"
            :class="{ active: route.path === item.path }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
          <template v-if="authStore.role === 'ADMIN'">
            <span class="navbar-sep"></span>
            <router-link
              v-for="item in adminNavItems"
              :key="item.path"
              :to="item.path"
              class="navbar-link admin-link"
              :class="{ active: route.path === item.path }"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </router-link>
          </template>
        </div>
        <div class="navbar-date">{{ new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' }) }}</div>
      </div>
    </nav>

    <!-- 页面内容 -->
    <main class="app-main" :class="{ home: isHome }">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-inner">
        <div class="footer-col">
          <strong>南海海洋数据可视化平台</strong>
          <p>数据来源：国家地球系统科学数据中心<br/>南海再分析数据集（1986 至今）</p>
        </div>
        <div class="footer-col">
          <strong>功能导航</strong>
          <p>数据大屏 · 数据查询 · 监测地图 · AI 助手<br/>科普知识 · 数据导出 · 预警管理</p>
        </div>
        <div class="footer-col">
          <strong>联系方式</strong>
          <p>✉️ ocean@nmdis.org.cn<br/>信息系统综合实训课程项目</p>
        </div>
      </div>
      <div class="footer-bottom">
        © 2026 南海海洋数据可视化平台 · 信息系统综合实训 · 国家海洋信息中心数据支持
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ========== Shell ========== */
.app-shell {
  min-height: 100vh; display: flex; flex-direction: column;
  background: var(--bg-primary);
}

/* ========== Banner — 深海蓝渐变，作为页面视觉锚点 ========== */
.app-banner {
  position: relative; overflow: hidden;
  background: linear-gradient(135deg, #04182e 0%, #06203a 30%, #0a3d62 60%, #0d5e8a 100%);
  background-size: cover; background-position: center; background-repeat: no-repeat;
  flex-shrink: 0; min-height: 260px;
}
.banner-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(180deg, rgba(4,24,46,.65) 0%, rgba(6,32,58,.45) 60%, rgba(10,61,98,.35) 100%);
  pointer-events: none; z-index: 0;
}
.banner-bg { position: absolute; inset: 0; pointer-events: none; }
.banner-wave {
  position: absolute; bottom: 0; left: 0; right: 0; height: 60px;
  background: url("data:image/svg+xml,%3Csvg viewBox='0 0 1200 60' preserveAspectRatio='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 30 Q75 10 150 30 Q225 50 300 30 Q375 10 450 30 Q525 50 600 30 Q675 10 750 30 Q825 50 900 30 Q975 10 1050 30 Q1125 50 1200 30 L1200 60 L0 60 Z' fill='rgba(255,255,255,.04)'/%3E%3Cpath d='M0 42 Q100 25 200 42 Q300 59 400 42 Q500 25 600 42 Q700 59 800 42 Q900 25 1000 42 Q1100 59 1200 42 L1200 60 L0 60 Z' fill='rgba(255,255,255,.02)'/%3E%3C/svg%3E") repeat-x;
  background-size: 600px 60px;
}
.banner-content {
  position: relative; z-index: 1;
  display: flex; flex-direction: column;
  height: 100%; min-height: 260px;
  padding: 0 48px;
}
.banner-topbar {
  display: flex; align-items: center; justify-content: flex-end; gap: 16px;
  padding: 16px 0;
}
.banner-topbar-welcome {
  color: rgba(255,255,255,.75); font-size: 13px;
  display: flex; align-items: center; gap: 6px;
}
.banner-user-dropdown {
  cursor: pointer; user-select: none;
  padding: 5px 10px; border-radius: 6px;
  transition: background .15s;
}
.banner-user-dropdown:hover { background: rgba(255,255,255,.1); }
.dropdown-arrow { font-size: 9px; color: rgba(255,255,255,.4); margin-left: 2px; }
.banner-topbar-date { color: rgba(255,255,255,.4); font-size: 12px; }
.banner-admin-badge {
  background: rgba(239,68,68,.25); color: #fca5a5; font-size: 10px;
  padding: 2px 10px; border-radius: 10px; border: 1px solid rgba(239,68,68,.4);
}
.banner-hero {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  flex: 1; padding-bottom: 30px; gap: 16px; text-align: center;
}
.banner-logo { flex-shrink: 0; }
.banner-title-group { display: flex; flex-direction: column; align-items: center; }
.banner-title { color: #fff; font-size: 32px; font-weight: 800; margin: 0; letter-spacing: 2px; text-shadow: 0 2px 12px rgba(0,0,0,.3); }
.banner-sub { color: rgba(255,255,255,.55); font-size: 13px; margin: 8px 0 0; letter-spacing: 1px; }

/* ========== 横向导航栏 — 白底清爽风 ========== */
.app-navbar {
  background: #fff; flex-shrink: 0;
  border-bottom: 1px solid #e8edf4;
  box-shadow: 0 2px 12px rgba(6,32,58,.06);
  position: sticky; top: 0; z-index: 100;
}
.navbar-inner {
  max-width: 1280px; margin: 0 auto;
  display: flex; align-items: center; justify-content: center;
  padding: 0 24px; height: 52px;
}
.navbar-links { display: flex; align-items: center; gap: 2px; }
.navbar-date { display: none; }
.navbar-link {
  display: flex; align-items: center; gap: 6px;
  padding: 8px 16px; border-radius: 8px;
  font-size: 14px; color: #475569; text-decoration: none;
  transition: all .15s; font-weight: 500;
}
.navbar-link:hover { background: #f1f5f9; color: #0a3d62; }
.navbar-link.active { background: #e8f4fd; color: #0ea5e9; font-weight: 600; }
.navbar-sep { width: 1px; height: 22px; background: #e2e8f0; margin: 0 6px; }
.admin-link { color: #64748b; }
.admin-link.active { background: #fef2f2; color: #ef4444; }
.navbar-date { font-size: 12px; color: #94a3b8; flex-shrink: 0; }

/* ========== 主体 ========== */
.app-main {
  flex: 1;
  padding: 0;
}

/* ========== 页脚 — 深蓝底白字，收束页面 ========== */
.app-footer {
  background: #06203a; color: rgba(255,255,255,.7); flex-shrink: 0;
}
.footer-inner {
  max-width: 1280px; margin: 0 auto; padding: 36px 24px 24px;
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 32px;
}
.footer-col strong { color: #fff; font-size: 14px; display: block; margin-bottom: 8px; }
.footer-col p { font-size: 12px; line-height: 1.8; margin: 0; }
.footer-bottom {
  text-align: center; font-size: 11px; padding: 14px;
  border-top: 1px solid rgba(255,255,255,.08); color: rgba(255,255,255,.35);
}
@media (max-width: 768px) {
  .footer-inner { grid-template-columns: 1fr; }
  .navbar-links { overflow-x: auto; }
  .banner-title { font-size: 22px; }
  .banner-sub { font-size: 11px; }
  .banner-content { padding: 0 20px; }
  .banner-topbar { justify-content: center; flex-wrap: wrap; }
}
</style>
