package com.example.setucompose.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// 定义 API 接口方法
interface SetuService {
    @POST("setu/v2")
    suspend fun getSetu(@Body request: SetuRequest): SetuResponse
}

// 单例模式创建 Retrofit 实例
object RetrofitInstance {
    private const val BASE_URL = "https://api.lolicon.app/"

    val api: SetuService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SetuService::class.java)
    }
}