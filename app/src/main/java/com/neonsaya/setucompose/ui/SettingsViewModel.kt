package com.neonsaya.setucompose.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// 1. Define DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// 2. Define Theme options
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

class SettingsViewModel(context: Context) : ViewModel() {

    private val settingsDataStore = context.dataStore

    // 3. Define the key for storing theme preference
    private val themeKey = stringPreferencesKey("theme_setting")

    // 4. Expose the theme setting as a StateFlow
    val themeState = settingsDataStore.data
        .map { preferences ->
            // Default to SYSTEM if no setting is found
            ThemeSetting.valueOf(preferences[themeKey] ?: ThemeSetting.SYSTEM.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.SYSTEM
        )

    // 5. Function to update the theme setting
    fun updateTheme(theme: ThemeSetting) {
        viewModelScope.launch {
            settingsDataStore.edit {
                it[themeKey] = theme.name
            }
        }
    }
}
