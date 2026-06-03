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

// 图表主题色
const TEXT_COLOR = '#5a6a7e'
const LINE_COLOR = '#e8edf2'
const BLUE = '#3b82f6'
const TEAL = '#0ea5e9'
const RED = '#ef4444'
const ORANGE = '#f59e0b'
const GREEN = '#10b981'
const PURPLE = '#8b5cf6'

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
    title: { text: '南海海域数据概览', left: 'center', top: 8, textStyle: { color: '#1e293b', fontSize: 16, fontWeight: 600 } },
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
        shadowColor: 'rgba(59, 130, 246, 0.3)',
        color: (p: any) => {
          const t = p.value[2]
          return t > 30 ? RED : t > 28 ? ORANGE : BLUE
        },
      },
      label: { show: true, position: 'right', color: '#475569', fontSize: 12, fontWeight: 500 },
      emphasis: { scale: 2, itemStyle: { shadowBlur: 20 } },
    }],
    grid: { left: 55, right: 80, top: 50, bottom: 30 },
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
    title: { text: '海域平均水温与溶氧趋势', left: 'center', top: 8, textStyle: { color: '#1e293b', fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均水温(°C)', '溶解氧(mg/L)'], bottom: 0, textStyle: { color: TEXT_COLOR } },
    xAxis: { type: 'category', data: times, axisLabel: { color: TEXT_COLOR, rotate: 30, fontSize: 10 }, axisLine: { lineStyle: { color: LINE_COLOR } }, axisTick: { alignWithLabel: true } },
    yAxis: [
      { type: 'value', name: '°C', axisLabel: { color: TEXT_COLOR }, splitLine: { lineStyle: { color: LINE_COLOR } } },
      { type: 'value', name: 'mg/L', axisLabel: { color: TEXT_COLOR }, splitLine: { show: false } },
    ],
    series: [
      { name: '平均水温(°C)', type: 'line', smooth: true, data: avgTemp, itemStyle: { color: RED }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(239,68,68,0.15)' }, { offset: 1, color: 'rgba(239,68,68,0)' }]) }, symbol: 'circle', symbolSize: 6 },
      { name: '溶解氧(mg/L)', type: 'line', smooth: true, yAxisIndex: 1, data: avgOxy, itemStyle: { color: TEAL }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(14,165,233,0.15)' }, { offset: 1, color: 'rgba(14,165,233,0)' }]) }, symbol: 'diamond', symbolSize: 6 },
    ],
    grid: { left: 55, right: 55, top: 50, bottom: 40 },
  })
}

function renderGauge() {
  const chart = initChart(gaugeChart.value!)
  if (!chart) return
  const latest = trendData.value[trendData.value.length - 1]
  chart.setOption({
    title: { text: '最新观测指标', left: 'center', top: 8, textStyle: { color: '#1e293b', fontSize: 14, fontWeight: 600 } },
    series: [
      {
        type: 'gauge', center: ['18%', '60%'], radius: '65%', min: 0, max: 40,
        title: { show: true, offsetCenter: [0, '85%'], fontSize: 11, color: TEXT_COLOR },
        detail: { formatter: '{value}°C', fontSize: 20, color: ORANGE, offsetCenter: [0, '60%'], fontWeight: 600 },
        data: [{ value: latest?.temperature || 0, name: '水温' }],
        axisLine: { lineStyle: { color: [[0.7, TEAL], [0.85, ORANGE], [1, RED]], width: 10 } },
        pointer: { length: '60%', width: 5, itemStyle: { color: '#475569' } },
        axisTick: { distance: -10, length: 6, lineStyle: { color: '#cbd5e1' } },
        splitLine: { distance: -20, length: 14, lineStyle: { color: '#cbd5e1' } },
        axisLabel: { distance: 20, color: TEXT_COLOR, fontSize: 10 },
      },
      {
        type: 'gauge', center: ['50%', '60%'], radius: '65%', min: 25, max: 40,
        title: { show: true, offsetCenter: [0, '85%'], fontSize: 11, color: TEXT_COLOR },
        detail: { formatter: '{value} PSU', fontSize: 20, color: BLUE, offsetCenter: [0, '60%'], fontWeight: 600 },
        data: [{ value: latest?.salinity || 0, name: '盐度' }],
        axisLine: { lineStyle: { color: [[0.5, BLUE], [1, PURPLE]], width: 10 } },
        pointer: { length: '60%', width: 5, itemStyle: { color: '#475569' } },
        axisTick: { distance: -10, length: 6, lineStyle: { color: '#cbd5e1' } },
        splitLine: { distance: -20, length: 14, lineStyle: { color: '#cbd5e1' } },
        axisLabel: { distance: 20, color: TEXT_COLOR, fontSize: 10 },
      },
      {
        type: 'gauge', center: ['82%', '60%'], radius: '65%', min: 0, max: 14,
        title: { show: true, offsetCenter: [0, '85%'], fontSize: 11, color: TEXT_COLOR },
        detail: { formatter: '{value} pH', fontSize: 20, color: GREEN, offsetCenter: [0, '60%'], fontWeight: 600 },
        data: [{ value: latest?.ph || 0, name: 'pH值' }],
        axisLine: { lineStyle: { color: [[0.5, GREEN], [0.75, ORANGE], [1, RED]], width: 10 } },
        pointer: { length: '60%', width: 5, itemStyle: { color: '#475569' } },
        axisTick: { distance: -10, length: 6, lineStyle: { color: '#cbd5e1' } },
        splitLine: { distance: -20, length: 14, lineStyle: { color: '#cbd5e1' } },
        axisLabel: { distance: 20, color: TEXT_COLOR, fontSize: 10 },
      },
    ],
  })
}

function renderBar() {
  const chart = initChart(barChart.value!)
  if (!chart) return
  const areas = Object.keys(overviewData.value).slice(0, 6)
  const temps = Object.values(overviewData.value).slice(0, 6).map(d => d.temperature)
  const waves = Object.values(overviewData.value).slice(0, 6).map(d => d.waveHeight)
  const tides = Object.values(overviewData.value).slice(0, 6).map(d => d.tideLevel)
  chart.setOption({
    title: { text: '各海域多指标对比', left: 'center', top: 8, textStyle: { color: '#1e293b', fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['水温(°C)', '浪高(m)', '潮汐(m)'], bottom: 0, textStyle: { color: TEXT_COLOR } },
    xAxis: { type: 'category', data: areas, axisLabel: { color: TEXT_COLOR, fontSize: 10, rotate: 20 }, axisLine: { lineStyle: { color: LINE_COLOR } }, axisTick: { alignWithLabel: true } },
    yAxis: { type: 'value', axisLabel: { color: TEXT_COLOR }, splitLine: { lineStyle: { color: LINE_COLOR } } },
    series: [
      { name: '水温(°C)', type: 'bar', data: temps, itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#f87171' }, { offset: 1, color: '#ef4444' }]) }, barGap: '12%', barWidth: '22%' },
      { name: '浪高(m)', type: 'bar', data: waves, itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#38bdf8' }, { offset: 1, color: '#0ea5e9' }]) }, barWidth: '22%' },
      { name: '潮汐(m)', type: 'bar', data: tides, itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#a78bfa' }, { offset: 1, color: '#8b5cf6' }]) }, barWidth: '22%' },
    ],
    grid: { left: 55, right: 20, top: 50, bottom: 40 },
  })
}

// 预警等级
const alertLevelMap: Record<string, { label: string; color: string; bg: string }> = {
  RED: { label: '红色', color: '#dc2626', bg: '#fef2f2' },
  ORANGE: { label: '橙色', color: '#ea580c', bg: '#fff7ed' },
  YELLOW: { label: '黄色', color: '#ca8a04', bg: '#fefce8' },
  INFO: { label: '蓝色', color: '#2563eb', bg: '#eff6ff' },
}

// 统计指标
const totalPoints = computed(() => points.value.length)
const activeCount = computed(() => points.value.filter(p => p.status === 'ACTIVE').length)
</script>

<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <div class="stat-row">
      <div class="stat-card">
        <div class="stat-icon" style="background: #eff6ff">
          <span style="font-size: 24px">📍</span>
        </div>
        <div class="stat-body">
          <div class="stat-label">监测点位</div>
          <div class="stat-value">{{ totalPoints }} <small>个</small></div>
          <div class="stat-desc">运行中 {{ activeCount }} 个</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: #f0fdf4">
          <span style="font-size: 24px">🌡️</span>
        </div>
        <div class="stat-body">
          <div class="stat-label">海域覆盖</div>
          <div class="stat-value">{{ Object.keys(overviewData).length }} <small>个</small></div>
          <div class="stat-desc">南海重点海域</div>
        </div>
      </div>
      <div class="stat-card" v-for="(item, key) in overviewData" :key="key">
        <div class="stat-icon" :style="{ background: (item.temperature ?? 0) > 30 ? '#fef2f2' : '#eff6ff' }">
          <span style="font-size: 24px">{{ (item.temperature ?? 0) > 30 ? '🔥' : '💧' }}</span>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ key }}</div>
          <div class="stat-value" :style="{ color: (item.temperature ?? 0) > 30 ? '#ef4444' : '#0ea5e9' }">
            {{ item.temperature ?? '--' }}°<small>C</small>
          </div>
          <div class="stat-desc">
            盐度 {{ item.salinity ?? '--' }} · 溶氧 {{ item.oxygen ?? '--' }}
          </div>
        </div>
      </div>
      <div class="stat-card alert-summary" v-if="activeAlerts.length">
        <div class="stat-icon" style="background: #fef2f2">
          <span style="font-size: 24px">⚠️</span>
        </div>
        <div class="stat-body">
          <div class="stat-label">活跃预警</div>
          <div class="stat-value" style="color: #ef4444">{{ activeAlerts.length }} <small>条</small></div>
          <div class="stat-desc">
            <span v-for="a in activeAlerts.slice(0, 2)" :key="a.id" :style="{ color: alertLevelMap[a.level]?.color }">
              {{ alertLevelMap[a.level]?.label }}预警 ·
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 预警滚动条 -->
    <div class="alert-bar" v-if="activeAlerts.length">
      <div class="alert-scroll">
        <span class="alert-item" v-for="(a, i) in [...activeAlerts, ...activeAlerts]" :key="`${a.id}-${i}`">
          <span class="alert-dot" :style="{ background: alertLevelMap[a.level]?.color }"></span>
          <span class="alert-tag" :style="{ background: alertLevelMap[a.level]?.bg, color: alertLevelMap[a.level]?.color }">
            {{ alertLevelMap[a.level]?.label }}
          </span>
          {{ a.title }} — {{ a.content?.substring(0, 60) }}...
        </span>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-row">
      <div class="chart-card map-card">
        <div ref="mapChart" class="chart-inner"></div>
      </div>
    </div>
    <div class="chart-row cols-2">
      <div class="chart-card">
        <div ref="trendChart" class="chart-inner"></div>
      </div>
      <div class="chart-card">
        <div ref="gaugeChart" class="chart-inner"></div>
      </div>
    </div>
    <div class="chart-row cols-2">
      <div class="chart-card">
        <div ref="barChart" class="chart-inner"></div>
      </div>
      <!-- 监测点列表 -->
      <div class="chart-card">
        <div class="card-title">监测点位状态</div>
        <div class="point-list">
          <div class="point-item" v-for="p in points.slice(0, 8)" :key="p.id">
            <div class="point-status-dot" :class="p.status === 'ACTIVE' ? 'active' : p.status === 'MAINTENANCE' ? 'maintenance' : 'inactive'"></div>
            <div class="point-info">
              <div class="point-name">{{ p.name }}</div>
              <div class="point-meta">{{ p.seaArea || '' }} · {{ p.type === 'WATER_QUALITY' ? '水质' : p.type === 'CURRENT' ? '洋流' : p.type === 'POLLUTION' ? '污染' : '潮汐' }}</div>
            </div>
            <el-tag size="small" :type="p.status === 'ACTIVE' ? 'success' : p.status === 'MAINTENANCE' ? 'warning' : 'info'" round>
              {{ p.status === 'ACTIVE' ? '运行中' : p.status === 'MAINTENANCE' ? '维护中' : '停用' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(180deg, #f0f5fa 0%, #e8f0f8 30%, #f8fafc 100%);
  margin: -20px;
  padding: 24px;
}

/* ===== 统计卡片 ===== */
.stat-row {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.stat-card {
  flex: 1;
  min-width: 200px;
  background: #fff;
  border-radius: 14px;
  padding: 18px 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  border: 1px solid #e8edf4;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 150, 200, 0.1);
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-body { flex: 1; min-width: 0; }
.stat-label { font-size: 12px; color: #94a3b8; margin-bottom: 2px; font-weight: 500; }
.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.1;
  font-family: 'DIN', 'SF Mono', monospace;
}
.stat-value small { font-size: 14px; font-weight: 500; color: #94a3b8; }
.stat-desc { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.alert-summary { border-left: 3px solid #ef4444; }

/* ===== 预警滚动条 ===== */
.alert-bar {
  background: linear-gradient(90deg, #fef2f2, #fff7ed);
  border: 1px solid #fee2e2;
  border-radius: 10px;
  padding: 10px 0;
  margin-bottom: 18px;
  overflow: hidden;
}
.alert-scroll {
  display: flex;
  white-space: nowrap;
  animation: scroll-left 30s linear infinite;
}
.alert-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-right: 48px;
  font-size: 13px;
  color: #64748b;
  flex-shrink: 0;
}
.alert-dot { width: 7px; height: 7px; border-radius: 50%; animation: pulse 2s infinite; }
.alert-tag { padding: 1px 8px; border-radius: 4px; font-size: 11px; font-weight: 600; }
@keyframes scroll-left {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
@keyframes pulse { 0%, 100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.3); } }

/* ===== 图表卡片 ===== */
.chart-row { display: grid; gap: 16px; margin-bottom: 16px; }
.chart-row.cols-2 { grid-template-columns: 1fr 1fr; }
.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid #e8edf4;
}
.map-card { grid-column: 1 / -1; }
.chart-inner { height: 340px; }
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  text-align: center;
  margin-bottom: 12px;
}

/* ===== 监测点位列表 ===== */
.point-list { max-height: 300px; overflow-y: auto; }
.point-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}
.point-item:last-child { border-bottom: none; }
.point-status-dot {
  width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
}
.point-status-dot.active { background: #10b981; box-shadow: 0 0 6px rgba(16, 185, 129, 0.4); }
.point-status-dot.maintenance { background: #f59e0b; box-shadow: 0 0 6px rgba(245, 158, 11, 0.4); }
.point-status-dot.inactive { background: #94a3b8; }
.point-info { flex: 1; min-width: 0; }
.point-name { font-size: 13px; font-weight: 600; color: #1e293b; }
.point-meta { font-size: 11px; color: #94a3b8; margin-top: 1px; }

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .chart-row.cols-2 { grid-template-columns: 1fr; }
  .map-card { grid-column: 1; }
  .stat-card { min-width: 150px; }
  .stat-value { font-size: 22px; }
}
</style>
