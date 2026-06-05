<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { monitorAPI } from '@/api'
import type { MonitorPoint } from '@/types'
import SvgIcon from '@/components/SvgIcon.vue'

const points = ref<MonitorPoint[]>([])
const typeFilter = ref('')
const statusFilter = ref('')

let map: L.Map | null = null
let markersLayer: L.LayerGroup | null = null

const typeLabels: Record<string, string> = { WATER_QUALITY: '水质', CURRENT: '洋流', POLLUTION: '污染', TIDE: '潮汐' }
const typeColors: Record<string, string> = { WATER_QUALITY: '#3b82f6', CURRENT: '#06b6d4', POLLUTION: '#ef4444', TIDE: '#8b5cf6' }
const typeIcons: Record<string, string> = {
  WATER_QUALITY: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  CURRENT: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  POLLUTION: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
  TIDE: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-violet.png',
}

delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({ iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png', iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png', shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png' })

const activeCount = computed(() => points.value.filter(p => p.status === 'ACTIVE').length)
const maintenanceCount = computed(() => points.value.filter(p => p.status === 'MAINTENANCE').length)
const inactiveCount = computed(() => points.value.filter(p => p.status === 'INACTIVE').length)

const statusLabel = (s: string) => s === 'ACTIVE' ? '运行中' : s === 'MAINTENANCE' ? '维护中' : '已停用'
const statusClass = (s: string) => s === 'ACTIVE' ? 'on' : s === 'MAINTENANCE' ? 'warn' : 'off'

onMounted(async () => {
  await nextTick()
  initMap()
  await fetchData()
})

onBeforeUnmount(() => { map?.remove() })

function initMap() {
  map = L.map('leaflet-map', { center: [17.5, 114], zoom: 7, minZoom: 5, maxZoom: 14, zoomControl: true })
  L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Ocean/World_Ocean_Base/MapServer/tile/{z}/{y}/{x}', {
    attribution: 'Esri | 南海海洋数据可视化', maxZoom: 14,
  }).addTo(map)
  L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/Ocean/World_Ocean_Reference/MapServer/tile/{z}/{y}/{x}', {
    attribution: '', maxZoom: 14,
  }).addTo(map)
  markersLayer = L.layerGroup().addTo(map)
}

async function fetchData() {
  const res: any = await monitorAPI.list({ type: typeFilter.value, status: statusFilter.value })
  points.value = res.data || []
  renderMarkers()
}

function renderMarkers() {
  markersLayer?.clearLayers()
  if (!map) return
  const bounds: [number, number][] = []
  points.value.forEach(p => {
    const icon = L.icon({ iconUrl: typeIcons[p.type] || typeIcons.WATER_QUALITY, iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34] })
    const marker = L.marker([p.latitude, p.longitude], { icon }).addTo(markersLayer!)
    const color = typeColors[p.type] || '#3b82f6'
    const statusEmoji = p.status === 'ACTIVE' ? '🟢' : p.status === 'MAINTENANCE' ? '🟡' : '⚫'
    marker.bindPopup(`
      <div style="font-family:-apple-system,PingFang SC,sans-serif;min-width:220px;padding:4px 0">
        <b style="font-size:15px">${p.name}</b>
        <div style="margin-top:6px;display:flex;gap:8px;align-items:center">
          <span style="display:inline-block;padding:2px 8px;border-radius:4px;font-size:11px;font-weight:600;background:${color}18;color:${color}">${typeLabels[p.type] || p.type}</span>
          <span style="font-size:12px;color:#64748b">${statusEmoji} ${statusLabel(p.status)}</span>
        </div>
        <hr style="margin:8px 0;border:none;border-top:1px solid #e2e8f0"/>
        <div style="font-size:13px;color:#475569;line-height:1.6">${p.description || '暂无描述'}</div>
        <div style="font-size:12px;color:#94a3b8;margin-top:6px">📍 ${p.seaArea || '未知海域'} · ${p.longitude?.toFixed(2)}, ${p.latitude?.toFixed(2)}</div>
      </div>
    `)
    bounds.push([p.latitude, p.longitude])
  })
  if (bounds.length) map.fitBounds(bounds, { padding: [50, 50] })
}
</script>

<template>
  <div class="page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><SvgIcon name="map" :size="22" color="#0ea5e9"/> 监测地图</h2>
      <p>Leaflet 交互式地图 · 南海海洋环境监测点位分布</p>
    </div>

    <!-- 统计概览 -->
    <div class="stats-row">
      <div class="stats-card">
        <span class="stats-num">{{ points.length }}</span>
        <span class="stats-label">总点位</span>
      </div>
      <div class="stats-card status-on">
        <span class="stats-num">{{ activeCount }}</span>
        <span class="stats-label">运行中</span>
      </div>
      <div class="stats-card status-warn">
        <span class="stats-num">{{ maintenanceCount }}</span>
        <span class="stats-label">维护中</span>
      </div>
      <div class="stats-card status-off">
        <span class="stats-num">{{ inactiveCount }}</span>
        <span class="stats-label">已停用</span>
      </div>
    </div>

    <!-- 地图卡片 -->
    <div class="map-card">
      <div class="map-toolbar">
        <div class="toolbar-left">
          <div class="filter-group">
            <label>监测类型</label>
            <el-select v-model="typeFilter" clearable @change="fetchData" placeholder="全部类型" size="default" style="width:140px">
              <el-option label="全部类型" value="" />
              <el-option label="水质监测" value="WATER_QUALITY" />
              <el-option label="洋流监测" value="CURRENT" />
              <el-option label="污染监测" value="POLLUTION" />
              <el-option label="潮汐监测" value="TIDE" />
            </el-select>
          </div>
          <div class="filter-group">
            <label>运行状态</label>
            <el-select v-model="statusFilter" clearable @change="fetchData" placeholder="全部状态" size="default" style="width:140px">
              <el-option label="全部状态" value="" />
              <el-option label="运行中" value="ACTIVE" />
              <el-option label="维护中" value="MAINTENANCE" />
              <el-option label="已停用" value="INACTIVE" />
            </el-select>
          </div>
        </div>
        <div class="toolbar-legend">
          <span class="legend-item" v-for="(color, type) in typeColors" :key="type">
            <span class="legend-dot" :style="{ background: color }"></span>
            {{ typeLabels[type] }}
          </span>
        </div>
      </div>
      <div id="leaflet-map" class="map-container"></div>
    </div>

    <!-- 点位详情表 -->
    <div class="table-card">
      <div class="table-header">监测点位详情</div>
      <el-table :data="points" size="small" stripe max-height="320">
        <el-table-column prop="name" label="名称" width="180" fixed>
          <template #default="{ row }">
            <span class="point-name-link">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <span class="type-tag" :style="{ color: typeColors[row.type], background: typeColors[row.type] + '14' }">
              {{ typeLabels[row.type] || row.type }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="status-dot" :class="statusClass(row.status)"></span>
            {{ statusLabel(row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="坐标" width="150">
          <template #default="{ row }">
            <span class="coord-text">{{ row.longitude?.toFixed(2) }}, {{ row.latitude?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="seaArea" label="所属海域" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      </el-table>
      <div class="table-footer">
        <span>共 {{ points.length }} 个监测点位</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 4px 0;
  max-width: 1400px;
  margin: 0 auto;
}

/* ========== 标题 ========== */
.page-header {
  margin-bottom: 16px;
}
.page-header h2 {
  margin: 0; font-size: 20px; color: #0a3d62;
  display: flex; align-items: center; gap: 10px; font-weight: 700;
}
.page-header p {
  margin: 4px 0 0; color: #94a3b8; font-size: 13px;
}

/* ========== 统计卡 ========== */
.stats-row {
  display: flex; gap: 14px; margin-bottom: 14px;
}
.stats-card {
  flex: 1; background: #fff; border-radius: 10px;
  padding: 16px 20px; border: 1px solid #eaf0f6;
  box-shadow: 0 1px 3px rgba(0,0,0,.04); text-align: center;
  border-left: 3px solid #3b82f6;
}
.stats-card.status-on { border-left-color: #10b981; }
.stats-card.status-warn { border-left-color: #f59e0b; }
.stats-card.status-off { border-left-color: #94a3b8; }
.stats-num { display: block; font-size: 22px; font-weight: 700; color: #0a3d62; }
.stats-label { display: block; font-size: 12px; color: #94a3b8; margin-top: 4px; }

/* ========== 地图卡片 ========== */
.map-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.04); border: 1px solid #eaf0f6;
  margin-bottom: 14px;
}
.map-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 20px; border-bottom: 1px solid #f1f5f9;
  flex-wrap: wrap; gap: 10px;
}
.toolbar-left { display: flex; gap: 14px; }
.filter-group { display: flex; flex-direction: column; gap: 4px; }
.filter-group label { font-size: 12px; color: #94a3b8; font-weight: 500; }
.toolbar-legend { display: flex; align-items: center; gap: 14px; }
.legend-item { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748b; }
.legend-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.map-container { height: 460px; width: 100%; }

/* ========== 点位表 ========== */
.table-card {
  background: #fff; border-radius: 12px; overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,.04); border: 1px solid #eaf0f6;
}
.table-header {
  padding: 14px 20px; font-size: 14px; font-weight: 600;
  color: #1e293b; border-bottom: 1px solid #f1f5f9;
}
.table-card :deep(.el-table th.el-table__cell) {
  background: #f8fafc; color: #475569; font-weight: 600; font-size: 13px;
}
.table-card :deep(.el-table td.el-table__cell) { font-size: 13px; }
.table-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 20px; border-top: 1px solid #f1f5f9;
  font-size: 13px; color: #94a3b8;
}

.point-name-link { color: #1e293b; font-weight: 500; }
.type-tag { display: inline-block; padding: 2px 10px; border-radius: 4px; font-size: 12px; font-weight: 600; }
.coord-text { color: #64748b; font-size: 12px; font-family: 'JetBrains Mono', 'Fira Code', monospace; }
.status-dot {
  display: inline-block; width: 7px; height: 7px; border-radius: 50%;
  margin-right: 6px; vertical-align: middle;
}
.status-dot.on { background: #10b981; }
.status-dot.warn { background: #f59e0b; }
.status-dot.off { background: #94a3b8; }

@media (max-width: 768px) {
  .stats-row { flex-wrap: wrap; }
  .stats-card { flex: 1 1 calc(50% - 7px); }
  .map-container { height: 320px; }
  .toolbar-left { flex-direction: column; gap: 10px; }
}
@media (max-width: 480px) {
  .stats-card { flex: 1 1 100%; }
}
</style>

<style>
#leaflet-map { z-index: 1; }
.leaflet-popup-content { margin: 10px 14px; line-height: 1.5; }
</style>
