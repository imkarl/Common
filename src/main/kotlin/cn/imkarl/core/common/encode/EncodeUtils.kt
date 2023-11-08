package cn.imkarl.core.common.encode

import cn.imkarl.core.common.log.LogUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

/**
 * 编解码相关工具类
 * @author imkarl
 */
object EncodeUtils {

    val ISO_8859_1 = Charset.forName("ISO-8859-1")
    val UTF_8 = Charset.forName("UTF-8")
    val UTF_16BE = Charset.forName("UTF-16BE")
    val UTF_16LE = Charset.forName("UTF-16LE")
    val UTF_16 = Charset.forName("UTF-16")

    private val PATTERN_UNICODE = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")


    /**
     * Base64编码
     *
     * @param data 待编码的字符
     * @return 编码结果
     */
    @JvmStatic
    fun encodeBase64(data: ByteArray): String? {
        return try {
            Base64.getEncoder().encodeToString(data)
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
    @JvmStatic
    fun decodeBase64(data: String): ByteArray? {
        return try {
            Base64.getDecoder().decode(data)
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
    @JvmStatic
    fun encodeUrl(data: String, charset: Charset = UTF_8): String {
        return try {
            URLEncoder.encode(data, charset.name())
        } catch (e: UnsupportedEncodingException) {
            data
        }
    }

    /**
     * URL解码
     *
     * @param data 待解码的字符
     * @param charset 字符集
     * @return 解码结果，若解码失败则直接将data原样返回
     */
    @JvmStatic
    fun decodeUrl(data: String, charset: Charset = UTF_8): String {
        return try {
            URLDecoder.decode(data, charset.name())
        } catch (e: UnsupportedEncodingException) {
            data
        }
    }


    /**
     * Unicode编码
     *
     * @param data 待编码的字符
     * @return 编码结果，若编码失败则直接将data原样返回
     */
    @JvmStatic
    fun encodeUnicode(data: String?): String? {
        if (data == null) {
            return null
        }
        if (data.isBlank()) {
            return ""
        }

        val unicodeBytes = StringBuilder()
        for (ch in data.toCharArray()) {
            if (ch.code < 10) {
                unicodeBytes.append("\\u000").append(Integer.toHexString(ch.code))
                continue
            }

            if (Character.UnicodeBlock.of(ch) === Character.UnicodeBlock.BASIC_LATIN) {
                // 英文及数字等
                unicodeBytes.append(ch)
            } else {
                // to Unicode
                val hex = Integer.toHexString(ch.toInt())
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
    @JvmStatic
    fun decodeUnicode(data: String?): String? {
        if (data == null) {
            return null
        }
        if (!data.contains("\\u")) {
            return data
        }

        val buf = StringBuffer()
        val matcher = PATTERN_UNICODE.matcher(data)
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
    @JvmStatic
    fun bytesToHexString(data: ByteArray?): String? {
        if (data == null || data.isEmpty()) {
            return null
        }

        val hexBuilder = StringBuilder()
        for (b in data) {
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
    fun hexStringToBytes(str: String?): ByteArray? {
        if (str.isNullOrBlank()) {
            return null
        }

        val length = str.length / 2
        val bytes = ByteArray(length)
        for (i in 0 until length) {
            bytes[i] = Integer.valueOf(str.substring(i * 2, i * 2 + 2), 16).toByte()
        }
        return bytes
    }

}