package com.t9launcher.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM apps ORDER BY appName COLLATE NOCASE ASC")
    fun getAll(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps")
    suspend fun getAllOnce(): List<AppEntity>

    @Upsert
    suspend fun upsertAll(apps: List<AppEntity>)

    @Query("DELETE FROM apps")
    suspend fun clearAll()

    /** 固定区：已固定的应用，按最近打开排序。 */
    @Query("SELECT * FROM apps WHERE pinned = 1 ORDER BY lastOpenedTime DESC")
    fun pinnedApps(): Flow<List<AppEntity>>

    /** 最近打开：未固定的应用，按最近打开排序。 */
    @Query("SELECT * FROM apps WHERE pinned = 0 AND lastOpenedTime > 0 ORDER BY lastOpenedTime DESC LIMIT :limit")
    fun recentApps(limit: Int): Flow<List<AppEntity>>

    @Query("UPDATE apps SET lastOpenedTime = :time, openCount = openCount + 1 WHERE componentName = :componentName")
    suspend fun recordOpen(componentName: String, time: Long)

    @Query("UPDATE apps SET pinned = :pinned WHERE componentName = :componentName")
    suspend fun setPinned(componentName: String, pinned: Boolean)

    @Query("UPDATE apps SET lastOpenedTime = 0, openCount = 0")
    suspend fun clearUsage()

    @Query("SELECT COUNT(*) FROM apps")
    suspend fun count(): Int
}
