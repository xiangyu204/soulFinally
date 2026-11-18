package com.example.soul_android.data.remote

import com.example.soul_android.BuildConfig
import com.example.soul_android.data.api.QuizApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    /*
     * Android Emulator 访问电脑 localhost：
     *
     * 电脑 Spring Boot:
     * http://localhost:8080
     *
     * Android Emulator:
     * http://10.0.2.2:8080
     *
     * 注意最后必须有 /
     */
    private val BASE_URL =
        BuildConfig.API_BASE_URL


    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {

            // Logcat 可以看到完整请求和返回
            level =
                if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
        }


    private val okHttpClient =
        OkHttpClient.Builder()

            .addInterceptor(
                loggingInterceptor
            )

            // AI 生成题目可能比较慢
            .connectTimeout(
                30,
                TimeUnit.SECONDS
            )

            .readTimeout(
                60,
                TimeUnit.SECONDS
            )

            .writeTimeout(
                30,
                TimeUnit.SECONDS
            )

            .build()


    private val retrofit =
        Retrofit.Builder()

            .baseUrl(
                BASE_URL
            )

            .client(
                okHttpClient
            )

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()


    val quizApi: QuizApiService by lazy {

        retrofit.create(
            QuizApiService::class.java
        )
    }
}