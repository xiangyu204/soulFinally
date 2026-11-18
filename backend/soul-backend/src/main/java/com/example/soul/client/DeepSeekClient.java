package com.example.soul.client;

import com.example.soul.entity.Message;
import com.example.soul.entity.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 大模型客户端（用于翻译 / AI 建议 / 出题）。
 * 通过 JDK 自带的 HttpClient 调用 DeepSeek 的 chat completions HTTP 接口（非流式）。
 * 连接超时 30 秒，单次请求超时 120 秒；API 地址、密钥、模型名均由配置项注入。
 */
@Component
public class DeepSeekClient {

    /** 复用的 HTTP 客户端，连接超时 30 秒。 */
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    /** JSON 序列化 / 解析工具：构造请求体、解析响应体。 */
    private final ObjectMapper mapper = new ObjectMapper();

    /** DeepSeek API 密钥，来自配置项 deepseek.api.key（默认空串）；调用时放在 Authorization 请求头。 */
    @Value("${deepseek.api.key:}")
    private String apiKey;

    /** DeepSeek 接口地址，来自配置项 deepseek.api.url（有默认的官方 chat/completions 地址）。 */
    @Value("${deepseek.api.url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    /** 使用的模型名，来自配置项 deepseek.model（默认 deepseek-chat）。 */
    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    /**
     * 发送一轮对话，返回助手文本。
     * 向 DeepSeek 发起 POST 请求（stream=false），请求超时 120 秒。
     *
     * @param messages  对话消息列表，每条含 role（system/user/assistant）与 content
     * @param maxTokens 回复的最大 token 数
     * @return 模型回复文本（已去除首尾空白；响应缺少内容时为空串）
     * @throws RuntimeException HTTP 状态码非 200、网络异常或解析失败时抛出
     */
    public String chat(List<Map<String, String>> messages, int maxTokens) {
        try {
            // 组装请求体：模型、消息、非流式、最大 token
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("stream", false);
            body.put("max_tokens", maxTokens);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(120))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new RuntimeException("DeepSeek HTTP " + resp.statusCode() + ": " + resp.body());
            }

            // 取第一个候选回复：choices[0].message.content
            JsonNode root = mapper.readTree(resp.body());
            return root.path("choices").path(0).path("message").path("content").asText("").trim();
        } catch (Exception e) {
            throw new RuntimeException("DeepSeek 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构造一条对话消息（包含 role 与 content 两个键）。
     *
     * @param role    角色，如 system / user
     * @param content 消息内容
     */
    private static Map<String, String> msg(String role, String content) {
        Map<String, String> m = new HashMap<>();
        m.put("role", role);
        m.put("content", content);
        return m;
    }

    /**
     * 把 text 翻译成 targetLang。
     * 调用 chat（最大 1024 token）；系统提示要求只输出译文，并使用敬语/礼貌语体。
     *
     * @param text       待翻译的原文
     * @param targetLang 目标语言
     * @return 译文
     */
    public String translate(String text, String targetLang) {
        String sys = "你是专业翻译。请把用户给的内容翻译成目标语言，只输出译文本身，不要任何解释、前缀或引号。请使用敬语/礼貌语体：韩语一律使用 존댓말（습니다/요 结尾），日语使用 です・ます 体，中文使用礼貌用语。";
        String user = "目标语言：" + targetLang + "\n\n需要翻译的内容：\n" + text;
        return chat(List.of(msg("system", sys), msg("user", user)), 1024);
    }

    /**
     * 生成匹配建议。
     * 调用 chat（最大 512 token），以匹配顾问身份用目标语言给出简短建议。
     *
     * @param username   需要建议的用户账号
     * @param targetLang 回答所用语言
     * @return 建议文本
     */
    public String guide(String username, String targetLang) {
        String sys = "你是 SOUL 技能交换平台的匹配顾问，请用用户的目标语言给出简洁实用的匹配建议。";
        String user = "用户 " + username + " 想获得匹配建议。请用 " + targetLang + " 回答。";
        return chat(List.of(msg("system", sys), msg("user", user)), 512);
    }
}
