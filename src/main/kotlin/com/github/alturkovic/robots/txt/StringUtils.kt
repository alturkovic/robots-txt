package com.github.alturkovic.robots.txt

import kotlin.math.min

internal object StringUtils {
    fun greatestCommonPrefix(a: String, b: String): String {
        val minLength = min(a.length, b.length)
        for (i in 0 until minLength) if (a[i] != b[i]) return a.substring(0, i)
        return a.substring(0, minLength)
    }
}
