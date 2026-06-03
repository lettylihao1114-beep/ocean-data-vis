import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '数据大屏' },
        },
        {
          path: 'data-query',
          name: 'DataQuery',
          component: () => import('@/views/DataQuery.vue'),
          meta: { title: '数据查询' },
        },
        {
          path: 'monitor-map',
          name: 'MonitorMap',
          component: () => import('@/views/MonitorMap.vue'),
          meta: { title: '监测地图' },
        },
        {
          path: 'alerts',
          name: 'Alerts',
          component: () => import('@/views/Alerts.vue'),
          meta: { title: '预警管理' },
        },
        {
          path: 'knowledge',
          name: 'Knowledge',
          component: () => import('@/views/Knowledge.vue'),
          meta: { title: '科普知识' },
        },
        {
          path: 'export',
          name: 'Export',
          component: () => import('@/views/ExportData.vue'),
          meta: { title: '数据导出' },
        },
        {
          path: 'admin',
          name: 'Admin',
          component: () => import('@/views/Admin.vue'),
          meta: { title: '系统管理', role: 'ADMIN' },
        },
      ],
    },
  ],
})

// 路由守卫 — 未登录跳转 & 角色权限检查
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== localStorage.getItem('role')) {
    ElMessage.error('权限不足，仅管理员可访问')
    next('/dashboard')
  } else {
    next()
  }
})

export default router
