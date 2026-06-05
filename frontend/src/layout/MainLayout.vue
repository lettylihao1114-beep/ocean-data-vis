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
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside width="220px" style="background: #001529">
      <div class="logo">
        🌊 海洋数据平台
      </div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#fff"
        style="border-right: none"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
        <template v-if="authStore.role === 'ADMIN'">
          <el-menu-item v-for="item in adminMenus" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主体 -->
    <el-container>
      <el-header style="background: #fff; display: flex; justify-content: flex-end; align-items: center; border-bottom: 1px solid #eee; padding: 0 24px">
        <span style="margin-right: 16px; color: #666">
          👤 {{ authStore.username }}
          <el-tag v-if="authStore.role === 'ADMIN'" size="small" type="danger" style="margin-left: 8px">管理员</el-tag>
        </span>
        <el-button text @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main style="background: #f0f2f5; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
</style>
