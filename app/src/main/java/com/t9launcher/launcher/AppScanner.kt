package com.t9launcher.launcher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * 用 PackageManager 扫描所有可启动入口（含 LAUNCHER 的 Activity）。
 *
 * 每个 Activity 一个条目，保留 componentName 以便精确启动；
 * 这样同一应用声明的多个入口（如主界面 + 扫码）都能被搜索到。
 * 排除自身、重复 Activity、无图标/无标签的项。
 */
class AppScanner(private val context: Context) {

    data class ScannedApp(
        val componentName: String,
        val packageName: String,
        val appName: String,
        val icon: Drawable
    )

    fun scan(): List<ScannedApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val infos = pm.queryIntentActivities(intent, 0)

        val seen = HashSet<String>()
        val result = ArrayList<ScannedApp>(infos.size)

        for (info in infos) {
            val activityInfo = info.activityInfo ?: continue
            val pkg = activityInfo.packageName ?: continue
            if (pkg == context.packageName) continue // 排除本应用
            val component = ComponentName(pkg, activityInfo.name).flattenToString()
            if (!seen.add(component)) continue // 按 Activity 去重

            val label = info.loadLabel(pm)?.toString()?.trim()
            if (label.isNullOrEmpty()) continue

            val icon = runCatching { info.loadIcon(pm) }.getOrNull() ?: continue

            result.add(ScannedApp(component, pkg, label, icon))
        }

        result.sortBy { it.appName.lowercase() }
        return result
    }
}
