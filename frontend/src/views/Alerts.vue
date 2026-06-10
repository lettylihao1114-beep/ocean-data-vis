<script setup lang="ts">
import { ref, onMounted, computed, reactive } from 'vue'
import { alertAPI } from '@/api'
import type { Alert } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'

const isAdmin = computed(() => localStorage.getItem('role') === 'ADMIN')
const list = ref<Alert[]>([])
const active = ref<Alert[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

const form = reactive({ type: '', level: '', title: '', content: '', seaArea: '', startTime: '', endTime: '' })
const typeOptions = ['TYPHOON', 'EXTREME_TEMP', 'TIDE', 'RED_TIDE', 'POLLUTION', 'OTHER']
const levelOptions = ['INFO', 'YELLOW', 'ORANGE', 'RED']
const seaAreas = ['南海中部', '湛江海域', '茂名海域', '阳江海域', '珠江口海域', '万山海域', '红海湾', '西沙海域', '南沙海域', '北部湾', '粤东海域', '南海沿岸']

const fetchData = async () => {
  loading.value = true
  try {
    const [activeRes, allRes]: any = await Promise.all([
      alertAPI.active(), alertAPI.page({ pageNum: 1, pageSize: 50 }),
    ])
    active.value = activeRes.data || []
    list.value = allRes.data?.records || []
  } finally { loading.value = false }
}

const resetForm = () => Object.assign(form, { type: '', level: '', title: '', content: '', seaArea: '', startTime: '', endTime: '' })

const handleSubmit = async () => {
  if (!form.type || !form.level || !form.title || !form.content) return ElMessage.warning('请填写类型、等级、标题和内容')
  submitting.value = true
  try { await alertAPI.add(form); ElMessage.success('预警发布成功'); dialogVisible.value = false; fetchData() }
  catch {}
  finally { submitting.value = false }
}

const handleResolve = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认解除该预警？', '提示', { type: 'warning' })
    await alertAPI.resolve(id); ElMessage.success('已解除'); fetchData()
  } catch {}
}

const getLevelType = (level: string): any => ({ RED: 'danger', ORANGE: 'warning', YELLOW: 'warning', INFO: 'info' }[level] || 'info')
const typeLabel = (type: string) => ({ TYPHOON: '台风', EXTREME_TEMP: '极端水温', TIDE: '潮汐异常', RED_TIDE: '赤潮', POLLUTION: '污染', OTHER: '其他' }[type] || type)

onMounted(fetchData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2><SvgIcon name="alert" :size="22" color="#ef4444"/> 预警管理</h2>
      <p>极端海况预警发布与监控</p>
    </div>

    <!-- 活跃预警 -->
    <div class="alert-grid" v-if="active.length">
      <div class="alert-card" v-for="a in active" :key="a.id" :class="'level-'+a.level.toLowerCase()">
        <div class="alert-card-bar"></div>
        <div class="alert-card-body">
          <div class="alert-card-top">
            <span class="alert-card-level" :class="'level-'+a.level.toLowerCase()">{{ a.level }}</span>
            <span class="alert-card-type">{{ typeLabel(a.type) }}</span>
          </div>
          <h4>{{ a.title }}</h4>
          <p>{{ (a.content || '').substring(0, 100) }}...</p>
          <div class="alert-card-meta">
            <span>{{ a.seaArea }}</span>
            <span>{{ a.startTime?.substring(0, 16) }}</span>
          </div>
          <el-button v-if="isAdmin" size="small" @click="handleResolve(a.id!)" class="resolve-btn">解除预警</el-button>
        </div>
      </div>
    </div>

    <!-- 全部记录 -->
    <div class="table-card">
      <div class="table-topbar">
        <span class="table-title">全部预警记录</span>
        <el-button v-if="isAdmin" type="primary" @click="resetForm(); dialogVisible = true">
          <SvgIcon name="alert" :size="14"/> 发布预警
        </el-button>
      </div>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)" size="small" effect="dark">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ typeLabel(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="seaArea" label="影响海域" width="120" />
        <el-table-column prop="startTime" label="开始时间" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status==='ACTIVE'?'danger':'success'" size="small" effect="plain">
              {{ row.status==='ACTIVE'?'活跃':'已解除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" v-if="isAdmin">
          <template #default="{ row }">
            <el-button v-if="row.status==='ACTIVE'" size="small" type="danger" text @click="handleResolve(row.id)">解除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 发布弹窗 -->
    <el-dialog v-model="dialogVisible" title="发布预警" width="560px" :close-on-click-modal="false">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型" required>
          <el-select v-model="form.type" placeholder="选择类型" style="width:100%">
            <el-option v-for="t in typeOptions" :key="t" :label="typeLabel(t)" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级" required>
          <el-radio-group v-model="form.level">
            <el-radio-button v-for="l in levelOptions" :key="l" :value="l">{{ l }}</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="如：台风橙色预警" maxlength="100" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="预警详细描述..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="海域">
          <el-select v-model="form.seaArea" placeholder="选择海域" style="width:100%" clearable>
            <el-option v-for="a in seaAreas" :key="a" :label="a" :value="a" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="开始时间" format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="结束时间" format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { max-width: 1280px; margin: 0 auto; padding: 4px 24px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; color: #0a3d62; display: flex; align-items: center; gap: 10px; font-weight: 700; }
.page-header p { margin: 4px 0 0; color: #94a3b8; font-size: 13px; }

/* 活跃预警卡片 */
.alert-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; margin-bottom: 20px; }
.alert-card { background: #fff; border-radius: 12px; display: flex; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; }
.alert-card.level-red { border-color: #fecaca; }
.alert-card.level-orange { border-color: #fed7aa; }
.alert-card.level-yellow { border-color: #fef08a; }
.alert-card-bar { width: 4px; flex-shrink: 0; }
.alert-card.level-red .alert-card-bar { background: #ef4444; }
.alert-card.level-orange .alert-card-bar { background: #f59e0b; }
.alert-card.level-yellow .alert-card-bar { background: #eab308; }
.alert-card.level-info .alert-card-bar { background: #3b82f6; }
.alert-card-body { padding: 16px 18px; flex: 1; }
.alert-card-top { display: flex; gap: 8px; margin-bottom: 8px; }
.alert-card-level { font-size: 10px; padding: 2px 8px; border-radius: 4px; font-weight: 700; }
.alert-card-level.level-red { color: #ef4444; background: #fef2f2; }
.alert-card-level.level-orange { color: #f59e0b; background: #fffbeb; }
.alert-card-level.level-yellow { color: #eab308; background: #fefce8; }
.alert-card-level.level-info { color: #3b82f6; background: #eff6ff; }
.alert-card-type { font-size: 12px; color: #64748b; }
.alert-card-body h4 { margin: 0 0 6px; font-size: 15px; color: #1e293b; }
.alert-card-body p { margin: 0 0 8px; font-size: 12px; color: #94a3b8; line-height: 1.5; }
.alert-card-meta { display: flex; gap: 12px; font-size: 11px; color: #a0aec0; margin-bottom: 10px; }
.resolve-btn { margin-top: 4px; }

/* 表格 */
.table-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; }
.table-card :deep(.el-table th) { background: #f8fafc; color: #475569; font-weight: 600; }
.table-topbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid #f1f5f9; }
.table-title { font-size: 14px; font-weight: 600; color: #1e293b; }
</style>
