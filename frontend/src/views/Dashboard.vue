<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
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

function renderMap() {
  const chart = initChart(mapChart.value!)
  if (!chart) return
  const data = [...new Set(trendData.value.map(d => d.seaArea))].map(area => {
    const d = trendData.value.find(t => t.seaArea === area)
    // approximate coords for each sea area
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
    title: { text: '🌊 南海海域数据概览', left: 'center', top: 10, textStyle: { color: '#c5e8ff', fontSize: 16 } },
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `<b>${p.name}</b><br/>水温: ${p.value[2]}°C<br/>盐度: ${p.value[3]} PSU<br/>溶氧: ${p.value[4]} mg/L`,
    },
    backgroundColor: 'transparent',
    xAxis: { type: 'value', name: '经度', min: 105, max: 120, axisLabel: { color: '#9fc6e0' }, splitLine: { lineStyle: { color: '#1a3a5c' } } },
    yAxis: { type: 'value', name: '纬度', min: 7, max: 26, axisLabel: { color: '#9fc6e0' }, splitLine: { lineStyle: { color: '#1a3a5c' } } },
    series: [{
      type: 'scatter',
      symbolSize: (val: number[]) => Math.max(12, (val[2] as number - 25) * 6),
      data: data,
      itemStyle: {
        shadowBlur: 20,
        shadowColor: 'rgba(0, 200, 255, 0.5)',
        color: (p: any) => {
          const t = p.value[2]
          return t > 30 ? '#ff6b6b' : t > 28 ? '#ffa502' : '#00d2ff'
        },
      },
      label: { show: true, position: 'right', color: '#b0d4f1', fontSize: 12 },
      emphasis: { scale: 2, itemStyle: { shadowBlur: 30 } },
    }],
    grid: { left: 50, right: 80, top: 50, bottom: 30 },
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
    title: { text: '📈 海域平均水温与溶氧趋势', left: 'center', top: 10, textStyle: { color: '#c5e8ff', fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['平均水温(°C)', '溶解氧(mg/L)'], bottom: 0, textStyle: { color: '#9fc6e0' } },
    backgroundColor: 'transparent',
    xAxis: { type: 'category', data: times, axisLabel: { color: '#9fc6e0', rotate: 30, fontSize: 10 }, axisLine: { lineStyle: { color: '#2a5a8c' } } },
    yAxis: [
      { type: 'value', name: '°C', axisLabel: { color: '#9fc6e0' }, splitLine: { lineStyle: { color: '#1a3a5c' } } },
      { type: 'value', name: 'mg/L', axisLabel: { color: '#9fc6e0' }, splitLine: { show: false } },
    ],
    series: [
      { name: '平均水温(°C)', type: 'line', smooth: true, data: avgTemp, itemStyle: { color: '#ff6b6b' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255,107,107,0.3)' }, { offset: 1, color: 'rgba(255,107,107,0)' }]) }, symbol: 'circle', symbolSize: 6 },
      { name: '溶解氧(mg/L)', type: 'line', smooth: true, yAxisIndex: 1, data: avgOxy, itemStyle: { color: '#00d2ff' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(0,210,255,0.3)' }, { offset: 1, color: 'rgba(0,210,255,0)' }]) }, symbol: 'diamond', symbolSize: 6 },
    ],
    grid: { left: 55, right: 55, top: 50, bottom: 40 },
  })
}

function renderGauge() {
  const chart = initChart(gaugeChart.value!)
  if (!chart) return
  const latest = trendData.value[trendData.value.length - 1]
  chart.setOption({
    title: { text: '🎯 最新观测指标', left: 'center', top: 10, textStyle: { color: '#c5e8ff', fontSize: 14 } },
    backgroundColor: 'transparent',
    series: [
      {
        type: 'gauge', center: ['18%', '60%'], radius: '65%', min: 0, max: 40,
        title: { show: true, offsetCenter: [0, '85%'], fontSize: 11, color: '#9fc6e0' },
        detail: { formatter: '{value}°C', fontSize: 18, color: '#ffa502', offsetCenter: [0, '60%'] },
        data: [{ value: latest?.temperature || 0, name: '水温' }],
        axisLine: { lineStyle: { color: [[0.7, '#00d2ff'], [0.85, '#ffa502'], [1, '#ff6b6b']], width: 8 } },
        pointer: { length: '60%', width: 4 },
      },
      {
        type: 'gauge', center: ['50%', '60%'], radius: '65%', min: 25, max: 40,
        title: { show: true, offsetCenter: [0, '85%'], fontSize: 11, color: '#9fc6e0' },
        detail: { formatter: '{value} PSU', fontSize: 18, color: '#00d2ff', offsetCenter: [0, '60%'] },
        data: [{ value: latest?.salinity || 0, name: '盐度' }],
        axisLine: { lineStyle: { color: [[0.5, '#00d2ff'], [1, '#7b2cbf']], width: 8 } },
        pointer: { length: '60%', width: 4 },
      },
      {
        type: 'gauge', center: ['82%', '60%'], radius: '65%', min: 0, max: 14,
        title: { show: true, offsetCenter: [0, '85%'], fontSize: 11, color: '#9fc6e0' },
        detail: { formatter: '{value} pH', fontSize: 18, color: '#67c23a', offsetCenter: [0, '60%'] },
        data: [{ value: latest?.ph || 0, name: 'pH值' }],
        axisLine: { lineStyle: { color: [[0.5, '#67c23a'], [0.75, '#e6a23c'], [1, '#ff6b6b']], width: 8 } },
        pointer: { length: '60%', width: 4 },
      },
    ],
  })
}

function renderBar() {
  const chart = initChart(barChart.value!)
  if (!chart) return
  chart.setOption({
    title: { text: '🌡️ 各海域多指标对比', left: 'center', top: 10, textStyle: { color: '#c5e8ff', fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: ['水温(°C)', '浪高(m)', '潮汐(m)'], bottom: 0, textStyle: { color: '#9fc6e0' } },
    backgroundColor: 'transparent',
    xAxis: { type: 'category', data: Object.keys(overviewData.value).slice(0, 6), axisLabel: { color: '#9fc6e0', fontSize: 10, rotate: 20 }, axisLine: { lineStyle: { color: '#2a5a8c' } } },
    yAxis: { type: 'value', axisLabel: { color: '#9fc6e0' }, splitLine: { lineStyle: { color: '#1a3a5c' } } },
    series: [
      { name: '水温(°C)', type: 'bar', data: Object.values(overviewData.value).slice(0, 6).map(d => d.temperature), itemStyle: { borderRadius: [4, 4, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#ff6b6b' }, { offset: 1, color: '#c0392b' }]) }, barGap: '10%' },
      { name: '浪高(m)', type: 'bar', data: Object.values(overviewData.value).slice(0, 6).map(d => d.waveHeight), itemStyle: { borderRadius: [4, 4, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#00d2ff' }, { offset: 1, color: '#0984e3' }]) } },
      { name: '潮汐(m)', type: 'bar', data: Object.values(overviewData.value).slice(0, 6).map(d => d.tideLevel), itemStyle: { borderRadius: [4, 4, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#a29bfe' }, { offset: 1, color: '#6c5ce7' }]) } },
    ],
    grid: { left: 55, right: 20, top: 50, bottom: 40 },
  })
}

const getLevelColor = (level: string) => {
  const map: Record<string, string> = { RED: '#ff4757', ORANGE: '#ffa502', YELLOW: '#eccc68', INFO: '#70a1ff' }
  return map[level] || '#70a1ff'
}
</script>

<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <div class="stat-row">
      <div class="stat-card" v-for="(item, key) in overviewData" :key="key">
        <div class="stat-area">{{ key }}</div>
        <div class="stat-main">
          <span class="stat-val">{{ item.temperature ?? '--' }}°C</span>
          <span class="stat-unit">水温</span>
        </div>
        <div class="stat-sub">
          <span>盐度 {{ item.salinity ?? '--' }} PSU</span>
          <span>溶氧 {{ item.oxygen ?? '--' }} mg/L</span>
        </div>
      </div>
      <div class="stat-card alert-card" v-if="activeAlerts.length">
        <div class="stat-area">⚠ 活跃预警</div>
        <div class="stat-main">
          <span class="stat-val">{{ activeAlerts.length }}</span>
          <span class="stat-unit">条</span>
        </div>
        <div class="stat-sub" v-for="a in activeAlerts.slice(0, 2)" :key="a.id">
          <el-tag :color="getLevelColor(a.level)" size="small" effect="dark" style="border: none">{{ a.level }}</el-tag>
          {{ a.title?.substring(0, 15) }}
        </div>
      </div>
    </div>

    <!-- 预警条 -->
    <div class="alert-bar" v-if="activeAlerts.length">
      <span class="alert-item" v-for="a in activeAlerts" :key="a.id">
        <span class="alert-dot" :style="{ background: getLevelColor(a.level) }"></span>
        [{{ a.level }}] {{ a.title }} — {{ a.content?.substring(0, 80) }}
      </span>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <div class="chart-box map-box"><div ref="mapChart" class="chart-inner"></div></div>
      <div class="chart-box"><div ref="trendChart" class="chart-inner"></div></div>
    </div>
    <div class="chart-grid">
      <div class="chart-box"><div ref="gaugeChart" class="chart-inner"></div></div>
      <div class="chart-box"><div ref="barChart" class="chart-inner"></div></div>
    </div>
  </div>
</template>

<style scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(160deg, #0a1628 0%, #0d2137 30%, #0f2b4a 60%, #132e42 100%);
  margin: -20px;
  padding: 20px;
}
.stat-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.stat-card {
  flex: 1;
  min-width: 180px;
  background: linear-gradient(135deg, rgba(15, 43, 74, 0.9), rgba(20, 55, 90, 0.7));
  border: 1px solid rgba(0, 210, 255, 0.15);
  border-radius: 12px;
  padding: 16px 20px;
  backdrop-filter: blur(10px);
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 180, 255, 0.2);
}
.stat-area { font-size: 13px; color: #7ea8c9; margin-bottom: 6px; }
.stat-main { display: flex; align-items: baseline; gap: 6px; margin-bottom: 4px; }
.stat-val { font-size: 34px; font-weight: 800; color: #00d2ff; font-family: 'DIN', monospace; }
.stat-unit { font-size: 13px; color: #5a8ab5; }
.stat-sub { display: flex; gap: 12px; font-size: 12px; color: #7ea8c9; }
.alert-card { border-color: rgba(255, 165, 2, 0.3); }
.alert-card .stat-val { color: #ffa502; }
.alert-bar {
  background: rgba(255, 165, 2, 0.08);
  border: 1px solid rgba(255, 165, 2, 0.15);
  border-radius: 8px;
  padding: 10px 16px;
  margin-bottom: 16px;
  overflow: hidden;
  white-space: nowrap;
}
.alert-item { display: inline-block; margin-right: 40px; font-size: 13px; color: #e0c68c; animation: scroll 25s linear infinite; }
.alert-dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; margin-right: 6px; animation: pulse 2s infinite; }
@keyframes scroll { 0% { transform: translateX(0); } 100% { transform: translateX(-50%); } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.3; } }
.chart-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
.chart-box {
  background: rgba(15, 43, 74, 0.6);
  border: 1px solid rgba(0, 210, 255, 0.1);
  border-radius: 12px;
  padding: 8px;
  backdrop-filter: blur(5px);
}
.map-box { grid-column: 1 / -1; }
.chart-inner { height: 340px; }
@media (max-width: 768px) {
  .chart-grid { grid-template-columns: 1fr; }
  .map-box { grid-column: 1; }
  .stat-card { min-width: 140px; }
  .stat-val { font-size: 24px; }
}
</style>
