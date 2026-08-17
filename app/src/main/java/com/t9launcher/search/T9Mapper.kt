package com.t9launcher.search

/**
 * T9 数字键盘字母映射。
 *
 * 2 = ABC, 3 = DEF, 4 = GHI, 5 = JKL,
 * 6 = MNO, 7 = PQRS, 8 = TUV, 9 = WXYZ
 */
object T9Mapper {

    fun letterToDigit(c: Char): Char = when (c.lowercaseChar()) {
        'a', 'b', 'c' -> '2'
        'd', 'e', 'f' -> '3'
        'g', 'h', 'i' -> '4'
        'j', 'k', 'l' -> '5'
        'm', 'n', 'o' -> '6'
        'p', 'q', 'r', 's' -> '7'
        't', 'u', 'v' -> '8'
        'w', 'x', 'y', 'z' -> '9'
        else -> c // 数字等原样保留
    }

    /** 将字符串逐字符映射为 T9 数字串。 */
    fun toT9(input: String): String = buildString {
        for (c in input) append(letterToDigit(c))
    }

    /** 判断输入是否由纯数字组成。 */
    fun isDigits(input: String): Boolean = input.isNotEmpty() && input.all { it.isDigit() }
}
