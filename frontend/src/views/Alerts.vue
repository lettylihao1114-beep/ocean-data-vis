<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { alertAPI } from '@/api'
import type { Alert } from '@/types'
import { ElMessage } from 'element-plus'

const list = ref<Alert[]>([])
const active = ref<Alert[]>([])
const loading = ref(false)

const fetchData = async () => {
  const [activeRes, allRes]: any = await Promise.all([
    alertAPI.active(),
    alertAPI.page({ pageNum: 1, pageSize: 50 }),
  ])
  active.value = activeRes.data || []
  list.value = allRes.data?.records || []
}

const handleResolve = async (id: number) => {
  await alertAPI.resolve(id)
  ElMessage.success('已解除')
  fetchData()
}

const getLevelType = (level: string) => {
  const map: Record<string, any> = { RED: 'danger', ORANGE: 'warning', YELLOW: 'warning', INFO: 'info' }
  return map[level] || 'info'
}

onMounted(fetchData)
</script>

<template>
  <div>
    <el-card v-if="active.length" style="margin-bottom: 16px" header="⚠️ 当前活跃预警">
      <el-row :gutter="16">
        <el-col :span="8" v-for="a in active" :key="a.id">
          <el-alert :title="`[${a.level}] ${a.title}`" :type="getLevelType(a.level)" :description="a.content?.substring(0, 120) + '...'" show-icon style="margin-bottom: 8px">
            <template #default>
              <el-button size="small" type="primary" @click="handleResolve(a.id!)" style="margin-top: 8px">解除预警</el-button>
            </template>
          </el-alert>
        </el-col>
      </el-row>
    </el-card>

    <el-card header="全部预警记录">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="level" label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="110" />
        <el-table-column prop="title" label="标题" width="260" />
        <el-table-column prop="seaArea" label="影响海域" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'danger' : 'success'">
              {{ row.status === 'ACTIVE' ? '活跃' : '已解除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ACTIVE'" size="small" type="primary" text @click="handleResolve(row.id)">解除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>
