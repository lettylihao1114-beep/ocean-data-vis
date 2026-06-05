<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { oceanDataAPI, alertAPI, monitorAPI, knowledgeAPI } from '@/api'
import type { OceanData, Alert, MonitorPoint, Knowledge } from '@/types'

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
  { key: 'temperature', title: '海水温度', icon: '🌡️', desc: '表层与垂直水温分布', color: '#ef4444', bg: '#fef2f2', path: '/data-query' },
  { key: 'salinity', title: '海水盐度', icon: '💎', desc: '盐度时空变化特征', color: '#0ea5e9', bg: '#eff6ff', path: '/data-query' },
  { key: 'current', title: '洋流系统', icon: '🔄', desc: '季风环流与深层洋流', color: '#06b6d4', bg: '#ecfeff', path: '/data-query' },
  { key: 'tide', title: '潮汐监测', icon: '🌙', desc: '全日潮与半日潮数据', color: '#8b5cf6', bg: '#f5f3ff', path: '/data-query' },
  { key: 'pollution', title: '污染监测', icon: '🛡️', desc: 'COD·重金属·微塑料', color: '#f59e0b', bg: '#fffbeb', path: '/monitor-map' },
  { key: 'ecology', title: '海洋生态', icon: '🐠', desc: '珊瑚礁·红树林·生物多样性', color: '#10b981', bg: '#ecfdf5', path: '/knowledge' },
]

const quickActions = [
  { title: '数据大屏', icon: '📊', desc: 'ECharts 多图表联动', path: '/dashboard', color: '#0ea5e9' },
  { title: 'AI 助手', icon: '🤖', desc: 'RAG 检索增强问答', path: '/ai-chat', color: '#06b6d4' },
  { title: '监测地图', icon: '🗺️', desc: 'Leaflet 点位标注', path: '/monitor-map', color: '#10b981' },
  { title: '数据导出', icon: '📥', desc: 'Excel / CSV 导出', path: '/export', color: '#f59e0b' },
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

    <!-- Hero 区 -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-body">
        <h1 class="hero-title">南海海洋环境数据可视化</h1>
        <p class="hero-desc">
          基于国家地球系统科学数据中心南海再分析数据集（1986 至今），
          提供水温、盐度、洋流、潮汐、污染监测等海洋环境数据的查询、可视化、预警与 AI 智能分析服务。
        </p>
        <div class="hero-actions">
          <button class="hero-btn primary" @click="goTo('/dashboard')">📊 进入数据大屏</button>
          <button class="hero-btn" @click="goTo('/data-query')">🔍 数据查询</button>
          <button class="hero-btn" @click="goTo('/ai-chat')">🤖 AI 助手</button>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-num">{{ totalPoints }}</span>
            <span class="hero-stat-label">监测点位</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ Object.keys(overviewData).length }}</span>
            <span class="hero-stat-label">海域覆盖</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ activeCount }}</span>
            <span class="hero-stat-label">运行中</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-num">{{ knowledgeList.length }}</span>
            <span class="hero-stat-label">科普文章</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 数据分类卡片 -->
    <section class="section">
      <div class="section-header">
        <h2><span class="section-icon">📂</span> 数据分类</h2>
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
              <span class="category-icon">{{ cat.icon }}</span>
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
        <h2><span class="section-icon">⚠️</span> 实时预警</h2>
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
        <h2><span class="section-icon">📖</span> 科普知识</h2>
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
            <span class="knowledge-card-emoji">{{ item.category === 'OCEAN_CURRENT' ? '🔄' : item.category === 'TIDE' ? '🌙' : item.category === 'POLLUTION' ? '🛡️' : item.category === 'ECOLOGY' ? '🐠' : item.category === 'GEOGRAPHY' ? '🌊' : item.category === 'WATER_QUALITY' ? '🌡️' : '📖' }}</span>
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
        <h2><span class="section-icon">⚡</span> 快捷入口</h2>
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
          <span class="quick-icon">{{ act.icon }}</span>
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
/* ========== Hero ========== */
.hero {
  position: relative; overflow: hidden;
  background: linear-gradient(160deg, #04182e 0%, #06203a 25%, #0a3d62 55%, #0d5e8a 80%, #0f6a9e 100%);
  padding: 60px 36px 56px;
  text-align: center;
}
.hero-bg {
  position: absolute; inset: 0; pointer-events: none;
  background:
    radial-gradient(ellipse 800px 300px at 20% 50%, rgba(6,182,212,.12), transparent),
    radial-gradient(ellipse 600px 300px at 80% 30%, rgba(14,165,233,.1), transparent);
}
.hero-body { position: relative; z-index: 1; max-width: 800px; margin: 0 auto; }
.hero-title { color: #fff; font-size: 32px; font-weight: 800; margin: 0; letter-spacing: 1px; }
.hero-desc {
  color: rgba(255,255,255,.65); font-size: 15px; line-height: 1.8;
  margin: 16px 0 28px; max-width: 640px; margin-left: auto; margin-right: auto;
}
.hero-actions { display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; margin-bottom: 36px; }
.hero-btn {
  padding: 10px 24px; border-radius: 8px; font-size: 14px; font-weight: 600;
  border: 1px solid rgba(255,255,255,.2); background: rgba(255,255,255,.08);
  color: rgba(255,255,255,.85); cursor: pointer; transition: .2s;
}
.hero-btn:hover { background: rgba(255,255,255,.16); color: #fff; }
.hero-btn.primary { background: #0ea5e9; border-color: #0ea5e9; color: #fff; }
.hero-btn.primary:hover { background: #06b6d4; }
.hero-stats { display: flex; justify-content: center; gap: 40px; }
.hero-stat { text-align: center; }
.hero-stat-num { display: block; color: #fff; font-size: 32px; font-weight: 800; }
.hero-stat-label { color: rgba(255,255,255,.5); font-size: 13px; margin-top: 4px; }

/* ========== Section 通用 ========== */
.section { max-width: 1200px; margin: 0 auto; padding: 40px 24px; }
.section-header { text-align: center; margin-bottom: 28px; }
.section-header h2 { font-size: 22px; color: #0a3d62; margin: 0; display: flex; align-items: center; justify-content: center; gap: 8px; font-weight: 700; }
.section-icon { font-size: 24px; }
.section-header p { color: #94a3b8; font-size: 13px; margin: 6px 0 0; }

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
.category-icon { font-size: 22px; }
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
.knowledge-card-emoji { font-size: 28px; }
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
.quick-icon { font-size: 28px; flex-shrink: 0; }
.quick-info { flex: 1; }
.quick-info h3 { margin: 0; font-size: 15px; color: #1e293b; font-weight: 600; }
.quick-info p { margin: 3px 0 0; font-size: 12px; color: #94a3b8; }
.quick-arrow { color: #94a3b8; font-size: 18px; flex-shrink: 0; }
</style>
