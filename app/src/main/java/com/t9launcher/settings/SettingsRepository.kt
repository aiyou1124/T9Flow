package com.t9launcher.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object DarkMode {
    const val SYSTEM = "system"
    const val LIGHT = "light"
    const val DARK = "dark"
}

data class Settings(
    val recentCount: Int = 8,
    val darkMode: String = DarkMode.SYSTEM,
    val onboardingShown: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private val appContext = context.applicationContext

    val settings: Flow<Settings> = appContext.dataStore.data.map { prefs ->
        Settings(
            recentCount = prefs[Keys.RECENT_COUNT] ?: 8,
            darkMode = prefs[Keys.DARK_MODE] ?: DarkMode.SYSTEM,
            onboardingShown = prefs[Keys.ONBOARDING_SHOWN] ?: false
        )
    }

    suspend fun setRecentCount(value: Int) {
        appContext.dataStore.edit { it[Keys.RECENT_COUNT] = value }
    }

    suspend fun setDarkMode(value: String) {
        appContext.dataStore.edit { it[Keys.DARK_MODE] = value }
    }

    suspend fun setOnboardingShown(value: Boolean) {
        appContext.dataStore.edit { it[Keys.ONBOARDING_SHOWN] = value }
    }

    private object Keys {
        val RECENT_COUNT = intPreferencesKey("recent_count")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val ONBOARDING_SHOWN = booleanPreferencesKey("onboarding_shown")
    }
}
