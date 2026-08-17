package com.t9launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t9launcher.data.AppEntity

/** 固定区：已固定的应用（长按取消固定）。无固定应用时不显示。 */
@Composable
fun PinnedApps(
    apps: List<AppEntity>,
    onAppClick: (AppEntity) -> Unit,
    onAppLongClick: (AppEntity) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(apps) {
        if (apps.isNotEmpty()) listState.scrollToItem(0)
    }

    if (apps.isEmpty()) return

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "固定",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
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
