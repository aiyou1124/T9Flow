package com.t9launcher.data

import androidx.room.withTransaction
import com.t9launcher.launcher.AppScanner
import com.t9launcher.pinyin.PinyinConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AppRepository(
    private val dao: AppDao,
    private val db: AppDatabase,
    private val scanner: AppScanner
) {

    /** 串行化所有扫描，避免并发 rescan 互相覆盖（尤其固定状态）。 */
    private val rescanMutex = Mutex()

    val allApps: Flow<List<AppEntity>> = dao.getAll()

    /** 首次启动时若数据库为空则扫描。 */
    suspend fun ensureScanned() {
        if (dao.count() == 0) {
            rescan()
        }
    }

    /**
     * 重新扫描已安装应用并重建索引。
     * 保留已有的 lastOpenedTime / openCount / pinned 统计。
     * @return 本次扫描到的可启动应用数量。
     */
    suspend fun rescan(): Int = rescanMutex.withLock {
        val scanned = withContext(Dispatchers.IO) { scanner.scan() }
        db.withTransaction {
            val existing = dao.getAllOnce().associateBy { it.componentName }
            val entities = scanned.map { s ->
                val old = existing[s.componentName]
                val index = PinyinConverter.toIndex(s.appName)
                AppEntity(
                    componentName = s.componentName,
                    packageName = s.packageName,
                    appName = s.appName,
                    normalizedName = s.appName.lowercase().trim(),
                    pinyin = index.pinyin,
                    pinyinCompact = index.pinyinCompact,
                    initials = index.initials,
                    t9Full = index.t9Full,
                    t9Initials = index.t9Initials,
                    lastOpenedTime = old?.lastOpenedTime ?: 0L,
                    openCount = old?.openCount ?: 0,
                    pinned = old?.pinned ?: false
                )
            }
            dao.clearAll()
            dao.upsertAll(entities)
        }
        scanned.size
    }

    fun pinnedApps(): Flow<List<AppEntity>> = dao.pinnedApps()

    fun recentApps(limit: Int): Flow<List<AppEntity>> = dao.recentApps(limit)

    suspend fun recordOpen(componentName: String) {
        // 与 rescan 共用同一把锁，避免「记录最近打开」与「重建索引」并发交错
        // 导致记录被 rescan 的旧数据覆盖丢失
        rescanMutex.withLock {
            dao.recordOpen(componentName, System.currentTimeMillis())
        }
    }

    suspend fun setPinned(componentName: String, pinned: Boolean) {
        // 与 recordOpen 同理：固定操作也要与 rescan 串行，避免被覆盖
        rescanMutex.withLock {
            dao.setPinned(componentName, pinned)
        }
    }

    suspend fun clearUsage() = rescanMutex.withLock { dao.clearUsage() }
}
