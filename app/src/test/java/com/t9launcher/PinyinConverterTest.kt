package com.t9launcher

import com.t9launcher.pinyin.PinyinConverter
import com.t9launcher.pinyin.PinyinInitializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test

/**
 * 多音字词语级识别验收（对应规格第五、六、二十四节）。
 */
class PinyinConverterTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun initPinyin() {
            PinyinInitializer.ensureInitialized()
        }
    }

    @Test
    fun `招商银行首字母和完整拼音`() {
        val idx = PinyinConverter.toIndex("招商银行")
        assertEquals("zsyh", idx.initials)
        assertEquals("zhaoshangyinhang", idx.pinyinCompact)
        assertEquals("9794", idx.t9Initials)
    }

    @Test
    fun `招商银行不生成错误读音xing`() {
        val idx = PinyinConverter.toIndex("招商银行")
        assertFalse(idx.initials.contains("x"))
        assertFalse(idx.pinyinCompact.contains("xing"))
        assertEquals("zhaoshangyinhang", idx.pinyinCompact)
    }

    @Test
    fun `音乐读音yinyue`() {
        val idx = PinyinConverter.toIndex("音乐")
        assertEquals("yy", idx.initials)
        assertEquals("yinyue", idx.pinyinCompact)
    }

    @Test
    fun `行动读音xingdong`() {
        val idx = PinyinConverter.toIndex("行动")
        assertEquals("xd", idx.initials)
        assertEquals("xingdong", idx.pinyinCompact)
    }

    @Test
    fun `行业读音hangye`() {
        val idx = PinyinConverter.toIndex("行业")
        assertEquals("hy", idx.initials)
        assertEquals("hangye", idx.pinyinCompact)
    }

    @Test
    fun `行走读音xingzou`() {
        val idx = PinyinConverter.toIndex("行走")
        assertEquals("xz", idx.initials)
        assertEquals("xingzou", idx.pinyinCompact)
    }

    @Test
    fun `音乐播放器首字母yybfq`() {
        val idx = PinyinConverter.toIndex("音乐播放器")
        assertEquals("yybfq", idx.initials)
        assertEquals("yinyuebofangqi", idx.pinyinCompact)
    }

    @Test
    fun `Chrome英文名索引`() {
        val idx = PinyinConverter.toIndex("Chrome")
        assertEquals("chrome", idx.pinyinCompact)
        assertEquals("c", idx.initials)
        assertEquals("247663", idx.t9Full)
    }

    @Test
    fun `中英混合QQ音乐`() {
        val idx = PinyinConverter.toIndex("QQ音乐")
        assertEquals("qqyinyue", idx.pinyinCompact)
        // QQ 作为英文 token，首字母取 q（单词首字母），音乐 -> yin yue -> y y
        assertEquals("qyy", idx.initials)
    }
}
