# 南海海洋环境数据可视化系统

> **课程**：信息系统综合实训 (j3225706x)  
> **选题**：附录A-10 — 南海海洋环境数据可视化系统  
> **技术栈**：Vue3 + Spring Boot 3.2 + MyBatis-Plus + MySQL 8.0 + Redis + DeepSeek API + SiliconFlow  
> **开发周期**：第15-16周（10天）  
> **GitHub**：https://github.com/lettylihao1114-beep/ocean-data-vis

---

## 一、项目概述

基于真实南海海洋数据集（水温、盐度、洋流、潮汐、污染监测），构建一个集**数据查询、可视化大屏、污染地图、极端海况预警、科普知识、数据导出、AI 智能助手**为一体的 Web 信息系统。

**亮点功能**：
- 🤖 **AI 智能助手**「南海智答」— DeepSeek 流式对话 + **RAG 向量检索**，基于知识库精准回答南海海洋问题
- 📊 **数据可视化大屏** — 明亮海洋风格，ECharts 多图表联动
- 🗺️ **污染监测地图** — Leaflet 交互式地图 + 10 个监测点位
- 🔐 **三级权限体系** — Spring Security + JWT + RBAC（GUEST / USER / ADMIN）
- ⚡ **Redis 缓存加速** — DTO 层 7 个独立 TTL 缓存 + Cache-Aside 模式
- 👤 **个人中心** — 查看/编辑个人资料 + 修改密码，Banner 下拉菜单入口

数据来源：国家地球系统科学数据中心 — 南海再分析数据集（1986 至今）  
https://ocean.geodata.cn/

---

## 二、团队成员与分工

| 姓名 | 学号 | GitHub | 角色 | 负责模块 |
|------|------|--------|------|---------|
| 黎浩 | — | [lettylihao1114-beep](https://github.com/lettylihao1114-beep) | 后端架构师 + 全栈开发 | 系统架构设计、数据库设计、Spring Boot 后端、JWT+RBAC 认证、RESTful API、安全防护（XSS/SQL注入）、Redis DTO 层缓存（7 TTL + Cache-Aside）、API 文档（Knife4j）、用户管理模块、个人中心模块、AI 大模型流式对话 + RAG 向量检索增强（DeepSeek + SiliconFlow 嵌入）、环境变量安全化、代码审查与优化 |
| 郑伟民 | — | [buliish](https://github.com/buliish) | 前端开发 + 数据可视化 | Vue3 前端框架、Leaflet 地图可视化、ECharts 数据大屏、业务页面（查询/预警/科普/导出/管理/AI助手）、前后端联调、ER 图、实训报告 |

### 详细分工

```
黎浩（后端 & 架构）
├── 系统架构设计与技术选型
├── 数据库设计（7 张表 ER 图 + schema.sql + seed.sql）
├── Spring Boot 3.2 后端框架搭建
├── Spring Security + JWT + RBAC 认证授权（401/403 精准区分）
├── 8 个 Controller（Auth / OceanData / MonitorPoint / Alert / Knowledge / Export / User / AI）
├── 10 个 Service（含 EmbeddingService + KnowledgeVectorStore）
├── MyBatis-Plus 数据访问层 + 分页查询
├── BCrypt 密码加密 + 防 SQL 注入（XML Mapper）+ 防 XSS（XssFilter）
├── Redis DTO 层缓存（7 个独立 TTL + Cache-Aside + JSR310 序列化）
├── 全局异常拦截器（GlobalExceptionHandler）
├── Knife4j / Swagger API 文档
├── 用户管理模块（角色切换 / 账号启停）
├── 个人中心模块（资料查看编辑 / 密码修改）🆕
├── AI 大模型流式对话（SSE + DeepSeek API）+ RAG 向量检索增强（SiliconFlow BGE 嵌入 + 余弦相似度 Top-3 + 阈值过滤）
├── 注册密码校验增强（字母+数字组合）
├── 环境变量安全化（java-dotenv + .env + ${ENV_VAR} 占位符）
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
├── 用户管理后台页面 + 个人中心页面 🆕
├── 前后端 API 联调测试
├── ER 图绘制
└── 实训报告 + 实训日志撰写
```

---

## 三、用户角色 & 权限矩阵

| 操作 | GUEST | USER | ADMIN |
|------|:--:|:--:|:--:|
| 浏览科普 / 数据大屏 | ✅ | ✅ | ✅ |
| 活跃预警滚动条 | ✅ | ✅ | ✅ |
| 监测地图 | ✅ | ✅ | ✅ |
| 数据查询 / 导出 | ❌ | ✅ | ✅ |
| AI 智能助手 | ❌ | ✅ | ✅ |
| 个人中心（查看/编辑资料/改密） | ❌ | ✅ | ✅ |
| 预警管理（发布/解除/列表） | ❌ | ❌ | ✅ |
| 科普管理 / 用户管理 | ❌ | ❌ | ✅ |

**测试账号**：`admin` / `admin123`（管理员）· `testuser` / `test1234`（普通用户）

---

## 四、功能模块（9 个）

```
Module 01 — 用户认证
  ├── 注册 / 登录（JWT Token + BCrypt 加密）
  ├── RBAC 三级角色权限控制（GUEST / USER / ADMIN）
  └── 401（未认证→跳登录）/ 403（角色不足→弹提示）精准区分

Module 02 — 海洋数据查询
  ├── 水温 / 盐度 / 溶氧 / pH 多维查询
  ├── 洋流 / 潮汐时空筛选
  └── 分页列表 + 详情（Redis DTO 层缓存）

Module 03 — 数据可视化大屏
  ├── 大屏概览（明亮海洋风格，白卡片 + 柔和蓝绿配色）
  ├── 历史趋势图（散点图 + 折线图 + 柱状图 + 仪表盘）
  └── ECharts 多图表联动

Module 04 — 污染监测
  ├── Leaflet 交互式地图 + 10 个监测点位标注
  ├── 点位详情 + 历史对比
  └── 超标高亮 + 趋势预警线

Module 05 — 预警管理
  ├── 活跃预警滚动条（公开）+ 管理员专享列表
  ├── 管理员：发布预警（弹窗表单：类型/等级/标题/内容/海域/时间）
  └── 解除活跃预警（仅 ADMIN）

Module 06 — 科普知识
  ├── 分类图文列表 + Markdown 渲染
  ├── 6 篇科普文章（地理/洋流/潮汐/水温盐度/污染/生态）
  └── 管理员编辑管理（新增/修改/发布）

Module 07 — 数据导出
  ├── 查询结果 → Excel / CSV（EasyExcel）
  └── 支持按时间/海域筛选导出

Module 08 — AI 智能助手「南海智答」🆕
  ├── 大模型流式对话（DeepSeek deepseek-chat，SSE 逐字输出）
  ├── RAG 向量检索增强（SiliconFlow BGE 1024 维嵌入）
  │   ├── 知识库向量化：标题 + 摘要 + 正文前 500 字
  │   ├── 余弦相似度 Top-3 检索 + 0.35 阈值过滤
  │   ├── Redis 24h 缓存 + 内存快照双重加速
  │   └── 降级策略：嵌入 API 故障 → 自动退化为普通对话
  ├── 智能引导：知识库未命中 → 拒绝回答 + 推荐 7 个话题
  └── 参考文档：docs/RAG.md

Module 09 — 个人中心 🆕
  ├── 查看个人资料（用户名/角色/邮箱/手机号/注册时间）
  ├── 编辑资料（邮箱 + 手机号）
  ├── 修改密码（旧密码验证 + BCrypt 加密更新）
  └── Banner 用户下拉菜单入口（个人中心 + 退出登录）
```

---

## 五、技术架构

```
┌──────────────────────────────────────────────────────────┐
│                 前端 SPA (Vue3 + TS)                      │
│     Element Plus │ ECharts │ Leaflet │ Pinia             │
│            Axios + JWT Token 拦截器                       │
└───────────────────────┬──────────────────────────────────┘
                        │ RESTful API + SSE (AI 流式)
┌───────────────────────┴──────────────────────────────────┐
│             后端服务 (Spring Boot 3.2)                    │
│  Spring Security │ JWT │ MyBatis-Plus │ Redis            │
│  9 Controller → 11 Service → Mapper → Entity            │
│                                                          │
│  AI 模块：AiService → KnowledgeVectorStore               │
│           → EmbeddingService (SiliconFlow BGE)           │
│           → DeepSeek Chat API (SSE 流式)                 │
└───────────────────────┬──────────────────────────────────┘
                        │
┌───────────────────────┴──────────────────────────────────┐
│              MySQL 8.0 + Redis 5.0                       │
│  Redis: DTO 缓存 + RAG 向量缓存 (rag:vectors, 24h TTL)   │
└──────────────────────────────────────────────────────────┘
                        │
┌───────────────────────┴──────────────────────────────────┐
│              外部 API                                     │
│  DeepSeek Chat API (对话) + SiliconFlow Embedding API     │
└──────────────────────────────────────────────────────────┘
```

| 层 | 技术 | 说明 |
|----|------|------|
| 前端框架 | Vue 3 + TypeScript | SPA 单页应用 |
| UI 组件 | Element Plus | 表格/表单/弹窗 |
| 图表 | ECharts 5 | 散点/折线/柱状/仪表盘 |
| 地图 | Leaflet | 监测点位标注 |
| 状态管理 | Pinia | Vue3 官方推荐 |
| 后端框架 | Spring Boot 3.2 | REST 服务 |
| ORM | MyBatis-Plus 3.5.5 | 单表 CRUD 零 SQL |
| 安全 | Spring Security + JWT + RBAC + BCrypt | A 级评分要求 |
| 缓存 | Redis（DTO 7 TTL + RAG 向量缓存） | Cache-Aside 模式 |
| 数据库 | MySQL 8.0 | 结构化存储 |
| 导出 | EasyExcel 3.3.3 | Excel / CSV |
| AI 对话 | DeepSeek `deepseek-chat` | SSE 流式逐字输出 |
| AI 嵌入 | SiliconFlow `BAAI/bge-large-zh-v1.5` | 1024 维中文语义向量 |
| 环境变量 | java-dotenv 5.2.2 | .env 自动加载 |

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
├── frontend/                     # Vue3 前端
│   └── src/
│       ├── api/                 # Axios 接口封装
│       ├── views/               # 12 页面（Home/Login/Dashboard/DataQuery/MonitorMap/
│       │                          Alerts/Knowledge/Export/AIChat/Profile/Admin）
│       ├── components/          # 复用组件（图表卡片/地图/表格）
│       ├── store/               # Pinia 状态管理
│       └── router/              # 路由 + 角色权限守卫
├── backend/                      # Spring Boot 后端
│   ├── .env.example             # 环境变量模板（可提交 Git）
│   ├── .env                     # 本地环境变量（gitignored）
│   └── src/main/java/com/ocean/
│       ├── controller/          # 9 个 REST Controller
│       ├── service/             # 11 个 Service
│       │   ├── AiService.java           # DeepSeek SSE 流式对话
│       │   ├── EmbeddingService.java    # SiliconFlow 向量嵌入
│       │   └── KnowledgeVectorStore.java # RAG 向量存储与检索
│       ├── mapper/              # MyBatis-Plus Mapper
│       ├── entity/              # 7 个实体类
│       ├── dto/                 # 数据传输对象（含 PageVO）
│       ├── config/              # Security / JWT / Redis / CORS 配置
│       └── utils/               # JwtUtil 等工具类
├── database/                     # SQL 脚本
│   ├── schema.sql               # 7 张表建表语句
│   └── seed.sql                 # 测试数据（6 篇科普 + 3 测试账号）
└── docs/                         # 文档
    ├── RAG.md                   # RAG 架构详解
    ├── PRD-个人中心.md           # 个人中心 PRD 🆕
    ├── requirements.md          # 需求分析
    ├── api-design.md            # 接口设计
    └── er-diagram.md            # ER 图
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
| 8 | 第16周周三 | 前端开发-收尾 | 地图、预警、科普、导出页 + AI 助手页面 | — |
| 9 | 第16周周四 | 测试 & 优化 | 单元测试 + 接口测试 + 功能测试 + 用户管理 + 权限审查 + XSS 防护 | 日志5 |
| 10 | 第16周周五 | 报告撰写 | 论文 + 答辩 PPT | — |

---

## 九、评分对照自查

| 评分维度 | 分值 | 本项目覆盖情况 |
|---------|------|--------------|
| 选题难度 | 15 | 前后端分离 + 8 模块 + JWT+RBAC + 地图可视化 + AI 大模型 → **A级** |
| 论文结构格式 | 12 | ER图/架构图/时序图/接口文档/≥5参考文献 → **A级** |
| 设计内容论证 | 33 | RESTful + 3NF + BCrypt/防注入/XSS + 三级权限 + 环境变量安全化 → **A级** |
| 系统开发能力 | 20 | Git协作 + 分层架构（8 Controller + 10 Service）+ Redis缓存 + API文档 → **A级** |
| 运行展示效果 | 20 | 无BUG + 数据大屏（明亮海洋风）+ 地图 + AI流式对话 + RAG检索 → **A级** |
| 加分项 | +5 | 真实数据集 ✅ · 数据可视化大屏 ✅ · **AI大模型 + RAG向量检索** ✅ → **+5** |

### 一票否决项自查
| 否决条件 | 状态 |
|---------|:--:|
| 未前后端分离 → D/E | ✅ 已分离 |
| 无认证/权限控制 → C以下 | ✅ JWT + RBAC 三级 |
| 明文存储密码 → C以下 | ✅ BCrypt 加密 |

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

## 十一、快速启动

### 环境要求
- JDK 17+ · Maven 3.9+ · Node.js v24+ · MySQL 8.0 · Redis 5.0+

### 1. 配置环境变量
```bash
cd backend
cp .env.example .env
# 编辑 .env，填入 DeepSeek 和 SiliconFlow 的 API Key
```

### 2. 启动服务
```bash
# Redis
cd E:\Redis && .\redis-server.exe

# 后端（端口 8080）
cd backend
mvn spring-boot:run
# API 文档：http://localhost:8080/doc.html

# 前端（端口 5173）
cd frontend
npm install && npm run dev
```

### 3. 测试账号
| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| testuser | test1234 | 普通用户 |

### 4. 环境变量说明
| 变量 | 必需 | 说明 |
|------|:--:|------|
| `DEEPSEEK_API_KEY` | ✅ | DeepSeek 对话 API Key |
| `SILICONFLOW_API_KEY` | ✅ | SiliconFlow 嵌入 API Key（RAG 用） |
| `DB_USERNAME` | — | 数据库用户名（默认 root） |
| `DB_PASSWORD` | — | 数据库密码（默认 123456） |
| `JWT_SECRET` | — | JWT 签名密钥（有默认值） |

> ⚠️ `.env` 文件已 gitignored，不会被提交到 Git。生产环境请使用系统环境变量。

---

## 十二、参考资源

- 国家地球系统科学数据中心 — 南海再分析数据集：https://ocean.geodata.cn/
- 阿里巴巴 Java 开发手册（终极版）
- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- Vue 3 官方文档：https://cn.vuejs.org/
- ECharts 官方示例：https://echarts.apache.org/examples/
- DeepSeek API 文档：https://platform.deepseek.com/api-docs/
- SiliconFlow 嵌入模型：https://siliconflow.cn/models/embedding
- RAG 架构详解：docs/RAG.md
