<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { alertAPI, monitorAPI, knowledgeAPI } from '@/api'
import type { Alert, MonitorPoint, Knowledge } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const router = useRouter()

const activeAlerts = ref<Alert[]>([])
const points = ref<MonitorPoint[]>([])
const knowledgeList = ref<Knowledge[]>([])
const knowledgeTotal = ref(0)
const knowledgeLoading = ref(false)

// ========== 文章详情弹窗 ==========
const detailVisible = ref(false)
const currentArticle = ref<Knowledge | null>(null)
const articleHtml = ref('')

const categoryColors: Record<string, string> = {
  OCEAN_CURRENT: '#06b6d4', TIDE: '#8b5cf6', ECOLOGY: '#10b981',
  POLLUTION: '#f59e0b', GEOGRAPHY: '#0ea5e9', WATER_QUALITY: '#ef4444', GENERAL: '#64748b',
}
const categoryNames: Record<string, string> = {
  OCEAN_CURRENT: '洋流', TIDE: '潮汐', ECOLOGY: '生态',
  POLLUTION: '污染', GEOGRAPHY: '地理', WATER_QUALITY: '水温盐度', GENERAL: '综合',
}
const categoryIcons: Record<string, string> = {
  OCEAN_CURRENT: 'cycle', TIDE: 'moon', ECOLOGY: 'fish',
  POLLUTION: 'shield', GEOGRAPHY: 'wave', WATER_QUALITY: 'thermometer', GENERAL: 'book',
}

async function viewDetail(id: number) {
  try {
    const res: any = await knowledgeAPI.detail(id)
    currentArticle.value = res.data
    if (currentArticle.value?.content) {
      let html = currentArticle.value.content
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/^### (.+)$/gm, '<h3>$1</h3>').replace(/^## (.+)$/gm, '<h2>$1</h2>').replace(/^# (.+)$/gm, '<h1>$1</h1>')
        .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>').replace(/\*(.+?)\*/g, '<i>$1</i>')
        .replace(/^> (.+)$/gm, '<blockquote>$1</blockquote>')
        .replace(/^- (.+)$/gm, '<li>$1</li>').replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
        .replace(/\n\n/g, '</p><p>').replace(/\n/g, '<br/>')
      html = '<p>' + html + '</p>'
      html = html.replace(/<p><h([123])>/g, '<h$1>').replace(/<\/h([123])><\/p>/g, '</h$1>')
      articleHtml.value = html
    }
    detailVisible.value = true
  } catch (e) {
    console.error('文章加载失败', e)
  }
}

onMounted(async () => {
  try {
    const [alertsRes, pointsRes, knowledgeRes]: any = await Promise.all([
      alertAPI.active(),
      monitorAPI.list(),
      knowledgeAPI.publicList({ pageNum: 1, pageSize: 9 }),
    ])
    activeAlerts.value = alertsRes.data || []
    points.value = pointsRes.data || []
    knowledgeList.value = knowledgeRes.data?.records || []
    knowledgeTotal.value = knowledgeRes.data?.total || 0
  } catch (e) {
    console.error('首页数据加载失败', e)
  }
})

// ========== 统计数据 ==========
const totalPoints = computed(() => points.value.length)
const activePoints = computed(() => points.value.filter(p => p.status === 'ACTIVE').length)

const dataCategories = [
  { key: 'temperature', title: '海水温度', icon: 'thermometer', desc: '表层与垂直水温分布', color: '#ef4444', bg: '#fef2f2', path: '/data-query' },
  { key: 'salinity', title: '海水盐度', icon: 'droplet', desc: '盐度时空变化特征', color: '#0ea5e9', bg: '#eff6ff', path: '/data-query' },
  { key: 'current', title: '洋流系统', icon: 'cycle', desc: '季风环流与深层洋流', color: '#06b6d4', bg: '#ecfeff', path: '/data-query' },
  { key: 'tide', title: '潮汐监测', icon: 'moon', desc: '全日潮与半日潮数据', color: '#8b5cf6', bg: '#f5f3ff', path: '/data-query' },
  { key: 'pollution', title: '污染监测', icon: 'shield', desc: 'COD·重金属·微塑料', color: '#f59e0b', bg: '#fffbeb', path: '/monitor-map' },
  { key: 'ecology', title: '海洋生态', icon: 'fish', desc: '珊瑚礁·红树林·生物多样性', color: '#10b981', bg: '#ecfdf5', path: '/knowledge' },
]

const alertLevelMap: Record<string, { label: string; color: string }> = {
  RED: { label: '红色', color: '#ef4444' },
  ORANGE: { label: '橙色', color: '#f59e0b' },
  YELLOW: { label: '黄色', color: '#eab308' },
  INFO: { label: '蓝色', color: '#3b82f6' },
}

const goTo = (path: string) => router.push(path)
</script>

<template>
  <div class="home">

    <!-- 数据统计 -->
    <section class="section section-stats">
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-num">{{ totalPoints }}</span>
          <span class="stat-label">监测点位</span>
        </div>
        <div class="stat-card">
          <span class="stat-num">{{ activePoints }}</span>
          <span class="stat-label">运行中</span>
        </div>
        <div class="stat-card">
          <span class="stat-num">{{ knowledgeTotal }}</span>
          <span class="stat-label">科普文章</span>
        </div>
        <div class="stat-card">
          <span class="stat-num">{{ activeAlerts.length }}</span>
          <span class="stat-label">活跃预警</span>
        </div>
      </div>
    </section>

    <!-- 数据分类卡片 -->
    <section class="section">
      <div class="section-header">
        <h2><SvgIcon name="folder" :size="22" color="#0ea5e9"/> 数据分类</h2>
        <p>覆盖南海海洋环境核心监测指标</p>
      </div>
      <div class="category-grid">
        <div
          v-for="cat in dataCategories"
          :key="cat.key"
          class="category-card"
          @click="goTo(cat.path)"
        >
          <div class="category-accent" :style="{ background: cat.color }"></div>
          <div class="category-body">
            <div class="category-icon-wrap" :style="{ background: cat.bg }">
              <SvgIcon :name="cat.icon" :size="24" :color="cat.color"/>
            </div>
            <div class="category-info">
              <h3>{{ cat.title }}</h3>
              <p>{{ cat.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 预警 + 快览 -->
    <section class="section" v-if="activeAlerts.length">
      <div class="section-header">
        <h2><SvgIcon name="alert" :size="22" color="#ef4444"/> 实时预警</h2>
        <p>当前活跃预警信息</p>
      </div>
      <div class="alert-list">
        <div class="alert-item" v-for="a in activeAlerts.slice(0, 4)" :key="a.id">
          <span class="alert-dot" :style="{ background: alertLevelMap[a.level]?.color || '#ef4444' }"></span>
          <span class="alert-level-tag" :style="{ color: alertLevelMap[a.level]?.color || '#ef4444', background: (alertLevelMap[a.level]?.color || '#ef4444') + '18' }">
            {{ alertLevelMap[a.level]?.label || a.level }}
          </span>
          <span class="alert-title">{{ a.title }}</span>
          <span class="alert-time">{{ a.startTime?.substring(0, 10) }}</span>
        </div>
      </div>
    </section>

    <!-- 科普知识 -->
    <section class="section">
      <div class="section-header clickable" @click="goTo('/knowledge')">
        <h2><SvgIcon name="book" :size="22" color="#0ea5e9"/> 科普知识</h2>
        <p>了解南海海洋科学，共 {{ knowledgeTotal }} 篇文章 →</p>
      </div>
      <div class="knowledge-grid" v-loading="knowledgeLoading">
        <div
          v-for="item in knowledgeList"
          :key="item.id"
          class="knowledge-card"
          @click="viewDetail(item.id!)"
        >
          <div class="knowledge-card-img">
            <SvgIcon
              :name="categoryIcons[item.category] || 'book'"
              :size="28"
              :color="categoryColors[item.category] || '#0ea5e9'"
            />
          </div>
          <div class="knowledge-card-body">
            <h4>{{ item.title }}</h4>
            <p class="knowledge-summary">{{ item.summary }}</p>
            <p class="knowledge-excerpt" v-if="item.content">{{ item.content.replace(/[#*>`\n\r]/g, '').substring(0, 80) }}...</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 文章详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentArticle?.title" width="800px" top="5vh" class="article-dialog" destroy-on-close>
      <template v-if="currentArticle">
        <div class="detail-meta">
          <span class="detail-cat" :style="{ color: categoryColors[currentArticle.category], background: (categoryColors[currentArticle.category] || '#0ea5e9') + '14' }">
            <SvgIcon :name="categoryIcons[currentArticle.category] || 'book'" :size="14" :color="categoryColors[currentArticle.category] || '#0ea5e9'"/>
            {{ categoryNames[currentArticle.category] || currentArticle.category }}
          </span>
          <span>👁 {{ currentArticle.viewCount }} 次阅读</span>
          <span>{{ currentArticle.createTime }}</span>
        </div>
        <div v-if="articleHtml" class="markdown-body" v-html="articleHtml"></div>
        <el-empty v-else description="文章暂无内容" />
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
/* ========== Section 通用 ========== */
.section { max-width: 1200px; margin: 0 auto; padding: 40px 24px; }
.section-header { text-align: center; margin-bottom: 28px; }
.section-header h2 { font-size: 22px; color: #0a3d62; margin: 0; display: flex; align-items: center; justify-content: center; gap: 10px; font-weight: 700; }
.section-header p { color: #94a3b8; font-size: 13px; margin: 6px 0 0; }
.section-header.clickable { cursor: pointer; border-radius: 12px; padding: 16px; transition: background .2s; }
.section-header.clickable:hover { background: #f0f6ff; }
.section-header.clickable:hover p { color: #0ea5e9; }

/* ========== 数据统计 ========== */
.section-stats { padding-top: 16px; padding-bottom: 16px; }
.stats-row { display: flex; justify-content: center; gap: 40px; flex-wrap: wrap; }
.stat-card { text-align: center; min-width: 100px; }
.stat-num { display: block; font-size: 32px; font-weight: 800; color: #0a3d62; line-height: 1.2; }
.stat-label { display: block; font-size: 13px; color: #94a3b8; margin-top: 4px; }

/* ========== 数据分类卡片 ========== */
.category-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
.category-card {
  background: #fff; border-radius: 12px; display: flex; overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6;
  cursor: pointer; transition: transform .15s, box-shadow .15s;
}
.category-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(6,32,58,.08); }
.category-accent { width: 4px; flex-shrink: 0; }
.category-body { display: flex; align-items: center; gap: 14px; padding: 18px 20px; flex: 1; }
.category-icon-wrap { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.category-info h3 { margin: 0; font-size: 15px; color: #1e293b; font-weight: 600; }
.category-info p { margin: 3px 0 0; font-size: 12px; color: #94a3b8; }

/* ========== 预警列表 ========== */
.alert-list { margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; }
.alert-item {
  display: flex; align-items: center; gap: 12px; padding: 14px 20px;
  border-bottom: 1px solid #f1f5f9; font-size: 14px;
}
.alert-item:last-child { border-bottom: none; }
.alert-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.alert-level-tag { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 600; flex-shrink: 0; }
.alert-title { color: #1e293b; font-weight: 500; flex: 1; }
.alert-time { color: #94a3b8; font-size: 12px; flex-shrink: 0; }

/* ========== 科普卡片 ========== */
.knowledge-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 14px; }
.knowledge-card {
  background: #fff; border-radius: 12px; display: flex; overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6;
  cursor: pointer; transition: transform .15s, box-shadow .15s;
}
.knowledge-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(6,32,58,.08); }
.knowledge-card-img {
  width: 80px; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #eff6ff, #ecfeff); flex-shrink: 0;
}
.knowledge-card-body { padding: 14px 16px; flex: 1; min-width: 0; }
.knowledge-card-body h4 { margin: 0; font-size: 14px; color: #1e293b; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.knowledge-summary { margin: 4px 0 0; font-size: 12px; color: #64748b; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.knowledge-excerpt { margin: 6px 0 0; font-size: 11px; color: #94a3b8; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* ========== 文章详情弹窗 ========== */
.detail-meta { display: flex; align-items: center; gap: 14px; padding-bottom: 14px; margin-bottom: 16px; border-bottom: 1px solid #eaf0f6; font-size: 13px; color: #94a3b8; }
.detail-cat { display: inline-flex; align-items: center; gap: 4px; font-size: 12px; padding: 2px 10px; border-radius: 4px; font-weight: 600; }
.markdown-body { padding: 0 4px; line-height: 1.9; font-size: 15px; color: #333; max-height: 60vh; overflow-y: auto; }
.markdown-body :deep(h1) { font-size: 24px; border-bottom: 2px solid #0ea5e9; padding-bottom: 8px; color: #0a3d62; }
.markdown-body :deep(h2) { font-size: 20px; color: #0a3d62; margin-top: 24px; }
.markdown-body :deep(h3) { font-size: 17px; color: #333; }
.markdown-body :deep(blockquote) { border-left: 4px solid #0ea5e9; padding: 8px 16px; color: #555; background: #f5f9fc; margin: 12px 0; }
.markdown-body :deep(li) { margin: 4px 0 4px 20px; }
.markdown-body :deep(b) { color: #0a3d62; }
</style>
