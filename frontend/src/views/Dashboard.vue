<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { oceanDataAPI, alertAPI } from '@/api'
import type { OceanData, Alert } from '@/types'

const overviewData = ref<Record<string, OceanData>>({})
const activeAlerts = ref<Alert[]>([])
const trendData = ref<OceanData[]>([])

const tempChart = ref<HTMLDivElement>()
const salinityChart = ref<HTMLDivElement>()

onMounted(async () => {
  const [overviewRes, alertsRes, trendRes]: any = await Promise.all([
    oceanDataAPI.overview(),
    alertAPI.active(),
    oceanDataAPI.trend({}),
  ])
  overviewData.value = overviewRes.data || {}
  activeAlerts.value = alertsRes.data || []
  trendData.value = trendRes.data || []

  renderTempChart()
  renderSalinityChart()
})

function renderTempChart() {
  if (!tempChart.value) return
  const chart = echarts.init(tempChart.value)
  const areas = [...new Set(trendData.value.map(d => d.seaArea))]
  chart.setOption({
    title: { text: '各海域水温对比 (°C)', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: areas, bottom: 0 },
    xAxis: { type: 'category', data: [...new Set(trendData.value.map(d => d.dataTime?.substring(0, 10)))] },
    yAxis: { type: 'value' },
    series: areas.map(area => ({
      name: area,
      type: 'line',
      smooth: true,
      data: trendData.value.filter(d => d.seaArea === area).map(d => d.temperature),
    })),
    grid: { left: 60, right: 30, top: 50, bottom: 40 },
  })
}

function renderSalinityChart() {
  if (!salinityChart.value) return
  const chart = echarts.init(salinityChart.value)
  const areas = [...new Set(trendData.value.map(d => d.seaArea))]
  chart.setOption({
    title: { text: '各海域盐度对比 (PSU)', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { data: areas, bottom: 0 },
    xAxis: { type: 'category', data: [...new Set(trendData.value.map(d => d.dataTime?.substring(0, 10)))] },
    yAxis: { type: 'value' },
    series: areas.map(area => ({
      name: area,
      type: 'bar',
      data: trendData.value.filter(d => d.seaArea === area).map(d => d.salinity),
    })),
    grid: { left: 60, right: 30, top: 50, bottom: 40 },
  })
}
</script>

<template>
  <div>
    <!-- 指标卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6" v-for="(item, key) in overviewData" :key="key">
        <el-card shadow="hover">
          <div class="card-title">{{ key }}</div>
          <div class="card-value">{{ item.temperature ?? '--' }}°C</div>
          <div class="card-detail">盐度: {{ item.salinity ?? '--' }} PSU | 溶氧: {{ item.oxygen ?? '--' }} mg/L</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活跃预警 -->
    <el-alert
      v-for="alert in activeAlerts"
      :key="alert.id"
      :title="`[${alert.level}] ${alert.title}`"
      :description="alert.content?.substring(0, 100)"
      :type="alert.level === 'RED' ? 'error' : alert.level === 'ORANGE' ? 'warning' : 'info'"
      show-icon
      closable
      style="margin-bottom: 8px"
    />

    <!-- 图表 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card><div ref="tempChart" style="height: 350px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div ref="salinityChart" style="height: 350px"></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.card-title { font-size: 14px; color: #999; }
.card-value { font-size: 32px; font-weight: 700; color: #0a3d62; }
.card-detail { font-size: 12px; color: #bbb; margin-top: 4px; }
</style>
