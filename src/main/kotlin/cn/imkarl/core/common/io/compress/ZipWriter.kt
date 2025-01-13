package cn.imkarl.core.common.io.compress

import cn.imkarl.core.common.io.closeQuietly
import cn.imkarl.core.common.io.file.FileUtils
import java.io.Closeable
import java.io.File
import java.io.FileFilter
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipEntry

class ZipWriter(val out: ZipOutputStream): Closeable {

    private var zipFile: File? = null

    constructor(zipFile: File, charset: Charset = Charsets.UTF_8): this(ZipOutputStream(zipFile.outputStream())) {
        this.zipFile = zipFile
        this.out.encoding = charset.name()
    }

    /**
     * 递归压缩文件夹或压缩文件<br>
     * srcRootDir决定了路径截取的位置，例如：<br>
     * file的路径为d:/a/b/c/d.txt，srcRootDir为d:/a/b，则压缩后的文件与目录为结构为c/d.txt
     *
     * @param srcRootDir 被压缩的文件夹根目录
     * @param files      当前递归压缩的文件或目录对象
     */
    fun write(srcRootDir: File, vararg files: File): ZipWriter {
        // 获取文件相对于压缩文件夹根目录的子路径
        val rootPath = srcRootDir.canonicalFile.absolutePath
        files.forEach { file ->
            var subPath = file.canonicalFile.absolutePath.removeSuffix("/")
            subPath = subPath.removePrefix(rootPath.removeSuffix("/")).removePrefix("/")
            write("", file)
        }
        return this
    }

    /**
     * 递归压缩文件夹或压缩文件<br>
     * srcRootDir决定了路径截取的位置，例如：<br>
     * file的路径为d:/a/b/c/d.txt，srcRootDir为d:/a/b，则压缩后的文件与目录为结构为c/d.txt
     *
     * @param subPath    被压缩的文件夹根目录
     * @param file       当前递归压缩的文件或目录对象
     * @param filter     文件过滤器，通过实现此接口，自定义要过滤的文件（过滤掉哪些文件或文件夹不加入压缩），{@code null}表示不过滤
     */
    fun write(subPath: String, file: File, filter: FileFilter? = null): ZipWriter {
        if (null != filter && !filter.accept(file)) {
            return this
        }

        if (file.isDirectory()) {
            // 如果是目录，则压缩压缩目录中的文件或子目录
            val files = file.listFiles()
            if (files.isEmpty()) {
                // 加入目录，只有空目录时才加入目录，非空时会在创建文件时自动添加父级目录
                putEntry("${subPath.removePrefix("/")}/${file.name}/".removePrefix("/"), null)
            } else {
                // 压缩目录下的子文件或目录
                files.forEach { childFile ->
                    write("${subPath.removePrefix("/")}/${file.name}", childFile, filter)
                }
            }
        } else {
            // issue#IAGYDG 检查加入的文件是否为压缩结果文件本身，避免死循环
            if (zipFile == file) {
                return this
            }

            // 如果是文件或其它符号，则直接压缩该文件
            putEntry("${subPath.removePrefix("/")}/${file.name}".removePrefix("/"), file)
        }
        return this;
    }

    /**
     * 添加文件流到压缩包，添加后关闭输入文件流<br>
     * 如果输入流为null，则只创建空目录
     *
     * @param path   压缩包内的路径, null和""表示根目录下
     * @param file   需要压缩的文件
     */
    private fun putEntry(path: String, file: File?): ZipWriter {
        var entryName: String = path.removeSuffix(File.separator)
        if (file == null || path.endsWith("/")) {
            entryName = entryName + File.separator
        }
        val entry = ZipEntry("测试"+entryName)
        if (file != null) {
            entry.time = file.lastModified()
            entry.size = FileUtils.size(file)
        } else {
            entry.time = System.currentTimeMillis()
            entry.size = 0
        }

        putEntry(entry, file?.inputStream())
        return this
    }
    /**
     * 添加文件流到压缩包，添加后关闭输入文件流<br>
     * 如果输入流为null，则只创建空目录
     *
     * @param stream 需要压缩的输入流，使用完后自动关闭，null表示加入空目录
     */
    private fun putEntry(entry: ZipEntry, stream: InputStream?): ZipWriter {
        try {
            out.putNextEntry(entry)
            stream?.copyTo(out)
            out.closeEntry()
        } finally {
            stream.closeQuietly()
        }

        out.flush()
        return this
    }


    override fun close() {
        try {
            out.flush()
        } finally {
            out.closeQuietly()
        }
    }

}