package com.t9launcher

import com.t9launcher.data.AppEntity
import com.t9launcher.pinyin.PinyinConverter
import com.t9launcher.pinyin.PinyinInitializer
import com.t9launcher.search.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

/**
 * 搜索匹配与排序验收（对应规格第九、十、十一、二十四节）。
 */
class SearchEngineTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun initPinyin() {
            PinyinInitializer.ensureInitialized()
        }

        private fun app(name: String, openCount: Int = 0, lastOpened: Long = 0L): AppEntity {
            val idx = PinyinConverter.toIndex(name)
            return AppEntity(
                componentName = "pkg.$name/.MainActivity",
                packageName = "pkg.$name",
                appName = name,
                normalizedName = name.lowercase().trim(),
                pinyin = idx.pinyin,
                pinyinCompact = idx.pinyinCompact,
                initials = idx.initials,
                t9Full = idx.t9Full,
                t9Initials = idx.t9Initials,
                lastOpenedTime = lastOpened,
                openCount = openCount
            )
        }
    }

    @Test
    fun `首字母ZSYH找到招商银行并排第一`() {
        val apps = listOf(
            app("招商银行"),
            app("微信", openCount = 999),
            app("Chrome")
        )
        val results = SearchEngine.search("zsyh", apps)
        assertTrue(results.isNotEmpty())
        assertEquals("招商银行", results.first().app.appName)
    }

    @Test
    fun `完整拼音zhaoshangyinhang找到招商银行`() {
        val results = SearchEngine.search(
            "zhaoshangyinhang",
            listOf(app("招商银行"), app("Chrome"))
        )
        assertEquals("招商银行", results.first().app.appName)
    }

    @Test
    fun `英文chrome找到Chrome`() {
        val results = SearchEngine.search(
            "chrome",
            listOf(app("Chrome"), app("招商银行"))
        )
        assertEquals("Chrome", results.first().app.appName)
    }

    @Test
    fun `T9数字247663找到Chrome`() {
        val results = SearchEngine.search(
            "247663",
            listOf(app("Chrome"), app("招商银行"))
        )
        assertEquals("Chrome", results.first().app.appName)
    }

    @Test
    fun `中文名直接匹配`() {
        val results = SearchEngine.search(
            "招商银行",
            listOf(app("招商银行"), app("招商证券"))
        )
        assertEquals("招商银行", results.first().app.appName)
    }

    @Test
    fun `英文首字母c找到Chrome`() {
        val results = SearchEngine.search("c", listOf(app("Chrome")))
        assertTrue(results.any { it.app.appName == "Chrome" })
    }

    @Test
    fun `首字母子串jsyh匹配中国建设银行`() {
        val results = SearchEngine.search(
            "jsyh",
            listOf(app("中国建设银行"), app("招商银行"), app("Chrome"))
        )
        assertTrue(results.any { it.app.appName == "中国建设银行" })

        // 若存在「建设银行」，其首字母完全匹配应排在「中国建设银行」（子串模糊匹配）之前
        val results2 = SearchEngine.search(
            "jsyh",
            listOf(app("中国建设银行"), app("建设银行"))
        )
        assertEquals("建设银行", results2.first().app.appName)
        assertTrue(results2.any { it.app.appName == "中国建设银行" })
    }

    @Test
    fun `匹配度优先于使用次数`() {
        // 微信：拼音完全匹配，但使用次数低
        val exact = app("微信", openCount = 1, lastOpened = 100L)
        // 微信读书：仅前缀匹配，但使用次数很高
        val prefix = app("微信读书", openCount = 999, lastOpened = 99999L)
        val results = SearchEngine.search("weixin", listOf(prefix, exact))
        assertEquals("微信", results.first().app.appName)
    }

    @Test
    fun `同匹配度按最近使用排序`() {
        val older = app("招商银行", openCount = 1, lastOpened = 1000L)
        val newer = app("招商证券", openCount = 1, lastOpened = 2000L)
        // 两者都是 PREFIX 匹配（pinyin 前缀 "zhaoshang"）
        val results = SearchEngine.search("zhaoshang", listOf(older, newer))
        assertEquals("招商证券", results.first().app.appName)
    }
}
