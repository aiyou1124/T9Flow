package com.t9launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.t9launcher.settings.DarkMode
import com.t9launcher.ui.MainScreen
import com.t9launcher.ui.MainViewModel
import com.t9launcher.ui.theme.T9Theme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            if (pkg.isNullOrEmpty()) return
            // 只对真正的安装/卸载/更新触发重扫，忽略含自身在内的普通状态变化广播，
            // 避免运行期间频繁无意义重扫导致固定状态被刷新。
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED -> viewModel.rescan()
                Intent.ACTION_PACKAGE_CHANGED -> {
                    // 仅当组件确实变化（如组件启用/禁用）时才重扫
                    val changedComponents =
                        intent.getStringArrayExtra(Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST)
                    if (!changedComponents.isNullOrEmpty()) {
                        viewModel.rescan()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        registerPackageReceiver()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            T9Theme(darkMode = settings?.darkMode ?: DarkMode.SYSTEM) {
                MainScreen(viewModel)
            }
        }
    }

    private fun registerPackageReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        ContextCompat.registerReceiver(
            this,
            packageReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        unregisterReceiver(packageReceiver)
        super.onDestroy()
    }
}
