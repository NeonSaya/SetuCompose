package com.example.setucompose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.setucompose.ui.screens.AboutScreen // 记得导入这个
import com.example.setucompose.ui.screens.ConfigScreen
import com.example.setucompose.ui.screens.DetailScreen
import com.example.setucompose.ui.screens.ResultScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: SetuViewModel = viewModel()

                    NavHost(navController = navController, startDestination = "config") {

                        // 1. 配置页
                        composable("config") {
                            ConfigScreen(navController, viewModel)
                        }

                        // 2. 结果页
                        composable("results") {
                            ResultScreen(navController, viewModel)
                        }

                        // 3. 详情页
                        composable(
                            route = "detail/{index}",
                            arguments = listOf(navArgument("index") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val index = backStackEntry.arguments?.getInt("index") ?: 0
                            DetailScreen(navController, viewModel, index)
                        }

                        // 4. 【新增】关于页
                        composable("about") {
                            AboutScreen(navController)
                        }
                    }
                }
            }
        }
    }
}