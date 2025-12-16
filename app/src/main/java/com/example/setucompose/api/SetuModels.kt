package com.example.setucompose.api

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
data class SetuResponse(
    val error: String,
    val data: List<SetuData>
)

@Keep
@Parcelize
data class SetuData(
    val pid: Long,
    val p: Int,
    val uid: Int,
    val title: String,
    val author: String,
    val r18: Boolean,
    val width: Int,
    val height: Int,
    val tags: List<String>,
    val ext: String,
    val aiType: Int,
    val uploadDate: Long,
    val urls: Map<String, String>
) : Parcelable

@Keep
data class SetuRequest(
    val r18: Int = 0,
    val num: Int = 1,
    val excludeAI: Boolean = false,
    val tag: List<String> = emptyList(),
    val size: List<String> = listOf("original", "regular", "small", "thumb")
)
