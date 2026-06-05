<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { monitorAPI } from '@/api'
import type { MonitorPoint } from '@/types'

const points = ref<MonitorPoint[]>([])
const typeFilter = ref('')
const statusFilter = ref('')

let map: L.Map | null = null
let markersLayer: L.LayerGroup | null = null

const typeLabels: Record<string, string> = { WATER_QUALITY: '水质', CURRENT: '洋流', POLLUTION: '污染', TIDE: '潮汐' }
const typeIcons: Record<string, string> = {
  WATER_QUALITY: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  CURRENT: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  POLLUTION: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
  TIDE: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-violet.png',
}

delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({ iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png', iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png', shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png' })

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
    marker.bindPopup(`<div style="font-family:sans-serif;min-width:200px"><b style="font-size:15px">${p.name}</b><br/><span style="color:#666">${typeLabels[p.type]||p.type} · ${p.status==='ACTIVE'?'🟢 运行中':p.status==='MAINTENANCE'?'🟡 维护中':'⚫ 已停用'}</span><hr style="margin:6px 0"/><div style="font-size:13px;color:#444">${p.description||'暂无描述'}</div><div style="font-size:12px;color:#999;margin-top:4px">${p.seaArea||''}</div></div>`)
    bounds.push([p.latitude, p.longitude])
  })
  if (bounds.length) map.fitBounds(bounds, { padding: [50, 50] })
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2>🗺️ 监测地图</h2>
      <p>Leaflet 交互式地图 · {{ points.length }} 个监测点位</p>
    </div>

    <!-- 筛选 + 地图 -->
    <div class="map-card">
      <div class="map-toolbar">
        <div class="toolbar-filters">
          <el-select v-model="typeFilter" clearable @change="fetchData" placeholder="监测类型" size="default" style="width:150px">
            <el-option label="全部类型" value="" />
            <el-option label="水质监测" value="WATER_QUALITY" />
            <el-option label="洋流监测" value="CURRENT" />
            <el-option label="污染监测" value="POLLUTION" />
            <el-option label="潮汐监测" value="TIDE" />
          </el-select>
          <el-select v-model="statusFilter" clearable @change="fetchData" placeholder="运行状态" size="default" style="width:150px">
            <el-option label="全部状态" value="" />
            <el-option label="运行中" value="ACTIVE" />
            <el-option label="维护中" value="MAINTENANCE" />
            <el-option label="已停用" value="INACTIVE" />
          </el-select>
        </div>
        <div class="toolbar-legend">
          <span class="legend-dot" style="background:#3b82f6"></span>水质
          <span class="legend-dot" style="background:#06b6d4"></span>洋流
          <span class="legend-dot" style="background:#ef4444"></span>污染
          <span class="legend-dot" style="background:#8b5cf6"></span>潮汐
        </div>
      </div>
      <div id="leaflet-map" class="map-container"></div>
    </div>

    <!-- 底部点位表 -->
    <div class="table-card">
      <div class="table-header">监测点位详情</div>
      <el-table :data="points" size="small" stripe max-height="240">
        <el-table-column prop="name" label="名称" width="180" fixed />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.type==='POLLUTION'?'danger':row.type==='CURRENT'?'warning':row.type==='TIDE'?'info':'success'">
              {{ typeLabels[row.type] || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status==='ACTIVE'?'success':row.status==='MAINTENANCE'?'warning':'info'" effect="plain">
              {{ row.status==='ACTIVE'?'运行中':row.status==='MAINTENANCE'?'维护中':'停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="longitude" label="经度" width="90" />
        <el-table-column prop="latitude" label="纬度" width="85" />
        <el-table-column prop="seaArea" label="海域" width="110" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 4px 0; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; color: #0a3d62; font-weight: 700; }
.page-header p { margin: 4px 0 0; color: #94a3b8; font-size: 13px; }

.map-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; margin-bottom: 14px; }
.map-toolbar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; border-bottom: 1px solid #eaf0f6; flex-wrap: wrap; gap: 10px; }
.toolbar-filters { display: flex; gap: 10px; }
.toolbar-legend { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748b; }
.legend-dot { width: 8px; height: 8px; border-radius: 50%; }
.map-container { height: 420px; width: 100%; }

.table-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 6px rgba(0,0,0,.04); border: 1px solid #eaf0f6; }
.table-header { padding: 14px 20px; font-size: 14px; font-weight: 600; color: #1e293b; border-bottom: 1px solid #eaf0f6; }
.table-card :deep(.el-table th) { background: #f8fafc; color: #475569; font-weight: 600; }
</style>

<style>
#leaflet-map { z-index: 1; }
.leaflet-popup-content { margin: 10px 14px; line-height: 1.5; }
</style>
