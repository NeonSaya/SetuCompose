package com.neonsaya.setucompose.ui

import android.os.Build
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
import androidx.compose.material3.Surface
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
import com.neonsaya.setucompose.api.SetuData
import com.neonsaya.setucompose.ui.screens.AboutScreen
import com.neonsaya.setucompose.ui.screens.ConfigScreen
import com.neonsaya.setucompose.ui.screens.DetailScreen
import com.neonsaya.setucompose.ui.screens.FavoritesScreen
import com.neonsaya.setucompose.ui.screens.ResultScreen
import com.google.gson.Gson
import com.neonsaya.setucompose.theme.AppTheme

// A custom NavType to handle passing complex data objects.
class SetuDataNavType : NavType<SetuData>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): SetuData? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, SetuData::class.java)
        } else {
            @Suppress("DEPRECATION")
            bundle.getParcelable(key)
        }
    }

    override fun parseValue(value: String): SetuData {
        return Gson().fromJson(value, SetuData::class.java)
    }

    override fun put(bundle: Bundle, key: String, value: SetuData) {
        bundle.putParcelable(key, value)
    }
}


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
                                    settingsViewModel = settingsViewModel,
                                    modifier = Modifier.widthIn(max = 600.dp)
                                )
                            }
                        }
                        composable("results") { ResultScreen(navController, setuViewModel) }

                        composable(
                            route = "detail/{setuData}",
                            arguments = listOf(navArgument("setuData") { type = SetuDataNavType() })
                        ) { backStackEntry ->
                            val setuData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                backStackEntry.arguments?.getParcelable("setuData", SetuData::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                backStackEntry.arguments?.getParcelable("setuData")
                            }
                            if (setuData != null) {
                                DetailScreen(navController, setuData)
                            }
                        }

                        composable("about") { AboutScreen(navController) }
                        composable("favorites") { FavoritesScreen(navController) }
                    }
                }
            }
        }
    }
}
