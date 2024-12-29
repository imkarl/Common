package cn.imkarl.core.common.security

import cn.imkarl.core.common.encode.bytesToHexString
import java.io.File

/**
 * 计算MD5值
 * @return 32位MD5校验码
 */
fun String.md5(): String {
    return EncryptUtils.md5(this)!!
}

/**
 * 计算 MD5 值
 * @return 32位MD5校验码
 */
fun ByteArray.md5(): String {
    return EncryptUtils.md5(this)!!
}



/**
 * 计算 SHA1 值
 * @return 32位MD5校验码
 */
fun String.sha1(): String {
    return EncryptUtils.sha1(this)!!
}

/**
 * 计算 SHA1 值
 * @return 32位MD5校验码
 */
fun ByteArray.sha1(): String {
    return EncryptUtils.sha1(this)!!
}



/**
 * 计算 HmacMD5 值
 * @return 32位MD5校验码
 */
fun String.hmacMD5(key: String): String {
    return EncryptUtils.hmacMD5(key, this)!!.bytesToHexString()
}

/**
 * 计算 HmacMD5 值
 * @return 32位MD5校验码
 */
fun ByteArray.hmacMD5(key: String): String {
    return EncryptUtils.hmacMD5(key.toByteArray(), this)!!.bytesToHexString()
}



/**
 * 计算 HmacSHA1 值
 * @return 32位MD5校验码
 */
fun String.hmacSHA1(key: String): String {
    return EncryptUtils.hmacSHA1(key, this)!!.bytesToHexString()
}

/**
 * 计算 HmacSHA1 值
 * @return 32位MD5校验码
 */
fun ByteArray.hmacSHA1(key: String): String {
    return EncryptUtils.hmacSHA1(key.toByteArray(), this)!!.bytesToHexString()
}



/**
 * 字符 DES 加密
 */
fun String.encryptDES(key: String): ByteArray {
    return EncryptUtils.encryptDES(this, key)
}

/**
 * 字符 DES 解密
 */
fun ByteArray.decryptDES(key: String): String {
    return EncryptUtils.decryptDES(this, key)
}



/**
 * 字符 AES 加密
 */
fun String.encryptAES(key: String): ByteArray {
    return EncryptUtils.encryptAES(this, key)
}

/**
 * 字符 AES 解密
 */
fun ByteArray.decryptAES(key: String): String {
    return EncryptUtils.decryptAES(this, key)
}



/**
 * 字符 RSA 加密
 */
fun String.encryptRSA(publicKey: String): ByteArray {
    return EncryptUtils.encryptRSA(this, publicKey)
}

/**
 * 字符 RSA 解密
 */
fun ByteArray.decryptRSA(privateKey: String): String {
    return EncryptUtils.decryptRSA(this, privateKey)
}

