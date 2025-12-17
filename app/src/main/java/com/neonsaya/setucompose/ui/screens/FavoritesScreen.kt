package com.neonsaya.setucompose.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.neonsaya.setucompose.data.Favorite
import com.neonsaya.setucompose.ui.FavoriteViewModel
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(navController: NavController) {
    val favoriteViewModel: FavoriteViewModel = viewModel()
    val favorites by favoriteViewModel.allFavorites.collectAsState()
    val context = LocalContext.current

    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedPids by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    fun exitMultiSelectMode() {
        isMultiSelectMode = false
        selectedPids = emptySet()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            favoriteViewModel.saveFavoritesToGallery(selectedPids.toList())
            exitMultiSelectMode()
        } else {
            Toast.makeText(context, "需要权限才能保存图片", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveSelectedFavorites() {
        if (selectedPids.isEmpty()) {
            Toast.makeText(context, "请先选择要保存的图片", Toast.LENGTH_SHORT).show()
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                favoriteViewModel.saveFavoritesToGallery(selectedPids.toList())
                exitMultiSelectMode()
            } else {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            favoriteViewModel.saveFavoritesToGallery(selectedPids.toList())
            exitMultiSelectMode()
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要取消收藏这 ${selectedPids.size} 张图片吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        favoriteViewModel.removeFavorites(selectedPids.toList())
                        Toast.makeText(context, "已取消收藏 ${selectedPids.size} 张图片", Toast.LENGTH_SHORT).show()
                        showDeleteConfirmDialog = false
                        exitMultiSelectMode()
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (isMultiSelectMode) {
                TopAppBar(
                    title = { Text("已选择 ${selectedPids.size} 项") },
                    navigationIcon = {
                        IconButton(onClick = { exitMultiSelectMode() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (selectedPids.size == favorites.size) selectedPids = emptySet()
                            else selectedPids = favorites.map { it.pid }.toSet()
                        }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                        IconButton(onClick = { saveSelectedFavorites() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                        IconButton(onClick = { 
                            if (selectedPids.isNotEmpty()) {
                                showDeleteConfirmDialog = true
                            } else {
                                Toast.makeText(context, "请先选择要删除的图片", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("我的收藏") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("还没有收藏哦，快去添加吧！", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                items(favorites, key = { it.pid }) { favorite ->
                    FavoriteItem(favorite, selectedPids.contains(favorite.pid), {
                        if (isMultiSelectMode) {
                            selectedPids = if (selectedPids.contains(favorite.pid)) selectedPids - favorite.pid else selectedPids + favorite.pid
                        } else {
                            val itemJson = Gson().toJson(favorite)
                            val encodedJson = URLEncoder.encode(itemJson, StandardCharsets.UTF_8.name())
                            navController.navigate("detail/$encodedJson")
                        }
                    }, {
                        if (!isMultiSelectMode) isMultiSelectMode = true
                        selectedPids = selectedPids + favorite.pid
                    })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteItem(favorite: Favorite, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box {
            AsyncImage(
                model = favorite.localPath?.let { File(it) } ?: favorite.urls["thumb"] ?: favorite.urls["small"] ?: favorite.urls["regular"],
                contentDescription = favorite.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(48.dp)
                    )
                }
            }
        }
    }
}
