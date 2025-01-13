package cn.imkarl.core.common.http

import io.ktor.http.*
import io.ktor.util.*

object ContentTypes {

    /**
     * 根据文件拓展名，获取所有对应的内容类型
     * @return 如果匹配不到，则返回 listOf(ContentType.Any)
     */
    fun fromFileExtension(extension: String?): List<ContentType> {
        if (extension.isNullOrEmpty()) {
            return listOf(ContentType.Any)
        }

        val contentType = when (extension.lowercase()) {
            "plain", "ini" -> ContentType.Text.Plain
            "md" -> ContentType.Text.Markdown

            "apng" -> ContentType.Image.Any

            "m4b", "m4p" -> ContentType.Video.MPEG

            "flac" -> ContentType.Audio.Any

            "proto" -> ContentType.Application.ProtoBuf

            else -> null
        }
        if (contentType != null) {
            return listOf(contentType)
        }

        return ContentType.fromFileExtension(extension).ifEmpty { listOf(ContentType.Any) }
    }

    fun fromFilePath(path: String): List<ContentType> {
        val slashIndex = path.lastIndexOfAny("/\\".toCharArray())
        val index = path.indexOf('.', startIndex = slashIndex + 1)
        if (index == -1) {
            return listOf(ContentType.Any)
        }
        return fromFileExtension(path.substring(index + 1))
    }

}



val ContentType.Image.Webp get() = ContentType("image", "webp")
val ContentType.Text.Markdown get() = ContentType("text", "markdown")

/**
 * 判断是否为图片类型
 */
fun ContentType.isImage(): Boolean {
    return this.contentType == "image"
}

/**
 * 判断是否为视频类型
 */
fun ContentType.isVideo(): Boolean {
    return this.contentType == "video"
}

/**
 * 判断是否为音频类型
 */
fun ContentType.isAudio(): Boolean {
    return this.contentType == "audio"
}

/**
 * 判断是否为字体类型
 */
fun ContentType.isFont(): Boolean {
    return this.contentType == "font"
}
