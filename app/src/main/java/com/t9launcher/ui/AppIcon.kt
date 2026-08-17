package com.t9launcher.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 从 PackageManager 加载应用图标（支持 adaptive icon / bitmap / vector），
 * 异步解码并在加载前显示占位图标。
 */
@Composable
fun AppIcon(
    packageName: String,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(packageName) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(packageName) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toBitmap(96, 96)
                    .asImageBitmap()
            }.getOrNull()
        }
    }

    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = modifier.size(size)
        )
    } else {
        Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.Apps,
                contentDescription = null,
                modifier = Modifier.size(size * 0.72f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
