<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { alertAPI } from '@/api'
import type { Alert } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')
const list = ref<Alert[]>([])
const active = ref<Alert[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const form = reactive({
  type: '',
  level: '',
  title: '',
  content: '',
  seaArea: '',
  startTime: '',
  endTime: '',
})

const typeOptions = ['TYPHOON', 'EXTREME_TEMP', 'TIDE', 'RED_TIDE', 'POLLUTION', 'OTHER']
const levelOptions = ['INFO', 'YELLOW', 'ORANGE', 'RED']
const seaAreas = ['南海中部', '湛江海域', '茂名海域', '阳江海域', '珠江口海域', '万山海域', '红海湾', '西沙海域', '南沙海域', '北部湾', '粤东海域', '南海沿岸']

const fetchData = async () => {
  loading.value = true
  try {
    const [activeRes, allRes]: any = await Promise.all([
      alertAPI.active(),
      alertAPI.page({ pageNum: 1, pageSize: 50 }),
    ])
    active.value = activeRes.data || []
    list.value = allRes.data?.records || []
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.type = ''
  form.level = ''
  form.title = ''
  form.content = ''
  form.seaArea = ''
  form.startTime = ''
  form.endTime = ''
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.type || !form.level || !form.title || !form.content) {
    ElMessage.warning('请填写类型、等级、标题和内容')
    return
  }
  submitting.value = true
  try {
    await alertAPI.add(form)
    ElMessage.success('预警发布成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

const handleResolve = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认解除该预警？', '提示', { type: 'warning' })
    await alertAPI.resolve(id)
    ElMessage.success('已解除')
    fetchData()
  } catch { /* 用户取消 */ }
}

const getLevelType = (level: string) => {
  const map: Record<string, any> = { RED: 'danger', ORANGE: 'warning', YELLOW: 'warning', INFO: 'info' }
  return map[level] || 'info'
}

const typeLabel = (type: string) => {
  const map: Record<string, string> = {
    TYPHOON: '台风', EXTREME_TEMP: '极端水温', TIDE: '潮汐异常',
    RED_TIDE: '赤潮', POLLUTION: '污染', OTHER: '其他'
  }
  return map[type] || type
}

onMounted(fetchData)
</script>

<template>
  <div>
    <!-- 活跃预警 -->
    <el-card v-if="active.length" style="margin-bottom: 16px" header="⚠️ 当前活跃预警">
      <el-row :gutter="16">
        <el-col :span="8" v-for="a in active" :key="a.id">
          <el-alert :title="`[${a.level}] ${a.title}`" :type="getLevelType(a.level)" :description="(a.content || '').substring(0, 120) + '...'" show-icon style="margin-bottom: 8px">
            <template #default>
              <el-button v-if="isAdmin" size="small" type="primary" @click="handleResolve(a.id!)" style="margin-top: 8px">解除预警</el-button>
            </template>
          </el-alert>
        </el-col>
      </el-row>
    </el-card>

    <!-- 全部预警 -->
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>全部预警记录</span>
          <el-button v-if="isAdmin" type="primary" @click="handleAdd">📢 发布预警</el-button>
        </div>
      </template>

      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="level" label="等级" width="90">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="110">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
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
            <el-button v-if="row.status === 'ACTIVE' && isAdmin" size="small" type="primary" text @click="handleResolve(row.id)">解除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 发布预警弹窗 -->
    <el-dialog v-model="dialogVisible" title="📢 发布预警" width="560px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="预警类型" required>
          <el-select v-model="form.type" placeholder="选择类型" style="width: 100%">
            <el-option v-for="t in typeOptions" :key="t" :label="typeLabel(t)" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警等级" required>
          <el-radio-group v-model="form.level">
            <el-radio-button value="INFO">INFO</el-radio-button>
            <el-radio-button value="YELLOW">YELLOW</el-radio-button>
            <el-radio-button value="ORANGE">ORANGE</el-radio-button>
            <el-radio-button value="RED">RED</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="如：台风橙色预警：南海热带气旋" maxlength="100" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="预警详细描述与应对建议..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="影响海域">
          <el-select v-model="form.seaArea" placeholder="选择海域" style="width: 100%" clearable>
            <el-option v-for="a in seaAreas" :key="a" :label="a" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>
