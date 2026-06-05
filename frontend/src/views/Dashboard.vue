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
const initChart = (el: HTMLDivElement | undefined) => {
  if (!el) return null
  const c = echarts.init(el)
  charts.push(c)
  return c
}

onMounted(async () => {
  const [overviewRes, alertsRes, trendRes, pointsRes]: any = await Promise.all([
    oceanDataAPI.overview(),
    alertAPI.active(),
    oceanDataAPI.trend({}),
    monitorAPI.list(),
  ])
  overviewData.value = overviewRes.data || {}
  activeAlerts.value = alertsRes.data || []
  trendData.value = trendRes.data || []
  points.value = pointsRes.data || []

  await nextTick()
  renderMap()
  renderTrend()
  renderGauge()
  renderBar()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  charts.forEach(c => c.dispose())
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => charts.forEach(c => c.resize())

const totalPoints = computed(() => points.value.length)
const activeCount = computed(() => points.value.filter(p => p.status === 'ACTIVE').length)

const alertLevelMap: Record<string, { label: string; color: string; bg: string }> = {
  RED: { label: '红色', color: '#ef4444', bg: '#fef2f2' },
  ORANGE: { label: '橙色', color: '#f59e0b', bg: '#fffbeb' },
  YELLOW: { label: '黄色', color: '#eab308', bg: '#fefce8' },
  INFO: { label: '蓝色', color: '#3b82f6', bg: '#eff6ff' },
}

// 图表主题色
const TEXT_COLOR = '#5a6a7e'
const LINE_COLOR = '#e8edf2'
const BLUE = '#0ea5e9'
const TEAL = '#06b6d4'
const RED = '#ef4444'
const ORANGE = '#f59e0b'

function renderMap() {
  const chart = initChart(mapChart.value!)
  if (!chart) return
  const data = [...new Set(trendData.value.map(d => d.seaArea))].map(area => {
    const d = trendData.value.find(t => t.seaArea === area)
    const coords: Record<string, [number, number]> = {
      '湛江海域': [110.36, 20.90], '茂名海域': [111.29, 21.48],
      '阳江海域': [111.98, 21.58], '珠江口海域': [113.58, 22.30],
      '万山海域': [113.78, 21.94], '红海湾': [115.38, 22.79],
      '西沙海域': [112.34, 16.83], '南沙海域': [112.90, 9.55],
      '北部湾': [109.11, 21.02], '粤东海域': [116.73, 23.35],
    }
    return {
      name: area,
      value: [...(coords[area] || [113, 18]), d?.temperature || 0, d?.salinity || 0, d?.oxygen || 0],
    }
  })
  chart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `<b>${p.name}</b><br/>水温: ${p.value[2]}°C<br/>盐度: ${p.value[3]} PSU<br/>溶氧: ${p.value[4]} mg/L`,
    },
    xAxis: { type: 'value', name: '经度', min: 105, max: 120, axisLabel: { color: TEXT_COLOR }, axisLine: { lineStyle: { color: LINE_COLOR } }, splitLine: { lineStyle: { color: LINE_COLOR } } },
    yAxis: { type: 'value', name: '纬度', min: 7, max: 26, axisLabel: { color: TEXT_COLOR }, axisLine: { lineStyle: { color: LINE_COLOR } }, splitLine: { lineStyle: { color: LINE_COLOR } } },
    series: [{
      type: 'scatter',
      symbolSize: (val: number[]) => Math.max(14, (val[2] as number - 25) * 7),
      data: data,
      itemStyle: {
        shadowBlur: 12,
        shadowColor: 'rgba(14, 165, 233, 0.25)',
        color: (p: any) => {
          const t = p.value[2]
          return t > 30 ? RED : t > 28 ? ORANGE : BLUE
        },
      },
      label: { show: true, position: 'right', color: '#475569', fontSize: 11, fontWeight: 500 },
      emphasis: { scale: 2, itemStyle: { shadowBlur: 20 } },
    }],
    grid: { left: 55, right: 80, top: 30, bottom: 30 },
  })
}

function renderTrend() {
  const chart = initChart(trendChart.value!)
  if (!chart) return
  const times = [...new Set(trendData.value.map(d => d.dataTime?.substring(5, 16)))]
  const avgTemp = times.map(t => {
    const items = trendData.value.filter(d => d.dataTime?.substring(5, 16) === t)
    return items.length ? +(items.reduce((s, i) => s + (i.temperature || 0), 0) / items.length).toFixed(1) : 0
  })
  const avgOxy = times.map(t => {
    const items = trendData.value.filter(d => d.dataTime?.substring(5, 16) === t)
    return items.length ? +(items.reduce((s, i) => s + (i.oxygen || 0), 0) / items.length).toFixed(2) : 0
  })
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, textStyle: { fontSize: 12, color: TEXT_COLOR } },
    xAxis: { type: 'category', data: times, axisLabel: { color: TEXT_COLOR, fontSize: 11, rotate: 30 }, axisLine: { lineStyle: { color: LINE_COLOR } } },
    yAxis: [
      { type: 'value', name: '°C', axisLabel: { color: TEXT_COLOR }, splitLine: { lineStyle: { color: LINE_COLOR } } },
      { type: 'value', name: 'mg/L', axisLabel: { color: TEXT_COLOR }, splitLine: { show: false } },
    ],
    series: [
      { name: '水温', type: 'line', data: avgTemp, smooth: true, symbol: 'circle', symbolSize: 6, lineStyle: { color: BLUE, width: 2.5 }, itemStyle: { color: BLUE }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(14,165,233,.15)' }, { offset: 1, color: 'rgba(14,165,233,0)' }]) } },
      { name: '溶氧', type: 'line', yAxisIndex: 1, data: avgOxy, smooth: true, symbol: 'diamond', symbolSize: 6, lineStyle: { color: TEAL, width: 2.5 }, itemStyle: { color: TEAL }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(6,182,212,.15)' }, { offset: 1, color: 'rgba(6,182,212,0)' }]) } },
    ],
    grid: { left: 50, right: 50, top: 25, bottom: 35 },
  })
}

function renderGauge() {
  const chart = initChart(gaugeChart.value!)
  if (!chart) return
  const latest = trendData.value[trendData.value.length - 1]
  const temp = latest?.temperature || 26
  chart.setOption({
    series: [{
      type: 'gauge', startAngle: 210, endAngle: -30, center: ['50%', '55%'], radius: '85%',
      min: 0, max: 40,
      axisLine: { show: true, lineStyle: { width: 20, color: [[0.3, '#06b6d4'], [0.7, '#0ea5e9'], [1, '#ef4444']] } },
      axisTick: { show: false },
      splitLine: { show: false },
      axisLabel: { show: false },
      pointer: { length: '60%', width: 6, itemStyle: { color: '#0a3d62' } },
      detail: { valueAnimation: true, fontSize: 36, color: '#0a3d62', fontWeight: 700, offsetCenter: [0, '60%'], formatter: '{value}°C' },
      data: [{ value: temp, name: '当前水温' }],
    }],
  })
}

function renderBar() {
  const chart = initChart(barChart.value!)
  if (!chart) return
  const areas = [...new Set(trendData.value.map(d => d.seaArea))]
  const salinities = areas.map(area => {
    const items = trendData.value.filter(d => d.seaArea === area)
    return items.length ? +(items.reduce((s, i) => s + (i.salinity || 0), 0) / items.length).toFixed(2) : 0
  })
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: (p: any) => `${p[0].name}<br/>平均盐度: ${p[0].value} PSU` },
    xAxis: { type: 'category', data: areas, axisLabel: { color: TEXT_COLOR, fontSize: 10, rotate: 35 }, axisLine: { lineStyle: { color: LINE_COLOR } } },
    yAxis: { type: 'value', name: 'PSU', axisLabel: { color: TEXT_COLOR }, splitLine: { lineStyle: { color: LINE_COLOR } } },
    series: [{
      type: 'bar', data: salinities, barWidth: '55%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#0ea5e9' }, { offset: 1, color: '#06b6d4' }]),
      },
    }],
    grid: { left: 45, right: 15, top: 20, bottom: 40 },
  })
}
</script>

<template>
  <div class="dashboard">

    <!-- 大屏标题 -->
    <div class="page-hero">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="hero-icon">🌊</span>
          南海海洋环境数据可视化
        </h1>
        <p class="hero-sub">South China Sea Environmental Data Visualization · 国家地球系统科学数据中心</p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="hero-stat-num">{{ totalPoints }}</span>
          <span class="hero-stat-label">监测点位</span>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-num">{{ Object.keys(overviewData).length }}</span>
          <span class="hero-stat-label">海域覆盖</span>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-num">{{ activeCount }}</span>
          <span class="hero-stat-label">运行中</span>
        </div>
      </div>
    </div>

    <!-- 预警滚动条 -->
    <div class="alert-ticker" v-if="activeAlerts.length">
      <div class="alert-ticker-label">⚠️ 预警</div>
      <div class="alert-ticker-wrap">
        <div class="alert-ticker-track">
          <span class="alert-ticker-item" v-for="(a, i) in [...activeAlerts, ...activeAlerts]" :key="`${a.id}-${i}`">
            <span class="alert-dot" :style="{ background: alertLevelMap[a.level]?.color }"></span>
            <span class="alert-badge" :style="{ background: alertLevelMap[a.level]?.bg, color: alertLevelMap[a.level]?.color }">{{ alertLevelMap[a.level]?.label }}</span>
            {{ a.title }}
          </span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="(item, key) in overviewData" :key="key">
        <div class="stat-card-accent" :style="{ background: (item.temperature ?? 0) > 30 ? '#ef4444' : '#0ea5e9' }"></div>
        <div class="stat-card-body">
          <div class="stat-card-header">
            <span class="stat-card-icon">{{ (item.temperature ?? 0) > 30 ? '🔥' : '💧' }}</span>
            <span class="stat-card-title">{{ key }}</span>
          </div>
          <div class="stat-card-value" :style="{ color: (item.temperature ?? 0) > 30 ? '#ef4444' : '#0a3d62' }">
            {{ item.temperature ?? '--' }}<small>°C</small>
          </div>
          <div class="stat-card-meta">
            <span>盐度 {{ item.salinity ?? '--' }} PSU</span>
            <span class="stat-sep">|</span>
            <span>溶氧 {{ item.oxygen ?? '--' }} mg/L</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-section">
      <div class="chart-card chart-full">
        <div class="chart-card-header">
          <h3>📍 南海海域数据概览</h3>
          <span class="chart-card-badge">散点图 · 水温热力</span>
        </div>
        <div ref="mapChart" class="chart-body" style="height: 380px"></div>
      </div>
    </div>

    <div class="chart-section cols-2">
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>📈 水温与溶氧趋势</h3>
          <span class="chart-card-badge">双轴折线图</span>
        </div>
        <div ref="trendChart" class="chart-body" style="height: 300px"></div>
      </div>
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>🌡️ 当前水温仪表</h3>
          <span class="chart-card-badge">实时监测</span>
        </div>
        <div ref="gaugeChart" class="chart-body" style="height: 300px"></div>
      </div>
    </div>

    <div class="chart-section cols-2">
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>📊 各海域平均盐度</h3>
          <span class="chart-card-badge">柱状图</span>
        </div>
        <div ref="barChart" class="chart-body" style="height: 300px"></div>
      </div>

      <!-- 监测点位列表 -->
      <div class="chart-card">
        <div class="chart-card-header">
          <h3>🟢 监测点位状态</h3>
          <span class="chart-card-badge">{{ activeCount }}/{{ totalPoints }} 运行中</span>
        </div>
        <div class="point-list">
          <div class="point-row" v-for="p in points.slice(0, 8)" :key="p.id">
            <div class="point-status" :class="p.status === 'ACTIVE' ? 'on' : p.status === 'MAINTENANCE' ? 'warn' : 'off'"></div>
            <div class="point-info">
              <div class="point-name">{{ p.name }}</div>
              <div class="point-type">{{ p.seaArea || '' }} · {{ p.type === 'WATER_QUALITY' ? '水质' : p.type === 'CURRENT' ? '洋流' : p.type === 'POLLUTION' ? '污染' : '潮汐' }}</div>
            </div>
            <span class="point-tag" :class="p.status === 'ACTIVE' ? 'on' : p.status === 'MAINTENANCE' ? 'warn' : 'off'">
              {{ p.status === 'ACTIVE' ? '运行中' : p.status === 'MAINTENANCE' ? '维护' : '停用' }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ========== 整体 ========== */
.dashboard {
  margin: -24px;
  background: linear-gradient(175deg, #eef5fa 0%, #e3eff9 30%, #f5f9fc 100%);
  min-height: 100vh;
}

/* ========== Hero 标题栏 ========== */
.page-hero {
  background: linear-gradient(135deg, #06203a 0%, #0a3d62 50%, #0d5e8a 100%);
  padding: 28px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap; gap: 16px;
  position: relative;
  overflow: hidden;
}
.page-hero::after {
  content: '';
  position: absolute; bottom: 0; left: 0; right: 0; height: 3px;
  background: linear-gradient(90deg, #06b6d4, #0ea5e9, #3b82f6);
}
.hero-content { position: relative; z-index: 1; }
.hero-title { color: #fff; font-size: 22px; font-weight: 700; margin: 0; display: flex; align-items: center; gap: 10px; }
.hero-icon { font-size: 30px; }
.hero-sub { color: rgba(255,255,255,.55); font-size: 12px; margin: 6px 0 0; letter-spacing: .5px; }
.hero-stats { display: flex; gap: 24px; position: relative; z-index: 1; }
.hero-stat {
  text-align: center; padding: 0 20px;
  border-left: 1px solid rgba(255,255,255,.15);
}
.hero-stat:first-child { border-left: none; }
.hero-stat-num { display: block; color: #fff; font-size: 28px; font-weight: 800; }
.hero-stat-label { color: rgba(255,255,255,.5); font-size: 12px; margin-top: 2px; }

/* ========== 预警滚动条 ========== */
.alert-ticker {
  display: flex; align-items: center;
  margin: 0 24px; background: #fff; border-radius: 0 0 12px 12px;
  padding: 10px 16px; box-shadow: 0 1px 6px rgba(0,0,0,.04);
}
.alert-ticker-label {
  flex-shrink: 0; font-weight: 700; font-size: 13px; color: #ef4444;
  padding-right: 14px; margin-right: 14px;
  border-right: 1px solid #fee2e2;
}
.alert-ticker-wrap { flex: 1; overflow: hidden; }
.alert-ticker-track {
  display: flex; gap: 32px;
  animation: ticker-scroll 30s linear infinite;
  white-space: nowrap;
}
@keyframes ticker-scroll { 0% { transform: translateX(0); } 100% { transform: translateX(-50%); } }
.alert-ticker-item { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; color: #475569; }
.alert-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.alert-badge { font-size: 10px; padding: 1px 6px; border-radius: 4px; font-weight: 600; }

/* ========== 统计卡片网格 ========== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 14px; padding: 20px 24px 8px;
}
.stat-card {
  background: #fff; border-radius: 12px;
  display: flex; overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,.04);
  border: 1px solid #eaf0f6;
  transition: transform .15s, box-shadow .15s;
}
.stat-card:hover { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(6,32,58,.08); }
.stat-card-accent { width: 4px; flex-shrink: 0; }
.stat-card-body { padding: 16px 18px; flex: 1; }
.stat-card-header { display: flex; align-items: center; gap: 6px; margin-bottom: 8px; }
.stat-card-icon { font-size: 18px; }
.stat-card-title { font-size: 13px; color: #64748b; font-weight: 500; }
.stat-card-value { font-size: 30px; font-weight: 800; line-height: 1; margin-bottom: 6px; font-family: 'Inter', sans-serif; }
.stat-card-value small { font-size: 14px; font-weight: 500; }
.stat-card-meta { font-size: 12px; color: #94a3b8; display: flex; gap: 6px; }
.stat-sep { color: #d1d5db; }

/* ========== 图表区 ========== */
.chart-section {
  padding: 0 24px 16px;
  display: grid; gap: 14px;
}
.chart-section.cols-2 { grid-template-columns: 1fr 1fr; }
@media (max-width: 1080px) { .chart-section.cols-2 { grid-template-columns: 1fr; } }
.chart-card {
  background: #fff; border-radius: 12px;
  box-shadow: 0 1px 6px rgba(0,0,0,.04);
  border: 1px solid #eaf0f6;
  overflow: hidden;
}
.chart-card-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px 0;
}
.chart-card-header h3 { margin: 0; font-size: 14px; color: #1e293b; font-weight: 600; }
.chart-card-badge {
  font-size: 11px; color: #94a3b8;
  background: #f1f5f9; padding: 3px 10px; border-radius: 20px;
}
.chart-body { width: 100%; }

/* ========== 监测点位列表 ========== */
.point-list { padding: 8px 20px 16px; }
.point-row {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 0; border-bottom: 1px solid #f1f5f9;
}
.point-row:last-child { border-bottom: none; }
.point-status { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.point-status.on { background: #10b981; box-shadow: 0 0 6px rgba(16,185,129,.4); }
.point-status.warn { background: #f59e0b; box-shadow: 0 0 6px rgba(245,158,11,.4); }
.point-status.off { background: #94a3b8; }
.point-info { flex: 1; min-width: 0; }
.point-name { font-size: 13px; color: #1e293b; font-weight: 500; }
.point-type { font-size: 11px; color: #94a3b8; margin-top: 2px; }
.point-tag {
  font-size: 11px; padding: 2px 10px; border-radius: 10px; font-weight: 500;
}
.point-tag.on { background: #ecfdf5; color: #10b981; }
.point-tag.warn { background: #fffbeb; color: #f59e0b; }
.point-tag.off { background: #f1f5f9; color: #94a3b8; }
</style>
