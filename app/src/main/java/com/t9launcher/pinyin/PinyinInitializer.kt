package com.t9launcher.pinyin

import com.github.promeg.pinyinhelper.Pinyin

/**
 * TinyPinyin 全局初始化入口（幂等）。
 *
 * TinyPinyin 的 [Pinyin.init] 只允许调用一次；这里用标志位 + 同步保证
 * 生产环境（Application）与单元测试中都能安全、只初始化一次。
 */
object PinyinInitializer {

    @Volatile
    private var initialized = false

    fun ensureInitialized() {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    runCatching {
                        Pinyin.init(Pinyin.newConfig().with(CustomPinyinDictionary))
                    }
                    initialized = true
                }
            }
        }
    }
}
