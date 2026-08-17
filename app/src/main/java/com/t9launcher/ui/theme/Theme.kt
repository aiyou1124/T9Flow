package com.t9launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.t9launcher.settings.DarkMode

/**
 * 应用主题：跟随系统 / 强制浅色 / 强制深色（由设置控制）。
 */
@Composable
fun T9Theme(
    darkMode: String = DarkMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (darkMode) {
        DarkMode.DARK -> true
        DarkMode.LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme, content = content)
}
