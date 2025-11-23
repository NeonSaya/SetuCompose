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
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning // 用于显示错误
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
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage // 【必须引入】
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
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    if (item == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("图片未找到")
        }
        return
    }

    val originalUrl = item.urls["original"] ?: ""
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // --- 权限与分享逻辑 (保持不变) ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch { ImageSaver.saveImageToGallery(context, originalUrl, item.title) }
        } else {
            Toast.makeText(context, "需要权限保存图片", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
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
                withContext(Dispatchers.Main) { Toast.makeText(context, "获取图片中...", Toast.LENGTH_SHORT).show() }
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context).data(originalUrl).allowHardware(false).build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = result?.toBitmap()

                if (bitmap != null) {
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs()
                    val file = File(cachePath, "share_image.jpg")
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.close()

                    val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
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

    val dateString = remember(item.uploadDate) {
        DateFormat.format("yy-MM-dd HH:mm", Date(item.uploadDate)).toString()
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 85.dp,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            Column(
                modifier = Modifier
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
                    if (item.aiType == 2) {
                        Text("⚠️ AI 生成", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tags", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.tags.joinToString(" / "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 18.sp)
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
            // 【修改点】使用 SubcomposeAsyncImage 替换 AsyncImage
            // 它可以让我们自定义加载中和加载失败的 UI
            SubcomposeAsyncImage(
                model = originalUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
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
                // 【新增】加载中显示的组件
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "原图加载中...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                // 【新增】加载失败显示的组件
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "图片加载失败",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
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
                FilledTonalIconButton(
                    onClick = { navController.popBackStack() },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                FilledTonalIconButton(
                    onClick = { shareImageFile() },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
            }
        }
    }
}

// 辅助组件保持不变
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