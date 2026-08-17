package com.t9launcher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t9launcher.settings.DarkMode
import com.t9launcher.settings.Settings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: Settings,
    onBack: () -> Unit,
    onRecentCountChange: (Int) -> Unit,
    onDarkModeChange: (String) -> Unit,
    onClearUsage: () -> Unit,
    onRescan: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 最近打开数量
            SettingLabel("最近打开数量", "首页显示最近打开的 App 数量（${settings.recentCount}）")
            var recentCount by remember(settings.recentCount) {
                mutableFloatStateOf(settings.recentCount.toFloat())
            }
            Slider(
                value = recentCount,
                onValueChange = { recentCount = it },
                onValueChangeFinished = { onRecentCountChange(recentCount.roundToInt()) },
                valueRange = 5f..10f,
                steps = 4
            )

            // 深色模式
            SettingLabel("深色模式", "")
            DarkModeOption("跟随系统", DarkMode.SYSTEM, settings.darkMode, onDarkModeChange)
            DarkModeOption("浅色", DarkMode.LIGHT, settings.darkMode, onDarkModeChange)
            DarkModeOption("深色", DarkMode.DARK, settings.darkMode, onDarkModeChange)

            // 清空最近打开记录
            OutlinedButton(
                onClick = onClearUsage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("清空最近打开记录")
            }

            // 重新扫描应用
            OutlinedButton(
                onClick = onRescan,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("重新扫描应用")
            }
        }
    }
}

@Composable
private fun SettingLabel(title: String, subtitle: String) {
    Column(Modifier.padding(top = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DarkModeOption(
    label: String,
    value: String,
    current: String,
    onChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = current == value, onClick = { onChange(value) })
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
