package org.cwcc.ani.map.utils

import java.util.regex.Pattern

/**
 *
 */
fun String?.getFloat():Float
{
    var value = 1f
    if (this == null || this == "") return 1f
    try {
        value = this.toFloat()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return value
}

/**
 * 判断字符串是否全为数字
 */
fun String.isInteger():Boolean
{
    val pattern = Pattern.compile("^[-\\+]?[\\d]*$")
    return pattern.matcher(this).matches()
}
