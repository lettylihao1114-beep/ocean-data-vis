package com.ocean.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * 向量嵌入服务 — 调用 SiliconFlow Embedding API
 */
@Slf4j
@Service
public class EmbeddingService {

    @Value("${ai.siliconflow.key}")
    private String apiKey;

    @Value("${ai.siliconflow.url:https://api.siliconflow.cn/v1/embeddings}")
    private String apiUrl;

    @Value("${ai.siliconflow.model:BAAI/bge-large-zh-v1.5}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 对单条文本生成 embedding（1024 维）
     */
    public double[] embed(String text) {
        try {
            String body = objectMapper.writeValueAsString(
                    java.util.Map.of("model", model, "input", text, "encoding_format", "float")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Embedding API error {}: {}", response.statusCode(), response.body());
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode embedding = root.path("data").get(0).path("embedding");
            double[] vec = new double[embedding.size()];
            for (int i = 0; i < embedding.size(); i++) {
                vec[i] = embedding.get(i).asDouble();
            }
            return vec;

        } catch (Exception e) {
            log.error("Embedding failed", e);
            return null;
        }
    }

    /**
     * 余弦相似度（值越大越相似）
     */
    public double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 从候选向量中找出与 query 最相似的 topK 个索引
     */
    public int[] topK(double[] queryVec, List<double[]> candidates, int topK) {
        int n = Math.min(candidates.size(), topK);
        int[] indices = new int[n];
        double[] scores = new double[n];

        for (int i = 0; i < candidates.size(); i++) {
            double sim = cosineSimilarity(queryVec, candidates.get(i));
            // 插入排序（topK 很小，直接用插入法）
            for (int j = 0; j < n; j++) {
                if (sim > scores[j]) {
                    // 后移
                    for (int k = n - 1; k > j; k--) {
                        scores[k] = scores[k - 1];
                        indices[k] = indices[k - 1];
                    }
                    scores[j] = sim;
                    indices[j] = i;
                    break;
                }
            }
        }
        return indices;
    }
}
