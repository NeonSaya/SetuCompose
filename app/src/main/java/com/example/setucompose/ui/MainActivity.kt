package com.example.setucompose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.setucompose.ui.screens.AboutScreen
import com.example.setucompose.ui.screens.ConfigScreen
import com.example.setucompose.ui.screens.DetailScreen
import com.example.setucompose.ui.screens.ResultScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 开启全屏沉浸
        enableEdgeToEdge()
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
                        composable("config") { ConfigScreen(navController, viewModel) }
                        composable("results") { ResultScreen(navController, viewModel) }
                        composable(
                            route = "detail/{index}",
                            arguments = listOf(navArgument("index") { type = NavType.IntType })
                        ) { entry ->
                            DetailScreen(navController, viewModel, entry.arguments?.getInt("index") ?: 0)
                        }
                        composable("about") { AboutScreen(navController) }
                    }
                }
            }
        }
    }
}