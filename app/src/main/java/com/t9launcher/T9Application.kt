package com.t9launcher

import android.app.Application
import android.content.Context
import com.t9launcher.data.AppDao
import com.t9launcher.data.AppDatabase
import com.t9launcher.data.AppRepository
import com.t9launcher.launcher.AppLauncher
import com.t9launcher.launcher.AppScanner
import com.t9launcher.pinyin.PinyinInitializer
import com.t9launcher.settings.SettingsRepository

class T9Application : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // 初始化 TinyPinyin（注入自定义多音字词典），幂等。
        PinyinInitializer.ensureInitialized()
        container = AppContainer(this)
    }
}

/** 手动依赖容器（避免引入大型 DI 框架）。 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.getInstance(appContext)
    val dao: AppDao = database.appDao()
    val scanner: AppScanner = AppScanner(appContext)
    val repository: AppRepository = AppRepository(dao, database, scanner)
    val launcher: AppLauncher = AppLauncher(appContext)
    val settings: SettingsRepository = SettingsRepository(appContext)
}
