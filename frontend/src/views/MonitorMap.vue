<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { monitorAPI } from '@/api'
import type { MonitorPoint } from '@/types'

const points = ref<MonitorPoint[]>([])
const mapRef = ref<HTMLDivElement>()

const typeFilter = ref('')
const statusFilter = ref('')

const fetchData = async () => {
  const res: any = await monitorAPI.list({ type: typeFilter.value, status: statusFilter.value })
  points.value = res.data || []
  renderMap()
}

const renderMap = () => {
  if (!mapRef.value) return
  const chart = echarts.init(mapRef.value)
  // 使用散点图模拟地图标注
  chart.setOption({
    title: { text: '南海监测点位分布', left: 'center' },
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `<b>${p.name}</b><br/>类型: ${p.type}<br/>状态: ${p.status}<br/>${p.desc || ''}`,
    },
    xAxis: { type: 'value', name: '经度', min: 105, max: 120 },
    yAxis: { type: 'value', name: '纬度', min: 8, max: 26 },
    series: [{
      type: 'scatter',
      symbolSize: 14,
      data: points.value.map(p => ({
        name: p.name,
        value: [p.longitude, p.latitude],
        type: p.type,
        status: p.status,
        desc: p.description || '',
      })),
      itemStyle: {
        color: (p: any) => p.status === 'ACTIVE' ? '#67c23a' : p.status === 'MAINTENANCE' ? '#e6a23c' : '#909399',
      },
      label: { show: true, position: 'right', fontSize: 11, formatter: (p: any) => p.name },
      emphasis: { scale: 1.8 },
    }],
    grid: { left: 60, right: 160, top: 50, bottom: 40 },
  })
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <el-form :inline="true" style="margin-bottom: 16px">
      <el-form-item label="监测类型">
        <el-select v-model="typeFilter" clearable @change="fetchData" placeholder="全部">
          <el-option label="水质监测" value="WATER_QUALITY" />
          <el-option label="洋流监测" value="CURRENT" />
          <el-option label="污染监测" value="POLLUTION" />
          <el-option label="潮汐监测" value="TIDE" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="statusFilter" clearable @change="fetchData" placeholder="全部">
          <el-option label="运行中" value="ACTIVE" />
          <el-option label="维护中" value="MAINTENANCE" />
          <el-option label="已停用" value="INACTIVE" />
        </el-select>
      </el-form-item>
    </el-form>

    <div ref="mapRef" style="height: 500px"></div>

    <el-table :data="points" style="margin-top: 16px" border stripe>
      <el-table-column prop="name" label="监测点名称" width="180" />
      <el-table-column prop="longitude" label="经度" width="100" />
      <el-table-column prop="latitude" label="纬度" width="100" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.type === 'WATER_QUALITY'">水质监测</el-tag>
          <el-tag v-else-if="row.type === 'CURRENT'" type="warning">洋流监测</el-tag>
          <el-tag v-else-if="row.type === 'POLLUTION'" type="danger">污染监测</el-tag>
          <el-tag v-else type="info">潮汐监测</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : row.status === 'MAINTENANCE' ? 'warning' : 'info'">
            {{ row.status === 'ACTIVE' ? '运行中' : row.status === 'MAINTENANCE' ? '维护中' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="seaArea" label="所属海域" width="110" />
      <el-table-column prop="description" label="描述" />
    </el-table>
  </el-card>
</template>
