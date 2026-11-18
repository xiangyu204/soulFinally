package com.example.soul_android.data.api

import com.example.soul_android.data.remote.QuizStartRequest
import com.example.soul_android.data.remote.QuizStartResponse
import com.example.soul_android.data.remote.QuizSubmitRequest
import com.example.soul_android.data.remote.QuizSubmitResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * QuizApiService 是用于处理 AI 技能问答与测试相关的 Retrofit API 服务接口。
 * 负责与后端 Spring Boot 服务进行交互，提供开始测试、提交答案并获取评分等网络请求。
 */
interface QuizApiService {

    /**
     * 启动 AI 技能问答测试。
     * 对应后端端点：POST /api/quiz/start
     *
     * @param request 包含用户名称、测试技能、测验类型及目标语言的请求体 [QuizStartRequest]。
     * @return 返回包含测验ID、技能名称及题目列表的响应对象 [QuizStartResponse]。
     */
    @POST("api/quiz/start")
    suspend fun startQuiz(
        @Body request: QuizStartRequest
    ): QuizStartResponse

    /**
     * 提交 AI 技能问答答案并获取评测结果。
     * 对应后端端点：POST /api/quiz/submit
     *
     * @param request 包含测验ID、用户名称、技能、测验类型及用户答题详情列表的请求体 [QuizSubmitRequest]。
     * @return 返回包含得分、总分、等级及等级描述的评分响应对象 [QuizSubmitResponse]。
     */
    @POST("api/quiz/submit")
    suspend fun submitQuiz(
        @Body request: QuizSubmitRequest
    ): QuizSubmitResponse
}
