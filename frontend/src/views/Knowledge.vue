<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { knowledgeAPI } from '@/api'
import type { Knowledge } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const list = ref<Knowledge[]>([])
const total = ref(0)
const pageNum = ref(1)
const category = ref('')
const detailVisible = ref(false)
const current = ref<Knowledge | null>(null)
const rawHtml = ref('')

const categories = ['', 'OCEAN_CURRENT', 'TIDE', 'ECOLOGY', 'POLLUTION', 'GEOGRAPHY', 'WATER_QUALITY', 'GENERAL']
const categoryNames: Record<string, string> = {
  '': '全部', OCEAN_CURRENT: '洋流', TIDE: '潮汐', ECOLOGY: '生态', POLLUTION: '污染', GEOGRAPHY: '地理', WATER_QUALITY: '水温盐度', GENERAL: '综合',
}
const categoryIcons: Record<string, string> = {
  OCEAN_CURRENT: 'cycle', TIDE: 'moon', ECOLOGY: 'fish', POLLUTION: 'shield', GEOGRAPHY: 'wave', WATER_QUALITY: 'thermometer', GENERAL: 'book',
}
const categoryColors: Record<string, string> = {
  OCEAN_CURRENT: '#06b6d4', TIDE: '#8b5cf6', ECOLOGY: '#10b981', POLLUTION: '#f59e0b', GEOGRAPHY: '#0ea5e9', WATER_QUALITY: '#ef4444', GENERAL: '#64748b',
}

const fetchData = async () => {
  try {
    const res: any = await knowledgeAPI.publicList({ pageNum: pageNum.value, pageSize: 9, category: category.value })
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch {}
}

const viewDetail = async (id: number) => {
  try {
    const res: any = await knowledgeAPI.detail(id)
    current.value = res.data
    if (current.value?.content) {
      let html = current.value.content
        .replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
        .replace(/^### (.+)$/gm,'<h3>$1</h3>').replace(/^## (.+)$/gm,'<h2>$1</h2>').replace(/^# (.+)$/gm,'<h1>$1</h1>')
        .replace(/\*\*(.+?)\*\*/g,'<b>$1</b>').replace(/\*(.+?)\*/g,'<i>$1</i>')
        .replace(/^> (.+)$/gm,'<blockquote>$1</blockquote>')
        .replace(/^- (.+)$/gm,'<li>$1</li>').replace(/^(\d+)\. (.+)$/gm,'<li>$2</li>')
        .replace(/\n\n/g,'</p><p>').replace(/\n/g,'<br/>')
      html = '<p>'+html+'</p>'
      html = html.replace(/<p><h([123])>/g,'<h$1>').replace(/<\/h([123])><\/p>/g,'</h$1>')
      rawHtml.value = html
    }
    detailVisible.value = true
  } catch {}
}

onMounted(fetchData)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2><SvgIcon name="book" :size="22" color="#0ea5e9"/> 科普知识</h2>
      <p>了解南海海洋科学，共 {{ total }} 篇文章</p>
    </div>

    <!-- 分类筛选 -->
    <div class="category-filter">
      <button
        v-for="c in categories" :key="c"
        class="cat-btn" :class="{ active: category === c }"
        @click="category = c; pageNum = 1; fetchData()"
      >
        <SvgIcon v-if="c && categoryIcons[c]" :name="categoryIcons[c]" :size="16" :color="category === c ? '#fff' : categoryColors[c]"/>
        {{ categoryNames[c] }}
      </button>
    </div>

    <!-- 文章卡片 -->
    <div class="article-grid">
      <div class="article-card" v-for="item in list" :key="item.id" @click="viewDetail(item.id!)">
        <div class="article-cover" :style="{ background: 'linear-gradient(135deg,'+(categoryColors[item.category]||'#0ea5e9')+'18, '+ (categoryColors[item.category]||'#0ea5e9')+'08)' }">
          <SvgIcon :name="categoryIcons[item.category]||'book'" :size="36" :color="categoryColors[item.category]||'#0ea5e9'"/>
        </div>
        <div class="article-body">
          <div class="article-tags">
            <span class="article-cat" :style="{color:categoryColors[item.category],background:(categoryColors[item.category]||'#0ea5e9')+'14'}">
              {{ categoryNames[item.category] || item.category }}
            </span>
          </div>
          <h3>{{ item.title }}</h3>
          <p>{{ item.summary || item.content?.substring(0, 80) + '...' }}</p>
          <div class="article-meta">
            <span>👁 {{ item.viewCount }}</span>
            <span>{{ item.createTime?.substring(0, 10) }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!total && !list.length" description="暂无科普文章" />

    <div class="pagination-wrap" v-if="total > 9">
      <el-pagination v-model:current-page="pageNum" :total="total" :page-size="9" layout="prev, pager, next" @current-change="fetchData" />
    </div>

    <!-- 文章详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="current?.title" width="800px" top="5vh" class="article-dialog">
      <template v-if="current">
        <div class="detail-meta">
          <span class="detail-cat" :style="{color:categoryColors[current.category],background:(categoryColors[current.category]||'#0ea5e9')+'14'}">
            <SvgIcon :name="categoryIcons[current.category]||'book'" :size="14" :color="categoryColors[current.category]"/>
            {{ categoryNames[current.category] || current.category }}
          </span>
          <span>👁 {{ current.viewCount }} 次阅读</span>
          <span>{{ current.createTime }}</span>
        </div>
        <div v-if="rawHtml" class="markdown-body" v-html="rawHtml"></div>
        <el-empty v-else description="文章暂无内容" />
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 4px 0; }
.page-header { margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 20px; color: var(--text-primary); display: flex; align-items: center; gap: 10px; font-weight: 700; }
.page-header p { margin: 4px 0 0; color: var(--text-muted); font-size: 13px; }

/* 分类筛选 */
.category-filter { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px; }
.cat-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 18px; border-radius: 20px; border: 1px solid var(--border-default);
  background: var(--bg-card); color: var(--text-secondary); font-size: 13px; cursor: pointer;
  transition: all .15s; font-weight: 500;
}
.cat-btn:hover { border-color: var(--border-hover); color: var(--accent-cyan); }
.cat-btn.active { background: var(--accent-cyan); border-color: var(--accent-cyan); color: #0a1628; font-weight: 600; }

/* 文章网格 */
.article-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(340px, 1fr)); gap: 16px; }
.article-card {
  background: var(--bg-card); backdrop-filter: blur(8px); border-radius: 12px; overflow: hidden;
  box-shadow: var(--shadow-card); border: 1px solid var(--border-default);
  cursor: pointer; transition: transform .15s, box-shadow .15s, border-color .15s; display: flex;
}
.article-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-hover); border-color: var(--border-hover); }
.article-cover {
  width: 100px; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  background: rgba(255, 255, 255, 0.02);
}
.article-body { padding: 16px 18px; flex: 1; min-width: 0; }
.article-tags { margin-bottom: 6px; }
.article-cat { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
.article-body h3 { margin: 0 0 6px; font-size: 15px; color: var(--text-primary); font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.article-body p { margin: 0 0 10px; font-size: 12px; color: var(--text-muted); line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.article-meta { display: flex; gap: 14px; font-size: 11px; color: var(--text-muted); }

.pagination-wrap { display: flex; justify-content: center; margin-top: 24px; }

/* 详情弹窗 */
.detail-meta { display: flex; align-items: center; gap: 14px; padding-bottom: 14px; margin-bottom: 16px; border-bottom: 1px solid var(--border-subtle); font-size: 13px; color: var(--text-muted); }
.detail-cat { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; padding: 2px 10px; border-radius: 4px; font-weight: 600; }
.markdown-body { padding: 0 4px; line-height: 1.9; font-size: 15px; color: var(--text-secondary); }
.markdown-body :deep(h1) { font-size: 24px; border-bottom: 2px solid var(--accent-cyan); padding-bottom: 8px; color: var(--text-primary); }
.markdown-body :deep(h2) { font-size: 20px; color: var(--text-primary); margin-top: 24px; }
.markdown-body :deep(h3) { font-size: 17px; color: var(--text-secondary); }
.markdown-body :deep(blockquote) { border-left: 4px solid var(--accent-cyan); padding: 8px 16px; color: var(--text-secondary); background: rgba(0, 229, 255, 0.04); margin: 12px 0; }
.markdown-body :deep(li) { margin: 4px 0 4px 20px; }
.markdown-body :deep(b) { color: var(--accent-cyan); }
</style>
