package cn.imkarl.core.common.security

import cn.imkarl.core.common.encode.EncodeUtils
import cn.imkarl.core.common.file.closeQuietly
import cn.imkarl.core.common.log.LogUtils
import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 加解密相关工具类
 * @author imkarl
 */
object EncryptUtils {

    /**
     * MD5加密
     *
     * @param data 待加密的字符串
     * @return 32位MD5校验码
     */
    fun md5(data: String?): String? {
        if (data == null) {
            return null
        }
        return md5(data.toByteArray())
    }

    /**
     * MD5加密
     *
     * @param data 待加密的字节数组
     * @return 32位MD5校验码
     */
    fun md5(data: ByteArray?): String? {
        if (data == null) {
            return null
        }
        return EncodeUtils.bytesToHexString(hashTemplate("MD5", data))
    }

    /**
     * MD5加密
     *
     * @param file 待加密的文件
     * @return 32位MD5校验码
     */
    fun md5(file: File?): String? {
        if (file == null) {
            return null
        }
        var fis: FileInputStream? = null
        val digestInputStream: DigestInputStream
        try {
            fis = FileInputStream(file)
            var md = MessageDigest.getInstance("MD5")
            digestInputStream = DigestInputStream(fis, md)
            val buffer = ByteArray(256 * 1024)
            while (true) {
                if (digestInputStream.read(buffer) <= 0) {
                    break
                }
            }
            md = digestInputStream.messageDigest
            return EncodeUtils.bytesToHexString(md.digest())
        } catch (e: Exception) {
            LogUtils.e(e)
            return null
        } finally {
            fis.closeQuietly()
        }
    }

    /**
     * SHA1加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha1(data: String?): String? {
        if (data == null) {
            return null
        }
        return sha1(data.toByteArray())
    }

    /**
     * SHA1加密
     *
     * @param data 明文
     * @return 密文
     */
    fun sha1(data: ByteArray?): String? {
        if (data == null) {
            return null
        }
        return EncodeUtils.bytesToHexString(hashTemplate("SHA1", data))
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5ToString(key: String, data: String?): String? {
        if (data == null) {
            return null
        }
        return hmacMD5ToString(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5ToString(key: ByteArray, data: ByteArray?): String? {
        if (data == null) {
            return null
        }
        return EncodeUtils.bytesToHexString(hmacMD5(key, data))
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5(key: String, data: String?): ByteArray? {
        if (data == null) {
            return null
        }
        return hmacMD5(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacMD5加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacMD5(key: ByteArray, data: ByteArray?): ByteArray? {
        if (data == null) {
            return null
        }
        return hmacTemplate("HmacMD5", key, data)
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1ToString(key: String, data: String?): String? {
        if (data == null) {
            return null
        }
        return hmacSHA1ToString(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1ToString(key: ByteArray, data: ByteArray?): String? {
        if (data == null) {
            return null
        }
        return EncodeUtils.bytesToHexString(hmacSHA1(key, data))
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1(key: String, data: String?): ByteArray? {
        if (data == null) {
            return null
        }
        return hmacSHA1(key.toByteArray(), data.toByteArray())
    }

    /**
     * HmacSHA1加密
     *
     * @param key  秘钥
     * @param data 明文
     * @return 密文
     */
    fun hmacSHA1(key: ByteArray, data: ByteArray?): ByteArray? {
        if (data == null) {
            return null
        }
        return hmacTemplate("HmacSHA1", key, data)
    }

    /**
     * hash加密模板
     *
     * @param algorithm 加密算法
     * @param data      待加密的数据
     * @return 密文字节数组
     */
    private fun hashTemplate(algorithm: String, data: ByteArray?): ByteArray? {
        if (data == null) {
            return null
        }

        try {
            if (data.size > 0) {
                val md = MessageDigest.getInstance(algorithm)
                md.update(data)
                return md.digest()
            }
        } catch (e: NoSuchAlgorithmException) {
            LogUtils.e(e)
        }

        return null
    }

    /**
     * Hmac加密模板
     *
     * @param algorithm 加密算法
     * @param key       秘钥
     * @param data      待加密的数据
     * @return 密文字节数组
     */
    private fun hmacTemplate(algorithm: String, key: ByteArray, data: ByteArray?): ByteArray? {
        if (data == null) {
            return null
        }

        try {
            if (key.size > 0 && data.size > 0) {
                val mac = Mac.getInstance(algorithm)
                val secretKey = SecretKeySpec(key, algorithm)
                mac.init(secretKey)
                return mac.doFinal(data)
            }
        } catch (e: InvalidKeyException) {
            LogUtils.e(e)
        } catch (e: NoSuchAlgorithmException) {
            LogUtils.e(e)
        }

        return null
    }

}
