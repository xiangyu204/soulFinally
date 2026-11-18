package com.example.soul.controller;

import com.example.soul.entity.Message;
import com.example.soul.entity.User;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * AI 控制器：提供基于 DeepSeek 的翻译、AI 学习/交流指南、客服聊天助手等接口。
 * 所有接口在调用失败时都会降级返回提示或原文，而不是抛出异常。
 */
@RestController
public class AiController extends BaseApiController {

    /** 翻译请求 DTO：text 待翻译文本；targetLang 目标语言。 */
    public record TranslateRequest(String text, String targetLang) {}

    /**
     * POST /api/translate —— 文本翻译（返回纯文本 UTF-8）。
     *
     * @param req 翻译请求（text、targetLang）
     * @return 翻译结果；翻译失败时原样返回原文（text 为 null 则返回空字符串）
     */
    @PostMapping(value = "/api/translate", produces = "text/plain;charset=UTF-8")
    public String translateAlias(@RequestBody TranslateRequest req) {
        try {
            return deepSeek.translate(req.text(), req.targetLang());
        } catch (Exception e) {
            return req.text() == null ? "" : req.text();
        }
    }

    /**
     * GET /api/ai/guide —— 获取 AI 推荐/指南。
     *
     * @param username 当前用户名
     * @param targetLang 目标语言
     * @return AiGuideResponse：AI 生成的内容；失败时内容为错误提示
     */
    @GetMapping("/api/ai/guide")
    public AiGuideResponse getAiGuide(@RequestParam("username") String username,
                                      @RequestParam("targetLang") String targetLang) {
        try {
            return new AiGuideResponse(deepSeek.guide(username, targetLang));
        } catch (Exception e) {
            return new AiGuideResponse("AI 추천 기능을 사용할 수 없습니다: " + e.getMessage());
        }
    }

    /** AI 聊天请求 DTO：message 用户输入的消息；lang 用户语言（当前逻辑中未使用）。 */
    public record AiChatRequest(String message, String lang) {}

    /**
     * POST /api/ai/chat —— SOUL 平台客服助手聊天。
     *
     * @param req 聊天请求（message、lang）
     * @return Map：成功时 success=true、answer 为回答；失败时 success=false、answer 为空并带 error 信息
     * 业务规则：通过系统提示词限定助手只回答匹配、聊天翻译、技能测验、好友、通话、评分、资料设置相关问题，
     * 不确定时需如实说明；回答最大长度参数为 700。
     */
    @PostMapping("/api/ai/chat")
    public java.util.Map<String, Object> aiChat(@RequestBody AiChatRequest req) {
        try {
            // 系统提示词：约束助手角色与回答范围
            String sys = "You are SOUL's support assistant for a skill-exchange platform. "
                    + "Answer concisely in the user's language about: matching, chat and translation, "
                    + "AI skill quiz, friends, calls, ratings and profile settings. "
                    + "If you are not sure about a feature, say so instead of guessing.";
            // 消息为空时按空字符串处理
            String user = req.message() == null ? "" : req.message();
            String answer = deepSeek.chat(java.util.List.of(
                    java.util.Map.of("role", "system", "content", sys),
                    java.util.Map.of("role", "user", "content", user)), 700);
            return java.util.Map.of("success", true, "answer", answer == null ? "" : answer);
        } catch (Exception e) {
            return java.util.Map.of("success", false, "answer", "", "error", String.valueOf(e.getMessage()));
        }
    }

    /**
     * POST /api/ai/translate —— 翻译文本（表单/查询参数形式）。
     *
     * @param text 待翻译文本
     * @param targetLang 目标语言
     * @return AiGuideResponse：翻译结果；失败时内容为“翻译失败: 原因”
     */
    @PostMapping("/api/ai/translate")
    public AiGuideResponse translateText(@RequestParam("text") String text,
                                         @RequestParam("targetLang") String targetLang) {
        try {
            return new AiGuideResponse(deepSeek.translate(text, targetLang));
        } catch (Exception e) {
            return new AiGuideResponse("翻译失败: " + e.getMessage());
        }
    }

}
