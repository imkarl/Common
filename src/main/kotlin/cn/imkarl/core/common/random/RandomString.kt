package cn.imkarl.core.common.random

import kotlin.random.Random


/**
 * 生成随机的字符串
 */
fun randomString(length: Int): String {
    val str = ('0'..'9') + ('a'..'z') + ('A'..'Z')
    return (0 until length).joinToString("") { str[Random.nextInt(str.size) % str.size].toString() }
}
