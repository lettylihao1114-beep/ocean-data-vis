# 前端界面优化方案 — 基于参考图对标

> 参考图来源：`image/屏幕截图 2026-06-05 213044.png`
> 目标：将当前 NMDIS 风格优化为参考图中**深色科技感数据平台**风格

---

## 一、整体风格差异对比

| 维度 | 当前设计 | 参考图设计 |
|------|----------|-----------|
| 底色 | 浅灰 `#f0f5fa` 白卡片 | **深色系** `#0c1929` ~ `#112240` |
| Banner | 深蓝渐变 + 波浪 SVG | **全宽深色头部**，科技线条动效 |
| 导航栏 | 白底 sticky | **深色半透明导航**，发光 hover |
| 卡片 | 白底 + 浅阴影 | **深色玻璃态**（glassmorphism）+ 发光边框 |
| 图表 | 白底卡片包裹 | **透明/深色背景**，霓虹色数据线 |
| 配色 | 青蓝 + 灰白 | **深蓝底 + 青色/蓝色霓虹高亮** |
| 字体颜色 | 深灰 `#1e293b` / 浅灰 `#94a3b8` | **白色/浅蓝** 为主 |
| 装饰 | 简洁扁平 | 粒子/网格线/光晕/科技边角 |

---

## 二、色彩系统

### 2.1 背景层（由深到浅）

```css
--bg-primary: #0a1628;      /* 页面底色 */
--bg-secondary: #0f2035;    /* 卡片/区块底色 */
--bg-tertiary: #142d4c;     /* 表格行/hover */
--bg-card: rgba(13, 35, 64, 0.75);  /* 玻璃态卡片 */
```

### 2.2 主题高亮色

```css
--accent-cyan: #00e5ff;     /* 主高亮（数据、hover、标题下划线） */
--accent-blue: #2979ff;     /* 次高亮（按钮、链接） */
--accent-teal: #00bcd4;     /* 图表数据线 */
--accent-green: #00e676;    /* 正常状态/成功 */
--accent-orange: #ff9100;   /* 警告 */
--accent-red: #ff5252;      /* 错误/红色预警 */
--glow-cyan: 0 0 12px rgba(0, 229, 255, 0.4);  /* 发光效果 */
```

### 2.3 文本色

```css
--text-primary: rgba(255, 255, 255, 0.95);  /* 主标题 */
--text-secondary: rgba(255, 255, 255, 0.7); /* 副标题/描述 */
--text-muted: rgba(255, 255, 255, 0.45);    /* 辅助信息 */
--text-accent: #00e5ff;                      /* 强调数字 */
```

---

## 三、组件级修改参数

### 3.1 全局 Shell（`MainLayout.vue`）

```css
.app-shell {
  background: var(--bg-primary);
  /* 可选：添加网格背景 */
  background-image: 
    linear-gradient(rgba(0, 229, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 229, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}
```

### 3.2 Banner 头部

```css
.app-banner {
  background: linear-gradient(180deg, #0a1628 0%, #0d2137 50%, #0f2a45 100%);
  min-height: 220px;  /* 适当降低高度 */
  border-bottom: 1px solid rgba(0, 229, 255, 0.15);
}
/* 去除波浪 SVG，改为科技线条 */
.banner-overlay {
  background: radial-gradient(ellipse at 50% 0%, rgba(0, 229, 255, 0.08) 0%, transparent 60%);
}
.banner-title {
  color: #fff;
  font-size: 28px;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(0, 229, 255, 0.3);
}
.banner-sub {
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 2px;
}
```

### 3.3 导航栏（`app-navbar`）

```css
.app-navbar {
  background: rgba(10, 22, 40, 0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 229, 255, 0.12);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}
.navbar-link {
  color: rgba(255, 255, 255, 0.65);
  border-radius: 6px;
  padding: 8px 16px;
  transition: all 0.2s;
}
.navbar-link:hover {
  color: #00e5ff;
  background: rgba(0, 229, 255, 0.08);
}
.navbar-link.active {
  color: #00e5ff;
  background: rgba(0, 229, 255, 0.12);
  box-shadow: 0 0 8px rgba(0, 229, 255, 0.2);
  /* 底部指示线 */
  border-bottom: 2px solid #00e5ff;
}
```

### 3.4 卡片通用（替代当前白卡片）

```css
.card {
  background: rgba(13, 35, 64, 0.75);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 229, 255, 0.12);
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.card:hover {
  border-color: rgba(0, 229, 255, 0.3);
  box-shadow: 0 4px 24px rgba(0, 229, 255, 0.08);
}
/* 卡片标题 */
.card h3 {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 600;
}
/* 卡片描述 */
.card p {
  color: rgba(255, 255, 255, 0.5);
}
```

### 3.5 科技边角装饰（可选）

```css
/* 给重要卡片添加科技边角 */
.card::before {
  content: '';
  position: absolute;
  top: 0; left: 0;
  width: 20px; height: 20px;
  border-top: 2px solid #00e5ff;
  border-left: 2px solid #00e5ff;
  border-radius: 2px 0 0 0;
}
.card::after {
  content: '';
  position: absolute;
  bottom: 0; right: 0;
  width: 20px; height: 20px;
  border-bottom: 2px solid #00e5ff;
  border-right: 2px solid #00e5ff;
  border-radius: 0 0 2px 0;
}
```

### 3.6 统计数字

```css
.stat-num {
  color: #00e5ff;
  font-size: 36px;
  font-weight: 800;
  font-family: 'DIN Alternate', 'Orbitron', monospace;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.4);
}
.stat-label {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
}
```

### 3.7 图表卡片（Dashboard）

```css
.chart-card {
  background: rgba(13, 35, 64, 0.6);
  border: 1px solid rgba(0, 229, 255, 0.1);
  border-radius: 12px;
}
.chart-card-header {
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding: 16px 20px;
}
.chart-card-header h3 {
  color: rgba(255, 255, 255, 0.9);
}
.chart-card-badge {
  background: rgba(0, 229, 255, 0.1);
  color: #00e5ff;
  border: 1px solid rgba(0, 229, 255, 0.2);
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
}
```

### 3.8 ECharts 主题参数（Dashboard.vue）

```javascript
// 全局图表配色
const CHART_THEME = {
  backgroundColor: 'transparent',
  textStyle: { color: 'rgba(255,255,255,0.65)' },
  axisLine: { lineStyle: { color: 'rgba(255,255,255,0.15)' } },
  splitLine: { lineStyle: { color: 'rgba(255,255,255,0.06)' } },
  axisLabel: { color: 'rgba(255,255,255,0.5)' },
  series: {
    line: { color: ['#00e5ff', '#00e676', '#ff9100'] },
    bar: { color: ['#2979ff', '#00bcd4'] },
  },
  tooltip: {
    backgroundColor: 'rgba(10, 22, 40, 0.9)',
    borderColor: 'rgba(0, 229, 255, 0.3)',
    textStyle: { color: '#fff' },
  },
}
```

### 3.9 页脚

```css
.app-footer {
  background: #060e1a;
  border-top: 1px solid rgba(0, 229, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
}
.footer-col strong {
  color: #00e5ff;
}
.footer-bottom {
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.25);
}
```

### 3.10 预警条

```css
.alert-ticker {
  background: rgba(13, 35, 64, 0.8);
  border: 1px solid rgba(255, 82, 82, 0.2);
  border-radius: 8px;
}
.alert-ticker-label {
  background: rgba(255, 82, 82, 0.15);
  color: #ff5252;
}
```

### 3.11 表格/列表

```css
/* 表格 */
table th {
  background: rgba(0, 229, 255, 0.05);
  color: rgba(255, 255, 255, 0.7);
  border-bottom: 1px solid rgba(0, 229, 255, 0.15);
}
table td {
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}
table tr:hover td {
  background: rgba(0, 229, 255, 0.03);
}
```

---

## 四、页面级修改清单

| 页面 | 核心改动 |
|------|---------|
| `MainLayout.vue` | Shell背景改深色 + 导航栏暗色 + 页脚暗色 |
| `Home.vue` | 所有白卡片 → 深色玻璃态 + 统计数字改霓虹色 |
| `Dashboard.vue` | 图表背景透明 + ECharts 配色切暗色主题 |
| `DataQuery.vue` | 表格深色主题 + 筛选区暗色 |
| `MonitorMap.vue` | 地图容器暗色边框 + 信息面板暗色 |
| `AIChat.vue` | 聊天气泡暗色 + 输入框暗底 |
| `Knowledge.vue` | 卡片列表暗色 |
| `Alerts.vue` | 表格暗色 + 预警标签保留彩色 |
| `ExportData.vue` | 表单区暗色 |
| `Admin.vue` | 管理面板暗色 |
| `Login.vue` | 登录卡片暗色玻璃态 + 深色全屏背景 |

---

## 五、字体建议

```css
/* 推荐组合 */
--font-display: 'DIN Alternate', 'Orbitron', sans-serif;  /* 数字/标题 */
--font-body: -apple-system, 'PingFang SC', 'Microsoft YaHei', sans-serif;  /* 正文 */
--font-mono: 'JetBrains Mono', 'Fira Code', monospace;   /* 代码/数据 */
```

如使用 Google Fonts，index.html 添加：
```html
<link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700&display=swap" rel="stylesheet">
```

---

## 六、可选动效

| 效果 | CSS 实现 |
|------|---------|
| 卡片入场 | `@keyframes fadeInUp { from { opacity:0; transform:translateY(20px) } }` |
| 数字跳动 | CSS `counter` + `@keyframes countUp` 或 Vue `<Transition>` |
| 背景粒子 | `particles.js` 或纯 CSS 浮动光点 |
| 边框呼吸光 | `@keyframes glow { 0%,100% { border-color: rgba(0,229,255,0.1) } 50% { border-color: rgba(0,229,255,0.4) } }` |
| 导航下划线滑动 | `::after` + `transform: scaleX()` + `transition` |

---

## 七、实施优先级

1. **P0** — 全局变量 + Shell 背景 + 导航栏暗色（影响全站）
2. **P1** — 卡片组件统一暗色玻璃态样式
3. **P2** — Dashboard ECharts 暗色主题
4. **P3** — 各业务页面逐个适配
5. **P4** — 动效 / 科技边角 / 粒子背景（锦上添花）

---

## 八、注意事项

- Element Plus 组件需要覆盖深色样式（`el-table`, `el-input`, `el-select` 等）
- Leaflet 地图本身支持暗色底图（换用 CartoDB Dark / Mapbox Dark 瓦片）
- 保留响应式断点，暗色主题不影响移动端适配
- 如有无障碍要求，确保对比度 ≥ 4.5:1（`#00e5ff` on `#0a1628` = 约 8.2:1 ✓）
