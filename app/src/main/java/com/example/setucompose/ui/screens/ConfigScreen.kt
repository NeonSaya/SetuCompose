package com.example.setucompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.setucompose.ui.SetuViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(navController: NavController, viewModel: SetuViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // R18 下拉菜单状态
    var r18Expanded by remember { mutableStateOf(false) }
    val r18Options = listOf(0 to "非 R18", 1 to "R18", 2 to "混合")
    val currentR18Text = r18Options.find { it.first == viewModel.r18Mode }?.second ?: "非 R18"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setu API 配置", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                actions = {
                    // 【修改】点击直接跳转到 About 页面
                    IconButton(onClick = { navController.navigate("about") }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. R18 模式
            Column {
                Text("R18 模式", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = r18Expanded,
                    onExpandedChange = { r18Expanded = !r18Expanded }
                ) {
                    OutlinedTextField(
                        value = currentR18Text,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = r18Expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    ExposedDropdownMenu(
                        expanded = r18Expanded,
                        onDismissRequest = { r18Expanded = false }
                    ) {
                        r18Options.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.r18Mode = value
                                    r18Expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 2. 排除 AI
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("排除 AI 作品", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = viewModel.excludeAI,
                    onCheckedChange = { viewModel.excludeAI = it },
                    modifier = Modifier.scale(0.8f)
                )
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 3. 数量
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("数量", style = MaterialTheme.typography.bodyMedium)
                    Text("${viewModel.numToFetch} 张", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = viewModel.numToFetch.toFloat(),
                    onValueChange = { viewModel.numToFetch = it.toInt() },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.height(24.dp)
                )

                // 【修改】警告颜色改为黄色 (Dark Yellow/Orange 以保证可读性)
                if (viewModel.numToFetch > 10) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ 一次不要弄太多，太多会导致滥用",
                        // 使用深黄色，纯 Yellow 在白色背景看不清
                        color = Color(0xFFF57F17),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 4. 关键词
            OutlinedTextField(
                value = viewModel.tagsInput,
                onValueChange = { viewModel.tagsInput = it },
                label = { Text("关键词 / Tags") },
                placeholder = { Text("例: 白丝|黑丝") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        viewModel.fetchSetu()
                        navController.navigate("results")
                    }
                )
            )

            Spacer(modifier = Modifier.weight(3f))

            // 5. 按钮
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.fetchSetu()
                    navController.navigate("results")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("开始涩涩")
            }
        }
    }
}


fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)