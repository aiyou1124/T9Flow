package com.t9launcher.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.t9launcher.data.AppEntity
import com.t9launcher.settings.Settings

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val settingsState by viewModel.settings.collectAsStateWithLifecycle()
    val settings = settingsState ?: Settings()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val recent by viewModel.recentApps.collectAsStateWithLifecycle()
    val pinned by viewModel.pinnedApps.collectAsStateWithLifecycle()
    val scanCompleted by viewModel.scanCompleted.collectAsStateWithLifecycle()

    var showSettings by rememberSaveable { mutableStateOf(false) }

    // 重新扫描完成后的反馈
    val context = LocalContext.current
    LaunchedEffect(scanCompleted) {
        val count = scanCompleted ?: return@LaunchedEffect
        Toast.makeText(context, "已扫描 $count 个应用", Toast.LENGTH_SHORT).show()
        viewModel.consumeScanCompleted()
    }

    // 长按：固定 / 取消固定
    val onAppLongClick: (AppEntity) -> Unit = { app ->
        viewModel.togglePinned(app)
        Toast.makeText(context, if (app.pinned) "已取消固定" else "已固定", Toast.LENGTH_SHORT).show()
    }

    // 首次启动提醒：读取应用列表（仅当设置已加载且未看过时显示，避免冷启动闪烁）
    if (settingsState != null && !settings.onboardingShown) {
        AlertDialog(
            onDismissRequest = { viewModel.setOnboardingShown(true) },
            title = { Text("读取应用列表") },
            text = {
                Text(
                    "本应用需要读取您设备上已安装的应用列表，用于本地搜索和快速启动。" +
                        "所有数据仅保存在本地，不会上传网络。"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setOnboardingShown(true) }) {
                    Text("知道了")
                }
            }
        )
    }

    if (showSettings) {
        SettingsScreen(
            settings = settings,
            onBack = { showSettings = false },
            onRecentCountChange = viewModel::setRecentCount,
            onDarkModeChange = viewModel::setDarkMode,
            onClearUsage = viewModel::clearUsage,
            onRescan = viewModel::rescan
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            // 顶部留白，把应用 + 搜索框 + 键盘整体推到底部
            Spacer(Modifier.weight(1f))

            // 应用横排展示（紧挨搜索框上方）
            if (query.isBlank()) {
                PinnedApps(
                    apps = pinned,
                    onAppClick = viewModel::launchApp,
                    onAppLongClick = onAppLongClick
                )
                RecentApps(
                    apps = recent,
                    onAppClick = viewModel::launchApp,
                    onAppLongClick = onAppLongClick,
                    onClearClick = viewModel::clearUsage
                )
            } else {
                SearchResults(
                    results = results,
                    onAppClick = viewModel::launchApp,
                    onAppLongClick = onAppLongClick
                )
            }

            // 搜索框与 T9 键盘融为一体
            SearchBar(
                query = query,
                onClear = viewModel::clearQuery,
                onSettingsClick = { showSettings = true },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // T9 键盘下方留一点空白
            T9Keyboard(
                onDigit = viewModel::appendDigit,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}
