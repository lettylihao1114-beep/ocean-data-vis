<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { oceanDataAPI } from '@/api'
import type { OceanData, OceanDataQuery } from '@/types'

const loading = ref(false)
const list = ref<OceanData[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = ref<OceanDataQuery>({
  seaArea: '',
  startTime: '',
  endTime: '',
})

const seaAreas = ['', '湛江海域', '茂名海域', '阳江海域', '珠江口海域', '万山海域', '红海湾', '西沙海域', '南沙海域', '北部湾', '粤东海域']

const fetchData = async () => {
  loading.value = true
  try {
    const q: any = { ...query.value }
    if (!q.seaArea) delete q.seaArea
    const res: any = await oceanDataAPI.page({ pageNum: pageNum.value, pageSize: pageSize.value, query: q })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <el-form :inline="true" style="margin-bottom: 16px">
      <el-form-item label="海域">
        <el-select v-model="query.seaArea" clearable placeholder="全部海域" style="width: 160px">
          <el-option v-for="a in seaAreas" :key="a" :label="a || '全部'" :value="a" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="query.startTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="query.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="fetchData" :loading="loading">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="dataTime" label="时间" width="160" />
      <el-table-column prop="seaArea" label="海域" width="100" />
      <el-table-column prop="longitude" label="经度" width="100" />
      <el-table-column prop="latitude" label="纬度" width="90" />
      <el-table-column prop="temperature" label="水温(°C)" width="100">
        <template #default="{ row }">
          <el-tag :type="row.temperature > 30 ? 'danger' : row.temperature > 26 ? 'warning' : 'success'" size="small">
            {{ row.temperature }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="salinity" label="盐度(PSU)" width="100" />
      <el-table-column prop="oxygen" label="溶氧(mg/L)" width="100" />
      <el-table-column prop="ph" label="pH" width="80" />
      <el-table-column prop="currentSpeed" label="洋流(m/s)" width="100" />
      <el-table-column prop="currentDir" label="洋流方向" width="100" />
      <el-table-column prop="tideLevel" label="潮汐(m)" width="90" />
      <el-table-column prop="waveHeight" label="浪高(m)" width="90" />
      <el-table-column prop="pressure" label="气压(hPa)" width="100" />
    </el-table>

    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next, sizes"
      @current-change="fetchData"
      @size-change="fetchData"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </el-card>
</template>
