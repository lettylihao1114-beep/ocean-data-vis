package com.ocean.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocean.entity.Knowledge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 知识库向量存储 — 启动时预计算所有科普文章向量，支持语义检索
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeVectorStore {

    private static final String REDIS_KEY = "rag:vectors";

    private final KnowledgeService knowledgeService;
    private final EmbeddingService embeddingService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 文章摘要列表（内存快照，避免每次查 DB） */
    private final List<DocEntry> docs = new ArrayList<>();
    /** 对应的向量列表 */
    private final List<double[]> vectors = new ArrayList<>();
    /** 是否就绪 */
    private volatile boolean ready = false;

    /**
     * 应用启动后自动初始化向量存储
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        new Thread(() -> {
            log.info("Initializing RAG vector store...");
            try {
                // 先尝试从 Redis 加载缓存
                String cached = (String) redisTemplate.opsForValue().get(REDIS_KEY);
                if (cached != null) {
                    List<DocEntry> entries = objectMapper.readValue(cached,
                            new TypeReference<List<DocEntry>>() {});
                    if (entries != null && !entries.isEmpty()) {
                        for (DocEntry e : entries) {
                            docs.add(e);
                            vectors.add(e.embedding);
                        }
                        ready = true;
                        log.info("RAG vector store loaded from Redis cache ({} docs)", docs.size());
                        return;
                    }
                }

                // 从数据库加载所有已发布文章
                var page = knowledgeService.page(1, 100, null);
                List<Knowledge> articles = page.getRecords();
                if (articles.isEmpty()) {
                    log.warn("No Knowledge articles found for RAG");
                    return;
                }

                for (Knowledge article : articles) {
                    // 向量化标题+摘要+正文（截断到 500 字，适配嵌入模型 token 限制）
                    String content = article.getContent() != null ? article.getContent() : "";
                    String text = article.getTitle() + "：" + article.getSummary()
                            + " " + truncateText(content, 500);
                    double[] vec = embeddingService.embed(text);
                    if (vec != null) {
                        docs.add(new DocEntry(article.getId(), article.getTitle(),
                                article.getSummary(), article.getContent(), article.getCategory(), vec));
                        vectors.add(vec);
                        log.info("RAG embedded: {}", article.getTitle());
                    }
                    // 限速：每秒最多 5 次调用
                    Thread.sleep(200);
                }

                // 写入 Redis 缓存（24 小时有效）
                String json = objectMapper.writeValueAsString(docs);
                redisTemplate.opsForValue().set(REDIS_KEY, json, 24, TimeUnit.HOURS);
                ready = true;
                log.info("RAG vector store initialized ({} docs)", docs.size());

            } catch (Exception e) {
                log.error("RAG initialization failed", e);
            }
        }, "rag-init").start();
    }

    /** 相似度阈值：低于此值认为不相关，不注入 RAG 上下文 */
    private static final double SIMILARITY_THRESHOLD = 0.35;

    /**
     * 检索与问题最相关的 Top-K 文章上下文，返回拼接好的文本。
     * 相似度低于阈值的结果会被过滤，避免注入不相关内容。
     */
    public String retrieveContext(String question, int topK) {
        if (!ready || vectors.isEmpty()) return "";

        double[] queryVec = embeddingService.embed(question);
        if (queryVec == null) return "";

        int[] indices = embeddingService.topK(queryVec, vectors, Math.min(topK, vectors.size()));

        StringBuilder sb = new StringBuilder();
        int added = 0;
        for (int idx : indices) {
            double similarity = embeddingService.cosineSimilarity(queryVec, vectors.get(idx));
            if (similarity < SIMILARITY_THRESHOLD) {
                log.debug("RAG skip [{}] similarity={} < threshold", docs.get(idx).title,
                        String.format("%.3f", similarity));
                continue;
            }
            DocEntry doc = docs.get(idx);
            sb.append("---\n");
            sb.append("【").append(doc.title).append("】\n");
            if (doc.content != null && !doc.content.isEmpty()) {
                sb.append(stripMarkdown(doc.content)).append("\n");
            } else {
                sb.append(doc.summary).append("\n");
            }
            added++;
        }

        if (added == 0) {
            log.info("RAG no relevant docs found (best similarity below threshold), returning empty");
            return "";
        }

        return sb.toString();
    }

    public boolean isReady() { return ready; }

    /**
     * 重建向量存储（文章增删后调用）
     */
    public void rebuild() {
        docs.clear();
        vectors.clear();
        redisTemplate.delete(REDIS_KEY);
        init();
    }

    /** 文档条目（含预计算的 embedding） */
    public static class DocEntry {
        public Long id;
        public String title;
        public String summary;
        public String content;
        public String category;
        public double[] embedding;

        public DocEntry() {}
        public DocEntry(Long id, String title, String summary, String content,
                        String category, double[] embedding) {
            this.id = id;
            this.title = title;
            this.summary = summary;
            this.content = content;
            this.category = category;
            this.embedding = embedding;
        }
    }

    /** 截断文本到指定字符数（保持语义完整） */
    private String truncateText(String text, int maxChars) {
        if (text == null || text.isEmpty()) return "";
        // 去掉 Markdown 标记再截断
        String plain = stripMarkdown(text);
        if (plain.length() <= maxChars) return plain;
        return plain.substring(0, maxChars);
    }

    /** 去掉 Markdown 标记符号，保留纯文本 */
    private String stripMarkdown(String text) {
        if (text == null) return "";
        return text
                .replaceAll("#+\\s*", "")       // 标题 #
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")  // 加粗
                .replaceAll("\\*(.+?)\\*", "$1")        // 斜体
                .replaceAll("`(.+?)`", "$1")            // 行内代码
                .replaceAll(">\\s*", "")                // 引用
                .replaceAll("[-*+]\\s+", "")            // 无序列表
                .replaceAll("\\d+\\.\\s+", "")          // 有序列表
                .replaceAll("\\n{2,}", "\n")            // 多余空行
                .trim();
    }
}
