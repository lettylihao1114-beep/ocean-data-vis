<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { knowledgeAPI } from '@/api'
import type { Knowledge } from '@/types'

const list = ref<Knowledge[]>([])
const total = ref(0)
const pageNum = ref(1)
const category = ref('')
const detailVisible = ref(false)
const current = ref<Knowledge | null>(null)
const rawHtml = ref('')

const categories = ['', 'OCEAN_CURRENT', 'TIDE', 'ECOLOGY', 'POLLUTION', 'CLIMATE', 'GENERAL']
const categoryNames: Record<string, string> = {
  '': '全部', OCEAN_CURRENT: '洋流', TIDE: '潮汐', ECOLOGY: '生态', POLLUTION: '污染', CLIMATE: '气候', GENERAL: '综合',
}

const fetchData = async () => {
  try {
    const res: any = await knowledgeAPI.publicList({ pageNum: pageNum.value, pageSize: 10, category: category.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {}
}

const viewDetail = async (id: number) => {
  try {
    const res: any = await knowledgeAPI.detail(id)
    current.value = res.data
    if (current.value?.content) {
      // simple markdown→html
      let html = current.value.content
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/^### (.+)$/gm, '<h3>$1</h3>')
        .replace(/^## (.+)$/gm, '<h2>$1</h2>')
        .replace(/^# (.+)$/gm, '<h1>$1</h1>')
        .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
        .replace(/\*(.+?)\*/g, '<i>$1</i>')
        .replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
        .replace(/\n\n/g, '</p><p>')
        .replace(/\n/g, '<br/>')
      html = '<p>' + html + '</p>'
      html = html.replace(/<p><h([123])>/g, '<h$1>').replace(/<\/h([123])><\/p>/g, '</h$1>')
      rawHtml.value = html
    }
    detailVisible.value = true
  } catch (e) {
    console.error('Failed to load article:', e)
  }
}

onMounted(fetchData)
</script>

<template>
  <el-card>
    <el-form :inline="true" style="margin-bottom: 16px">
      <el-form-item label="分类">
        <el-select v-model="category" @change="fetchData" placeholder="全部分类">
          <el-option v-for="c in categories" :key="c" :label="categoryNames[c]" :value="c" />
        </el-select>
      </el-form-item>
    </el-form>

    <el-row :gutter="16">
      <el-col :span="8" v-for="item in list" :key="item.id">
        <el-card shadow="hover" @click="viewDetail(item.id!)" style="margin-bottom: 16px; cursor: pointer">
          <template #header>
            <el-tag size="small" style="margin-right: 8px">{{ categoryNames[item.category] }}</el-tag>
            <b>{{ item.title }}</b>
          </template>
          <div class="summary">{{ item.summary || item.content?.substring(0, 100) + '...' }}</div>
          <div style="color: #bbb; font-size: 12px; margin-top: 8px">
            👁 {{ item.viewCount }} 次 · {{ item.createTime }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!total" description="暂无科普文章" />

    <el-pagination v-model:current-page="pageNum" :total="total" layout="total, prev, pager, next" @current-change="fetchData" style="justify-content: center; margin-top: 16px" />

    <el-dialog v-model="detailVisible" :title="current?.title" width="800px" top="5vh">
      <template v-if="current">
        <div style="color: #999; font-size: 13px; margin-bottom: 16px; border-bottom: 1px solid #eee; padding-bottom: 12px">
          📂 {{ categoryNames[current.category] }} · 👁 {{ current.viewCount }} 次阅读 · {{ current.createTime }}
        </div>
        <div v-if="rawHtml" class="markdown-body" v-html="rawHtml"></div>
        <el-empty v-else description="文章暂无内容" />
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.summary { color: #666; font-size: 13px; line-height: 1.6; }
.markdown-body { padding: 16px; line-height: 1.9; font-size: 15px; color: #333; }
.markdown-body :deep(h1) { font-size: 24px; border-bottom: 2px solid #0984e3; padding-bottom: 8px; }
.markdown-body :deep(h2) { font-size: 20px; color: #0a3d62; margin-top: 24px; }
.markdown-body :deep(h3) { font-size: 17px; color: #333; }
.markdown-body :deep(blockquote) { border-left: 4px solid #0984e3; padding: 8px 16px; color: #555; background: #f5f9fc; margin: 12px 0; }
.markdown-body :deep(li) { margin: 4px 0 4px 20px; }
.markdown-body :deep(b) { color: #0a3d62; }
</style>
