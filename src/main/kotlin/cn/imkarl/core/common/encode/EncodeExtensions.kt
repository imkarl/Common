package cn.imkarl.core.common.encode

import cn.imkarl.core.common.log.LogUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern


private val PATTERN_UNICODE = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")

/**
 * Base64编码
 *
 * @param data 待编码的字符
 * @return 编码结果
 */
fun ByteArray.encodeBase64(): String? {
    return try {
        Base64.getEncoder().encodeToString(this)
    } catch (e: Exception) {
        LogUtils.e(e)
        null
    }
}

/**
 * Base64解码
 *
 * @param data 待解码的字符
 * @return 解码结果
 */
fun String.decodeBase64(): ByteArray? {
    return try {
        Base64.getDecoder().decode(this)
    } catch (e: Exception) {
        LogUtils.e(e)
        null
    }
}

/**
 * URL编码
 *
 * @param data 待编码的字符
 * @param charset 字符集
 * @return 编码结果，若编码失败则直接将data原样返回
 */
fun String.encodeUrl(charset: Charset = Charsets.UTF_8): String {
    try {
        return URLEncoder.encode(this, charset.name())
    } catch (e: UnsupportedEncodingException) {
        return this
    }
}

/**
 * URL解码
 *
 * @param data 待解码的字符
 * @param charset 字符集
 * @return 解码结果，若解码失败则直接将data原样返回
 */
fun String.decodeUrl(charset: Charset = Charsets.UTF_8): String {
    return try {
        URLDecoder.decode(this, charset.name())
    } catch (e: UnsupportedEncodingException) {
        this
    }
}


/**
 * Unicode编码
 *
 * @param data 待编码的字符
 * @return 编码结果，若编码失败则直接将data原样返回
 */
fun String.encodeUnicode(): String {
    if (this.isBlank()) {
        return this
    }

    val unicodeBytes = StringBuilder()
    for (ch in this.toCharArray()) {
        if (ch.code < 10) {
            unicodeBytes.append("\\u000").append(Integer.toHexString(ch.code))
            continue
        }

        if (Character.UnicodeBlock.of(ch) === Character.UnicodeBlock.BASIC_LATIN) {
            // 英文及数字等
            unicodeBytes.append(ch)
        } else {
            // to Unicode
            val hex = Integer.toHexString(ch.code)
            if (hex.length == 1) {
                unicodeBytes.append("\\u000").append(hex)
            } else if (hex.length == 2) {
                unicodeBytes.append("\\u00").append(hex)
            } else if (hex.length == 3) {
                unicodeBytes.append("\\u0").append(hex)
            } else if (hex.length == 4) {
                unicodeBytes.append("\\u").append(hex)
            }
        }
    }
    return unicodeBytes.toString()
}

/**
 * Unicode解码
 *
 * @param data 待解码的字符
 * @return 解码结果，若解码失败则直接将data原样返回
 */
fun String.decodeUnicode(): String {
    if (this.isBlank() || !this.contains("\\u")) {
        return this
    }

    val buf = StringBuffer()
    val matcher = PATTERN_UNICODE.matcher(this)
    while (matcher.find()) {
        try {
            matcher.appendReplacement(buf, "")
            buf.appendCodePoint(Integer.parseInt(matcher.group(1), 16))
        } catch (ignored: NumberFormatException) {
        }
    }
    matcher.appendTail(buf)
    return buf.toString()
}


/**
 * 字节数组转16进制字符串
 *
 * @param data 待转换的字节数组
 * @return 16进制字符串
 */
fun ByteArray.bytesToHexString(): String {
    if (this.isEmpty()) {
        return ""
    }

    val hexBuilder = StringBuilder()
    for (b in this) {
        val hv = Integer.toHexString(b.toInt() and 0xFF)
        if (hv.length < 2) {
            hexBuilder.append(0)
        }
        hexBuilder.append(hv)
    }
    return hexBuilder.toString()
}


/**
 * 十六进制字符串转bytes
 *
 * @param str 16进制字符串
 * @return 字节数组
 */
fun String.hexStringToBytes(): ByteArray {
    val length = this.length / 2
    val bytes = ByteArray(length)
    for (i in 0 until length) {
        bytes[i] = Integer.valueOf(this.substring(i * 2, i * 2 + 2), 16).toByte()
    }
    return bytes
}
