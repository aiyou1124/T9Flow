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
            if (!pkg.isNullOrEmpty()) {
                viewModel.rescan()
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
