package com.example.setucompose.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface SetuService {
    @POST("setu/v2")
    suspend fun getSetu(@Body request: SetuRequest): SetuResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://api.lolicon.app/"

    // 【优化】配置超时时间
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: SetuService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // 绑定 Client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SetuService::class.java)
    }
}