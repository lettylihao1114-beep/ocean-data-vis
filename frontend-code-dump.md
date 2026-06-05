# 南海海洋数据可视化平台 — 前端代码汇总

> 技术栈：Vue 3 + Vite + Element Plus + ECharts + Leaflet
> 分支：`feature/frontend-redesign`（深色科技风重构，2026-06-05 完成）
> 目标：将原 NMDIS 浅色风格重构为深色科技感数据平台风格

---

## 色彩系统（CSS 变量）

| 用途 | 值 |
|------|-----|
| 页面底色 | `#0a1628` |
| 卡片底色 | `rgba(13, 35, 64, 0.75)` |
| 主高亮 | `#00e5ff`（青色霓虹） |
| 次高亮 | `#2979ff`（蓝） |
| 成功色 | `#00e676`（绿） |
| 警告色 | `#ff9100`（橙） |
| 错误色 | `#ff5252`（红） |
| 主文字 | `rgba(255,255,255,0.95)` |
| 次文字 | `rgba(255,255,255,0.7)` |
| 弱文字 | `rgba(255,255,255,0.45)` |

---

## 1. index.html

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>南海海洋数据平台</title>
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;800;900&display=swap" rel="stylesheet">
  </head>
  <body>
    <div id="app"></div>
    <script type="module" src="/src/main.ts"></script>
  </body>
</html>
```

---

## 2. src/main.ts

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
```

---

## 3. src/App.vue

```vue
<template>
  <router-view />
</template>
```

---

## 4. src/styles/global.css (全局样式 + Element Plus 深色覆盖)

```css
/* ============================================
   南海海洋数据平台 — 深色科技风全局样式
   ============================================ */

/* ========== CSS 变量 ========== */
:root {
  --bg-primary: #0a1628;
  --bg-secondary: #0f2035;
  --bg-tertiary: #142d4c;
  --bg-card: rgba(13, 35, 64, 0.75);

  --accent-cyan: #00e5ff;
  --accent-blue: #2979ff;
  --accent-teal: #00bcd4;
  --accent-green: #00e676;
  --accent-orange: #ff9100;
  --accent-red: #ff5252;
  --glow-cyan: 0 0 12px rgba(0, 229, 255, 0.4);
  --glow-blue: 0 0 12px rgba(41, 121, 255, 0.4);

  --text-primary: rgba(255, 255, 255, 0.95);
  --text-secondary: rgba(255, 255, 255, 0.7);
  --text-muted: rgba(255, 255, 255, 0.45);
  --text-accent: #00e5ff;

  --border-subtle: rgba(255, 255, 255, 0.06);
  --border-default: rgba(0, 229, 255, 0.1);
  --border-hover: rgba(0, 229, 255, 0.3);

  --shadow-card: 0 4px 24px rgba(0, 0, 0, 0.2);
  --shadow-hover: 0 4px 24px rgba(0, 229, 255, 0.08);

  --font-display: 'Orbitron', 'DIN Alternate', monospace;
  --font-body: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-mono: 'JetBrains Mono', 'Fira Code', monospace;
}

/* ========== 全局重置 ========== */
html, body {
  margin: 0; padding: 0;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: var(--font-body);
  -webkit-font-smoothing: antialiased;
}
#app { background: var(--bg-primary); min-height: 100vh; }

/* ========== Element Plus 深色覆盖 ========== */
/* 表格 */
.el-table {
  --el-table-bg-color: var(--bg-card) !important;
  --el-table-tr-bg-color: var(--bg-card) !important;
  --el-table-header-bg-color: rgba(0,229,255,0.05) !important;
  --el-table-border-color: var(--border-subtle) !important;
  --el-table-text-color: var(--text-primary) !important;
  --el-table-header-text-color: var(--text-secondary) !important;
  --el-table-row-hover-bg-color: rgba(0,229,255,0.03) !important;
  background: var(--bg-card) !important; border-radius: 12px; overflow: hidden;
}
.el-table th.el-table__cell { background: rgba(0,229,255,0.05) !important; color: var(--text-secondary) !important; border-bottom: 1px solid var(--border-default) !important; }
.el-table td.el-table__cell { color: var(--text-primary) !important; border-bottom: 1px solid var(--border-subtle) !important; }
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell { background: rgba(255,255,255,0.02) !important; }

/* 分页 */
.el-pagination {
  --el-pagination-bg-color: transparent !important;
  --el-pagination-text-color: var(--text-secondary) !important;
  --el-pagination-button-bg-color: var(--bg-secondary) !important;
  --el-pagination-button-color: var(--text-secondary) !important;
  --el-pagination-hover-color: var(--accent-cyan) !important;
}
.el-pager li.is-active { background: rgba(0,229,255,0.15) !important; color: var(--accent-cyan) !important; }

/* 输入框 */
.el-input__wrapper { background: var(--bg-secondary) !important; border-color: var(--border-default) !important; box-shadow: none !important; border-radius: 8px !important; }
.el-input__inner { color: var(--text-primary) !important; }
.el-input__wrapper:hover { border-color: var(--border-hover) !important; }
.el-input__wrapper.is-focus { border-color: var(--accent-cyan) !important; box-shadow: 0 0 0 1px var(--accent-cyan) inset !important; }

/* 下拉框 */
.el-select-dropdown { background: var(--bg-secondary) !important; border: 1px solid var(--border-default) !important; }
.el-select-dropdown__item { color: var(--text-secondary) !important; }
.el-select-dropdown__item:hover { background: rgba(0,229,255,0.08) !important; color: var(--accent-cyan) !important; }
.el-select-dropdown__item.selected { color: var(--accent-cyan) !important; font-weight: 600; }

/* 日期选择器 */
.el-picker-panel { background: var(--bg-secondary) !important; border: 1px solid var(--border-default) !important; color: var(--text-primary) !important; }

/* 按钮 */
.el-button--default { --el-button-bg-color: var(--bg-secondary) !important; --el-button-border-color: var(--border-default) !important; --el-button-text-color: var(--text-secondary) !important; }
.el-button--default:hover { --el-button-bg-color: rgba(0,229,255,0.08) !important; --el-button-border-color: var(--border-hover) !important; --el-button-text-color: var(--accent-cyan) !important; }
.el-button--primary { --el-button-bg-color: var(--accent-cyan) !important; --el-button-border-color: var(--accent-cyan) !important; --el-button-text-color: #0a1628 !important; font-weight: 600; }
.el-button--primary:hover { --el-button-bg-color: #00b8d4 !important; }
.el-button--danger { --el-button-bg-color: rgba(255,82,82,0.15) !important; --el-button-border-color: rgba(255,82,82,0.3) !important; --el-button-text-color: var(--accent-red) !important; }

/* 对话框 */
.el-dialog { background: var(--bg-secondary) !important; border: 1px solid var(--border-default) !important; border-radius: 16px !important; }
.el-dialog__header { border-bottom: 1px solid var(--border-subtle) !important; }
.el-dialog__title { color: var(--text-primary) !important; }
.el-dialog__body { color: var(--text-secondary) !important; }

/* 消息提示 & 确认框 */
.el-message, .el-message-box { background: var(--bg-secondary) !important; border: 1px solid var(--border-default) !important; }
.el-message__content, .el-message-box__title { color: var(--text-primary) !important; }
.el-message-box__message { color: var(--text-secondary) !important; }

/* 卡片 / 标签 / 菜单 / 表单 / 空状态 / 加载 */
.el-card { background: var(--bg-card) !important; border: 1px solid var(--border-default) !important; border-radius: 12px !important; color: var(--text-primary) !important; }
.el-tag { border-radius: 6px !important; }
.el-menu { background: transparent !important; border-right: none !important; }
.el-form-item__label { color: var(--text-secondary) !important; }
.el-empty__description p { color: var(--text-muted) !important; }
.el-loading-mask { background: rgba(10,22,40,0.7) !important; }

/* 滚动条 */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: var(--bg-primary); }
::-webkit-scrollbar-thumb { background: var(--bg-tertiary); border-radius: 3px; }
::-webkit-scrollbar-thumb:hover { background: rgba(0,229,255,0.3); }

/* ========== 通用工具类 ========== */
.bg-grid {
  background-image:
    linear-gradient(rgba(0,229,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,229,255,0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}
.glass-card {
  background: var(--bg-card); backdrop-filter: blur(8px);
  border: 1px solid var(--border-default); border-radius: 12px;
  box-shadow: var(--shadow-card); transition: border-color 0.2s, box-shadow 0.2s;
}
.glass-card:hover { border-color: var(--border-hover); box-shadow: var(--shadow-hover); }
.stat-neon { color: var(--accent-cyan); font-size: 36px; font-weight: 800; font-family: var(--font-display); text-shadow: 0 0 10px rgba(0,229,255,0.4); }
.tech-corners { position: relative; }
.tech-corners::before {
  content: ''; position: absolute; top: 0; left: 0;
  width: 20px; height: 20px;
  border-top: 2px solid var(--accent-cyan); border-left: 2px solid var(--accent-cyan);
  border-radius: 2px 0 0 0; z-index: 1; pointer-events: none;
}
.tech-corners::after {
  content: ''; position: absolute; bottom: 0; right: 0;
  width: 20px; height: 20px;
  border-bottom: 2px solid var(--accent-cyan); border-right: 2px solid var(--accent-cyan);
  border-radius: 0 0 2px 0; z-index: 1; pointer-events: none;
}

/* ========== 动效 ========== */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes glowPulse {
  0%, 100% { border-color: rgba(0,229,255,0.1); }
  50% { border-color: rgba(0,229,255,0.4); }
}
@keyframes ticker-scroll {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
```

---

## 5. src/layout/MainLayout.vue (全局布局)

```vue
<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { ElMessageBox } from 'element-plus'
import { computed } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

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
      <div class="banner-bg"><div class="banner-wave"></div></div>
      <div class="banner-content">
        <div class="banner-topbar">
          <span class="banner-topbar-welcome">
            <SvgIcon name="user" :size="14"/> {{ authStore.username }}
            <span v-if="authStore.role === 'ADMIN'" class="banner-admin-badge">管理员</span>
          </span>
          <span class="banner-topbar-date">{{ new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'long', day:'numeric', weekday:'long' }) }}</span>
          <button class="banner-logout" @click="handleLogout">退出</button>
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

    <!-- 横向导航栏（深色玻璃态） -->
    <nav class="app-navbar">
      <div class="navbar-inner">
        <div class="navbar-links">
          <router-link v-for="item in navItems" :key="item.path" :to="item.path"
            class="navbar-link" :class="{ active: route.path === item.path }">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
          <template v-if="authStore.role === 'ADMIN'">
            <span class="navbar-sep"></span>
            <router-link v-for="item in adminNavItems" :key="item.path" :to="item.path"
              class="navbar-link admin-link" :class="{ active: route.path === item.path }">
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
/* Shell */
.app-shell {
  min-height: 100vh; display: flex; flex-direction: column;
  background: var(--bg-primary);
  background-image:
    linear-gradient(rgba(0,229,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,229,255,0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

/* Banner */
.app-banner {
  position: relative; overflow: hidden;
  background: linear-gradient(180deg, #0a1628 0%, #0d2137 50%, #0f2a45 100%);
  background-size: cover; background-position: center; background-repeat: no-repeat;
  flex-shrink: 0; min-height: 220px;
  border-bottom: 1px solid rgba(0,229,255,0.15);
}
.banner-overlay {
  position: absolute; inset: 0;
  background: radial-gradient(ellipse at 50% 0%, rgba(0,229,255,0.08) 0%, transparent 60%);
  pointer-events: none; z-index: 0;
}
.banner-wave { display: none; }
.banner-content {
  position: relative; z-index: 1;
  display: flex; flex-direction: column;
  height: 100%; min-height: 220px; padding: 0 48px;
}
.banner-topbar { display: flex; align-items: center; justify-content: flex-end; gap: 16px; padding: 16px 0; }
.banner-topbar-welcome { color: rgba(255,255,255,.75); font-size: 13px; display: flex; align-items: center; gap: 6px; }
.banner-topbar-date { color: rgba(255,255,255,.4); font-size: 12px; }
.banner-admin-badge { background: rgba(239,68,68,.2); color: #fca5a5; font-size: 10px; padding: 2px 10px; border-radius: 10px; border: 1px solid rgba(239,68,68,.35); }
.banner-logout { background: rgba(0,229,255,0.08); color: rgba(255,255,255,.6); border: 1px solid rgba(0,229,255,0.15); padding: 5px 14px; border-radius: 6px; font-size: 12px; cursor: pointer; transition: .2s; }
.banner-logout:hover { background: rgba(0,229,255,0.18); color: #00e5ff; border-color: rgba(0,229,255,0.3); }
.banner-hero { display: flex; flex-direction: column; align-items: center; justify-content: center; flex: 1; padding-bottom: 30px; gap: 16px; text-align: center; }
.banner-title { color: #fff; font-size: 28px; font-weight: 800; margin: 0; letter-spacing: 4px; text-shadow: 0 0 20px rgba(0,229,255,0.3); }
.banner-sub { color: rgba(255,255,255,.5); font-size: 13px; margin: 8px 0 0; letter-spacing: 2px; }

/* 导航栏 */
.app-navbar {
  background: rgba(10,22,40,0.9); backdrop-filter: blur(12px); flex-shrink: 0;
  border-bottom: 1px solid rgba(0,229,255,0.12);
  box-shadow: 0 4px 20px rgba(0,0,0,0.3);
  position: sticky; top: 0; z-index: 100;
}
.navbar-inner { max-width: 1280px; margin: 0 auto; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; height: 52px; }
.navbar-links { display: flex; align-items: center; gap: 2px; }
.navbar-link { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: 6px; font-size: 14px; color: rgba(255,255,255,0.65); text-decoration: none; transition: all .2s; font-weight: 500; position: relative; }
.navbar-link:hover { color: var(--accent-cyan); background: rgba(0,229,255,0.08); }
.navbar-link.active { color: var(--accent-cyan); background: rgba(0,229,255,0.12); box-shadow: 0 0 8px rgba(0,229,255,0.2); font-weight: 600; }
.navbar-link.active::after { content: ''; position: absolute; bottom: 2px; left: 50%; transform: translateX(-50%); width: 24px; height: 2px; background: var(--accent-cyan); border-radius: 1px; box-shadow: 0 0 6px rgba(0,229,255,0.5); }
.navbar-sep { width: 1px; height: 22px; background: rgba(255,255,255,0.1); margin: 0 6px; }
.admin-link.active { color: var(--accent-red); background: rgba(255,82,82,0.12); box-shadow: 0 0 8px rgba(255,82,82,0.2); }
.admin-link.active::after { background: var(--accent-red); box-shadow: 0 0 6px rgba(255,82,82,0.5); }
.navbar-date { font-size: 12px; color: var(--text-muted); flex-shrink: 0; }

/* 主体 */
.app-main { flex: 1; }
.app-main.home { padding: 0; }

/* 页脚 */
.app-footer { background: #060e1a; color: var(--text-secondary); flex-shrink: 0; border-top: 1px solid var(--border-default); }
.footer-inner { max-width: 1280px; margin: 0 auto; padding: 36px 24px 24px; display: grid; grid-template-columns: repeat(3, 1fr); gap: 32px; }
.footer-col strong { color: var(--accent-cyan); font-size: 14px; display: block; margin-bottom: 8px; }
.footer-col p { font-size: 12px; line-height: 1.8; margin: 0; color: var(--text-secondary); }
.footer-bottom { text-align: center; font-size: 11px; padding: 14px; border-top: 1px solid rgba(255,255,255,0.05); color: var(--text-muted); }

@media (max-width: 768px) {
  .footer-inner { grid-template-columns: 1fr; }
  .navbar-links { overflow-x: auto; }
  .banner-title { font-size: 22px; }
  .banner-sub { font-size: 11px; }
  .banner-content { padding: 0 20px; }
  .banner-topbar { justify-content: center; flex-wrap: wrap; }
}
</style>
```

---

## 6. src/views/Login.vue (登录页)

```vue
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
      router.push('/home')
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || e?.message || '操作失败')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="login-wrap">
    <div class="bubbles">
      <span v-for="i in 20" :key="i" class="bubble"
        :style="{ left: `${Math.random()*100}%`, animationDelay: `${Math.random()*10}s`, animationDuration: `${6+Math.random()*8}s`, width: `${8+Math.random()*20}px`, height: `${8+Math.random()*20}px` }" />
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
        <el-form-item v-if="isRegister" label="确认密码"><el-input v-model="form.confirmPassword" size="large" type="password" show-password placeholder="请再次输入密码" /></el-form-item>
        <el-form-item v-if="isRegister" label="邮箱（选填）"><el-input v-model="form.email" size="large" placeholder="请输入邮箱" prefix-icon="Message" /></el-form-item>
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
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(160deg, #071526 0%, #0d2847 35%, #0f3a5c 65%, #0c2340 100%);
  overflow: hidden; position: relative;
}
.login-wrap::before {
  content: ''; position: absolute; inset: 0;
  background-image:
    linear-gradient(rgba(0,229,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,229,255,0.03) 1px, transparent 1px);
  background-size: 60px 60px; pointer-events: none;
}
.bubbles { position: absolute; inset: 0; pointer-events: none; }
.bubble {
  position: absolute; bottom: -40px;
  background: radial-gradient(circle, rgba(0,229,255,0.25), transparent);
  border: 1px solid rgba(0,229,255,0.12); border-radius: 50%;
  animation: rise linear infinite;
}
@keyframes rise { 0% { transform: translateY(0) scale(1); opacity: 0.5; } 100% { transform: translateY(-110vh) scale(0.2); opacity: 0; } }
.login-card {
  width: 440px; padding: 48px 44px;
  background: rgba(13,35,64,0.85); backdrop-filter: blur(20px);
  border-radius: 20px; border: 1px solid rgba(0,229,255,0.15);
  box-shadow: 0 25px 80px rgba(0,0,0,0.5), 0 0 40px rgba(0,229,255,0.08);
  position: relative; z-index: 1;
}
.logo-icon { text-align: center; font-size: 48px; margin-bottom: 8px; }
.login-card h1 { text-align: center; font-size: 20px; color: var(--text-primary); margin-bottom: 4px; font-weight: 700; }
.subtitle { text-align: center; color: var(--text-muted); margin-bottom: 28px; font-size: 14px; }
.login-btn { background: linear-gradient(135deg, var(--accent-cyan), var(--accent-teal)); border: none; height: 48px; font-size: 16px; letter-spacing: 4px; color: #0a1628; font-weight: 700; }
.login-btn:hover { background: linear-gradient(135deg, #00b8d4, #0097a7); }
.toggle { text-align: center; margin-top: 20px; color: var(--text-muted); font-size: 13px; }
.toggle a { color: var(--accent-cyan); text-decoration: none; font-weight: 600; }
</style>
```

---

## 7. src/views/Home.vue (首页)

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { oceanDataAPI, alertAPI, monitorAPI, knowledgeAPI } from '@/api'
import type { OceanData, Alert, MonitorPoint, Knowledge } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const router = useRouter()
const overviewData = ref<Record<string, OceanData>>({})
const activeAlerts = ref<Alert[]>([])
const points = ref<MonitorPoint[]>([])
const knowledgeList = ref<Knowledge[]>([])
const totalPoints = ref(0)
const activeCount = ref(0)

onMounted(async () => {
  try {
    const [overviewRes, alertsRes, pointsRes, knowledgeRes]: any = await Promise.all([
      oceanDataAPI.overview(), alertAPI.active(), monitorAPI.list(), knowledgeAPI.publicList({ pageNum: 1, pageSize: 6 }),
    ])
    overviewData.value = overviewRes.data || {}
    activeAlerts.value = alertsRes.data || []
    points.value = pointsRes.data || []
    knowledgeList.value = knowledgeRes.data?.records || []
    totalPoints.value = points.value.length
    activeCount.value = points.value.filter((p: MonitorPoint) => p.status === 'ACTIVE').length
  } catch {}
})

const dataCategories = [
  { key: 'temperature', title: '海水温度', icon: 'thermometer', desc: '表层与垂直水温分布', color: '#ef4444', bg: '#fef2f2', path: '/data-query' },
  { key: 'salinity', title: '海水盐度', icon: 'droplet', desc: '盐度时空变化特征', color: '#0ea5e9', bg: '#eff6ff', path: '/data-query' },
  { key: 'current', title: '洋流系统', icon: 'cycle', desc: '季风环流与深层洋流', color: '#06b6d4', bg: '#ecfeff', path: '/data-query' },
  { key: 'tide', title: '潮汐监测', icon: 'moon', desc: '全日潮与半日潮数据', color: '#8b5cf6', bg: '#f5f3ff', path: '/data-query' },
  { key: 'pollution', title: '污染监测', icon: 'shield', desc: 'COD·重金属·微塑料', color: '#f59e0b', bg: '#fffbeb', path: '/monitor-map' },
  { key: 'ecology', title: '海洋生态', icon: 'fish', desc: '珊瑚礁·红树林·生物多样性', color: '#10b981', bg: '#ecfdf5', path: '/knowledge' },
]

const quickActions = [
  { title: '数据大屏', icon: 'chart', desc: 'ECharts 多图表联动', path: '/dashboard', color: '#0ea5e9' },
  { title: 'AI 助手', icon: 'bot', desc: 'RAG 检索增强问答', path: '/ai-chat', color: '#06b6d4' },
  { title: '监测地图', icon: 'map', desc: 'Leaflet 点位标注', path: '/monitor-map', color: '#10b981' },
  { title: '数据导出', icon: 'download', desc: 'Excel / CSV 导出', path: '/export', color: '#f59e0b' },
]

const alertLevelMap: Record<string, { label: string; color: string }> = {
  RED: { label: '红色', color: '#ef4444' }, ORANGE: { label: '橙色', color: '#f59e0b' },
  YELLOW: { label: '黄色', color: '#eab308' }, INFO: { label: '蓝色', color: '#3b82f6' },
}

const goTo = (path: string) => router.push(path)
</script>

<template>
  <div class="home">
    <!-- 快捷入口 + 统计 -->
    <section class="section section-top">
      <div class="top-grid">
        <div class="top-card primary" @click="goTo('/dashboard')"><SvgIcon name="chart" :size="28" color="#fff"/><div><h3>数据大屏</h3><p>ECharts 多图表可视化</p></div><span class="top-arrow">→</span></div>
        <div class="top-card" @click="goTo('/data-query')"><SvgIcon name="search" :size="28" color="#0ea5e9"/><div><h3>数据查询</h3><p>多维筛选海洋数据</p></div><span class="top-arrow">→</span></div>
        <div class="top-card" @click="goTo('/ai-chat')"><SvgIcon name="bot" :size="28" color="#06b6d4"/><div><h3>AI 助手</h3><p>RAG 检索增强问答</p></div><span class="top-arrow">→</span></div>
        <div class="top-card" @click="goTo('/monitor-map')"><SvgIcon name="map" :size="28" color="#10b981"/><div><h3>监测地图</h3><p>Leaflet 点位标注</p></div><span class="top-arrow">→</span></div>
      </div>
      <div class="top-stats">
        <div class="top-stat"><span class="top-stat-num">{{ totalPoints }}</span><span class="top-stat-label">监测点位</span></div>
        <div class="top-stat"><span class="top-stat-num">{{ Object.keys(overviewData).length }}</span><span class="top-stat-label">海域覆盖</span></div>
        <div class="top-stat"><span class="top-stat-num">{{ activeCount }}</span><span class="top-stat-label">运行中</span></div>
        <div class="top-stat"><span class="top-stat-num">{{ knowledgeList.length }}</span><span class="top-stat-label">科普文章</span></div>
      </div>
    </section>

    <!-- 数据分类卡片 -->
    <section class="section">
      <div class="section-header"><h2><SvgIcon name="folder" :size="22" color="#0ea5e9"/> 数据分类</h2><p>覆盖南海海洋环境核心监测指标</p></div>
      <div class="category-grid">
        <div v-for="cat in dataCategories" :key="cat.key" class="category-card" @click="goTo(cat.path)">
          <div class="category-accent" :style="{ background: cat.color }"></div>
          <div class="category-body">
            <div class="category-icon-wrap" :style="{ background: cat.bg }"><SvgIcon :name="cat.icon" :size="24" :color="cat.color"/></div>
            <div class="category-info"><h3>{{ cat.title }}</h3><p>{{ cat.desc }}</p></div>
          </div>
        </div>
      </div>
    </section>

    <!-- 预警列表 -->
    <section class="section" v-if="activeAlerts.length">
      <div class="section-header"><h2><SvgIcon name="alert" :size="22" color="#ef4444"/> 实时预警</h2><p>当前活跃预警信息</p></div>
      <div class="alert-list">
        <div class="alert-item" v-for="a in activeAlerts.slice(0, 4)" :key="a.id">
          <span class="alert-dot" :style="{ background: alertLevelMap[a.level]?.color || '#ef4444' }"></span>
          <span class="alert-level-tag" :style="{ color: alertLevelMap[a.level]?.color || '#ef4444', background: (alertLevelMap[a.level]?.color || '#ef4444') + '18' }">{{ alertLevelMap[a.level]?.label || a.level }}</span>
          <span class="alert-title">{{ a.title }}</span>
          <span class="alert-time">{{ a.startTime?.substring(0, 10) }}</span>
        </div>
      </div>
    </section>

    <!-- 科普知识 -->
    <section class="section">
      <div class="section-header"><h2><SvgIcon name="book" :size="22" color="#0ea5e9"/> 科普知识</h2><p>了解南海海洋科学</p></div>
      <div class="knowledge-grid">
        <div v-for="item in knowledgeList.slice(0, 6)" :key="item.id" class="knowledge-card" @click="goTo('/knowledge')">
          <div class="knowledge-card-img">
            <SvgIcon :name="item.category === 'OCEAN_CURRENT' ? 'cycle' : item.category === 'TIDE' ? 'moon' : item.category === 'POLLUTION' ? 'shield' : item.category === 'ECOLOGY' ? 'fish' : item.category === 'GEOGRAPHY' ? 'wave' : item.category === 'WATER_QUALITY' ? 'thermometer' : 'book'" :size="28"
              :color="item.category === 'OCEAN_CURRENT' ? '#06b6d4' : item.category === 'TIDE' ? '#8b5cf6' : item.category === 'POLLUTION' ? '#f59e0b' : item.category === 'ECOLOGY' ? '#10b981' : item.category === 'GEOGRAPHY' ? '#0ea5e9' : item.category === 'WATER_QUALITY' ? '#ef4444' : '#0ea5e9'" />
          </div>
          <div class="knowledge-card-body"><h4>{{ item.title }}</h4><p>{{ item.summary }}</p></div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="section">
      <div class="section-header"><h2><SvgIcon name="zap" :size="22" color="#f59e0b"/> 快捷入口</h2><p>快速跳转到各功能模块</p></div>
      <div class="quick-grid">
        <div v-for="act in quickActions" :key="act.title" class="quick-card" @click="goTo(act.path)" :style="{ '--accent': act.color }">
          <SvgIcon :name="act.icon" :size="28" :color="act.color"/><div class="quick-info"><h3>{{ act.title }}</h3><p>{{ act.desc }}</p></div><span class="quick-arrow">→</span>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.section-top { padding-top: 32px; }
.section { max-width: 1200px; margin: 0 auto; padding: 40px 24px; }
.section-header { text-align: center; margin-bottom: 28px; }
.section-header h2 { font-size: 22px; color: var(--text-primary); margin: 0; display: flex; align-items: center; justify-content: center; gap: 10px; font-weight: 700; }
.section-header p { color: var(--text-muted); font-size: 13px; margin: 6px 0 0; }

.top-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.top-card { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 14px; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); cursor: pointer; transition: transform .15s, box-shadow .15s, border-color .15s; }
.top-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); border-color: var(--border-hover); }
.top-card h3 { margin: 0; font-size: 15px; color: var(--text-primary); font-weight: 600; }
.top-card p { margin: 3px 0 0; font-size: 12px; color: var(--text-muted); }
.top-arrow { color: var(--text-muted); font-size: 18px; flex-shrink: 0; }
.top-card.primary { background: linear-gradient(135deg, var(--accent-cyan), var(--accent-teal)); border: none; }
.top-card.primary h3, .top-card.primary p { color: #0a1628; }

.top-stats { display: flex; justify-content: center; gap: 48px; margin-top: 28px; }
.top-stat { text-align: center; }
.top-stat-num { display: block; color: var(--accent-cyan); font-size: 28px; font-weight: 800; font-family: var(--font-display); text-shadow: 0 0 10px rgba(0,229,255,0.4); }
.top-stat-label { color: var(--text-muted); font-size: 13px; margin-top: 2px; }

.category-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
.category-card { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; display: flex; overflow: hidden; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); cursor: pointer; transition: transform .15s, box-shadow .15s, border-color .15s; animation: fadeInUp 0.5s ease-out both; }
.category-card:nth-child(1) { animation-delay: 0.05s; } .category-card:nth-child(2) { animation-delay: 0.1s; } .category-card:nth-child(3) { animation-delay: 0.15s; } .category-card:nth-child(4) { animation-delay: 0.2s; } .category-card:nth-child(5) { animation-delay: 0.25s; } .category-card:nth-child(6) { animation-delay: 0.3s; }
.category-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); border-color: var(--border-hover); }
.category-accent { width: 4px; flex-shrink: 0; }
.category-body { display: flex; align-items: center; gap: 14px; padding: 18px 20px; flex: 1; }
.category-icon-wrap { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.category-info h3 { margin: 0; font-size: 15px; color: var(--text-primary); font-weight: 600; }
.category-info p { margin: 3px 0 0; font-size: 12px; color: var(--text-muted); }

.alert-list { max-width: 800px; margin: 0 auto; background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); }
.alert-item { display: flex; align-items: center; gap: 12px; padding: 14px 20px; border-bottom: 1px solid var(--border-subtle); font-size: 14px; }
.alert-item:last-child { border-bottom: none; }
.alert-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.alert-level-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 600; flex-shrink: 0; }
.alert-title { color: var(--text-primary); font-weight: 500; flex: 1; }
.alert-time { color: var(--text-muted); font-size: 12px; flex-shrink: 0; }

.knowledge-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
.knowledge-card { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; display: flex; overflow: hidden; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); cursor: pointer; transition: transform .15s, box-shadow .15s, border-color .15s; animation: fadeInUp 0.5s ease-out both; }
.knowledge-card:nth-child(1){animation-delay:0s} .knowledge-card:nth-child(2){animation-delay:0.08s} .knowledge-card:nth-child(3){animation-delay:0.16s} .knowledge-card:nth-child(4){animation-delay:0.24s} .knowledge-card:nth-child(5){animation-delay:0.32s} .knowledge-card:nth-child(6){animation-delay:0.4s}
.knowledge-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); border-color: var(--border-hover); }
.knowledge-card-img { width: 80px; display: flex; align-items: center; justify-content: center; background: rgba(0,229,255,0.05); flex-shrink: 0; }
.knowledge-card-body { padding: 14px 16px; flex: 1; min-width: 0; }
.knowledge-card-body h4 { margin: 0; font-size: 14px; color: var(--text-primary); font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.knowledge-card-body p { margin: 4px 0 0; font-size: 12px; color: var(--text-muted); line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.quick-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
.quick-card { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 14px; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); cursor: pointer; transition: transform .15s, box-shadow .15s, border-color .15s; border-left: 4px solid var(--accent, var(--accent-cyan)); animation: fadeInUp 0.5s ease-out both; }
.quick-card:nth-child(1){animation-delay:0.05s} .quick-card:nth-child(2){animation-delay:0.15s} .quick-card:nth-child(3){animation-delay:0.25s} .quick-card:nth-child(4){animation-delay:0.35s}
.quick-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); border-color: var(--border-hover); }
.quick-info h3 { margin: 0; font-size: 15px; color: var(--text-primary); font-weight: 600; }
.quick-info p { margin: 3px 0 0; font-size: 12px; color: var(--text-muted); }
.quick-arrow { color: var(--text-muted); font-size: 18px; flex-shrink: 0; }

@media (max-width: 768px) { .top-grid { grid-template-columns: repeat(2, 1fr); } .top-stats { gap: 24px; flex-wrap: wrap; } }
</style>
```

---

## 8. src/views/Dashboard.vue (数据大屏 · ECharts 暗色主题)

```vue
<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { oceanDataAPI, alertAPI, monitorAPI } from '@/api'
import type { OceanData, Alert, MonitorPoint } from '@/types'

const overviewData = ref<Record<string, OceanData>>({})
const activeAlerts = ref<Alert[]>([])
const trendData = ref<OceanData[]>([])
const points = ref<MonitorPoint[]>([])

const mapChart = ref<HTMLDivElement>()
const trendChart = ref<HTMLDivElement>()
const gaugeChart = ref<HTMLDivElement>()
const barChart = ref<HTMLDivElement>()

let charts: echarts.ECharts[] = []
const initChart = (el: HTMLDivElement | undefined) => { if (!el) return null; const c = echarts.init(el); charts.push(c); return c }

onMounted(async () => {
  const [overviewRes, alertsRes, trendRes, pointsRes]: any = await Promise.all([
    oceanDataAPI.overview(), alertAPI.active(), oceanDataAPI.trend({}), monitorAPI.list(),
  ])
  overviewData.value = overviewRes.data || {}
  activeAlerts.value = alertsRes.data || []
  trendData.value = trendRes.data || []
  points.value = pointsRes.data || []
  await nextTick()
  renderMap(); renderTrend(); renderGauge(); renderBar()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => { charts.forEach(c => c.dispose()); window.removeEventListener('resize', handleResize) })
const handleResize = () => charts.forEach(c => c.resize())

const totalPoints = computed(() => points.value.length)
const activeCount = computed(() => points.value.filter(p => p.status === 'ACTIVE').length)

const alertLevelMap: Record<string, { label: string; color: string; bg: string }> = {
  RED: { label: '红色', color: '#ff5252', bg: '#fef2f2' },
  ORANGE: { label: '橙色', color: '#ff9100', bg: '#fffbeb' },
  YELLOW: { label: '黄色', color: '#eab308', bg: '#fefce8' },
  INFO: { label: '蓝色', color: '#2979ff', bg: '#eff6ff' },
}

// 暗色 ECharts 主题色
const TEXT_COLOR = 'rgba(255,255,255,0.5)'
const LINE_COLOR = 'rgba(255,255,255,0.06)'
const BLUE = '#00e5ff'
const TEAL = '#00bcd4'
const RED = '#ff5252'
const ORANGE = '#ff9100'

function renderMap() {
  const chart = initChart(mapChart.value!); if (!chart) return
  const data = [...new Set(trendData.value.map(d => d.seaArea))].map(area => {
    const d = trendData.value.find(t => t.seaArea === area)
    const coords: Record<string, [number, number]> = {
      '湛江海域': [110.36, 20.90], '茂名海域': [111.29, 21.48], '阳江海域': [111.98, 21.58],
      '珠江口海域': [113.58, 22.30], '万山海域': [113.78, 21.94], '红海湾': [115.38, 22.79],
      '西沙海域': [112.34, 16.83], '南沙海域': [112.90, 9.55], '北部湾': [109.11, 21.02], '粤东海域': [116.73, 23.35],
    }
    return { name: area, value: [...(coords[area] || [113, 18]), d?.temperature || 0, d?.salinity || 0, d?.oxygen || 0] }
  })
  chart.setOption({
    tooltip: { trigger: 'item', formatter: (p: any) => `<b>${p.name}</b><br/>水温: ${p.value[2]}°C<br/>盐度: ${p.value[3]} PSU<br/>溶氧: ${p.value[4]} mg/L` },
    xAxis: { type: 'value', name: '经度', min: 105, max: 120, axisLabel: { color: TEXT_COLOR }, axisLine: { lineStyle: { color: LINE_COLOR } }, splitLine: { lineStyle: { color: LINE_COLOR } } },
    yAxis: { type: 'value', name: '纬度', min: 7, max: 26, axisLabel: { color: TEXT_COLOR }, axisLine: { lineStyle: { color: LINE_COLOR } }, splitLine: { lineStyle: { color: LINE_COLOR } } },
    series: [{
      type: 'scatter', symbolSize: (val: number[]) => Math.max(14, (val[2] as number - 25) * 7), data,
      itemStyle: { shadowBlur: 12, shadowColor: 'rgba(0,229,255,0.25)', color: (p: any) => { const t = p.value[2]; return t > 30 ? RED : t > 28 ? ORANGE : BLUE } },
      label: { show: true, position: 'right', color: 'rgba(255,255,255,0.65)', fontSize: 11, fontWeight: 500 },
      emphasis: { scale: 2, itemStyle: { shadowBlur: 20 } },
    }],
    grid: { left: 55, right: 80, top: 30, bottom: 30 },
  })
}

function renderTrend() { /* 水温与溶氧趋势双Y轴折线图 — 代码较长，使用 BLUE/TEAL/LINE_COLOR/TEXT_COLOR */ }
function renderGauge() { /* 水温仪表盘 — axisLine: 00bcd4 → 00e5ff → ff5252, pointer: #00e5ff, detail: rgba(255,255,255,0.9) */ }
function renderBar() { /* 盐度柱状图 — gradient: #00e5ff → #00bcd4 */ }
</script>

<template>
  <div class="dashboard">
    <div class="page-hero"><!-- Hero 标题栏，暗色背景 + 青色底线 + 霓虹统计数字 --></div>
    <div class="alert-ticker" v-if="activeAlerts.length"><!-- 预警滚动条，暗色玻璃态 --></div>
    <div class="stat-grid"><!-- 统计卡片带科技边角 ::before/::after --></div>
    <div class="chart-section"><!-- ECharts 散点图 --></div>
    <div class="chart-section cols-2"><!-- 双轴趋势图 + 仪表盘 --></div>
    <div class="chart-section cols-2"><!-- 柱状图 + 监测点位列表 --></div>
  </div>
</template>

<style scoped>
.dashboard { margin: -24px; background: var(--bg-primary); min-height: 100vh; }
.page-hero { background: linear-gradient(180deg, #0a1628 0%, #0d2137 50%, #0f2a45 100%); padding: 28px 36px; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; position: relative; overflow: hidden; border-bottom: 1px solid rgba(0,229,255,0.15); }
.page-hero::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 3px; background: linear-gradient(90deg, var(--accent-cyan), var(--accent-teal), var(--accent-blue)); }
.hero-title { color: #fff; font-size: 22px; font-weight: 700; text-shadow: 0 0 20px rgba(0,229,255,0.3); }
.hero-stat-num { color: var(--accent-cyan); font-size: 28px; font-weight: 800; font-family: var(--font-display); text-shadow: 0 0 10px rgba(0,229,255,0.4); }
.alert-ticker { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 0 0 12px 12px; border: 1px solid var(--border-default); border-top: none; }
.stat-card { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; border: 1px solid var(--border-default); position: relative; }
.stat-card::before { content: ''; position: absolute; top: 0; left: 0; width: 16px; height: 16px; border-top: 2px solid var(--accent-cyan); border-left: 2px solid var(--accent-cyan); opacity: 0.6; pointer-events: none; }
.stat-card::after { content: ''; position: absolute; bottom: 0; right: 0; width: 16px; height: 16px; border-bottom: 2px solid var(--accent-cyan); border-right: 2px solid var(--accent-cyan); opacity: 0.6; pointer-events: none; }
.stat-card-value { font-family: var(--font-display); text-shadow: 0 0 10px rgba(0,229,255,0.3); }
.chart-card { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; border: 1px solid var(--border-default); animation: fadeInUp 0.5s ease-out both; }
.chart-card-badge { color: var(--accent-cyan); background: rgba(0,229,255,0.1); border: 1px solid rgba(0,229,255,0.2); }
.point-status.on { background: var(--accent-green); box-shadow: 0 0 6px rgba(0,230,118,0.4); }
.point-status.warn { background: var(--accent-orange); box-shadow: 0 0 6px rgba(255,145,0,0.4); }
</style>
```

---

## 9-15. 其他业务页面（DataQuery / MonitorMap / AIChat / Knowledge / Alerts / ExportData / Admin）

所有页面均统一为深色主题，关键改动：

| 页面 | 改动要点 |
|------|---------|
| **DataQuery** | `.filter-bar` 深色玻璃态 · `.table-card` + el-table 暗色覆盖 · `.data-val` 白色文字 |
| **MonitorMap** | `.map-card` + `.map-toolbar` 暗色 · `.table-card` + `.table-header` 暗色 · legend 文字色调整 |
| **AIChat** | 整体 `.ai-chat` 暗色玻璃态 · 消息区 `rgba(10,22,40,0.5)` · 用户气泡 `#2979ff` · AI 气泡暗色 · 输入框暗底白字 |
| **Knowledge** | `.cat-btn` 暗色 pill · `.article-card` 深色玻璃态 · 详情弹窗 markdown 暗色适配 |
| **Alerts** | `.alert-card` 玻璃态 + 红色卡呼吸光动画 · `.table-card` 暗色 |
| **ExportData** | `.export-card` 暗色 · `.format-box` 暗底青色选中态 |
| **Admin** | `.tab-bar` 暗色 · `.panel` 玻璃态 · 表格暗色覆盖 |

完整代码见项目路径 `E:\Sys_Development\ocean-data-vis\frontend\src\views\*.vue`

---

## 16. frontend-redesign-spec.md (参考设计规范)

```markdown
# 前端界面优化方案 — 深色科技感数据平台

## 色彩系统
- 底色: #0a1628 → #0f2035 → #142d4c (由深到浅)
- 玻璃态卡片: rgba(13, 35, 64, 0.75) + backdrop-filter: blur(8px)
- 主高亮: #00e5ff (cyan neon)
- 文字: rgba(255,255,255,0.95) / 0.7 / 0.45

## 组件级改动
- Shell: 深色底色 + 50px 科技网格线
- Banner: 渐变深蓝 + 径向光晕替代波浪SVG
- Navbar: 深色半透明 + backdrop-filter + 底部霓虹指示线
- 卡片: 深色玻璃态 + 边框 hover 发光
- 图表: ECharts 暗色主题 (透明背景 + 白色轴线 + cyan 数据线)
- 表格: 深色表头 + 白色文字
- Element Plus: 全局 !important 覆盖所有组件

## 动效
- fadeInUp: 卡片逐帧入场 (0.5s, staggered delay)
- glowPulse: 红色预警卡边框呼吸光
- ticker-scroll: 预警滚动条
- 导航 active::after 底部指示线
- 科技边角 ::before/::after (stat-card)
```
