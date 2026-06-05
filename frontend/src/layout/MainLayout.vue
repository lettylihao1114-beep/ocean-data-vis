<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessageBox } from 'element-plus'
import { computed } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

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
        <div class="banner-left">
          <div class="banner-logo">
            <svg viewBox="0 0 48 48" fill="none" width="48" height="48">
              <circle cx="24" cy="24" r="22" stroke="rgba(255,255,255,.3)" stroke-width="1.5"/>
              <path d="M14 32 Q24 10 34 32" stroke="white" stroke-width="2" fill="none" opacity=".9"/>
              <path d="M10 34 Q24 16 38 34" stroke="white" stroke-width="1" fill="none" opacity=".5"/>
            </svg>
          </div>
          <div class="banner-title-group">
            <h1 class="banner-title">南海海洋数据可视化平台</h1>
            <p class="banner-sub">South China Sea Environmental Data Visualization</p>
          </div>
        </div>
        <div class="banner-right">
          <span class="banner-user">
            <SvgIcon name="user" :size="16"/> {{ authStore.username }}
            <span v-if="authStore.role === 'ADMIN'" class="banner-admin-badge">管理员</span>
          </span>
          <button class="banner-logout" @click="handleLogout">退出</button>
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
  background: #f0f5fa;
}

/* ========== Banner ========== */
.app-banner {
  position: relative; overflow: hidden;
  background: linear-gradient(135deg, #04182e 0%, #06203a 30%, #0a3d62 60%, #0d5e8a 100%);
  background-size: cover; background-position: center; background-repeat: no-repeat;
  flex-shrink: 0;
}
.banner-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(135deg, rgba(4,24,46,.75) 0%, rgba(6,32,58,.6) 50%, rgba(10,61,98,.45) 100%);
  pointer-events: none; z-index: 0;
}
.banner-bg { position: absolute; inset: 0; pointer-events: none; }
.banner-wave {
  position: absolute; bottom: 0; left: 0; right: 0; height: 40px;
  background: url("data:image/svg+xml,%3Csvg viewBox='0 0 1200 40' preserveAspectRatio='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M0 20 Q75 5 150 20 Q225 35 300 20 Q375 5 450 20 Q525 35 600 20 Q675 5 750 20 Q825 35 900 20 Q975 5 1050 20 Q1125 35 1200 20 L1200 40 L0 40 Z' fill='rgba(255,255,255,.03)'/%3E%3Cpath d='M0 28 Q100 15 200 28 Q300 41 400 28 Q500 15 600 28 Q700 41 800 28 Q900 15 1000 28 Q1100 41 1200 28 L1200 40 L0 40 Z' fill='rgba(255,255,255,.02)'/%3E%3C/svg%3E") repeat-x;
  background-size: 600px 40px;
}
.banner-content {
  position: relative; z-index: 1;
  display: flex; align-items: center; justify-content: space-between;
  padding: 20px 36px; flex-wrap: wrap; gap: 14px;
}
.banner-left { display: flex; align-items: center; gap: 16px; }
.banner-logo { flex-shrink: 0; }
.banner-title-group { display: flex; flex-direction: column; }
.banner-title { color: #fff; font-size: 20px; font-weight: 700; margin: 0; letter-spacing: 1px; }
.banner-sub { color: rgba(255,255,255,.5); font-size: 11px; margin: 4px 0 0; letter-spacing: 1px; text-transform: uppercase; }
.banner-right { display: flex; align-items: center; gap: 14px; }
.banner-user { color: rgba(255,255,255,.85); font-size: 14px; display: flex; align-items: center; gap: 8px; }
.banner-admin-badge {
  background: rgba(239,68,68,.25); color: #fca5a5; font-size: 10px;
  padding: 2px 10px; border-radius: 10px; border: 1px solid rgba(239,68,68,.4);
}
.banner-logout {
  background: rgba(255,255,255,.1); color: rgba(255,255,255,.7); border: 1px solid rgba(255,255,255,.15);
  padding: 6px 16px; border-radius: 6px; font-size: 13px; cursor: pointer; transition: .2s;
}
.banner-logout:hover { background: rgba(255,255,255,.18); color: #fff; }

/* ========== 横向导航栏 ========== */
.app-navbar {
  background: #fff; flex-shrink: 0;
  border-bottom: 1px solid #e8edf4;
  box-shadow: 0 2px 12px rgba(6,32,58,.06);
  position: sticky; top: 0; z-index: 100;
}
.navbar-inner {
  max-width: 1280px; margin: 0 auto;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; height: 52px;
}
.navbar-links { display: flex; align-items: center; gap: 2px; }
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
}
.app-main.home { padding: 0; }

/* ========== 页脚 ========== */
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
  .banner-content { flex-direction: column; text-align: center; }
  .banner-left { flex-direction: column; }
}
</style>
