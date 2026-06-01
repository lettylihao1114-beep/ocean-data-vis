<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { knowledgeAPI, monitorAPI } from '@/api'
import { ElMessage } from 'element-plus'
import type { Knowledge, MonitorPoint } from '@/types'

// 科普管理
const articles = ref<Knowledge[]>([])
const articleDialog = ref(false)
const currentArticle = ref<Knowledge>({ title: '', category: '', content: '', status: 'PUBLISHED' })
const isEdit = ref(false)

const fetchArticles = async () => {
  const res: any = await knowledgeAPI.adminList({ pageNum: 1, pageSize: 100 })
  articles.value = res.data?.records || []
}

const editArticle = (a: Knowledge) => {
  currentArticle.value = { ...a }
  isEdit.value = true
  articleDialog.value = true
}

const saveArticle = async () => {
  if (isEdit.value) {
    await knowledgeAPI.update(currentArticle.value.id!, currentArticle.value)
    ElMessage.success('修改成功')
  } else {
    await knowledgeAPI.add(currentArticle.value)
    ElMessage.success('发布成功')
  }
  articleDialog.value = false
  fetchArticles()
}

const deleteArticle = async (id: number) => {
  await knowledgeAPI.delete(id)
  ElMessage.success('已删除')
  fetchArticles()
}

// 监测点管理
const points = ref<MonitorPoint[]>([])
const pointDialog = ref(false)
const currentPoint = ref<MonitorPoint>({ name: '', longitude: 0, latitude: 0, type: 'WATER_QUALITY', status: 'ACTIVE' })

const fetchPoints = async () => {
  const res: any = await monitorAPI.list()
  points.value = res.data || []
}

const savePoint = async () => {
  if (currentPoint.value.id) {
    await monitorAPI.update(currentPoint.value.id, currentPoint.value)
  } else {
    await monitorAPI.add(currentPoint.value)
  }
  ElMessage.success('保存成功')
  pointDialog.value = false
  fetchPoints()
}

onMounted(() => {
  fetchArticles()
  fetchPoints()
})
</script>

<template>
  <div>
    <el-card header="📚 科普文章管理" style="margin-bottom: 16px">
      <el-button type="primary" @click="currentArticle = { title: '', category: '', content: '', status: 'PUBLISHED' }; isEdit = false; articleDialog = true" style="margin-bottom: 12px">新建文章</el-button>
      <el-table :data="articles" border stripe>
        <el-table-column prop="title" label="标题" width="260" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="viewCount" label="阅读量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">{{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="editArticle(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteArticle(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card header="📍 监测点位管理">
      <el-button type="primary" @click="currentPoint = { name: '', longitude: 0, latitude: 0, type: 'WATER_QUALITY', status: 'ACTIVE' }; pointDialog = true" style="margin-bottom: 12px">新建监测点</el-button>
      <el-table :data="points" border stripe>
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="longitude" label="经度" width="100" />
        <el-table-column prop="latitude" label="纬度" width="100" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="currentPoint = { ...row }; pointDialog = true">编辑</el-button>
            <el-button size="small" type="danger" @click="monitorAPI.delete(row.id!).then(fetchPoints)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 文章编辑弹窗 -->
    <el-dialog v-model="articleDialog" :title="isEdit ? '编辑文章' : '新建文章'" width="700px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="currentArticle.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="currentArticle.category">
            <el-option label="洋流" value="OCEAN_CURRENT" />
            <el-option label="潮汐" value="TIDE" />
            <el-option label="生态" value="ECOLOGY" />
            <el-option label="污染" value="POLLUTION" />
            <el-option label="气候" value="CLIMATE" />
            <el-option label="综合" value="GENERAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要"><el-input v-model="currentArticle.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="currentArticle.content" type="textarea" :rows="12" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="articleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveArticle">保存</el-button>
      </template>
    </el-dialog>

    <!-- 监测点编辑弹窗 -->
    <el-dialog v-model="pointDialog" title="编辑监测点" width="500px">
      <el-form label-width="80px">
        <el-form-item label="名称"><el-input v-model="currentPoint.name" /></el-form-item>
        <el-form-item label="经度"><el-input-number v-model="currentPoint.longitude" :precision="4" /></el-form-item>
        <el-form-item label="纬度"><el-input-number v-model="currentPoint.latitude" :precision="4" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="currentPoint.type">
            <el-option label="水质监测" value="WATER_QUALITY" />
            <el-option label="洋流监测" value="CURRENT" />
            <el-option label="污染监测" value="POLLUTION" />
            <el-option label="潮汐监测" value="TIDE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="currentPoint.status">
            <el-option label="运行中" value="ACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已停用" value="INACTIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="currentPoint.description" /></el-form-item>
        <el-form-item label="海域"><el-input v-model="currentPoint.seaArea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointDialog = false">取消</el-button>
        <el-button type="primary" @click="savePoint">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
