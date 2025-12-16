package com.neonsaya.setucompose.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.Coil
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.neonsaya.setucompose.api.SetuData
import com.neonsaya.setucompose.ui.FavoriteViewModel
import com.neonsaya.setucompose.util.ImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, setuData: SetuData) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )

    val favoriteViewModel: FavoriteViewModel = viewModel()
    val isFavorite by favoriteViewModel.isFavorite(setuData.pid).collectAsState()

    val originalUrl = setuData.urls["original"] ?: ""
    val thumbnailUrl = setuData.urls["thumb"] ?: setuData.urls["small"] ?: setuData.urls["regular"]

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var retryTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, setuData.title) }
        else Toast.makeText(context, "需要权限保存", Toast.LENGTH_SHORT).show()
    }

    fun saveImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, setuData.title) }
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, setuData.title) }
        }
    }

    fun shareImageFile() {
        scope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { Toast.makeText(context, "处理中...", Toast.LENGTH_SHORT).show() }
                val imageLoader = Coil.imageLoader(context)
                val request = ImageRequest.Builder(context).data(originalUrl).allowHardware(false).build()
                val result = (imageLoader.execute(request) as? SuccessResult)?.drawable
                val bitmap = result?.toBitmap()

                if (bitmap != null) {
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs()
                    val ext = if (setuData.ext.lowercase().contains("png")) "png" else "jpg"
                    val format = if (ext == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    val file = File(cachePath, "share_${setuData.pid}.$ext")

                    if (!file.exists()) {
                        val stream = FileOutputStream(file)
                        bitmap.compress(format, 100, stream)
                        stream.close()
                    }

                    val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/$ext"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_TEXT, "Title: ${setuData.title}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    withContext(Dispatchers.Main) { context.startActivity(Intent.createChooser(shareIntent, "分享")) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "失败: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    val dateString = remember(setuData.uploadDate) { DateFormat.format("yy-MM-dd HH:mm", Date(setuData.uploadDate)).toString() }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 85.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(setuData.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(setuData.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                        }
                        FilledTonalIconButton(
                            onClick = {
                                if (isFavorite) {
                                    favoriteViewModel.removeFavorite(setuData.pid)
                                } else {
                                    favoriteViewModel.addFavorite(setuData)
                                }
                            },
                            modifier = Modifier.size(40.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = if (isFavorite) Color.Red.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalIconButton(onClick = { saveImage() }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CompactInfoRow("UID", setuData.uid.toString(), "PID", setuData.pid.toString())
                        CompactInfoRow("Size", "${setuData.width}x${setuData.height}", "Time", dateString)
                        if (setuData.aiType == 2) Text("⚠️ AI 生成", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tags", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(setuData.tags.joinToString(" / "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
                .clipToBounds()
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(originalUrl)
                    .setParameter("retry_hash", retryTrigger)
                    .crossfade(true)
                    .placeholderMemoryCacheKey(thumbnailUrl)
                    .allowRgb565(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            scale = if (scale > 1f) 1f else 2f; offset = Offset.Zero
                        })
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale == 1f) {
                                val yDelta = pan.y
                                scope.launch {
                                    if (yDelta < -10) scaffoldState.bottomSheetState.expand()
                                    else if (yDelta > 10) scaffoldState.bottomSheetState.partialExpand()
                                }
                                offset = Offset.Zero
                            } else {
                                val newX = offset.x + pan.x
                                val newY = offset.y + pan.y
                                offset = Offset(newX, newY)
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(56.dp))
                            Text("图片加载失败", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { retryTrigger = System.currentTimeMillis() }) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("点击重试")
                            }
                        }
                    }
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalIconButton(onClick = { navController.popBackStack() }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                FilledTonalIconButton(onClick = { shareImageFile() }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}

@Composable
fun CompactInfoRow(l1: String, v1: String, l2: String, v2: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(modifier = Modifier.weight(1f)) {
            Text(text = "$l1: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = v1, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
        Row(modifier = Modifier.weight(1f)) {
            Text(text = "$l2: ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = v2, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
    }
}
