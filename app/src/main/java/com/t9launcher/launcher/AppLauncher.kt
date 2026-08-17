package com.t9launcher.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** 通过 ComponentName 精确启动指定 Activity 入口。 */
class AppLauncher(private val context: Context) {

    /** 启动成功返回 true。 */
    fun launch(componentName: String): Boolean {
        val cn = ComponentName.unflattenFromString(componentName) ?: return false
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setComponent(cn)
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }
}
