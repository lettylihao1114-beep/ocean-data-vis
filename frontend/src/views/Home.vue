<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { oceanDataAPI, alertAPI, monitorAPI, knowledgeAPI } from '@/api'
import type { OceanData, Alert, MonitorPoint, Knowledge } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const router = useRouter()

const overviewData = ref<Record<string, OceanData>>({})
const activeAlerts = ref<Alert[]>([])
const points = ref<MonitorPoint[]>([])
const knowledgeList = ref<Knowledge[]>([])

const totalPoints = ref(0)
const activeCount = ref(0)

onMounted(async () => {
  try {
    const [overviewRes, alertsRes, pointsRes, knowledgeRes]: any = await Promise.all([
      oceanDataAPI.overview(),
      alertAPI.active(),
      monitorAPI.list(),
      knowledgeAPI.publicList({ pageNum: 1, pageSize: 6 }),
    ])
    overviewData.value = overviewRes.data || {}
    activeAlerts.value = alertsRes.data || []
    points.value = pointsRes.data || []
    knowledgeList.value = knowledgeRes.data?.records || []
    totalPoints.value = points.value.length
    activeCount.value = points.value.filter((p: MonitorPoint) => p.status === 'ACTIVE').length
  } catch {}
})

const dataCategories = [
  { key: 'temperature', title: '海水温度', icon: 'thermometer', desc: '表层与垂直水温分布', color: '#ef4444', bg: '#fef2f2', path: '/data-query' },
  { key: 'salinity', title: '海水盐度', icon: 'droplet', desc: '盐度时空变化特征', color: '#0ea5e9', bg: '#eff6ff', path: '/data-query' },
  { key: 'current', title: '洋流系统', icon: 'cycle', desc: '季风环流与深层洋流', color: '#06b6d4', bg: '#ecfeff', path: '/data-query' },
  { key: 'tide', title: '潮汐监测', icon: 'moon', desc: '全日潮与半日潮数据', color: '#8b5cf6', bg: '#f5f3ff', path: '/data-query' },
  { key: 'pollution', title: '污染监测', icon: 'shield', desc: 'COD·重金属·微塑料', color: '#f59e0b', bg: '#fffbeb', path: '/monitor-map' },
  { key: 'ecology', title: '海洋生态', icon: 'fish', desc: '珊瑚礁·红树林·生物多样性', color: '#10b981', bg: '#ecfdf5', path: '/knowledge' },
]

const quickActions = [
  { title: '数据大屏', icon: 'chart', desc: 'ECharts 多图表联动', path: '/dashboard', color: '#0ea5e9' },
  { title: 'AI 助手', icon: 'bot', desc: 'RAG 检索增强问答', path: '/ai-chat', color: '#06b6d4' },
  { title: '监测地图', icon: 'map', desc: 'Leaflet 点位标注', path: '/monitor-map', color: '#10b981' },
  { title: '数据导出', icon: 'download', desc: 'Excel / CSV 导出', path: '/export', color: '#f59e0b' },
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

    <!-- 快捷入口 + 统计 -->
    <section class="section section-top">
      <div class="top-grid">
        <div class="top-card primary" @click="goTo('/dashboard')">
          <SvgIcon name="chart" :size="28" color="#fff"/>
          <div>
            <h3>数据大屏</h3>
            <p>ECharts 多图表可视化</p>
          </div>
          <span class="top-arrow">→</span>
        </div>
        <div class="top-card" @click="goTo('/data-query')">
          <SvgIcon name="search" :size="28" color="#0ea5e9"/>
          <div>
            <h3>数据查询</h3>
            <p>多维筛选海洋数据</p>
          </div>
          <span class="top-arrow">→</span>
        </div>
        <div class="top-card" @click="goTo('/ai-chat')">
          <SvgIcon name="bot" :size="28" color="#06b6d4"/>
          <div>
            <h3>AI 助手</h3>
            <p>RAG 检索增强问答</p>
          </div>
          <span class="top-arrow">→</span>
        </div>
        <div class="top-card" @click="goTo('/monitor-map')">
          <SvgIcon name="map" :size="28" color="#10b981"/>
          <div>
            <h3>监测地图</h3>
            <p>Leaflet 点位标注</p>
          </div>
          <span class="top-arrow">→</span>
        </div>
      </div>
      <div class="top-stats">
        <div class="top-stat">
          <span class="top-stat-num">{{ totalPoints }}</span>
          <span class="top-stat-label">监测点位</span>
        </div>
        <div class="top-stat">
          <span class="top-stat-num">{{ Object.keys(overviewData).length }}</span>
          <span class="top-stat-label">海域覆盖</span>
        </div>
        <div class="top-stat">
          <span class="top-stat-num">{{ activeCount }}</span>
          <span class="top-stat-label">运行中</span>
        </div>
        <div class="top-stat">
          <span class="top-stat-num">{{ knowledgeList.length }}</span>
          <span class="top-stat-label">科普文章</span>
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
      <div class="section-header">
        <h2><SvgIcon name="book" :size="22" color="#0ea5e9"/> 科普知识</h2>
        <p>了解南海海洋科学</p>
      </div>
      <div class="knowledge-grid">
        <div
          v-for="item in knowledgeList.slice(0, 6)"
          :key="item.id"
          class="knowledge-card"
          @click="goTo('/knowledge')"
        >
          <div class="knowledge-card-img">
            <SvgIcon
              :name="item.category === 'OCEAN_CURRENT' ? 'cycle' : item.category === 'TIDE' ? 'moon' : item.category === 'POLLUTION' ? 'shield' : item.category === 'ECOLOGY' ? 'fish' : item.category === 'GEOGRAPHY' ? 'wave' : item.category === 'WATER_QUALITY' ? 'thermometer' : 'book'"
              :size="28"
              :color="item.category === 'OCEAN_CURRENT' ? '#06b6d4' : item.category === 'TIDE' ? '#8b5cf6' : item.category === 'POLLUTION' ? '#f59e0b' : item.category === 'ECOLOGY' ? '#10b981' : item.category === 'GEOGRAPHY' ? '#0ea5e9' : item.category === 'WATER_QUALITY' ? '#ef4444' : '#0ea5e9'"
            />
          </div>
          <div class="knowledge-card-body">
            <h4>{{ item.title }}</h4>
            <p>{{ item.summary }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="section">
      <div class="section-header">
        <h2><SvgIcon name="zap" :size="22" color="#f59e0b"/> 快捷入口</h2>
        <p>快速跳转到各功能模块</p>
      </div>
      <div class="quick-grid">
        <div
          v-for="act in quickActions"
          :key="act.title"
          class="quick-card"
          @click="goTo(act.path)"
          :style="{ '--accent': act.color }"
        >
          <SvgIcon :name="act.icon" :size="28" :color="act.color"/>
          <div class="quick-info">
            <h3>{{ act.title }}</h3>
            <p>{{ act.desc }}</p>
          </div>
          <span class="quick-arrow">→</span>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
/* ========== Section 通用 ========== */
.section-top { padding-top: 32px; }
.section { max-width: 1200px; margin: 0 auto; padding: 40px 24px; }
.section-header { text-align: center; margin-bottom: 28px; }
.section-header h2 { font-size: 22px; color: #0a3d62; margin: 0; display: flex; align-items: center; justify-content: center; gap: 10px; font-weight: 700; }
.section-header p { color: #94a3b8; font-size: 13px; margin: 6px 0 0; }

/* ========== 顶部快捷入口 ========== */
.top-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.top-card {
  background: #fff; border-radius: 12px; padding: 20px;
  display: flex; align-items: center; gap: 14px;
  box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6;
  cursor: pointer; transition: transform .15s, box-shadow .15s;
}
.top-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(6,32,58,.08); }
.top-card h3 { margin: 0; font-size: 15px; color: #1e293b; font-weight: 600; }
.top-card p { margin: 3px 0 0; font-size: 12px; color: #94a3b8; }
.top-arrow { color: #cbd5e1; font-size: 18px; flex-shrink: 0; }
.top-card.primary { background: linear-gradient(135deg, #0ea5e9, #06b6d4); border: none; }
.top-card.primary h3, .top-card.primary p { color: #fff; }
.top-card.primary .top-arrow { color: rgba(255,255,255,.7); }

.top-stats { display: flex; justify-content: center; gap: 48px; margin-top: 28px; }
.top-stat { text-align: center; }
.top-stat-num { display: block; color: #0a3d62; font-size: 28px; font-weight: 800; }
.top-stat-label { color: #94a3b8; font-size: 13px; margin-top: 2px; }

@media (max-width: 768px) {
  .top-grid { grid-template-columns: repeat(2, 1fr); }
  .top-stats { gap: 24px; flex-wrap: wrap; }
}

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
.alert-list { max-width: 800px; margin: 0 auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; }
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
.knowledge-card-body p { margin: 4px 0 0; font-size: 12px; color: #94a3b8; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* ========== 快捷入口 ========== */
.quick-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px; }
.quick-card {
  background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 14px;
  box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6;
  cursor: pointer; transition: transform .15s, box-shadow .15s;
  border-left: 4px solid var(--accent, #0ea5e9);
}
.quick-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(6,32,58,.08); }
.quick-icon-wrap { flex-shrink: 0; }
.quick-info { flex: 1; }
.quick-info h3 { margin: 0; font-size: 15px; color: #1e293b; font-weight: 600; }
.quick-info p { margin: 3px 0 0; font-size: 12px; color: #94a3b8; }
.quick-arrow { color: #94a3b8; font-size: 18px; flex-shrink: 0; }
</style>
