package com.example.setucompose.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val pid: Long,
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
    val urls: Map<String, String>,
    var localPath: String? // Path to the locally saved image file
)
