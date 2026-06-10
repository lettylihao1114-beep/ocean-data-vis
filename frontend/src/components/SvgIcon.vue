<script setup lang="ts">
defineProps<{ name: string; size?: number; color?: string }>()
</script>

<template>
  <!-- 温度计 -->
  <svg v-if="name === 'thermometer'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z"/>
  </svg>

  <!-- 水滴/盐度 -->
  <svg v-else-if="name === 'droplet'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/>
  </svg>

  <!-- 循环/洋流 -->
  <svg v-else-if="name === 'cycle'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
    <path d="M21 3v5h-5"/>
    <path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>
    <path d="M3 21v-5h5"/>
  </svg>

  <!-- 月亮/潮汐 -->
  <svg v-else-if="name === 'moon'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
  </svg>

  <!-- 盾牌/污染 -->
  <svg v-else-if="name === 'shield'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
  </svg>

  <!-- 鱼/生态 -->
  <svg v-else-if="name === 'fish'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M6.5 12a3.5 3.5 0 0 1 7 0c0 3-3 5.5-3.5 5.5S6.5 15 6.5 12z"/>
    <path d="M19.5 12s-2 5-9 5c-2.5 0-4-.5-5-1"/>
    <circle cx="18" cy="10" r="1.5"/>
  </svg>

  <!-- 波浪/海洋 -->
  <svg v-else-if="name === 'wave'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M3 16c2-3 4-3 6 0 2 3 4 3 6 0 2-3 4-3 6 0"/>
    <path d="M3 12c2-3 4-3 6 0 2 3 4 3 6 0 2-3 4-3 6 0"/>
  </svg>

  <!-- 文件夹 -->
  <svg v-else-if="name === 'folder'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
  </svg>

  <!-- 警告三角形 -->
  <svg v-else-if="name === 'alert'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
    <line x1="12" y1="9" x2="12" y2="13"/>
    <line x1="12" y1="17" x2="12.01" y2="17"/>
  </svg>

  <!-- 书/知识 -->
  <svg v-else-if="name === 'book'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
    <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
  </svg>

  <!-- 闪电/快捷 -->
  <svg v-else-if="name === 'zap'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
  </svg>

  <!-- 图表/大屏 -->
  <svg v-else-if="name === 'chart'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <line x1="18" y1="20" x2="18" y2="10"/>
    <line x1="12" y1="20" x2="12" y2="4"/>
    <line x1="6" y1="20" x2="6" y2="14"/>
  </svg>

  <!-- 搜索 -->
  <svg v-else-if="name === 'search'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <circle cx="11" cy="11" r="8"/>
    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
  </svg>

  <!-- AI/机器人 -->
  <svg v-else-if="name === 'bot'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <rect x="3" y="7" width="18" height="13" rx="2"/>
    <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
    <line x1="12" y1="12" x2="12.01" y2="12"/>
    <circle cx="9" cy="12" r="1"/>
    <circle cx="15" cy="12" r="1"/>
  </svg>

  <!-- 地图 -->
  <svg v-else-if="name === 'map'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"/>
    <line x1="8" y1="2" x2="8" y2="18"/>
    <line x1="16" y1="6" x2="16" y2="22"/>
  </svg>

  <!-- 下载 -->
  <svg v-else-if="name === 'download'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
    <polyline points="7 10 12 15 17 10"/>
    <line x1="12" y1="15" x2="12" y2="3"/>
  </svg>

  <!-- 用户 -->
  <svg v-else-if="name === 'user'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
  </svg>

  <!-- 发送 -->
  <svg v-else-if="name === 'send'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <line x1="22" y1="2" x2="11" y2="13"/>
    <polygon points="22 2 15 22 11 13 2 9 22 2"/>
  </svg>

  <!-- 垃圾桶 -->
  <svg v-else-if="name === 'trash'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <polyline points="3 6 5 6 21 6"/>
    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
    <line x1="10" y1="11" x2="10" y2="17"/>
    <line x1="14" y1="11" x2="14" y2="17"/>
  </svg>

  <!-- 锁 -->
  <svg v-else-if="name === 'lock'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
  </svg>

  <!-- 编辑/铅笔 -->
  <svg v-else-if="name === 'edit'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
  </svg>

  <!-- 信息 -->
  <svg v-else-if="name === 'info'" :width="size||24" :height="size||24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="16" x2="12" y2="12"/>
    <line x1="12" y1="8" x2="12.01" y2="8"/>
  </svg>

  <!-- 空白占位 -->
  <span v-else>{{ name }}</span>
</template>
