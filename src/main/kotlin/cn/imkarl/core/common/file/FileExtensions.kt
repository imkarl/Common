package cn.imkarl.core.common.file

import java.io.File
import java.nio.file.Files

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

