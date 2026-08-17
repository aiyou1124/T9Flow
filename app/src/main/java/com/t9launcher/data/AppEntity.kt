package com.t9launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 可启动入口（应用或其某个 Activity）的搜索索引 + 使用统计。
 *
 * 以 componentName（Activity 完整类名）为主键，
 * 这样同一应用声明的多个可启动入口（如主界面 + 扫码）可分别被搜索与启动。
 */
@Entity(tableName = "apps")
data class AppEntity(
    /** Activity 完整类名，如 "com.syyf.quickpay.act.AlipayScanner" */
    @PrimaryKey val componentName: String,
    val packageName: String,
    /** 入口显示名（Activity label，缺省回退到应用名） */
    val appName: String,
    /** 规范化名称（小写、去首尾空格），用于英文/中文直接匹配 */
    val normalizedName: String,
    /** 完整拼音（空格分隔），如 "zhao shang yin hang" */
    val pinyin: String,
    /** 完整拼音紧凑串，如 "zhaoshangyinhang" */
    val pinyinCompact: String,
    /** 首字母串，如 "zsyh" */
    val initials: String,
    /** 完整拼音逐字母映射的 T9 数字串 */
    val t9Full: String,
    /** 首字母映射的 T9 数字串 */
    val t9Initials: String,
    /** 最后打开时间（毫秒时间戳），0 表示从未打开 */
    val lastOpenedTime: Long = 0L,
    /** 打开次数 */
    val openCount: Int = 0,
    /** 是否固定（置顶显示在固定区） */
    val pinned: Boolean = false
)
