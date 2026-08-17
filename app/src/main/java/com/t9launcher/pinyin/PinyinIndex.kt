package com.t9launcher.pinyin

/**
 * 一个应用名称的完整搜索索引。
 *
 * @param pinyin        拼音（空格分隔），如 "zhao shang yin hang"
 * @param pinyinCompact 拼音紧凑串，如 "zhaoshangyinhang"
 * @param initials      首字母串（小写），如 "zsyh"
 * @param t9Full        完整拼音逐字母映射的 T9 数字串，如 "9427426464264"
 * @param t9Initials    首字母映射的 T9 数字串，如 "9794"
 */
data class PinyinIndex(
    val pinyin: String,
    val pinyinCompact: String,
    val initials: String,
    val t9Full: String,
    val t9Initials: String
)
