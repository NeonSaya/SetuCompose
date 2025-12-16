package com.example.setucompose.ui.screens

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.navigation.NavController
import coil.Coil
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.setucompose.ui.SetuViewModel
import com.example.setucompose.util.ImageSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, viewModel: SetuViewModel, index: Int) {
    val item = viewModel.setuList.getOrNull(index)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("图片未找到") }
        return
    }

    val originalUrl = item.urls["original"] ?: ""
    val thumbnailUrl = item.urls["thumb"] ?: item.urls["small"] ?: item.urls["regular"]
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var retryTrigger by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val permissionLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, item.title) }
        else Toast.makeText(context, "需要权限保存", Toast.LENGTH_SHORT).show()
    }

    fun saveImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, item.title) }
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, item.title) }
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
                    val ext = if (item.ext.lowercase().contains("png")) "png" else "jpg"
                    val format = if (ext == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    val file = File(cachePath, "share_${item.pid}.$ext")

                    if (!file.exists()) {
                        val stream = FileOutputStream(file)
                        bitmap.compress(format, 100, stream)
                        stream.close()
                    }

                    val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/$ext"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_TEXT, "Title: ${item.title}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    withContext(Dispatchers.Main) { context.startActivity(Intent.createChooser(shareIntent, "分享")) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "失败: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    val dateString = remember(item.uploadDate) { DateFormat.format("yy-MM-dd HH:mm", Date(item.uploadDate)).toString() }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 85.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            // Center and constrain the width of the bottom sheet content for tablets
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 700.dp) // Constrain max width for better readability
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(item.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                        }
                        FilledTonalIconButton(onClick = { saveImage() }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        CompactInfoRow("UID", item.uid.toString(), "PID", item.pid.toString())
                        CompactInfoRow("Size", "${item.width}x${item.height}", "Time", dateString)
                        if (item.aiType == 2) Text("⚠️ AI 生成", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Tags", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.tags.joinToString(" / "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
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
                    // --- Optimizations --- //
                    .placeholderMemoryCacheKey(thumbnailUrl) // Instantly show the thumbnail from cache
                    .allowRgb565(true) // Use 50% less memory for decoding
                    // --------------------- //
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                scale = if (scale > 1f) 1f else 2f
                                offset = Offset.Zero
                            }
                        )
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
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("原图加载中...", color = Color.White)
                        }
                    }
                },
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
            // 顶部导航
            Row(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp).statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalIconButton(onClick = { navController.popBackStack() }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
