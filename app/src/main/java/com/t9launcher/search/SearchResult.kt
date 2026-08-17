package com.t9launcher.search

import com.t9launcher.data.AppEntity

/**
 * 匹配类型，按优先级从高到低排列（rank 越小越靠前）。
 *
 * 对应规格第十一节：
 * 完全匹配 > 首字母完全匹配 > 拼音完全匹配 > T9 完全匹配 > 前缀匹配 > 模糊匹配
 * （最近使用、使用次数作为同匹配度下的次级排序键）
 */
enum class MatchType(val rank: Int) {
    EXACT(0),
    INITIALS_EXACT(1),
    PINYIN_EXACT(2),
    T9_EXACT(3),
    PREFIX(4),
    FUZZY(5)
}

data class SearchResult(
    val app: AppEntity,
    val matchType: MatchType
)
