<script setup lang="ts">
import { ref } from 'vue'
import { oceanDataAPI, exportAPI } from '@/api'

const downloading = ref(false)
const query = ref({
  seaArea: '',
  startTime: '',
  endTime: '',
})
const format = ref<'excel' | 'csv'>('excel')

const handleExport = async () => {
  downloading.value = true
  try {
    const fn = format.value === 'excel' ? exportAPI.excel : exportAPI.csv
    const res: any = await fn(query.value)
    const blob = new Blob([res], { type: format.value === 'excel' ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `ocean_data_export.${format.value === 'excel' ? 'xlsx' : 'csv'}`
    a.click()
    URL.revokeObjectURL(url)
  } catch {}
  downloading.value = false
}
</script>

<template>
  <el-card header="数据导出">
    <el-form label-width="100px" style="max-width: 500px">
      <el-form-item label="导出格式">
        <el-radio-group v-model="format">
          <el-radio value="excel">Excel (.xlsx)</el-radio>
          <el-radio value="csv">CSV (.csv)</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="海域（可选）">
        <el-select v-model="query.seaArea" clearable placeholder="全部海域">
          <el-option label="湛江海域" value="湛江海域" />
          <el-option label="茂名海域" value="茂名海域" />
          <el-option label="阳江海域" value="阳江海域" />
          <el-option label="珠江口海域" value="珠江口海域" />
          <el-option label="西沙海域" value="西沙海域" />
          <el-option label="南沙海域" value="南沙海域" />
          <el-option label="北部湾" value="北部湾" />
        </el-select>
      </el-form-item>
      <el-form-item label="时间范围（可选）">
        <el-date-picker
          v-model="query.startTime"
          type="datetime"
          placeholder="开始时间"
          format="YYYY-MM-DD HH:mm:ss"
          style="margin-right: 8px"
        />
        <el-date-picker
          v-model="query.endTime"
          type="datetime"
          placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="downloading" @click="handleExport" :icon="'Download'">
          导出 {{ format === 'excel' ? 'Excel' : 'CSV' }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>
