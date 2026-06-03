# 南海海洋环境数据可视化系统

> **课程**：信息系统综合实训 (j3225706x)  
> **选题**：附录A-10 — 南海海洋环境数据可视化系统  
> **技术栈**：Vue3 + Spring Boot + MyBatis-Plus + MySQL + Redis  
> **开发周期**：第15-16周（10天）

---


---

## 一、项目概述

基于真实南海海洋数据集（水温、盐度、洋流、潮汐、污染监测），构建一个集**数据查询、可视化大屏、污染地图、极端海况预警、科普知识、数据导出**为一体的 Web 信息系统。

数据来源：国家地球系统科学数据中心 — 南海再分析数据集（1986 至今）  
https://ocean.geodata.cn/

---

## 二、团队成员与分工

| 姓名 | 学号 | GitHub | 角色 | 负责模块 |
|------|------|--------|------|---------|
| 黎浩 | — | [lettylihao1114-beep](https://github.com/lettylihao1114-beep) | 后端架构师 + 项目管理 | 系统架构设计、数据库设计、Spring Boot 后端、JWT+RBAC 认证、RESTful API、安全防护、Redis 缓存、Knife4j 文档 |
| 郑伟民 | — | [buliish](https://github.com/buliish) | 前端开发 + 数据可视化 | Vue3 前端框架、Leaflet 地图可视化、ECharts 数据大屏、业务页面（查询/预警/科普/导出/管理）、前后端联调、ER 图、实训报告 |

### 详细分工

```
黎浩（后端 & 架构）
├── 系统架构设计与技术选型
├── 数据库设计（7 张表 ER 图 + schema.sql + seed.sql）
├── Spring Boot 3 后端框架搭建
├── Spring Security + JWT + RBAC 认证授权
├── 6 个 Controller（Auth / OceanData / MonitorPoint / Alert / Knowledge / Export）
├── MyBatis-Plus 数据访问层 + 分页查询
├── BCrypt 密码加密 + 防 SQL 注入（XML Mapper）+ 防 XSS
├── Redis 缓存集成与配置
├── 全局异常拦截器（GlobalExceptionHandler）
├── Knife4j / Swagger API 文档
└── 环境部署与项目配置

郑伟民（前端 & 可视化）
├── Vue3 + TypeScript 前端项目搭建
├── Element Plus UI 组件布局
├── 路由设计 + Pinia 状态管理 + Axios 拦截器
├── 登录/注册页面 + Token 管理
├── ECharts 数据可视化大屏（指标卡片 + 趋势图 + 热力图）
├── Leaflet 南海地图 + 监测点位标注
├── 数据查询页（多维筛选 + 分页表格）
├── 预警管理页面 + 科普知识页面
├── 数据导出页面（Excel/CSV + 图表 PNG）
├── 用户管理后台页面
├── 前后端 API 联调测试
├── ER 图绘制
└── 实训报告 + 实训日志撰写
```

---

## 三、用户角色 & 权限

| 角色 | 核心功能 | 权限范围 |
|------|---------|---------|
| **游客** | 浏览科普知识、查看公开数据概览 | 无需登录 |
| **注册用户** | 数据查询、趋势图、污染地图、导出报表 | JWT 认证 |
| **管理员** | 数据管理、预警配置、用户管理、科普编辑 | RBAC Admin |

---

## 四、功能模块（7 个）

```
Module 01 — 用户认证
  ├── 注册 / 登录（JWT Token）
  ├── RBAC 角色权限控制
  └── 个人中心

Module 02 — 海洋数据查询
  ├── 水温 / 盐度 / 溶氧 / pH 多维查询
  ├── 洋流 / 潮汐时空筛选
  └── 分页列表 + 详情

Module 03 — 数据可视化
  ├── 大屏概览（指标卡片 + 热力图）
  ├── 历史趋势图（折线、柱状、面积）
  ├── 多参数对比（双 Y 轴）
  └── 南海海域地图（ECharts + GeoJSON）

Module 04 — 污染监测
  ├── 监测点位地图标注
  ├── 点位详情 + 历史对比
  └── 超标高亮 + 趋势预警线

Module 05 — 预警管理
  ├── 极端海况预警规则配置
  ├── 预警列表 + 状态管理
  └── 首页预警通知

Module 06 — 科普知识
  ├── 分类图文列表
  ├── Markdown 文章详情
  └── 管理员富文本编辑

Module 07 — 数据导出
  ├── 查询结果 → Excel / CSV
  └── 图表 → PNG 图片
```

---

## 五、技术架构

```
┌─────────────────────────────────────────────────┐
│              前端 SPA (Vue3 + TS)                │
│  Element Plus │ ECharts │ Leaflet │ Pinia       │
│         Axios + JWT Token 拦截器                 │
└────────────────────┬────────────────────────────┘
                     │ RESTful API
┌────────────────────┴────────────────────────────┐
│          后端服务 (Spring Boot 3)                │
│  Spring Security │ JWT │ MyBatis-Plus │ Redis   │
│  Controller → Service → Mapper → Entity          │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────┴────────────────────────────┐
│           MySQL 8.0 + Redis                     │
└─────────────────────────────────────────────────┘
```

| 层 | 技术 | 说明 |
|----|------|------|
| 前端框架 | Vue 3 + TypeScript | SPA 单页应用 |
| UI 组件 | Element Plus | 表格/表单/弹窗 |
| 图表 | ECharts 5 | 折线/柱状/热力/地图 |
| 地图 | Leaflet / ECharts Map | 监测点位标注 |
| 状态管理 | Pinia | Vue3 官方推荐 |
| 后端框架 | Spring Boot 3.1+ | REST 服务 |
| ORM | MyBatis-Plus | 单表 CRUD 零 SQL |
| 安全 | Spring Security + JWT + RBAC | A 级评分要求 |
| 缓存 | Redis | 热点数据缓存 |
| 数据库 | MySQL 8.0 | 结构化存储 |
| 导出 | Apache POI + EasyExcel | Excel/CSV |

---

## 六、数据库设计（7 张核心表）

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `users` | 用户表 | id, username, password(加密), email, role |
| `ocean_data` | 海洋观测数据 | id, time, lng, lat, temperature, salinity, oxygen, ph, current_speed, tide_level |
| `monitor_points` | 监测点位 | id, name, lng, lat, type, status, description |
| `alerts` | 预警记录 | id, type, level, title, content, start_time, end_time, status |
| `knowledge` | 科普文章 | id, title, category, content, cover_url, author |
| `export_logs` | 导出日志 | id, user_id, type, params, create_time |
| `sys_logs` | 系统日志 | id, user, action, ip, create_time |

---

## 七、项目目录结构

```
ocean-data-vis/
├── frontend/                  # Vue3 前端
│   └── src/
│       ├── api/              # Axios 接口封装
│       ├── views/            # 页面（大屏/查询/地图/预警/科普/导出/管理）
│       ├── components/       # 复用组件（图表卡片/地图/表格）
│       ├── store/            # Pinia 状态管理
│       └── router/           # 路由 + 权限守卫
├── backend/                   # Spring Boot 后端
│   └── src/main/java/com/ocean/
│       ├── controller/       # REST 接口层
│       ├── service/          # 业务逻辑层
│       ├── mapper/           # MyBatis-Plus 数据层
│       ├── entity/           # 实体类
│       ├── dto/              # 数据传输对象
│       ├── config/           # Security / JWT / Redis 配置
│       └── utils/            # 工具类
├── database/                  # SQL 脚本
│   ├── schema.sql            # 建表语句
│   └── seed.sql              # 测试数据
└── docs/                      # 文档
    ├── requirements.md       # 需求分析
    ├── api-design.md         # 接口设计
    └── er-diagram.md         # ER 图
```

---

## 八、10 天开发计划

| 天 | 日期 | 阶段 | 核心产出 | 日志 |
|----|------|------|---------|------|
| 1 | 第15周周一 | 环境搭建 + 需求分析 | 项目骨架、数据库建表、需求文档 | 日志1 |
| 2 | 第15周周二 | 分析设计 | ER 图、接口文档、原型图 | — |
| 3 | 第15周周三 | 后端开发-认证 | JWT + RBAC + 用户模块 | 日志2 |
| 4 | 第15周周四 | 后端开发-数据 | 数据查询 API + 预警 + 导出 | — |
| 5 | 第15周周五 | 后端开发-收尾 | 科普 API + 日志 + 全接口联调 | 日志3 |
| 6 | 第16周周一 | 前端开发-框架 | 项目骨架、路由、登录页、布局 | — |
| 7 | 第16周周二 | 前端开发-核心 | 数据大屏、查询页、图表组件 | 日志4 |
| 8 | 第16周周三 | 前端开发-收尾 | 地图、预警、科普、导出页 | — |
| 9 | 第16周周四 | 测试 | 单元测试 + 接口测试 + 功能测试 | 日志5 |
| 10 | 第16周周五 | 报告撰写 | 论文 + 答辩 PPT | — |

---

## 九、评分对照自查

| 评分维度 | 分值 | 本项目覆盖情况 |
|---------|------|--------------|
| 选题难度 | 15 | 前后端分离 + 7模块 + JWT+RBAC + 地图可视化 → **A级** |
| 论文结构格式 | 12 | ER图/架构图/时序图/接口文档/≥5参考文献 → **A级** |
| 设计内容论证 | 33 | RESTful + 3NF + 加密/防注入/XSS + 完整权限 → **A级** |
| 系统开发能力 | 20 | Git协作 + 分层架构 + 单元测试 + Redis缓存 → **A级** |
| 运行展示效果 | 20 | 无BUG + 数据大屏 + 地图 + 响应<200ms → **A级** |
| 加分项 | +5 | 数据可视化大屏、真实数据集 → **+3~5** |

---

## 十、实训日志模板

> 每人每2天1篇，共5篇，每篇≥300字

<details>
<summary>📝 日志1：环境搭建与需求分析（Day1）</summary>

### 今日工作
1. 搭建开发环境（JDK17、Node.js、MySQL、Redis、IDEA、VS Code）
2. 分析选题需求，明确系统角色（游客/注册用户/管理员）
3. 完成功能模块划分（认证、查询、可视化、污染监测、预警、科普、导出）
4. 设计数据库 E-R 图，编写建表 SQL 脚本
5. 初始化 Git 仓库并推送 GitHub

### 遇到的问题
- （记录环境配置中的坑）

### 明日计划
- 完善接口文档
- 搭建 Spring Boot 后端骨架 + JWT 认证模块

---
</details>

<details>
<summary>📝 日志2：后端开发 — 认证与数据模块（Day3）</summary>

### 今日工作
1. 实现 Spring Security + JWT 登录认证
2. 完成 RBAC 角色权限控制（3 角色）
3. 开发海洋数据查询 API（水温/盐度/溶氧/pH/洋流/潮汐）
4. 实现 MyBatis-Plus 分页查询 + 多条件筛选
5. Redis 缓存热点数据

### 遇到的问题
- （记录开发中的技术难点）

### 明日计划
- 预警模块 + 数据导出接口
- 科普知识 CRUD 接口

---
</details>

<details>
<summary>📝 日志3：后端收尾 + 全接口联调（Day5）</summary>

### 今日工作
1. 完成预警规则配置 + 预警推送接口
2. 实现 Apache POI Excel/CSV 导出
3. 科普知识模块（分类、Markdown 渲染）
4. 系统日志 AOP 切面记录
5. Postman 全接口联调测试通过

### 遇到的问题
- （记录联调中的问题）

### 明日计划
- 搭建 Vue3 前端骨架 + 路由 + 登录页

---
</details>

<details>
<summary>📝 日志4：前端开发 — 核心页面（Day7）</summary>

### 今日工作
1. Vue3 + Element Plus 项目骨架搭建
2. 登录页 + 路由权限守卫 + Axios 拦截器
3. 数据可视化大屏（ECharts 指标卡片 + 趋势图 + 热力图）
4. 数据查询页（分页表格 + 多条件筛选 + 详情弹窗）
5. 前端调用后端 API 联调通过

### 遇到的问题
- （记录前端开发中的难点）

### 明日计划
- 污染地图 + 预警页面 + 科普页面 + 导出功能

---
</details>

<details>
<summary>📝 日志5：测试与部署（Day9）</summary>

### 今日工作
1. JUnit 5 + Mockito 编写后端单元测试（Service 层）
2. 接口集成测试（Controller 层 + 权限验证）
3. 前端功能测试（各模块流程走通）
4. 性能测试（Jmeter 并发测试，响应时间 < 200ms）
5. 修复测试中发现的 Bug

### 遇到的问题
- （记录测试中的 Bug 及修复方案）

### 实训总结
- （简要总结 2 周收获）

---
</details>

---

## 十一、参考资源

- 国家地球系统科学数据中心 — 南海再分析数据集：https://ocean.geodata.cn/
- 阿里巴巴 Java 开发手册（终极版）
- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- Vue 3 官方文档：https://cn.vuejs.org/
- ECharts 官方示例：https://echarts.apache.org/examples/
