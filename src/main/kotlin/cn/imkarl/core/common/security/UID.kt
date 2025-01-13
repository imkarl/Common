package cn.imkarl.core.common.security

import kotlin.math.pow


// UID加密字符
private val encryptCharsByUID3 by lazy {
    UidConvert(
        encryptChars = ('0'..'9') + ('a'..'z') + ('A'..'Z'),
        originalLength = 5,
        encryptLength = 3
    )
}
private val encryptCharsByUID2 by lazy {
    UidConvert(
        encryptChars = ('0'..'9') + ('a'..'z'),
        originalLength = 3,
        encryptLength = 2
    )
}

/**
 * 将长整形ID转化为UID字符串
 */
fun Long.longIdToUID(mode: Int = 3): String {
    return try {
        if (mode == 3) {
            encryptCharsByUID3.longIdToUID(this)
        } else {
            encryptCharsByUID2.longIdToUID(this)
        }
    } catch (_: Throwable) {
        ""
    }
}

/**
 * 将UID字符串转化为长整形ID
 */
fun String.uidToLongId(mode: Int = 3): Long {
    return try {
        if (mode == 3) {
            encryptCharsByUID3.uidToLongId(this)
        } else {
            encryptCharsByUID2.uidToLongId(this)
        }
    } catch (_: Throwable) {
        -1L
    }
}

private class UidConvert(val encryptChars: List<Char>, val originalLength: Int = 3, val encryptLength: Int = 2) {

    /**
     * 将长整形ID转化为UID字符串
     */
    fun longIdToUID(original: Long): String {
        val charCount = encryptChars.size

        var num = original
        val splitSize = 10.0.pow(originalLength.toDouble()).toInt()
        val results = mutableListOf(num % splitSize)
        while (num >= splitSize) {
            num /= splitSize
            results.add(0, num % splitSize)
        }

        return results.reversed().joinToString("") {
            if (encryptLength == 3) {
                "${encryptChars[(it / charCount / charCount).toInt()]}" +
                        "${encryptChars[(it / charCount % charCount).toInt()]}" +
                        "${encryptChars[(it % charCount).toInt()]}"
            } else {
                "${encryptChars[(it / charCount).toInt()]}${encryptChars[(it % charCount).toInt()]}"
            }
        }
    }

    /**
     * 将UID字符串转化为长整形ID
     */
    fun uidToLongId(encrypt: String): Long {
        val charCount = encryptChars.size

        var index = encrypt.length
        val results = mutableListOf<String>()
        while (index > 0) {
            index -= encryptLength
            results.add(encrypt.substring(kotlin.math.max(index, 0), index + encryptLength))
        }

        return results.joinToString("") { part ->
            val num = if (encryptLength == 3) {
                encryptChars.indexOf(part[0]) * charCount * charCount + encryptChars.indexOf(part[1]) * charCount + encryptChars.indexOf(part[2])
            } else {
                encryptChars.indexOf(part[0]) * charCount + encryptChars.indexOf(part[1])
            }
            num.toString().padStart(originalLength, '0')
        }.toLong()
    }

}
