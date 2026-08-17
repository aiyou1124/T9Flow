package com.t9launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t9launcher.data.AppEntity

/** 首页「最近打开」横排列表（紧贴搜索框上方）。 */
@Composable
fun RecentApps(
    apps: List<AppEntity>,
    onAppClick: (AppEntity) -> Unit,
    onAppLongClick: (AppEntity) -> Unit,
    onClearClick: () -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(apps) {
        if (apps.isNotEmpty()) listState.scrollToItem(0)
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近打开",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (apps.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = "清空最近打开")
                }
            }
        }

        if (apps.isEmpty()) {
            Text(
                text = "暂无最近打开的应用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(apps, key = { it.componentName }) { app ->
                    AppTile(
                        app = app,
                        onClick = { onAppClick(app) },
                        onLongClick = { onAppLongClick(app) }
                    )
                }
            }
        }
    }
}
