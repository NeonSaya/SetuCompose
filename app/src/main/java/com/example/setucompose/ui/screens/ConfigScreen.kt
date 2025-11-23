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
    var r18Expanded by remember { mutableStateOf(false) }
    val r18Options = listOf(0 to "非 R18", 1 to "R18", 2 to "混合")
    val currentR18Text = r18Options.find { it.first == viewModel.r18Mode }?.second ?: "非 R18"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("随机涩图", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                actions = {
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. 关键词输入 (Top)
            Column {
                Text("关键词 / Tags", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = viewModel.tagsInput,
                    onValueChange = { viewModel.tagsInput = it },
                    placeholder = { Text("例: 白丝|黑丝\n(留空则随机推荐)") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        viewModel.fetchSetu()
                        navController.navigate("results")
                    })
                )
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 2. 数量滑块 (Middle)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("数量", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${viewModel.numToFetch} 张",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = viewModel.numToFetch.toFloat(),
                    onValueChange = { viewModel.numToFetch = it.toInt() },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.height(30.dp)
                )
                if (viewModel.numToFetch > 10) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⚠️ 一次不要弄太多，太多会导致滥用", color = Color(0xFFF57F17), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 3. 选项设置 (Bottom)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // R18
                Column(modifier = Modifier.weight(1f)) {
                    Text("R18 模式", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))
                    ExposedDropdownMenuBox(expanded = r18Expanded, onExpandedChange = { r18Expanded = !r18Expanded }) {
                        OutlinedTextField(
                            value = currentR18Text, onValueChange = {}, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = r18Expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyMedium, singleLine = true
                        )
                        ExposedDropdownMenu(expanded = r18Expanded, onDismissRequest = { r18Expanded = false }) {
                            r18Options.forEach { (value, label) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.r18Mode = value; r18Expanded = false })
                            }
                        }
                    }
                }
                // AI
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 18.dp)) {
                    Text("排除 AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Switch(checked = viewModel.excludeAI, onCheckedChange = { viewModel.excludeAI = it }, modifier = Modifier.scale(0.9f))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 4. 搜索按钮
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.fetchSetu()
                    navController.navigate("results")
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
            ) {
                Text("开始涩涩", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            // 5. 版本号
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))