package com.t9launcher.pinyin

import com.github.promeg.pinyinhelper.PinyinMapDict
import java.util.HashMap

/**
 * 常见 App 名称多音字词语词典。
 *
 * 词典优先级高于单字拼音（TinyPinyin 的 ForwardLongestSelector 会做最长匹配），
 * 因此 "招商银行" 会命中完整词条并得到 "zhao shang yin hang"，
 * 而不是逐字枚举出 "yin xing" 之类错误读音。
 */
object CustomPinyinDictionary : PinyinMapDict() {

    private val entries: Map<String, Array<String>> = buildEntries()

    override fun mapping(): MutableMap<String, Array<String>> {
        return HashMap(entries)
    }

    private fun buildEntries(): Map<String, Array<String>> {
        val m = HashMap<String, Array<String>>()

        fun put(word: String, vararg pinyin: String) {
            m[word] = arrayOf(*pinyin)
        }

        // —— 银行类（"行" 读 hang）——
        put("银行", "YIN", "HANG")
        put("招商银行", "ZHAO", "SHANG", "YIN", "HANG")
        put("中国银行", "ZHONG", "GUO", "YIN", "HANG")
        put("工商银行", "GONG", "SHANG", "YIN", "HANG")
        put("建设银行", "JIAN", "SHE", "YIN", "HANG")
        put("交通银行", "JIAO", "TONG", "YIN", "HANG")
        put("农业银行", "NONG", "YE", "YIN", "HANG")
        put("中信银行", "ZHONG", "XIN", "YIN", "HANG")
        put("浦发银行", "PU", "FA", "YIN", "HANG")
        put("民生银行", "MIN", "SHENG", "YIN", "HANG")
        put("兴业银行", "XING", "YE", "YIN", "HANG")
        put("平安银行", "PING", "AN", "YIN", "HANG")
        put("光大银行", "GUANG", "DA", "YIN", "HANG")
        put("华夏银行", "HUA", "XIA", "YIN", "HANG")
        put("广发银行", "GUANG", "FA", "YIN", "HANG")
        put("邮政储蓄", "YOU", "ZHENG", "CHU", "XU")

        // —— "行" 的其他读音（hang / xing）——
        put("行业", "HANG", "YE")
        put("行长", "HANG", "ZHANG")
        put("行动", "XING", "DONG")
        put("行走", "XING", "ZOU")
        put("行为", "XING", "WEI")
        put("行程", "XING", "CHENG")
        put("旅行", "LYU", "XING")

        // —— "乐"（yue / le）——
        put("音乐", "YIN", "YUE")
        put("音乐播放器", "YIN", "YUE", "BO", "FANG", "QI")
        put("乐器", "YUE", "QI")
        put("网易云音乐", "WANG", "YI", "YUN", "YIN", "YUE")
        put("酷狗音乐", "KU", "GOU", "YIN", "YUE")
        put("酷我音乐", "KU", "WO", "YIN", "YUE")
        put("虾米音乐", "XIA", "MI", "YIN", "YUE")
        put("咪咕音乐", "MI", "GU", "YIN", "YUE")
        put("快乐", "KUAI", "LE")
        put("欢乐", "HUAN", "LE")

        // —— "长"（chang / zhang）、"重"（chong / zhong）、"藏"（zang / cang）——
        put("长城", "CHANG", "CHENG")
        put("长沙", "CHANG", "SHA")
        put("重庆", "CHONG", "QING")
        put("西藏", "XI", "ZANG")

        return m
    }
}
