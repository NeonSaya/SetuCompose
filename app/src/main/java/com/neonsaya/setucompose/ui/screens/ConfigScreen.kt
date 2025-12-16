package com.neonsaya.setucompose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
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
import com.neonsaya.setucompose.ui.SetuViewModel
import com.neonsaya.setucompose.ui.SettingsViewModel
import com.neonsaya.setucompose.ui.ThemeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    navController: NavController,
    setuViewModel: SetuViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("随机涩图", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                actions = {
                    IconButton(onClick = { navController.navigate("favorites") }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Favorites")
                    }
                    IconButton(onClick = { navController.navigate("about") }) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val isTablet = this.maxWidth >= 600.dp
            val isLargeScreen = this.maxWidth >= 840.dp

            if (isTablet) {
                TabletConfigLayout(navController, setuViewModel, settingsViewModel, isLargeScreen)
            } else {
                PhoneConfigLayout(navController, setuViewModel, settingsViewModel)
            }
        }
    }
}

@Composable
private fun PhoneConfigLayout(
    navController: NavController,
    setuViewModel: SetuViewModel,
    settingsViewModel: SettingsViewModel
) {
    val verticalSpacing = 16.dp
    val horizontalPadding = 16.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        ThemeChooser(settingsViewModel, isSmallScreen = true)
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        KeywordInput(navController, setuViewModel, isSmallScreen = true)
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        QuantitySlider(setuViewModel)
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        Options(setuViewModel, isSmallScreen = true)
        Spacer(modifier = Modifier.height(verticalSpacing))
        SearchButton(navController, setuViewModel, isSmallScreen = true, isTablet = false)
    }
}

@Composable
private fun TabletConfigLayout(
    navController: NavController,
    setuViewModel: SetuViewModel,
    settingsViewModel: SettingsViewModel,
    isLargeScreen: Boolean
) {
    val itemsSpacing = if (isLargeScreen) 32.dp else 24.dp

    // A scrollable column that centers its content horizontally.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Center the content panel
    ) {
        // Add a bit of space from the top.
        Spacer(modifier = Modifier.height(if (isLargeScreen) 48.dp else 24.dp))

        // The content panel with a constrained width.
        Column(
            modifier = Modifier
                .widthIn(max = 700.dp) // Constrain the width to avoid being too stretched
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(itemsSpacing)
        ) {
            // Section 1: Keyword and Quantity take full panel width
            KeywordInput(navController, setuViewModel, isSmallScreen = false, isLargeScreen = isLargeScreen)
            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            QuantitySlider(setuViewModel, isLargeScreen = isLargeScreen)
            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Section 2: Settings placed side-by-side in a Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(itemsSpacing),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ThemeChooser(settingsViewModel, isSmallScreen = false, isLargeScreen = isLargeScreen)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Options(setuViewModel, isSmallScreen = false, isLargeScreen = isLargeScreen)
                }
            }
            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(itemsSpacing))

            // Section 3: Search Button at the bottom of the panel
            SearchButton(navController, setuViewModel, isSmallScreen = false, isTablet = true, isLargeScreen = isLargeScreen)
        }
    }
}

@Composable
private fun ThemeChooser(settingsViewModel: SettingsViewModel, isSmallScreen: Boolean, isLargeScreen: Boolean = false) {
    val themeState by settingsViewModel.themeState.collectAsState()
    val themeOptions = listOf(
        ThemeSetting.SYSTEM to "跟随系统",
        ThemeSetting.LIGHT to "浅色",
        ThemeSetting.DARK to "深色"
    )

    Column {
        Text("显示模式", style = if (isLargeScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = if (isLargeScreen) 12.dp else 8.dp))
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
                    text = { Text(label, style = if (isSmallScreen) MaterialTheme.typography.labelMedium else if (isLargeScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KeywordInput(navController: NavController, setuViewModel: SetuViewModel, isSmallScreen: Boolean, isLargeScreen: Boolean = false) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column {
        Text("关键词 / Tags", style = if (isLargeScreen) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = if (isLargeScreen) 12.dp else 8.dp))
        OutlinedTextField(
            value = setuViewModel.tagsInput,
            onValueChange = { setuViewModel.tagsInput = it },
            placeholder = { Text("例: 白丝|黑丝\n(留空则随机推荐)") },
            modifier = Modifier.fillMaxWidth().height(if (isSmallScreen) 120.dp else if (isLargeScreen) 180.dp else 140.dp),
            textStyle = if (isLargeScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
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
}

@Composable
private fun QuantitySlider(setuViewModel: SetuViewModel, isLargeScreen: Boolean = false) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("数量", style = if (isLargeScreen) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium)
            Text(
                "${setuViewModel.numToFetch} 张",
                style = if (isLargeScreen) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineSmall,
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
            Text("⚠️ 一次不要弄太多，太多会导致滥用", color = Color(0xFFF57F17), style = if (isLargeScreen) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Options(setuViewModel: SetuViewModel, isSmallScreen: Boolean, isLargeScreen: Boolean = false) {
    var r18Expanded by remember { mutableStateOf(false) }
    val r18Options = listOf(0 to "非 R18", 1 to "R18", 2 to "混合")
    val currentR18Text = r18Options.find { it.first == setuViewModel.r18Mode }?.second ?: "非 R18"

    Column(verticalArrangement = Arrangement.spacedBy(if (isLargeScreen) 12.dp else 8.dp)) {
        Text("R18 模式", style = if (isLargeScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        ExposedDropdownMenuBox(expanded = r18Expanded, onExpandedChange = { r18Expanded = !r18Expanded }) {
            OutlinedTextField(
                value = currentR18Text, onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = r18Expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                textStyle = if (isLargeScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                singleLine = true
            )
            ExposedDropdownMenu(expanded = r18Expanded, onDismissRequest = { r18Expanded = false }) {
                r18Options.forEach { (value, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { setuViewModel.r18Mode = value; r18Expanded = false })
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("排除 AI", style = if (isLargeScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Switch(
                checked = setuViewModel.excludeAI,
                onCheckedChange = { setuViewModel.excludeAI = it },
                modifier = Modifier.scale(if (isSmallScreen) 0.8f else if (isLargeScreen) 1.0f else 0.9f)
            )
        }
    }
}

@Composable
private fun SearchButton(navController: NavController, setuViewModel: SetuViewModel, isSmallScreen: Boolean, isTablet: Boolean, isLargeScreen: Boolean = false) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Button(
        onClick = {
            keyboardController?.hide()
            setuViewModel.fetchSetu()
            navController.navigate("results")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallScreen && !isTablet) 56.dp else if (isLargeScreen) 80.dp else 64.dp),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp)
    ) {
        Text("开始涩涩", style = if (isSmallScreen && !isTablet) MaterialTheme.typography.titleMedium else if (isLargeScreen) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))