package com.example.setucompose.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.example.setucompose.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current))
            val themeState by settingsViewModel.themeState.collectAsState()

            val useDarkTheme = when (themeState) {
                ThemeSetting.SYSTEM -> isSystemInDarkTheme()
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
            }

            AppTheme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val setuViewModel: SetuViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = "config",
                        enterTransition = { fadeIn(initialAlpha = 0.3f) },
                        exitTransition = { fadeOut(targetAlpha = 0.3f) }
                    ) {
                        composable("config") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ConfigScreen(
                                    navController = navController,
                                    setuViewModel = setuViewModel,
                                    settingsViewModel = settingsViewModel, // Pass settings ViewModel
                                    modifier = Modifier.widthIn(max = 600.dp)
                                )
                            }
                        }
                        composable("results") { ResultScreen(navController, setuViewModel) }
                        composable(
                            route = "detail/{index}",
                            arguments = listOf(navArgument("index") { type = NavType.IntType })
                        ) { entry ->
                            DetailScreen(navController, setuViewModel, entry.arguments?.getInt("index") ?: 0)
                        }
                        composable("about") { AboutScreen(navController) }
                    }
                }
            }
        }
    }
}

@Composable
fun Surface(modifier: Modifier, content: @Composable () -> Unit) {
    // This is a simplified Surface. In a real app, you might want more customization.
    Box(modifier = modifier) {
        content()
    }
}
