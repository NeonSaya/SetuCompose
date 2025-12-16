package com.neonsaya.setucompose.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageSaver {

    suspend fun saveImageToGallery(context: Context, imageUrl: String, title: String) {
        withContext(Dispatchers.IO) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .build()

                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = result?.toBitmap()

                if (bitmap != null) {
                    saveBitmap(context, bitmap, title)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    throw Exception("下载图片失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, title: String) {
        val safeTitle = title.replace("[^a-zA-Z0-9\\u4E00-\\u9FA5]".toRegex(), "_")
        val filename = "SETU_${safeTitle}_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SetuApp")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            imageUri?.let { resolver.update(it, contentValues, null, null) }

        } else {
            val imagesDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SetuApp")

            if (!imagesDir.exists()) {
                val success = imagesDir.mkdirs()
                if (!success) {
                    throw Exception("无法创建文件夹，请检查存储权限")
                }
            }

            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }

            // 通知相册刷新
            MediaScannerConnection.scanFile(context, arrayOf(image.toString()), null, null)
        }
    }
}