<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { oceanDataAPI } from '@/api'
import type { OceanData, OceanDataQuery } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const loading = ref(false)
const list = ref<OceanData[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = ref<OceanDataQuery>({ seaArea: '', startTime: '', endTime: '' })
const seaAreas = ['', '湛江海域', '茂名海域', '阳江海域', '珠江口海域', '万山海域', '红海湾', '西沙海域', '南沙海域', '北部湾', '粤东海域']

const fetchData = async () => {
  loading.value = true
  try {
    const q: any = { ...query.value }
    if (!q.seaArea) delete q.seaArea
    const res: any = await oceanDataAPI.page({ pageNum: pageNum.value, pageSize: pageSize.value, query: q })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(fetchData)

const tempTagType = (t: number) => t > 30 ? 'danger' : t > 26 ? 'warning' : 'success'
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2><SvgIcon name="search" :size="22" color="#0ea5e9"/> 海洋数据查询</h2>
      <p>多维筛选南海海洋环境观测数据</p>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-select v-model="query.seaArea" clearable placeholder="全部海域" style="width:180px" size="large">
          <el-option v-for="a in seaAreas" :key="a" :label="a || '全部海域'" :value="a" />
        </el-select>
        <el-date-picker v-model="query.startTime" type="datetime" placeholder="开始时间" format="YYYY-MM-DD HH:mm:ss" size="large" />
        <span class="filter-sep">—</span>
        <el-date-picker v-model="query.endTime" type="datetime" placeholder="结束时间" format="YYYY-MM-DD HH:mm:ss" size="large" />
        <el-button type="primary" size="large" @click="fetchData" :loading="loading" class="filter-btn">
          <SvgIcon name="search" :size="16"/> 查询
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="dataTime" label="观测时间" width="170" />
        <el-table-column prop="seaArea" label="海域" width="110" />
        <el-table-column prop="longitude" label="经度" width="90" />
        <el-table-column prop="latitude" label="纬度" width="85" />
        <el-table-column label="水温" width="95">
          <template #default="{ row }">
            <el-tag :type="tempTagType(row.temperature)" size="small" effect="dark">{{ row.temperature }}°C</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="salinity" label="盐度" width="85">
          <template #default="{ row }">
            <span class="data-val">{{ row.salinity }} <small>PSU</small></span>
          </template>
        </el-table-column>
        <el-table-column prop="oxygen" label="溶氧" width="85">
          <template #default="{ row }">
            <span class="data-val">{{ row.oxygen }} <small>mg/L</small></span>
          </template>
        </el-table-column>
        <el-table-column prop="ph" label="pH" width="80" />
        <el-table-column prop="currentSpeed" label="洋流速度" width="95">
          <template #default="{ row }">
            <span class="data-val">{{ row.currentSpeed }} <small>m/s</small></span>
          </template>
        </el-table-column>
        <el-table-column prop="currentDir" label="洋流方向" width="95" />
        <el-table-column prop="tideLevel" label="潮汐" width="85">
          <template #default="{ row }">
            <span class="data-val">{{ row.tideLevel }} <small>m</small></span>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <span class="table-total">共 {{ total }} 条记录</span>
        <el-pagination
          v-model:current-page="pageNum" v-model:page-size="pageSize"
          :total="total" layout="prev, pager, next, sizes"
          @current-change="fetchData" @size-change="fetchData" size="small"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: #0a3d62; display: flex; align-items: center; gap: 10px; font-weight: 700; }
.page-header p { margin: 4px 0 0; color: #94a3b8; font-size: 13px; }

.filter-bar { background: #fff; border-radius: 12px; padding: 20px 24px; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; margin-bottom: 14px; }
.filter-row { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.filter-sep { color: #cbd5e1; }
.filter-btn { display: inline-flex; align-items: center; gap: 6px; }

.table-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; }
.table-card :deep(.el-table) { --el-table-border-color: #f1f5f9; }
.table-card :deep(.el-table th) { background: #f8fafc; color: #475569; font-weight: 600; }
.table-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) { background: #fafcfd; }
.table-footer { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; border-top: 1px solid #f1f5f9; }
.table-total { font-size: 13px; color: #94a3b8; }
.data-val { color: #1e293b; font-weight: 500; }
.data-val small { color: #94a3b8; font-weight: 400; }
</style>
