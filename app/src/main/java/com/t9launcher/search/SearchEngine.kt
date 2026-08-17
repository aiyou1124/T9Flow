package com.t9launcher.search

import com.t9launcher.data.AppEntity

/**
 * 纯内存搜索引擎：对提前建立好的索引字段做匹配与排序。
 *
 * 完全不重新做「中文 -> 拼音」转换，保证单次搜索在毫秒级完成。
 */
object SearchEngine {

    /**
     * 搜索并排序。
     *
     * 排序规则（规格第十一节）：
     * 完全匹配 > 首字母完全匹配 > 拼音完全匹配 > T9 完全匹配 > 前缀匹配 > 模糊匹配；
     * 同一匹配度下按 lastOpenedTime 降序、openCount 降序。
     */
    fun search(rawQuery: String, apps: List<AppEntity>): List<SearchResult> {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) return emptyList()

        val results = ArrayList<SearchResult>(apps.size)
        for (app in apps) {
            val matchType = matchType(q, app) ?: continue
            results.add(SearchResult(app, matchType))
        }

        results.sortWith(
            compareBy<SearchResult> { it.matchType.rank }
                .thenByDescending { it.app.lastOpenedTime }
                .thenByDescending { it.app.openCount }
                .thenBy { it.app.appName.lowercase() }
        )
        return results
    }

    /** 返回匹配类型；不匹配返回 null。 */
    fun matchType(q: String, app: AppEntity): MatchType? {
        val name = app.normalizedName
        val compact = app.pinyinCompact
        val initials = app.initials
        val t9Full = app.t9Full
        val t9Initials = app.t9Initials

        // 1. 完全匹配
        if (name == q) return MatchType.EXACT
        // 2. 首字母完全匹配
        if (initials.isNotEmpty() && initials == q) return MatchType.INITIALS_EXACT
        // 3. 拼音完全匹配
        if (compact.isNotEmpty() && compact == q) return MatchType.PINYIN_EXACT
        // 4. T9 完全匹配
        if (t9Full.isNotEmpty() && t9Full == q) return MatchType.T9_EXACT
        if (t9Initials.isNotEmpty() && t9Initials == q) return MatchType.T9_EXACT

        // 5. 前缀匹配
        if (name.startsWith(q) ||
            compact.startsWith(q) ||
            initials.startsWith(q) ||
            t9Full.startsWith(q) ||
            t9Initials.startsWith(q)
        ) {
            return MatchType.PREFIX
        }

        // 6. 模糊匹配（包含）
        if (name.contains(q) || compact.contains(q) || t9Full.contains(q)) {
            return MatchType.FUZZY
        }

        return null
    }
}
