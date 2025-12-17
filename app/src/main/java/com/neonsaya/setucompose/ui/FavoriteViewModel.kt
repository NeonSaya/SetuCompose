package com.neonsaya.setucompose.ui

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.neonsaya.setucompose.api.SetuData
import com.neonsaya.setucompose.data.AppDatabase
import com.neonsaya.setucompose.data.Favorite
import com.neonsaya.setucompose.util.ImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
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
            // 1. Immediately insert to update UI
            val initialFavorite = Favorite(
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
                localPath = null
            )
            favoriteDao.insert(initialFavorite)

            // 2. Download original image in the background
            val imageUrl = setuData.urls["original"] ?: setuData.urls["regular"] ?: return@launch
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Hardware bitmaps are tricky to manipulate.
                .build()

            val result = (Coil.imageLoader(context).execute(request) as? SuccessResult)?.drawable
            val originalBitmap = result?.toBitmap()

            if (originalBitmap != null) {
                // 3. Create and save a thumbnail
                val thumbnail = createThumbnail(originalBitmap, 400) // 400px width

                val favoritesDir = File(context.filesDir, "favorites")
                if (!favoritesDir.exists()) {
                    favoritesDir.mkdirs()
                }

                val file = File(favoritesDir, "${setuData.pid}.jpg")
                FileOutputStream(file).use {
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 85, it) // Compress with 85% quality
                }

                // 4. Recycle bitmaps as they are no longer needed
                originalBitmap.recycle()
                thumbnail.recycle()

                // 5. Update the database entry with the local path.
                val updatedFavorite = initialFavorite.copy(localPath = file.absolutePath)
                favoriteDao.insert(updatedFavorite)
            }
        }
    }

    fun removeFavorite(pid: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val favoriteToDelete = favoriteDao.getFavoriteByPid(pid)
            favoriteToDelete?.localPath?.let {
                val file = File(it)
                if (file.exists()) {
                    file.delete()
                }
            }
            favoriteDao.deleteByPid(pid)
        }
    }

    fun removeFavorites(pids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val favoritesToDelete = favoriteDao.getFavoritesByPids(pids)
            favoritesToDelete.forEach { favorite ->
                favorite.localPath?.let {
                    val file = File(it)
                    if (file.exists()) {
                        file.delete()
                    }
                }
            }
            favoriteDao.deleteByPids(pids)
        }
    }
    
    fun saveFavoritesToGallery(pids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val favoritesToSave = favoriteDao.getFavoritesByPids(pids)
            var successCount = 0
            for (favorite in favoritesToSave) {
                val imageUrl = favorite.urls["original"] ?: favorite.urls["regular"]
                if (imageUrl != null) {
                    ImageSaver.saveImageToGallery(context, imageUrl, favorite.title)
                    successCount++
                }
            }
            launch(Dispatchers.Main) {
                Toast.makeText(context, "成功保存 $successCount 张图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createThumbnail(bitmap: Bitmap, targetWidth: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        if (originalWidth <= targetWidth) {
            return bitmap
        }
        val aspectRatio = originalHeight.toFloat() / originalWidth.toFloat()
        val targetHeight = (targetWidth * aspectRatio).toInt()
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
}
