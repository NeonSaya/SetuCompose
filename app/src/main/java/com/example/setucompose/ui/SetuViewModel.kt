package com.example.setucompose.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.setucompose.api.RetrofitInstance
import com.example.setucompose.api.SetuData
import com.example.setucompose.api.SetuRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SetuViewModel : ViewModel() {

    // Parameters
    var r18Mode by mutableIntStateOf(0) // 0:非 R18 (默认)
    var excludeAI by mutableStateOf(false)
    var tagsInput by mutableStateOf("")

    // 默认数量 1
    var numToFetch by mutableIntStateOf(1)

    // State
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var setuList by mutableStateOf<List<SetuData>>(emptyList())

    // 【修复点 1】定义一个 Job 用来追踪网络请求
    private var fetchJob: Job? = null

    // Fetch Data
    fun fetchSetu() {
        // 【修复点 2】如果上一次请求还没结束，直接取消掉，防止数据覆盖错乱
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            try {
                // 【修复点 3】在请求开始前，立即重置 UI 状态
                // 这样用户进入结果页时看到的是 Loading 而不是旧图片
                isLoading = true
                errorMessage = null
                setuList = emptyList()

                val tagList = if (tagsInput.isBlank()) {
                    emptyList()
                } else {
                    listOf(tagsInput)
                }

                val req = SetuRequest(
                    r18 = r18Mode,
                    num = numToFetch,
                    excludeAI = excludeAI,
                    tag = tagList
                    // size 默认包含 thumb, small, regular, original
                )

                val response = RetrofitInstance.api.getSetu(req)
                if (response.error.isNotEmpty()) {
                    errorMessage = response.error
                } else {
                    setuList = response.data
                }
            } catch (e: Exception) {
                // 如果是手动取消造成的异常，通常忽略，或者显示错误
                errorMessage = e.message ?: "未知错误"
            } finally {
                isLoading = false
            }
        }
    }
}