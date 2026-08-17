package com.t9launcher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.t9launcher.T9Application
import com.t9launcher.data.AppEntity
import com.t9launcher.search.SearchEngine
import com.t9launcher.search.SearchResult
import com.t9launcher.settings.Settings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as T9Application).container
    private val repository = container.repository
    private val launcher = container.launcher
    private val settingsRepo = container.settings

    val settings: StateFlow<Settings?> =
        settingsRepo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val allApps: StateFlow<List<AppEntity>> =
        repository.allApps.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentApps: StateFlow<List<AppEntity>> = settings
        .filterNotNull()
        .flatMapLatest { repository.recentApps(it.recentCount) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pinnedApps: StateFlow<List<AppEntity>> =
        repository.pinnedApps().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val query = MutableStateFlow("")

    /** 最近一次扫描完成后的应用数量；null 表示暂无待展示的扫描结果。 */
    val scanCompleted = MutableStateFlow<Int?>(null)

    val searchResults: StateFlow<List<SearchResult>> = combine(allApps, query) { apps, q ->
        SearchEngine.search(q, apps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { repository.ensureScanned() }
    }

    fun appendDigit(digit: Char) {
        query.value = query.value + digit
    }

    fun clearQuery() {
        query.value = ""
    }

    fun launchApp(app: AppEntity) {
        if (launcher.launch(app.componentName)) {
            query.value = "" // 启动应用后清空输入
            viewModelScope.launch { repository.recordOpen(app.componentName) }
        }
    }

    fun rescan() {
        viewModelScope.launch {
            val count = repository.rescan()
            scanCompleted.value = count
        }
    }

    /** 消费扫描完成事件（避免重复展示反馈）。 */
    fun consumeScanCompleted() {
        scanCompleted.value = null
    }

    fun clearUsage() {
        viewModelScope.launch { repository.clearUsage() }
    }

    fun togglePinned(app: AppEntity) {
        viewModelScope.launch { repository.setPinned(app.componentName, !app.pinned) }
    }

    fun setRecentCount(value: Int) {
        viewModelScope.launch { settingsRepo.setRecentCount(value) }
    }

    fun setOnboardingShown(value: Boolean) {
        viewModelScope.launch { settingsRepo.setOnboardingShown(value) }
    }

    fun setDarkMode(value: String) {
        viewModelScope.launch { settingsRepo.setDarkMode(value) }
    }
}
