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

    var r18Mode by mutableIntStateOf(0)
    var excludeAI by mutableStateOf(false)
    var tagsInput by mutableStateOf("")
    var numToFetch by mutableIntStateOf(1) // 默认1张

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var setuList by mutableStateOf<List<SetuData>>(emptyList())

    private var fetchJob: Job? = null

    fun fetchSetu() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                setuList = emptyList() // 清空旧数据防止错乱

                val cleanTags = tagsInput
                    .replace("，", "|")
                    .replace(",", "|")
                    .replace(" ", "|")
                    .trim()

                val tagList = if (cleanTags.isBlank()) emptyList() else listOf(cleanTags)

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
                errorMessage = e.message ?: "网络请求失败"
            } finally {
                isLoading = false
            }
        }
    }
}