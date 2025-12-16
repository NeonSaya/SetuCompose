package com.neonsaya.setucompose.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.neonsaya.setucompose.api.SetuData
import com.neonsaya.setucompose.data.AppDatabase
import com.neonsaya.setucompose.data.Favorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
    private val context = getApplication<Application>().applicationContext

    val allFavorites = favoriteDao.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun isFavorite(pid: Long) = favoriteDao.isFavorite(pid)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun addFavorite(setuData: SetuData) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageUrl = setuData.urls["regular"] ?: setuData.urls["original"] ?: return@launch

            // 1. Download image using Coil
            val imageLoader = Coil.imageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Required for processing the bitmap
                .build()

            val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
            val bitmap = result?.toBitmap()

            var localPath: String? = null
            if (bitmap != null) {
                // 2. Save bitmap to internal storage
                val favoritesDir = File(context.filesDir, "favorites")
                if (!favoritesDir.exists()) {
                    favoritesDir.mkdirs()
                }

                val file = File(favoritesDir, "${setuData.pid}.jpg")
                val stream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.flush()
                stream.close()
                localPath = file.absolutePath
            }

            // 3. Save full data to database
            val favorite = Favorite(
                pid = setuData.pid,
                p = setuData.p,
                uid = setuData.uid,
                title = setuData.title,
                author = setuData.author,
                r18 = setuData.r18,
                width = setuData.width,
                height = setuData.height,
                tags = setuData.tags,
                ext = setuData.ext,
                aiType = setuData.aiType,
                uploadDate = setuData.uploadDate,
                urls = setuData.urls,
                localPath = localPath
            )
            favoriteDao.insert(favorite)
        }
    }

    fun removeFavorite(pid: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val favoriteToDelete = allFavorites.first().find { it.pid == pid }

            favoriteToDelete?.localPath?.let {
                val file = File(it)
                if (file.exists()) {
                    file.delete()
                }
            }

            val favorite = Favorite(pid, 0, 0, "", "", false, 0, 0, emptyList(), "", 0, 0, emptyMap(), null)
            favoriteDao.delete(favorite)
        }
    }
}
