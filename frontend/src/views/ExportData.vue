<script setup lang="ts">
import { ref } from 'vue'
import { exportAPI } from '@/api'
import { ElMessage } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'

const downloading = ref(false)
const query = ref({ seaArea: '', startTime: '', endTime: '' })
const format = ref<'excel' | 'csv'>('excel')

const handleExport = async () => {
  downloading.value = true
  try {
    const fn = format.value === 'excel' ? exportAPI.excel : exportAPI.csv
    const res: any = await fn(query.value)
    const blob = res instanceof Blob ? res : new Blob([res], { type: format.value === 'excel' ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `ocean_data_export.${format.value === 'excel' ? 'xlsx' : 'csv'}`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败，请重试') }
  downloading.value = false
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2><SvgIcon name="download" :size="22" color="#0ea5e9"/> 数据导出</h2>
      <p>导出海洋观测数据为 Excel 或 CSV 格式</p>
    </div>

    <div class="export-card">
      <div class="export-icon-wrap">
        <SvgIcon name="download" :size="40" color="#0ea5e9"/>
      </div>
      <div class="export-form">
        <div class="format-select">
          <label class="format-label">导出格式</label>
          <div class="format-options">
            <label class="format-option" :class="{ active: format === 'excel' }">
              <input type="radio" v-model="format" value="excel" />
              <div class="format-box">
                <span class="format-box-icon">📊</span>
                <span class="format-box-title">Excel</span>
                <span class="format-box-ext">.xlsx</span>
              </div>
            </label>
            <label class="format-option" :class="{ active: format === 'csv' }">
              <input type="radio" v-model="format" value="csv" />
              <div class="format-box">
                <span class="format-box-icon">📄</span>
                <span class="format-box-title">CSV</span>
                <span class="format-box-ext">.csv</span>
              </div>
            </label>
          </div>
        </div>

        <div class="filter-row">
          <div class="filter-item">
            <label>海域（可选）</label>
            <el-select v-model="query.seaArea" clearable placeholder="全部海域" size="large" style="width:100%">
              <el-option v-for="a in ['湛江海域','茂名海域','阳江海域','珠江口海域','西沙海域','南沙海域','北部湾']" :key="a" :label="a" :value="a" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>开始时间（可选）</label>
            <el-date-picker v-model="query.startTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm:ss" size="large" style="width:100%" />
          </div>
          <div class="filter-item">
            <label>结束时间（可选）</label>
            <el-date-picker v-model="query.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm:ss" size="large" style="width:100%" />
          </div>
        </div>

        <el-button type="primary" size="large" :loading="downloading" @click="handleExport" class="export-btn">
          <SvgIcon name="download" :size="18"/> 导出 {{ format === 'excel' ? 'Excel' : 'CSV' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 900px; margin: 0 auto; padding: 4px 24px; }
.page-header { margin-bottom: 24px; }
.page-header h2 { margin: 0; font-size: 22px; color: #0a3d62; display: flex; align-items: center; gap: 10px; font-weight: 700; }
.page-header p { margin: 4px 0 0; color: #94a3b8; font-size: 13px; }

.export-card {
  background: #fff; border-radius: 16px; padding: 40px;
  box-shadow: 0 2px 12px rgba(0,0,0,.06); border: 1px solid #eaf0f6;
  display: flex; gap: 36px; align-items: flex-start;
}
.export-icon-wrap {
  width: 88px; height: 88px; border-radius: 18px;
  background: linear-gradient(135deg, #eff6ff, #e0f2fe);
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.export-form { flex: 1; }

.format-label { font-size: 15px; font-weight: 600; color: #1e293b; display: block; margin-bottom: 12px; }
.format-options { display: flex; gap: 14px; margin-bottom: 28px; }
.format-option input { display: none; }
.format-box {
  padding: 20px 28px; border-radius: 12px; border: 2px solid #e2e8f0;
  cursor: pointer; text-align: center; transition: .2s; display: flex; flex-direction: column; gap: 6px; min-width: 100px;
}
.format-option.active .format-box { border-color: #0ea5e9; background: #f0f9ff; box-shadow: 0 0 0 4px rgba(14,165,233,.1); }
.format-box:hover { border-color: #0ea5e9; transform: translateY(-1px); }
.format-box-icon { font-size: 28px; }
.format-box-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.format-box-ext { font-size: 11px; color: #94a3b8; }

.filter-row { display: flex; gap: 16px; margin-bottom: 28px; flex-wrap: wrap; }
.filter-item { flex: 1; min-width: 180px; }
.filter-item label { display: block; font-size: 13px; color: #64748b; margin-bottom: 6px; font-weight: 500; }

.export-btn { display: inline-flex; align-items: center; gap: 8px; padding: 14px 36px; height: auto; font-size: 16px; border-radius: 12px; }

@media (max-width: 768px) {
  .export-card { flex-direction: column; padding: 24px; }
  .filter-row { flex-direction: column; }
}
</style>
