<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { knowledgeAPI, monitorAPI, userAPI } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Knowledge, MonitorPoint, User } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const tab = ref('users')

// 用户管理
const users = ref<User[]>([])
const userTotal = ref(0)
const userPage = ref(1)
const userKeyword = ref('')
const fetchUsers = async () => {
  const res: any = await userAPI.list({ pageNum: userPage.value, pageSize: 10, keyword: userKeyword.value })
  users.value = res.data?.records || []; userTotal.value = res.data?.total || 0
}
const changeRole = async (user: User) => {
  try {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN'
    await ElMessageBox.confirm(`确定将 ${user.username} 的角色${user.role==='ADMIN'?'降级为普通用户':'提升为管理员'}？`, '修改角色', { type: 'warning' })
    await userAPI.updateRole(user.id!, newRole); ElMessage.success('角色已更新'); fetchUsers()
  } catch {}
}
const toggleStatus = async (user: User) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1
    await ElMessageBox.confirm(`确定${newStatus===0?'禁用':'启用'}账号 ${user.username}？`, '切换状态', { type: 'warning' })
    await userAPI.updateStatus(user.id!, newStatus); ElMessage.success(newStatus===1?'账号已启用':'账号已禁用'); fetchUsers()
  } catch {}
}

// 科普管理
const articles = ref<Knowledge[]>([])
const articleDialog = ref(false)
const currentArticle = ref<Knowledge>({ title: '', category: '', content: '', status: 'PUBLISHED' })
const isEdit = ref(false)
const fetchArticles = async () => { const res: any = await knowledgeAPI.adminList({ pageNum: 1, pageSize: 100 }); articles.value = res.data?.records || [] }
const editArticle = (a: Knowledge) => { currentArticle.value = { ...a }; isEdit.value = true; articleDialog.value = true }
const saveArticle = async () => {
  if (isEdit.value) { await knowledgeAPI.update(currentArticle.value.id!, currentArticle.value); ElMessage.success('修改成功') }
  else { await knowledgeAPI.add(currentArticle.value); ElMessage.success('发布成功') }
  articleDialog.value = false; fetchArticles()
}
const deleteArticle = async (id: number) => {
  try { await ElMessageBox.confirm('确定删除该文章？', '警告', { type: 'warning', confirmButtonText: '删除' }); await knowledgeAPI.delete(id); ElMessage.success('已删除'); fetchArticles() }
  catch {}
}

// 监测点管理
const points = ref<MonitorPoint[]>([])
const pointDialog = ref(false)
const currentPoint = ref<MonitorPoint>({ name: '', longitude: 0, latitude: 0, type: 'WATER_QUALITY', status: 'ACTIVE' })
const fetchPoints = async () => { const res: any = await monitorAPI.list(); points.value = res.data || [] }
const savePoint = async () => {
  if (currentPoint.value.id) { await monitorAPI.update(currentPoint.value.id, currentPoint.value) }
  else { await monitorAPI.add(currentPoint.value) }
  ElMessage.success('保存成功'); pointDialog.value = false; fetchPoints()
}

onMounted(() => { fetchUsers(); fetchArticles(); fetchPoints() })
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2><SvgIcon name="shield" :size="22" color="#f59e0b"/> 系统管理</h2>
      <p>用户管理 · 科普文章管理 · 监测点位管理</p>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <button class="tab-btn" :class="{ active: tab === 'users' }" @click="tab = 'users'">👤 用户管理</button>
      <button class="tab-btn" :class="{ active: tab === 'articles' }" @click="tab = 'articles'">📚 科普文章</button>
      <button class="tab-btn" :class="{ active: tab === 'points' }" @click="tab = 'points'">📍 监测点位</button>
    </div>

    <!-- 用户管理 -->
    <div v-if="tab === 'users'" class="panel">
      <div class="panel-toolbar">
        <el-input v-model="userKeyword" placeholder="搜索用户名/邮箱" clearable @keyup.enter="fetchUsers" style="width:260px" size="large" />
        <el-button size="large" @click="fetchUsers">搜索</el-button>
      </div>
      <div class="panel-table">
        <el-table :data="users" stripe>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" width="130" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role==='ADMIN'?'danger':'info'" size="small" effect="dark">
                {{ row.role==='ADMIN'?'管理员':'普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status===1?'success':'danger'" size="small" effect="plain">
                {{ row.status===1?'正常':'禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="160" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="changeRole(row)">{{ row.role==='ADMIN'?'降为用户':'升为管理员' }}</el-button>
              <el-button size="small" :type="row.status===1?'danger':'success'" @click="toggleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="panel-footer">
          <el-pagination v-model:current-page="userPage" :total="userTotal" layout="prev, pager, next" @current-change="fetchUsers" size="small" />
        </div>
      </div>
    </div>

    <!-- 科普文章管理 -->
    <div v-if="tab === 'articles'" class="panel">
      <div class="panel-toolbar">
        <span class="panel-count">共 {{ articles.length }} 篇</span>
        <el-button type="primary" @click="currentArticle = { title: '', category: '', content: '', status: 'PUBLISHED' }; isEdit = false; articleDialog = true">
          + 新建文章
        </el-button>
      </div>
      <div class="panel-table">
        <el-table :data="articles" stripe>
          <el-table-column prop="title" label="标题" min-width="260" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="100" />
          <el-table-column prop="viewCount" label="阅读" width="70" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status==='PUBLISHED'?'success':'info'" size="small">{{ row.status==='PUBLISHED'?'已发布':'草稿' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="small" @click="editArticle(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteArticle(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 监测点位管理 -->
    <div v-if="tab === 'points'" class="panel">
      <div class="panel-toolbar">
        <span class="panel-count">共 {{ points.length }} 个点位</span>
        <el-button type="primary" @click="currentPoint = { name: '', longitude: 0, latitude: 0, type: 'WATER_QUALITY', status: 'ACTIVE' }; pointDialog = true">
          + 新建监测点
        </el-button>
      </div>
      <div class="panel-table">
        <el-table :data="points" stripe>
          <el-table-column prop="name" label="名称" width="180" />
          <el-table-column prop="longitude" label="经度" width="90" />
          <el-table-column prop="latitude" label="纬度" width="85" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="status" label="状态" width="90" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button size="small" @click="currentPoint = { ...row }; pointDialog = true">编辑</el-button>
              <el-button size="small" type="danger" @click="ElMessageBox.confirm('确定删除该监测点？','警告',{type:'warning'}).then(()=>monitorAPI.delete(row.id!)).then(fetchPoints)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 文章编辑弹窗 -->
    <el-dialog v-model="articleDialog" :title="isEdit?'编辑文章':'新建文章'" width="700px">
      <el-form label-width="80px">
        <el-form-item label="标题"><el-input v-model="currentArticle.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="currentArticle.category">
            <el-option v-for="c in ['OCEAN_CURRENT','TIDE','ECOLOGY','POLLUTION','GEOGRAPHY','WATER_QUALITY','GENERAL']" :key="c"
              :label="({OCEAN_CURRENT:'洋流',TIDE:'潮汐',ECOLOGY:'生态',POLLUTION:'污染',GEOGRAPHY:'地理',WATER_QUALITY:'水温盐度',GENERAL:'综合'})[c]" :value="c" />
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

<style scoped>
.page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: var(--text-primary); display: flex; align-items: center; gap: 10px; font-weight: 700; }
.page-header p { margin: 4px 0 0; color: var(--text-muted); font-size: 13px; }

/* Tab 栏 */
.tab-bar { display: flex; gap: 4px; margin-bottom: 16px; background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 10px; padding: 4px; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); }
.tab-btn { padding: 10px 24px; border: none; background: transparent; border-radius: 8px; font-size: 14px; color: var(--text-secondary); cursor: pointer; transition: .15s; font-weight: 500; }
.tab-btn:hover { background: rgba(0, 229, 255, 0.08); color: var(--accent-cyan); }
.tab-btn.active { background: rgba(0, 229, 255, 0.15); color: var(--accent-cyan); font-weight: 600; }

/* 面板 */
.panel { background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; overflow: hidden; box-shadow: var(--shadow-card); border: 1px solid var(--border-default); }
.panel-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px; border-bottom: 1px solid var(--border-subtle); }
.panel-count { font-size: 13px; color: var(--text-muted); }
.panel-table :deep(.el-table th) { background: rgba(0, 229, 255, 0.05); color: var(--text-secondary); font-weight: 600; }
.panel-footer { display: flex; justify-content: flex-end; padding: 12px 20px; border-top: 1px solid var(--border-subtle); }
</style>
