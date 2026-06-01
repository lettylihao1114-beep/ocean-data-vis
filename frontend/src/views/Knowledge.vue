<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { knowledgeAPI } from '@/api'
import type { Knowledge } from '@/types'
import { marked } from 'marked'

const list = ref<Knowledge[]>([])
const total = ref(0)
const pageNum = ref(1)
const category = ref('')

const detailVisible = ref(false)
const current = ref<Knowledge | null>(null)

const categories = ['', 'OCEAN_CURRENT', 'TIDE', 'ECOLOGY', 'POLLUTION', 'CLIMATE', 'GENERAL']
const categoryNames: Record<string, string> = {
  '': '全部', OCEAN_CURRENT: '洋流', TIDE: '潮汐', ECOLOGY: '生态', POLLUTION: '污染', CLIMATE: '气候', GENERAL: '综合',
}

const fetchData = async () => {
  const res: any = await knowledgeAPI.publicList({ pageNum: pageNum.value, pageSize: 10, category: category.value })
  list.value = res.data?.records || []
  total.value = res.data?.total || 0
}

const viewDetail = async (id: number) => {
  const res: any = await knowledgeAPI.detail(id)
  current.value = res.data
  detailVisible.value = true
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
            👁 {{ item.viewCount }} 次阅读 · {{ item.createTime }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-pagination v-model:current-page="pageNum" :total="total" layout="total, prev, pager, next" @current-change="fetchData" style="justify-content: center" />

    <!-- 文章详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="current?.title" width="800px" top="5vh">
      <div v-if="current" class="markdown-body">
        <el-skeleton :loading="!current?.content" animated :rows="6" />
        <div v-if="current?.content" v-html="marked.parse(current.content)"></div>
        <el-empty v-else description="文章内容为空" />
      </div>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.summary { color: #666; font-size: 13px; line-height: 1.6; }
.markdown-body { padding: 16px; line-height: 1.8; }
</style>
