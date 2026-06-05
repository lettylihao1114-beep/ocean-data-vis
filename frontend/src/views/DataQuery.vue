<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { oceanDataAPI } from '@/api'
import type { OceanData, OceanDataQuery } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const loading = ref(false)
const list = ref<OceanData[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)

const query = ref<OceanDataQuery>({ seaArea: '', startTime: '', endTime: '' })
const seaAreas = ['湛江海域', '茂名海域', '阳江海域', '珠江口海域', '万山海域', '红海湾', '西沙海域', '南沙海域', '北部湾', '粤东海域']

const fetchData = async () => {
  loading.value = true
  try {
    const q: any = { ...query.value }
    if (!q.seaArea) delete q.seaArea
    if (!q.startTime) delete q.startTime
    if (!q.endTime) delete q.endTime
    const res: any = await oceanDataAPI.page({ pageNum: pageNum.value, pageSize: pageSize.value, query: q })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

onMounted(fetchData)

const hasFilter = computed(() => !!(query.value.seaArea || query.value.startTime || query.value.endTime))

const resetFilter = () => {
  query.value = { seaArea: '', startTime: '', endTime: '' }
  pageNum.value = 1
  fetchData()
}

// 水温标签类型
const tempTagType = (t: number) => t > 30 ? 'danger' : t > 26 ? 'warning' : 'success'

// 统计概览
const tempRange = computed(() => {
  if (!list.value.length) return null
  const temps = list.value.map(d => d.temperature).filter(t => t != null) as number[]
  if (!temps.length) return null
  const min = Math.min(...temps).toFixed(1)
  const max = Math.max(...temps).toFixed(1)
  const avg = (temps.reduce((s, v) => s + v, 0) / temps.length).toFixed(1)
  return { min, max, avg }
})

const uniqueAreas = computed(() => new Set(list.value.map(d => d.seaArea).filter(Boolean)).size)
</script>

<template>
  <div class="page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><SvgIcon name="search" :size="22" color="#0ea5e9"/> 海洋数据查询</h2>
      <p>多维筛选南海海洋环境观测数据 · 共 {{ total }} 条记录</p>
    </div>

    <!-- 统计概览卡片 -->
    <div class="stats-row" v-if="list.length">
      <div class="stats-card">
        <span class="stats-num">{{ total }}</span>
        <span class="stats-label">总记录数</span>
      </div>
      <div class="stats-card">
        <span class="stats-num">{{ uniqueAreas }}</span>
        <span class="stats-label">覆盖海域</span>
      </div>
      <div class="stats-card" v-if="tempRange">
        <span class="stats-num">{{ tempRange.avg }}°C</span>
        <span class="stats-label">平均水温</span>
      </div>
      <div class="stats-card" v-if="tempRange">
        <span class="stats-num">{{ tempRange.min }} ~ {{ tempRange.max }}°C</span>
        <span class="stats-label">水温范围</span>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-card">
      <div class="filter-title">
        <SvgIcon name="search" :size="16" color="#0ea5e9"/>
        <span>筛选条件</span>
        <button v-if="hasFilter" class="reset-btn" @click="resetFilter">清除筛选</button>
      </div>
      <div class="filter-body">
        <div class="filter-item">
          <label>海域</label>
          <el-select v-model="query.seaArea" clearable placeholder="全部海域" size="large" style="width:100%">
            <el-option v-for="a in seaAreas" :key="a" :label="a" :value="a" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>开始时间</label>
          <el-date-picker v-model="query.startTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm:ss" size="large" style="width:100%" />
        </div>
        <div class="filter-item">
          <label>结束时间</label>
          <el-date-picker v-model="query.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm:ss" size="large" style="width:100%" />
        </div>
        <div class="filter-item filter-actions">
          <label>&nbsp;</label>
          <el-button type="primary" size="large" @click="pageNum = 1; fetchData()" :loading="loading">
            <SvgIcon name="search" :size="16"/> 查询
          </el-button>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" stripe size="medium">
        <el-table-column prop="dataTime" label="观测时间" width="170" sortable />
        <el-table-column prop="seaArea" label="海域" width="120">
          <template #default="{ row }">
            <span class="area-tag">{{ row.seaArea }}</span>
          </template>
        </el-table-column>
        <el-table-column label="坐标" width="150">
          <template #default="{ row }">
            <span class="coord-text">{{ row.longitude?.toFixed(2) }}, {{ row.latitude?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="水温" width="100" sortable>
          <template #default="{ row }">
            <el-tag :type="tempTagType(row.temperature)" size="small" effect="dark">{{ row.temperature }}°C</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="盐度" width="90">
          <template #default="{ row }">
            <span class="data-val">{{ row.salinity }} <small>PSU</small></span>
          </template>
        </el-table-column>
        <el-table-column label="溶氧" width="90">
          <template #default="{ row }">
            <span class="data-val">{{ row.oxygen }} <small>mg/L</small></span>
          </template>
        </el-table-column>
        <el-table-column prop="ph" label="pH" width="80" />
        <el-table-column label="洋流" width="140">
          <template #default="{ row }">
            <span v-if="row.currentSpeed != null" class="data-val">{{ row.currentSpeed }} m/s · {{ row.currentDir || '--' }}°</span>
            <span v-else class="data-na">--</span>
          </template>
        </el-table-column>
        <el-table-column label="潮汐" width="90">
          <template #default="{ row }">
            <span v-if="row.tideLevel != null" class="data-val">{{ row.tideLevel }} <small>m</small></span>
            <span v-else class="data-na">--</span>
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
.page {
  padding: 4px 0;
  max-width: 1400px;
  margin: 0 auto;
}

/* ========== 页面标题 ========== */
.page-header {
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #0a3d62;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}
.page-header p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 13px;
}

/* ========== 统计概览 ========== */
.stats-row {
  display: flex;
  gap: 14px;
  margin-bottom: 14px;
}
.stats-card {
  flex: 1;
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  border: 1px solid #eaf0f6;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
  text-align: center;
}
.stats-num {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #0a3d62;
  font-family: 'Inter', sans-serif;
}
.stats-label {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* ========== 筛选栏 ========== */
.filter-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eaf0f6;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
  margin-bottom: 14px;
  overflow: hidden;
}
.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  background: #f8fafc;
  border-bottom: 1px solid #f1f5f9;
}
.reset-btn {
  margin-left: auto;
  border: none;
  background: none;
  color: #94a3b8;
  font-size: 12px;
  cursor: pointer;
  padding: 4px 10px;
  border-radius: 4px;
}
.reset-btn:hover {
  color: #ef4444;
  background: #fef2f2;
}
.filter-body {
  display: flex;
  gap: 14px;
  padding: 18px 20px;
  align-items: flex-end;
}
.filter-item {
  flex: 1;
  min-width: 160px;
}
.filter-item label {
  display: block;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
  font-weight: 500;
}
.filter-actions {
  flex: 0 0 auto;
  min-width: auto;
}

/* ========== 表格 ========== */
.table-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
  border: 1px solid #eaf0f6;
}
.table-card :deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 13px;
}
.table-card :deep(.el-table td.el-table__cell) {
  font-size: 13px;
}
.table-card :deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: #fafcfd;
}
.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
}
.table-total {
  font-size: 13px;
  color: #94a3b8;
}

/* 数据展示 */
.data-val {
  color: #1e293b;
  font-weight: 500;
  font-size: 13px;
}
.data-val small {
  color: #94a3b8;
  font-weight: 400;
}
.data-na {
  color: #cbd5e1;
}
.area-tag {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 20px;
  background: #eff6ff;
  color: #3b82f6;
  font-size: 12px;
  font-weight: 500;
}
.coord-text {
  color: #64748b;
  font-size: 12px;
  font-family: 'JetBrains Mono', 'Fira Code', monospace;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .filter-body {
    flex-wrap: wrap;
  }
  .filter-item {
    flex: 1 1 calc(50% - 7px);
    min-width: 140px;
  }
  .stats-row {
    flex-wrap: wrap;
  }
  .stats-card {
    flex: 1 1 calc(50% - 7px);
  }
}
@media (max-width: 600px) {
  .filter-item {
    flex: 1 1 100%;
  }
  .stats-card {
    flex: 1 1 100%;
  }
}
</style>
