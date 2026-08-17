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
import com.t9launcher.search.SearchResult

/** 搜索结果横排列表（紧贴搜索框上方）。 */
@Composable
fun SearchResults(
    results: List<SearchResult>,
    onAppClick: (AppEntity) -> Unit,
    onAppLongClick: (AppEntity) -> Unit
) {
    val listState = rememberLazyListState()
    // 结果变化时滚回最左，确保匹配度最高的第一个可见
    LaunchedEffect(results) {
        if (results.isNotEmpty()) listState.scrollToItem(0)
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "搜索结果（${results.size}）",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (results.isEmpty()) {
            Text(
                text = "未找到匹配的应用",
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
                items(results, key = { it.app.componentName }) { r ->
                    AppTile(
                        app = r.app,
                        onClick = { onAppClick(r.app) },
                        onLongClick = { onAppLongClick(r.app) }
                    )
                }
            }
        }
    }
}
