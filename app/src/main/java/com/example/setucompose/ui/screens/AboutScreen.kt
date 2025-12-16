package com.example.setucompose.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于本应用") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "特别鸣谢",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text("感谢 Lolicon API 提供者！本应用仅供技术学习与交流使用，请勿用于非法用途。")

            HorizontalDivider()

            Text(
                text = "相关链接",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // API 链接
            ListItem(
                headlineContent = { Text("API 地址") },
                supportingContent = {
                    Text(
                        text = "https://api.lolicon.app/setu/v2",
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                },
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://api.lolicon.app/setu/v2")
                }
            )

            // 文档链接
            ListItem(
                headlineContent = { Text("说明文档") },
                supportingContent = {
                    Text(
                        text = "https://docs.api.lolicon.app/#/",
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                },
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://docs.api.lolicon.app/#/")
                }
            )
        }
    }
}