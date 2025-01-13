package cn.imkarl.core.common.io.compress

import cn.imkarl.core.common.io.LimitedInputStream
import cn.imkarl.core.common.io.closeQuietly
import cn.imkarl.core.common.io.file.FileUtils
import cn.imkarl.core.common.platform.Platform
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * Zip文件或流读取器，一般用于Zip文件解压
 */
class ZipReader : Closeable {
    private var zipFile: ZipFile? = null
    private var `in`: ZipInputStream? = null

    /**
     * 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     */
    private var maxSizeDiff: Int = DEFAULT_MAX_SIZE_DIFF

    /**
     * 构造
     *
     * @param zipFile 读取的的Zip文件
     * @param charset 编码
     */
    constructor(zipFile: File, charset: Charset = Charsets.UTF_8) {
        this.zipFile = ZipFile(zipFile, charset)
    }

    /**
     * 构造
     *
     * @param zipFile 读取的的Zip文件
     */
    constructor(zipFile: ZipFile) {
        this.zipFile = zipFile
    }

    /**
     * 构造
     *
     * @param in      读取的的Zip文件流
     * @param charset 编码
     */
    constructor(`in`: InputStream, charset: Charset = Charsets.UTF_8) {
        this.`in` = ZipInputStream(`in`, charset)
    }

    /**
     * 构造
     *
     * @param zin 读取的的Zip文件流
     */
    constructor(zin: ZipInputStream) {
        this.`in` = zin
    }

    /**
     * 设置检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     *
     * @param maxSizeDiff 检查ZipBomb文件差异倍数，-1表示不检查ZipBomb
     * @return this
     */
    fun setMaxSizeDiff(maxSizeDiff: Int): ZipReader {
        this.maxSizeDiff = maxSizeDiff
        return this
    }

    /**
     * 获取指定路径的文件流<br></br>
     * 如果是文件模式，则直接获取Entry对应的流，如果是流模式，则遍历entry后，找到对应流返回
     *
     * @param path 路径
     * @return 文件流
     */
    fun get(path: String): InputStream? {
        if (null != this.zipFile) {
            val zipFile = this.zipFile
            val entry = zipFile!!.getEntry(path)
            if (null != entry) {
                return LimitedInputStream(zipFile.getInputStream(entry), entry.size)
            }
        } else {
            var zipEntry: ZipEntry?
            while (null != (`in`!!.getNextEntry().also { zipEntry = it })) {
                if (zipEntry!!.getName() == path) {
                    return this.`in`
                }
            }
        }

        return null
    }

    /**
     * 解压到指定目录中
     *
     * @param outFile     解压到的目录
     * @param entryFilter 过滤器，排除不需要的文件
     * @return 解压的目录
     */
    fun readTo(outFile: File, entryFilter: ((ZipEntry) -> Boolean)? = null): File? {
        read { zipEntry ->
            if (null == entryFilter || entryFilter.invoke(zipEntry)) {
                //gitee issue #I4ZDQI
                var path = zipEntry.getName()
                if (Platform.osType == Platform.OSType.WINDOWS) {
                    // Win系统下
                    path = path.replace("*", "_")
                }
                // FileUtil.file会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
                val outItemFile: File = file(outFile, path)
                if (zipEntry.isDirectory()) {
                    // 目录
                    outItemFile.mkdirs()
                } else {
                    val `in`: InputStream?
                    if (null != this.zipFile) {
                        `in` = LimitedInputStream(this.zipFile!!.getInputStream(zipEntry), zipEntry.size)
                    } else {
                        `in` = this.`in`
                    }
                    // 文件
                    FileUtils.createNewFile(outItemFile)
                    outItemFile.outputStream().use {
                        `in`?.copyTo(it)
                    }
                }
            }
        }
        return outFile
    }

    /**
     * 限制解压后文件大小
     *
     * @param outFile 解压到的目录
     * @return 解压的目录
     */
    fun unzip(outFile: File): Boolean {
        if (outFile.exists() && outFile.isFile()) {
            throw IllegalArgumentException("Target path [${outFile.absolutePath}] exist!")
        }

        try {
            this.readTo(outFile)
            return true
        } finally {
            this.closeQuietly()
        }
        return false
    }

    /**
     * 创建File对象<br>
     * 根据的路径构建文件，在Win下直接构建，在Linux下拆分路径单独构建
     * 此方法会检查slip漏洞，漏洞说明见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parent 父文件对象
     * @param path   文件路径
     * @return File
     */
    private fun file(parent: File, path: String): File {
        if (path.isBlank()) {
            throw NullPointerException("File path is blank!")
        }
        return checkSlip(parent, File(parent, path))
    }
    /**
     * 检查父完整路径是否为自路径的前半部分，如果不是说明不是子路径，可能存在slip注入。
     * <p>
     * 见http://blog.nsfocus.net/zip-slip-2/
     *
     * @param parentFile 父文件或目录
     * @param file       子文件或目录
     * @return 子文件或目录
     * @throws IllegalArgumentException 检查创建的子文件不在父目录中抛出此异常
     */
    private fun checkSlip(parentFile: File, file: File): File {
        if (!isSub(parentFile, file)) {
            throw IllegalArgumentException("New file is outside of the parent dir: " + file.getName())
        }
        return file
    }
    /**
     * 判断给定的目录是否为给定文件或文件夹的子目录
     *
     * @param parent 父目录
     * @param sub    子目录
     * @return 子目录是否为父目录的子目录
     */
    private fun isSub(parent: File, sub: File): Boolean {
        return isSub(parent.toPath(), sub.toPath());
    }
    /**
     * 判断给定的目录是否为给定文件或文件夹的子目录
     *
     * @param parent 父目录
     * @param sub    子目录
     * @return 子目录是否为父目录的子目录
     */
    private fun isSub(parent: Path, sub: Path): Boolean {
        return toAbsNormal(sub).startsWith(toAbsNormal(parent));
    }

    /**
     * 将Path路径转换为标准的绝对路径
     *
     * @param path 文件或目录Path
     * @return 转换后的Path
     */
    private fun toAbsNormal(path: Path): Path {
        return path.toAbsolutePath().normalize()
    }


    /**
     * 读取并处理Zip文件中的每一个[ZipEntry]
     *
     * @param consumer [ZipEntry]处理器
     */
    fun read(consumer: (ZipEntry) -> Unit): ZipReader {
        if (null != this.zipFile) {
            readFromZipFile(consumer)
        } else {
            readFromStream(consumer)
        }
        return this
    }

    override fun close() {
        if (null != this.zipFile) {
            this.zipFile?.closeQuietly()
        } else {
            this.`in`?.closeQuietly()
        }
    }

    /**
     * 读取并处理Zip文件中的每一个[ZipEntry]
     *
     * @param consumer [ZipEntry]处理器
     */
    private fun readFromZipFile(consumer: (ZipEntry) -> Unit) {
        val em = zipFile!!.entries()
        while (em.hasMoreElements()) {
            checkZipBomb(em.nextElement())?.let { consumer.invoke(it) }
        }
    }

    /**
     * 读取并处理Zip流中的每一个[ZipEntry]
     *
     * @param consumer [ZipEntry]处理器
     * @throws IORuntimeException IO异常
     */
    private fun readFromStream(consumer: (ZipEntry) -> Unit) {
        var zipEntry: ZipEntry?
        while (null != (`in`!!.getNextEntry().also { zipEntry = it })) {
            zipEntry?.let { consumer.invoke(it) }
            // 检查ZipBomb放在读取内容之后，以便entry中的信息正常读取
            checkZipBomb(zipEntry)
        }
    }

    /**
     * 检查Zip bomb漏洞
     *
     * @param entry [ZipEntry]
     * @return 检查后的{@link ZipEntry}
     */
    private fun checkZipBomb(entry: ZipEntry?): ZipEntry? {
        if (null == entry) {
            return null
        }
        if (maxSizeDiff < 0 || entry.isDirectory()) {
            // 目录不检查
            return entry
        }

        val compressedSize = entry.getCompressedSize()
        val uncompressedSize = entry.getSize()
        if (compressedSize < 0 || uncompressedSize < 0 ||  // 默认压缩比例是100倍，一旦发现压缩率超过这个阈值，被认为是Zip bomb
            compressedSize * maxSizeDiff < uncompressedSize
        ) {
            throw IllegalStateException(
                "Zip bomb attack detected, invalid sizes: compressed ${compressedSize}, uncompressed ${uncompressedSize}, name ${entry.getName()}"
            )
        }
        return entry
    }

    companion object {
        // size of uncompressed zip entry shouldn't be bigger of compressed in MAX_SIZE_DIFF times
        private const val DEFAULT_MAX_SIZE_DIFF = 100

        /**
         * 创建ZipReader
         *
         * @param zipFile 生成的Zip文件
         * @param charset 编码
         * @return ZipReader
         */
        fun of(zipFile: File, charset: Charset = Charsets.UTF_8): ZipReader {
            return ZipReader(zipFile, charset)
        }

        /**
         * 创建ZipReader
         *
         * @param stream  Zip输入的流，一般为输入文件流
         * @param charset 编码
         * @return ZipReader
         */
        fun of(stream: InputStream, charset: Charset = Charsets.UTF_8): ZipReader {
            return ZipReader(stream, charset)
        }
    }
}
