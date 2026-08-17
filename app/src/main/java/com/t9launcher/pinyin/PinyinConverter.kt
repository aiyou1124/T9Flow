package com.t9launcher.pinyin

import com.github.promeg.pinyinhelper.Pinyin
import com.t9launcher.search.T9Mapper

/**
 * 中文名称 -> 拼音索引转换器。
 *
 * 依赖 TinyPinyin 提供单字拼音 + 词语级多音字词典（[CustomPinyinDictionary]）。
 * 关键点：多音字不是逐字暴力枚举，而是由 TinyPinyin 的正向最长匹配词典
 * （ForwardLongestSelector + Aho-Corasick）按完整词语上下文确定读音。
 *
 * 中英混合名称会先按字符类型切分为「中文段」与「ASCII 段」分别处理，
 * 避免 TinyPinyin 把英文逐字符用空格拆开。
 */
object PinyinConverter {

    /**
     * 把应用名称转换为搜索索引。
     * 例如 "招商银行" -> initials = "zsyh", pinyinCompact = "zhaoshangyinhang"。
     */
    fun toIndex(name: String): PinyinIndex {
        val tokens = tokenize(name)
        val compact = tokens.joinToString("")
        val initials = tokens.mapNotNull { it.firstOrNull() }.joinToString("")

        return PinyinIndex(
            pinyin = tokens.joinToString(" "),
            pinyinCompact = compact,
            initials = initials,
            t9Full = T9Mapper.toT9(compact),
            t9Initials = T9Mapper.toT9(initials)
        )
    }

    /**
     * 将名称切分为拼音/英文 token 列表。
     * - 中文段：交给 TinyPinyin 转拼音（含多音字词典），每个拼音一个 token。
     * - ASCII 字母/数字段：整体作为一个小写 token（保持英文单词完整）。
     * - 其它字符（空格、符号、emoji）：作为分隔符忽略。
     */
    fun tokenize(name: String): List<String> {
        val tokens = mutableListOf<String>()
        val chineseBuf = StringBuilder()
        val asciiBuf = StringBuilder()

        fun flushChinese() {
            if (chineseBuf.isNotEmpty()) {
                val pinyin = Pinyin.toPinyin(chineseBuf.toString(), " ")
                tokens.addAll(
                    pinyin.split(' ')
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                )
                chineseBuf.setLength(0)
            }
        }

        fun flushAscii() {
            if (asciiBuf.isNotEmpty()) {
                tokens.add(asciiBuf.toString().lowercase())
                asciiBuf.setLength(0)
            }
        }

        for (c in name) {
            when {
                Pinyin.isChinese(c) -> {
                    flushAscii()
                    chineseBuf.append(c)
                }
                c.code < 128 && c.isLetterOrDigit() -> {
                    flushChinese()
                    asciiBuf.append(c)
                }
                else -> {
                    flushChinese()
                    flushAscii()
                }
            }
        }
        flushChinese()
        flushAscii()

        return tokens
    }
}
