package cn.imkarl.core.common.encode

import cn.imkarl.core.common.log.LogUtils
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

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


    /**
     * Base64编码
     *
     * @param data 待编码的字符
     * @return 编码结果
     */
    @JvmStatic
    fun encodeBase64(data: ByteArray): String? {
        try {
            return Base64.getEncoder().encodeToString(data)
        } catch (e: Exception) {
            LogUtils.e(e)
            return null
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
        try {
            return Base64.getDecoder().decode(data)
        } catch (e: Exception) {
            LogUtils.e(e)
            return null
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
        try {
            return URLEncoder.encode(data, charset.name())
        } catch (e: UnsupportedEncodingException) {
            return data
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
        try {
            return URLDecoder.decode(data, charset.name())
        } catch (e: UnsupportedEncodingException) {
            return data
        }
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

        val length = str!!.length / 2
        val bytes = ByteArray(length)
        for (i in 0 until length) {
            bytes[i] = Integer.valueOf(str.substring(i * 2, i * 2 + 2), 16).toByte()
        }
        return bytes
    }

}