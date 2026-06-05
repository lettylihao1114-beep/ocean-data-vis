# RAG 向量检索增强生成 — 架构与实现

> 南海海洋数据可视化系统 · AI 智能助手 · 2026-06-05

---

## 1. 概述

AI 助手「南海智答」基于 **DeepSeek Chat API** 提供流式对话，并通过 **RAG（Retrieval-Augmented Generation）** 接入系统知识库，实现海洋领域知识的精准检索与增强回答。

### 技术选型

| 组件 | 技术 | 说明 |
|------|------|------|
| 对话模型 | DeepSeek `deepseek-chat` | SSE 流式输出，30s 超时 |
| 嵌入模型 | SiliconFlow `BAAI/bge-large-zh-v1.5` | 1024 维中文语义向量，免费 API |
| 向量存储 | 内存快照 + Redis 缓存 | 双重加速，24h 过期 |
| 相似度算法 | 余弦相似度 | 值域 [-1, 1]，越高越相似 |
| 检索策略 | Top-K + 阈值过滤 | Top-3 + 0.35 阈值 |

---

## 2. 架构流程

```
用户提问
  │
  ▼
EmbeddingService.embed(问题)
  │  SiliconFlow API → double[1024]
  ▼
KnowledgeVectorStore.retrieveContext(问题, topK=3)
  │
  ├─ 内存快照 List<double[]> vectors → 纳秒级读取
  ├─ Redis 缓存 rag:vectors → 秒级加载，24h 有效
  ├─ 余弦相似度 Top-3 检索
  ├─ 阈值过滤（< 0.35 丢弃）
  │
  ▼ 拼接文章标题 + 正文（去 Markdown）
  │
  ▼ 注入 System Prompt
  │  "请基于以下参考资料回答..."
  ▼
DeepSeek Chat API → SSE 流式逐字输出
```

### 降级策略

| 故障点 | 降级方式 |
|--------|---------|
| SiliconFlow 嵌入 API 不可用 | `embed()` 返回 null，对话退化为普通模式（无 RAG） |
| Redis 缓存未命中 | 从 MySQL `knowledge` 表加载全部文章，重新计算向量 |
| 知识库无匹配（< 阈值） | 上下文为空，AI 拒绝回答 + 推荐话题列表 |
| 嵌入计算超时 | `HttpClient` 30s 超时，返回 null |

---

## 3. 核心类

### 3.1 EmbeddingService

```java
@Service
public class EmbeddingService {
    // 配置
    @Value("${ai.siliconflow.key}")     // SiliconFlow API Key
    @Value("${ai.siliconflow.url}")     // https://api.siliconflow.cn/v1/embeddings
    @Value("${ai.siliconflow.model}")   // BAAI/bge-large-zh-v1.5

    // 核心方法
    double[] embed(String text)              // 文本 → 1024维向量
    double cosineSimilarity(double[] a, b)   // 余弦相似度
    int[] topK(queryVec, candidates, k)      // Top-K 检索（插入排序）
}
```

**向量化流程**：
1. 构建 JSON `{model, input, encoding_format: "float"}`
2. POST 到 SiliconFlow API
3. 解析 `response.data[0].embedding` → `double[1024]`

**相似度公式**：
```
cos(a, b) = Σ(a[i] · b[i]) / (√Σa[i]² · √Σb[i]²)
```

### 3.2 KnowledgeVectorStore

```java
@Component
public class KnowledgeVectorStore {
    // 常量
    REDIS_KEY = "rag:vectors"              // Redis 缓存键
    SIMILARITY_THRESHOLD = 0.35            // 相似度阈值

    // 内存快照
    List<DocEntry> docs                     // 文章元数据 + 预计算向量
    List<double[]> vectors                  // 向量数组
    volatile boolean ready                  // 就绪标志

    // 生命周期
    @EventListener(ApplicationReadyEvent.class)
    void init()                             // 启动后自动初始化
    String retrieveContext(question, topK)  // 检索 + 拼接上下文
    void rebuild()                          // 文章变更后重建
    boolean isReady()                       // 就绪检查
}
```

**初始化流程**：
1. 尝试从 Redis `rag:vectors` 加载缓存 JSON
2. 缓存命中 → 反序列化 DocEntry 列表 → 就绪
3. 缓存未命中 → 查询 MySQL 全部 PUBLISHED 文章
4. 逐篇调用 `EmbeddingService.embed(标题 + 摘要 + 正文前500字)`
5. 限速 200ms/篇（5 req/s，避免触发 API 限流）
6. 序列化写入 Redis（24h TTL）

**检索流程**：
1. `ready == false` → 返回空字符串
2. 对问题调用 `embed()` 获取查询向量
3. 对内存中所有向量计算余弦相似度
4. 插入排序取 Top-K
5. 过滤相似度 < 0.35 的结果
6. 拼接文章正文（已去除 Markdown 标记）

### 3.3 AiService

```java
@Service
public class AiService {
    void chat(userMessage, history) → SseEmitter
    List<Map> buildMessages(msg, history, ragContext)
}
```

**两种 Prompt 模式**：

| RAG 状态 | System Prompt 行为 |
|----------|-------------------|
| 有匹配 | 注入参考资料 + "请基于以上参考资料回答，如不足请如实说明" |
| 无匹配 | "知识库未收录，拒绝回答" + 7 个推荐话题（带 emoji 图标） |

**推荐话题**：
- 🌊 南海地理概况（范围、边界、岛礁、海峡）
- 🌡️ 海水温度与盐度（季节变化、垂直分布）
- 🔄 洋流系统（表层环流、深层环流、季风影响）
- 🌙 潮汐特征（成因、潮差、北部湾强潮区）
- 🛡️ 海洋污染监测（污染来源、监测指标、珠江口）
- 🐠 海洋生态保护（珊瑚礁、红树林、生物多样性）
- 📊 系统功能使用（数据大屏、监测地图、数据导出等）

---

## 4. DocEntry 数据结构

```java
public static class DocEntry {
    Long id;              // 文章 ID
    String title;         // 标题
    String summary;       // 摘要
    String content;       // 正文（Markdown）
    String category;      // 分类：GEOGRAPHY / OCEAN_CURRENT / TIDE / POLLUTION / ECOLOGY / WATER_QUALITY
    double[] embedding;   // 预计算 1024 维向量
}
```

**向量化文本**：`title + "：" + summary + " " + stripMarkdown(content).substring(0, 500)`
- BGE 模型 token 限制 512，截断 500 字符保证不超限
- 去 Markdown 标记（#、**、- 等）节省 token

---

## 5. 知识库现状（2026-06-05）

| ID | 标题 | 分类 | 覆盖话题 |
|----|------|------|---------|
| 1 | 南海的洋流系统 | OCEAN_CURRENT | 表层环流、深层环流、季风驱动 |
| 2 | 潮汐的奥秘 | TIDE | 潮汐成因、南海潮差、北部湾 |
| 3 | 海洋污染监测 | POLLUTION | 污染来源、COD/重金属、珠江口 |
| 4 | 南海地理概况 | GEOGRAPHY | 坐标范围、面积、四大群岛、海峡 |
| 5 | 南海水温与盐度 | WATER_QUALITY | 季节温度、盐度分布、层化 |
| 6 | 南海海洋生态保护 | ECOLOGY | 珊瑚礁、红树林、生物多样性 |

**维度**：1024 维 · **缓存**：Redis `rag:vectors`（24h TTL）· **启动耗时**：~1.2s（6篇 × 200ms）

---

## 6. 配置与环境变量

```yaml
# application.yml
ai:
  deepseek:
    key: ${DEEPSEEK_API_KEY}          # DeepSeek 对话 API Key（必需）
    url: https://api.deepseek.com/v1/chat/completions
    model: deepseek-chat
  siliconflow:
    key: ${SILICONFLOW_API_KEY}       # SiliconFlow 嵌入 API Key（必需）
    url: https://api.siliconflow.cn/v1/embeddings
    model: BAAI/bge-large-zh-v1.5
```

所有 Key 通过 `.env` 文件或环境变量注入，已从 Git 中排除。

---

## 7. 新增文章方法

```sql
-- 在 knowledge 表中插入新文章
INSERT INTO knowledge (title, category, summary, content, author, status)
VALUES ('标题', 'CATEGORY', '摘要', '# Markdown正文', '管理员', 'PUBLISHED');

-- 然后清除 Redis 缓存，重启后端即可自动重建向量
-- 或调用 KnowledgeVectorStore.rebuild() API
```

也可以在前端「科普知识」管理页面通过管理员账号添加，系统会自动触发 `rebuild()`。

---

## 8. 调试与监控

| 检查项 | 方法 |
|--------|------|
| RAG 是否就绪 | 查看启动日志 `RAG vector store initialized (N docs)` |
| 相似度分数 | DEBUG 日志 `RAG skip [title] similarity=X.XXX < threshold` |
| 嵌入 API 状态 | ERROR 日志 `Embedding API error XXX` |
| Redis 缓存 | `redis-cli GET rag:vectors` |
| 手动重建 | 调用 `KnowledgeVectorStore.rebuild()` |
