package com.ocean.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 对话服务 — 流式调用 DeepSeek API，接入向量 RAG 知识检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${ai.deepseek.key}")
    private String apiKey;

    @Value("${ai.deepseek.url}")
    private String apiUrl;

    @Value("${ai.deepseek.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KnowledgeVectorStore knowledgeVectorStore;

    /**
     * 流式对话（含 RAG 检索增强）
     */
    public SseEmitter chat(String userMessage, List<Map<String, String>> history) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 分钟超时

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // RAG 检索：从知识库匹配相关文章（失败不影响对话）
                String ragContext = "";
                try {
                    ragContext = knowledgeVectorStore.retrieveContext(userMessage, 3);
                } catch (Exception e) {
                    log.warn("RAG retrieval failed, proceeding without context", e);
                }

                // 构建消息列表（注入 RAG 上下文）
                List<Map<String, String>> messages = buildMessages(userMessage, history, ragContext);
                Map<String, Object> body = new HashMap<>();
                body.put("model", model);
                body.put("messages", messages);
                body.put("stream", true);
                body.put("temperature", 0.7);
                body.put("max_tokens", 2048);

                String jsonBody = objectMapper.writeValueAsString(body);

                // 建立连接
                conn = (HttpURLConnection) new URI(apiUrl).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10_000);
                conn.setReadTimeout(120_000);

                // 发送请求
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                // 读取流式响应
                int status = conn.getResponseCode();
                if (status != 200) {
                    String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                    log.error("AI API error {}: {}", status, error);
                    emitter.send(SseEmitter.event().name("error").data("AI 服务异常，请稍后重试"));
                    emitter.complete();
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            try {
                                JsonNode node = objectMapper.readTree(data);
                                JsonNode delta = node.path("choices").get(0).path("delta").path("content");
                                if (!delta.isMissingNode() && !delta.asText().isEmpty()) {
                                    emitter.send(SseEmitter.event().name("token").data(delta.asText()));
                                }
                            } catch (Exception e) {
                                // 跳过无法解析的 chunk
                            }
                        }
                    }
                }

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();

            } catch (Exception e) {
                log.error("AI stream error", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("连接失败：" + e.getMessage()));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();

        return emitter;
    }

    /**
     * 构建消息列表 — 系统提示词（含 RAG 上下文）+ 历史对话
     */
    private List<Map<String, String>> buildMessages(String userMessage,
                                                     List<Map<String, String>> history,
                                                     String ragContext) {
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示词
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是南海海洋环境数据可视化系统的 AI 助手，名为「南海智答」。\n");
        prompt.append("你的职责：\n");
        prompt.append("1. 回答用户关于南海海洋环境的问题（水温、盐度、洋流、潮汐、污染、海洋生态等）\n");
        prompt.append("2. 解释海洋科学概念，帮助用户理解数据含义\n");
        prompt.append("3. 提供海洋环境保护建议和科学知识\n");
        prompt.append("4. 如果用户询问当前系统内的数据，请告知用户可以在数据大屏、数据查询、监测地图等页面查看\n");
        prompt.append("\n要求：\n");
        prompt.append("- 回答简洁专业，用中文\n");
        prompt.append("- 涉及数据时引用具体指标（温度°C、盐度PSU、溶氧mg/L等）\n");
        prompt.append("- 每次回答控制在 300 字以内");

        // 注入 RAG 检索到的知识库上下文
        if (ragContext != null && !ragContext.isEmpty()) {
            prompt.append("\n\n---\n以下是知识库中与用户问题相关的参考资料，请参考这些内容回答问题：\n\n");
            prompt.append(ragContext);
            prompt.append("\n请基于以上参考资料回答，如果参考资料不足以回答问题，请如实说明。");
        } else {
            // 未匹配到相关知识 — 要求 AI 拒绝回答并引导用户
            prompt.append("\n\n---\n");
            prompt.append("⚠️ 重要：系统知识库中没有找到与该问题相关的资料。");
            prompt.append("请不要尝试用自己的知识回答，而是：\n");
            prompt.append("1. 礼貌告知用户该问题暂未收录\n");
            prompt.append("2. 列出你可以回答的话题类型，引导用户重新提问\n");
            prompt.append("\n你可以回答的话题包括：\n");
            prompt.append("- 🌊 南海地理概况（范围、边界、岛礁、海峡）\n");
            prompt.append("- 🌡️ 海水温度与盐度（季节变化、垂直分布）\n");
            prompt.append("- 🔄 洋流系统（表层环流、深层环流、季风影响）\n");
            prompt.append("- 🌙 潮汐特征（成因、潮差、北部湾强潮区）\n");
            prompt.append("- 🛡️ 海洋污染监测（污染来源、监测指标、珠江口）\n");
            prompt.append("- 🐠 海洋生态保护（珊瑚礁、红树林、生物多样性）\n");
            prompt.append("- 📊 系统功能使用（数据大屏、监测地图、数据导出等）");
        }

        system.put("content", prompt.toString());
        messages.add(system);

        // 历史对话（最近 10 轮）
        if (history != null) {
            int start = Math.max(0, history.size() - 20);
            for (int i = start; i < history.size(); i++) {
                messages.add(history.get(i));
            }
        }

        // 用户当前问题
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", userMessage);
        messages.add(user);

        return messages;
    }
}
