package com.example.soul.controller;

import com.example.soul.entity.User;
import com.example.soul.store.Store;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * AI 出题 / 判分控制器。
 * <p>
 * 开始测验时调用大模型（deepSeek）按"技能主题 + 目标语言"生成 10 道题（8 道四选一 CHOICE + 2 道填空 FILL_BLANK）；
 * 提交答案时：选择题由服务端本地对比答案判分，填空题再调用大模型判断语义是否等价（失败则退回本地规范化字符串比较）。
 * 题目（含标准答案）保存在内存 {@link Store#quizzes} 中，服务重启后会丢失；返回给客户端的题目不包含答案。
 * 等级判定：得分 ≥ 总分 80% 为 "고급"（高级），≥ 50% 为 "중급"（中级），否则为 "입문"（入门）。
 */
@RestController
public class QuizController extends BaseApiController {

    // ============ AI 出题 / 判分 ============

    /**
     * POST /api/quiz/start —— 开始一次测验（AI 现场出题）。
     * 请求体 QuizStartRequest：username（答题用户）、skill（测验主题/技能）、targetLang（题目语言）、quizType（测验类型，原样保存并回传）。
     * 逻辑：调用 generateQuestions 让大模型出题，生成新的 quizId，把完整题目（含答案）存入内存 Store；
     * 返回给客户端时只带 questionId、题干、题型、选项，不暴露答案。
     * 返回：QuizStartResponse；AI 出题失败返回 500。
     */
    @PostMapping("/api/quiz/start")
    public QuizStartResponse startQuiz(@RequestBody QuizStartRequest req) {
        List<Store.Question> questions = generateQuestions(req.skill(), req.targetLang());
        Store.Quiz quiz = new Store.Quiz();
        quiz.quizId = store.nextId();
        quiz.username = req.username();
        quiz.skill = req.skill();
        quiz.quizType = req.quizType();
        quiz.questions = questions;
        store.quizzes.add(quiz);

        List<QuestionResponse> qr = new ArrayList<>();
        for (Store.Question q : questions) {
            qr.add(new QuestionResponse(q.questionId, q.question, q.type, q.options));
        }
        return new QuizStartResponse(quiz.quizId, req.skill(), req.quizType(), qr);
    }

    /**
     * POST /api/quiz/submit —— 提交答案并判分。
     * 请求体 QuizSubmitRequest：quizId、answers（QuizAnswerRequest 列表：questionId + answer）。
     * 判分：选择题用 isCorrectChoice 本地判断（每题 1 分）；填空题统一交给 gradeFillBlanks（AI 判语义）；两者得分相加。
     * 等级：得分 ≥ 总题数 80% → "고급"，≥ 50% → "중급"，否则 "입문"。
     * 返回：QuizSubmitResponse（score、total、level、message）；找不到 quizId 时不抛错，返回 0 分和提示信息。
     */
    @PostMapping("/api/quiz/submit")
    public QuizSubmitResponse submitQuiz(@RequestBody QuizSubmitRequest req) {
        Store.Quiz quiz = store.quizzes.stream()
                .filter(q -> q.quizId.equals(req.quizId())).findFirst().orElse(null);
        if (quiz == null) {
            return new QuizSubmitResponse(0, req.answers() == null ? 0 : req.answers().size(), "입문", "퀴즈를 찾을 수 없습니다.");
        }
        // questionId → 学生答案（null 视为空串，并去掉首尾空白）
        Map<Long, String> studentAnswers = new HashMap<>();
        if (req.answers() != null) {
            for (QuizAnswerRequest a : req.answers()) {
                studentAnswers.put(a.questionId(), a.answer() == null ? "" : a.answer().trim());
            }
        }
        // 选择题当场判分；填空题先收集起来，稍后批量交给 AI 判
        int score = 0;
        List<Store.Question> fillQuestions = new ArrayList<>();
        for (Store.Question q : quiz.questions) {
            if ("FILL_BLANK".equals(q.type)) {
                fillQuestions.add(q);
            } else if (isCorrectChoice(q, studentAnswers.getOrDefault(q.questionId, ""))) {
                score++;
            }
        }
        score += gradeFillBlanks(fillQuestions, studentAnswers);
        int total = quiz.questions.size();
        String level = score >= total * 0.8 ? "고급" : (score >= total * 0.5 ? "중급" : "입문");
        return new QuizSubmitResponse(score, total, level, "테스트 완료");
    }

    /** 构造一条大模型对话消息：{"role": 角色, "content": 内容}。 */
    private static Map<String, String> m(String role, String content) {
        Map<String, String> map = new HashMap<>();
        map.put("role", role);
        map.put("content", content);
        return map;
    }

    /**
     * 调用大模型按主题出题：要求输出 10 道题（8 道 CHOICE + 2 道 FILL_BLANK）的纯 JSON 数组，题目语言为 targetLang。
     * 解析后给题目依次编号（questionId 从 1 开始），丢弃题干或答案为空的条目。
     * 调用失败、解析失败或没有任何有效题目时，抛 500 "AI 出题失败"。
     */
    private List<Store.Question> generateQuestions(String skill, String targetLang) {
        JsonNode arr;
        try {
            // 出题输出较长，第二个参数（4000）比判卷时（1000）大
            String json = deepSeek.chat(List.of(
                    m("system", "你是出题助手。请根据主题生成 10 道测试题：8 道四选一选择题（type=CHOICE，options 为 4 个选项的数组，answer 必须是正确选项的逐字完整原文）和 2 道填空题（type=FILL_BLANK，无 options，answer 为参考答案）。只输出 JSON 数组，格式：[{\"type\":\"CHOICE\",\"question\":\"题目\",\"options\":[\"选项一内容\",\"选项二内容\",\"选项三内容\",\"选项四内容\"],\"answer\":\"正确选项的完整原文\"},{\"type\":\"FILL_BLANK\",\"question\":\"题目\",\"answer\":\"参考答案\"}]，不要任何其他文字、代码块或注释。题目与选项请用「" + targetLang + "」语言输出。"),
                    m("user", "主题：" + skill)
            ), 4000);
            // 模型可能夹带多余文字，先截取最外层的 [...] 再解析
            arr = mapper.readTree(extractJson(json));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 出题失败，请稍后重试");
        }
        List<Store.Question> list = new ArrayList<>();
        long qid = 1;
        for (JsonNode node : arr) {
            String question = node.path("question").asText("");
            String answer = node.path("answer").asText("");
            if (question.isBlank() || answer.isBlank()) continue;
            // 题型只认 FILL_BLANK，其余一律当作选择题
            String type = "FILL_BLANK".equalsIgnoreCase(node.path("type").asText("")) ? "FILL_BLANK" : "CHOICE";
            List<String> options = new ArrayList<>();
            for (JsonNode o : node.path("options")) options.add(o.asText());
            Store.Question q = new Store.Question();
            q.questionId = qid++;
            q.question = question;
            q.answer = answer;
            q.type = type;
            q.options = options;
            list.add(q);
        }
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 出题失败，请稍后重试");
        }
        return list;
    }

    /**
     * 批量给填空题判分，返回答对的题数。
     * 把"题目、参考答案、学生答案"一次性发给大模型，让它按语义等价判断（忽略大小写、空格、标点、语气），返回 [{id, correct}]。
     * 若大模型调用或解析失败，退回本地比较：规范化后（见 normalize）与参考答案完全一致才算对。
     */
    private int gradeFillBlanks(List<Store.Question> fillQuestions, Map<Long, String> studentAnswers) {
        if (fillQuestions.isEmpty()) return 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (Store.Question q : fillQuestions) {
            Map<String, Object> it = new HashMap<>();
            it.put("id", q.questionId);
            it.put("question", q.question);
            it.put("reference", q.answer);
            it.put("student", studentAnswers.getOrDefault(q.questionId, "").trim());
            items.add(it);
        }
        try {
            String json = deepSeek.chat(List.of(
                    m("system", "你是判卷老师。判断学生答案与参考答案语义是否等价（忽略大小写、空格、标点和语气差异，意思对即算对）。只输出 JSON 数组：[{\"id\":1,\"correct\":true}]，不要其他文字。"),
                    m("user", mapper.writeValueAsString(items))
            ), 1000);
            JsonNode arr = mapper.readTree(extractJson(json));
            int correct = 0;
            for (JsonNode n : arr) {
                if (n.path("correct").asBoolean(false)) correct++;
            }
            return correct;
        } catch (Exception e) {
            // AI 判卷失败的降级方案：本地规范化字符串比较
            int correct = 0;
            for (Store.Question q : fillQuestions) {
                String student = studentAnswers.getOrDefault(q.questionId, "").trim();
                if (!student.isEmpty() && normalize(student).equals(normalize(q.answer))) correct++;
            }
            return correct;
        }
    }

    /** 规范化字符串用于本地比较：转小写，并只保留英文字母、数字和汉字（去掉空格与标点）。 */
    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", "");
    }

    /**
     * 判断选择题是否答对。兼容三种写法：
     * 1) 学生答案与标准答案文字完全一致；
     * 2) 标准答案是字母（A/B/C/D），对应选项文字与学生答案一致；
     * 3) 学生答案是字母，对应选项文字与标准答案一致。
     * 学生未作答则直接判错。
     */
    private static boolean isCorrectChoice(Store.Question q, String student) {
        if (q.answer == null || student == null || student.isBlank()) return false;
        String answer = q.answer.trim();
        String stu = student.trim();
        if (answer.equals(stu)) return true;
        int ai = letterIndex(answer);
        if (ai >= 0 && ai < q.options.size()) {
            return q.options.get(ai).equals(stu);
        }
        int si = letterIndex(stu);
        if (si >= 0 && si < q.options.size()) {
            return q.options.get(si).equals(answer);
        }
        return false;
    }

    /** 把单个字母（A→0、B→1……，不区分大小写）转成选项下标；不是单个字母时返回 -1。 */
    private static int letterIndex(String s) {
        if (s == null) return -1;
        String t = s.trim().toUpperCase();
        if (t.length() == 1) {
            char c = t.charAt(0);
            if (c >= 'A' && c <= 'Z') return c - 'A';
        }
        return -1;
    }

    /** 从大模型的回复中截取第一个 "[" 到最后一个 "]" 之间的 JSON 数组文本；找不到则原样返回。 */
    private static String extractJson(String s) {
        int i = s.indexOf('[');
        int j = s.lastIndexOf(']');
        if (i >= 0 && j > i) return s.substring(i, j + 1);
        return s;
    }

}
