package cn.imkarl.core.common.io.file

import cn.imkarl.core.common.security.EncryptUtils
import cn.imkarl.core.common.security.md5
import cn.imkarl.core.common.security.sha1
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes

/**
 * 是否为链接文件
 */
val File.isLinkFile: Boolean
    get() {
        if (!this.isLink) {
            return false
        }
        return !Files.isDirectory(this.toPath()) && this.exists()
    }

/**
 * 是否为链接文件
 */
val File.isLinkDirectory: Boolean
    get() {
        if (!this.isLink) {
            return false
        }
        return Files.isDirectory(this.toPath())
    }

/**
 * 是否为链接
 */
val File.isLink: Boolean
    get() {
        return Files.isSymbolicLink(this.toPath())
    }


/**
 * 获取链接的目标文件
 */
val File.linkTargetFile: File
    get() {
        return when {
            this.isLinkDirectory -> this.absoluteFile
            this.isLinkFile -> this.canonicalFile
            else -> this
        }
    }




/**
 * 文件创建时间
 */
val File.attributes: BasicFileAttributes?
    get() {
        return try {
            Files.readAttributes(this.toPath(), BasicFileAttributes::class.java)
        } catch (_: Throwable) {
            null
        }
    }


/**
 * 文件创建时间
 */
fun File.creationTime(): Long {
    return attributes?.creationTime()?.toMillis() ?: -1
}

/**
 * 计算 sha1 值
 * @return 文件的 sha1 摘要【如果获取失败，返回空字符串】
 */
fun File.sha1(): String {
    if (this.isDirectory || this.isLinkDirectory || !this.exists() || !this.canRead()) {
        return ""
    }
    return try {
        this.readBytes().sha1()
    } catch (_: Throwable) {
        ""
    }
}

/**
 * 计算 MD5 值
 * @return 32位MD5校验码【如果获取失败，返回空字符串】
 */
fun File.md5(): String {
    if (this.isDirectory || this.isLinkDirectory || !this.exists() || !this.canRead()) {
        return ""
    }
    return try {
        EncryptUtils.md5(this)!!
    } catch (_: Throwable) {
        ""
    }
}


fun File.outputStream(append: Boolean = false): FileOutputStream {
    return FileOutputStream(this, append)
}
