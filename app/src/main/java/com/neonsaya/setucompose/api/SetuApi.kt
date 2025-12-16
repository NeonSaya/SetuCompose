package com.neonsaya.setucompose.api

import com.neonsaya.setucompose.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    // 根据构建类型（Debug/Release）动态创建 Client
    private val client: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)

            // 只在 Debug 模式下添加日志拦截器
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                builder.addInterceptor(logging)
            }

            return builder.build()
        }

    val api: SetuService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // 绑定动态创建的 Client
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SetuService::class.java)
    }
}