package com.example.setucompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.setucompose.ui.SetuViewModel
import com.example.setucompose.ui.SettingsViewModel
import com.example.setucompose.ui.ThemeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    navController: NavController,
    setuViewModel: SetuViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var r18Expanded by remember { mutableStateOf(false) }
    val r18Options = listOf(0 to "非 R18", 1 to "R18", 2 to "混合")
    val currentR18Text = r18Options.find { it.first == setuViewModel.r18Mode }?.second ?: "非 R18"

    // Observe theme state from SettingsViewModel
    val themeState by settingsViewModel.themeState.collectAsState()
    val themeOptions = listOf(
        ThemeSetting.SYSTEM to "跟随系统",
        ThemeSetting.LIGHT to "浅色",
        ThemeSetting.DARK to "深色"
    )

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
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Theme Chooser (Reimplemented with TabRow)
            Column {
                Text("显示模式", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))

                val selectedTabIndex = themeOptions.indexOfFirst { it.first == themeState }

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(50))
                ) {
                    themeOptions.forEachIndexed { index, (theme, label) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { settingsViewModel.updateTheme(theme) },
                            text = { Text(label) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 2. 关键词输入
            Column {
                Text("关键词 / Tags", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = setuViewModel.tagsInput,
                    onValueChange = { setuViewModel.tagsInput = it },
                    placeholder = { Text("例: 白丝|黑丝\n(留空则随机推荐)") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    maxLines = 5,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        keyboardController?.hide()
                        setuViewModel.fetchSetu()
                        navController.navigate("results")
                    })
                )
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 3. 数量滑块
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("数量", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${setuViewModel.numToFetch} 张",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = setuViewModel.numToFetch.toFloat(),
                    onValueChange = { setuViewModel.numToFetch = it.toInt() },
                    valueRange = 1f..20f,
                    steps = 18,
                    modifier = Modifier.height(30.dp)
                )
                if (setuViewModel.numToFetch > 10) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("⚠️ 一次不要弄太多，太多会导致滥用", color = Color(0xFFF57F17), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 4. 选项设置
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
                                DropdownMenuItem(text = { Text(label) }, onClick = { setuViewModel.r18Mode = value; r18Expanded = false })
                            }
                        }
                    }
                }
                // AI
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 18.dp)) {
                    Text("排除 AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Switch(checked = setuViewModel.excludeAI, onCheckedChange = { setuViewModel.excludeAI = it }, modifier = Modifier.scale(0.9f))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 5. 搜索按钮
            Button(
                onClick = {
                    keyboardController?.hide()
                    setuViewModel.fetchSetu()
                    navController.navigate("results")
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = MaterialTheme.shapes.large,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
            ) {
                Text("开始涩涩", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))