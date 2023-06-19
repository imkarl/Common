package cn.imkarl.core.common.file

import cn.imkarl.core.common.app.AppUtils
import cn.imkarl.core.common.log.LogUtils
import java.io.*
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.jar.JarFile

/**
 * 文件相关工具类
 * @author imkarl
 */
object FileUtils {

    /**
     * 源码类型
     */
    enum class SourceType {
        Java, Kotlin
    }


    private const val FILE_SIZE_KB = 1024L
    private const val FILE_SIZE_MB = 1024 * FILE_SIZE_KB
    private const val FILE_SIZE_GB = 1024 * FILE_SIZE_MB

    private val fileSizeFormat = DecimalFormat("0.00")


    /**
     * 获取类所在的根目录
     */
    @JvmStatic
    fun getClassRootDir(sourceType: SourceType? = null): File {
        val classLoader = FileUtils::class.java.classLoader
        if (AppUtils.isJarRun) {
            val codeSourcePath = FileUtils::class.java.protectionDomain.codeSource.location.path.removePrefix("file:")
            var jarFile = File(codeSourcePath)
            while (!jarFile.exists()) {
                if (jarFile == jarFile.parentFile) {
                    jarFile = File(codeSourcePath)
                    break
                }
                jarFile = jarFile.parentFile
                if (jarFile.absolutePath.endsWith("!")) {
                    jarFile = File(jarFile.absolutePath.removeSuffix("!"))
                }
            }
            return jarFile
        }

        var classRootPath = classLoader.getResource("")!!.file
        if (classRootPath.endsWith("/build/classes/java/main/")) {
            classRootPath = classRootPath.removeSuffix("java/main/")
        } else if (classRootPath.endsWith("/build/classes/kotlin/main/")) {
            classRootPath = classRootPath.removeSuffix("kotlin/main/")
        }
        return when (sourceType) {
            SourceType.Kotlin -> File(classRootPath, "kotlin/main")
            SourceType.Java -> File(classRootPath, "java/main")
            else -> File(classRootPath)
        }
    }

    /**
     * 获取资源文件所在的根目录
     */
    @JvmStatic
    fun getResourceRootDir(): File {
        if (AppUtils.isJarRun) {
            return getClassRootDir()
        }

        var resourceRootFile = File(getClassRootDir().parent, "resources")
        File(resourceRootFile, "main").let {
            if (it.exists()) {
                resourceRootFile = it
            }
        }
        return resourceRootFile
    }

    /**
     * 获取资源文件
     */
    @JvmStatic
    fun getResourceFile(filePath: String): InputStream {
        val classResourceDir = getResourceRootDir()
        return if (AppUtils.isJarRun) {
            JarFile(classResourceDir).let {
                it.getInputStream(it.getJarEntry(filePath))
            }
        } else {
            try {
                File(classResourceDir, filePath).inputStream()
            } catch (throwable: FileNotFoundException) {
                File(File(classResourceDir.parentFile, "classes"), filePath).inputStream()
            }
        }
    }

    /**
     * 获取用户默认根目录
     */
    @JvmStatic
    fun getUserHomeDir(): File {
        return File(System.getProperty("user.home"))
    }

    /**
     * 获取当前运行目录
     */
    @JvmStatic
    fun getCurrentDir(): File {
        return File(System.getProperty("user.dir"))
    }

    /**
     * 获取APP数据存储根目录
     */
    @JvmStatic
    fun getAppStorageRootDir(): File {
        return File(getUserHomeDir(), ".AppData/${AppUtils.packageName.removePrefix("/")}")
    }

    /**
     * 获取绝对路径的File对象
     */
    @JvmStatic
    fun getAbsoluteFile(path: String): File {
        val absolutePath = path.trim()
        val file = when {
            absolutePath == "~" -> getUserHomeDir()
            absolutePath.startsWith("~/") -> File(getUserHomeDir(), absolutePath.removePrefix("~/"))
            absolutePath.startsWith("~\\") -> File(getUserHomeDir(), absolutePath.removePrefix("~\\"))

            absolutePath == "." -> getCurrentDir()
            absolutePath.startsWith("./") -> File(getCurrentDir(), absolutePath.removePrefix("./"))
            absolutePath.startsWith(".\\") -> File(getCurrentDir(), absolutePath.removePrefix(".\\"))
            !absolutePath.startsWith("/") -> File(getCurrentDir(), absolutePath)

            else -> File(absolutePath)
        }
        return file.absoluteFile
    }


    /**
     * 计算文件或文件夹的大小
     *
     * @return bytes, 字节大小
     */
    @JvmStatic
    fun size(file: File): Long {
        if (!file.exists()) {
            return 0
        }

        if (file.isDirectory) {
            val children = file.listFiles() ?: return 0

            var sum: Long = 0
            for (child in children) {
                sum += size(child)
            }
            return sum
        } else {
            return file.length()
        }
    }

    /**
     * 计算文件夹总空间容量，单位byte
     */
    @JvmStatic
    fun getTotalSpace(dir: File): Long {
        return if (dir.exists()) {
            dir.totalSpace
        } else {
            0
        }
    }

    /**
     * 计算文件夹剩余空间容量，单位byte
     */
    @JvmStatic
    fun getFreeSpace(dir: File): Long {
        return if (dir.exists()) {
            dir.freeSpace
        } else {
            0
        }
    }

    /**
     * 重命名文件
     *
     * @return 如果目标文件已存在，则先删除（删除失败则返回false）
     */
    @JvmStatic
    fun rename(from: File, to: File?): Boolean {
        if (!from.exists()) {
            LogUtils.w("File not found, 'from' is: $from")
            return false
        }
        if (to == null) {
            LogUtils.w("File not found, 'to' is: $to")
            return false
        }
        deleteFile(to)
        return from.renameTo(to)
    }

    /**
     * 创建文件夹（包括其父目录）
     *
     * @return 如果文件夹已存在，则直接返回true；如果该路径存在同名文件，则直接返回false
     * @see .createNewFile
     */
    @JvmStatic
    fun mkdirs(dir: File?): Boolean {
        if (dir == null) {
            return false
        }
        return if (dir.exists()) {
            dir.isDirectory
        } else dir.mkdirs() || dir.isDirectory
    }

    /**
     * 创建文件（包括其父目录）
     *
     * @return 如果文件已存在，则删除重建
     * @see .mkdirs
     */
    @JvmStatic
    fun createNewFile(file: File): Boolean {
        if (file.exists() && file.isFile) {
            deleteFile(file)
        }

        try {
            if (!mkdirs(file.parentFile)) {
                LogUtils.w("mkdirs 'NewFile' parent failed: " + file.parentFile.absolutePath)
                return false
            }
            return file.createNewFile()
        } catch (e: IOException) {
            LogUtils.w(e)
            return false
        }

    }


    /**
     * 删除文件（不支持文件夹）
     *
     * @return 不存在该文件，则返回true
     * @see .deleteDir 删除文件夹
     */
    @JvmStatic
    fun deleteFile(file: File): Boolean {
        if (!file.exists()) {
            //LogUtils.w("deleteFile 'file' not exist: "+file);
            return true
        }
        if (!file.isFile) {
            LogUtils.w("deleteFile 'file' is not a file")
            return true
        }
        return file.delete() || !file.exists()
    }

    /**
     * 删除文件夹（不支持文件）
     *
     * @return 不存在该文件夹，则返回true
     * @see .deleteFile 删除文件
     */
    @JvmStatic
    fun deleteDir(dir: File): Boolean {
        if (!dir.exists()) {
            LogUtils.w("deleteDir 'dir' not exist: $dir")
            return true
        }
        if (!dir.isDirectory) {
            LogUtils.w("deleteDir 'dir' is not a directory")
            return true
        }

        val files = dir.listFiles()
        if (files == null) {
            LogUtils.w("deleteDir 'dir' not a readable directory: $dir")
            return false
        } else {
            for (file in files) {
                val delete: Boolean
                if (file.isDirectory) {
                    delete = deleteDir(file)
                } else {
                    delete = deleteFile(file)
                }
                if (!delete) {
                    LogUtils.w("delete failed : " + (if (file.isDirectory) "dir " else "file ") + file)
                    return false
                }
            }
        }

        return dir.delete() || !dir.exists()
    }


    /**
     * 复制文件或文件夹（包含空文件和空文件夹）
     *
     * @return 不存在源文件，则返回true
     * @see .deleteDir 删除文件夹
     */
    @JvmStatic
    fun copy(source: File, target: File): Boolean {
        if (!source.exists()) {
            LogUtils.w("copy source not exists : $source to $target")
            return true
        }

        if (source.isDirectory) {
            val mkdir = mkdirs(target)
            if (!mkdir) {
                LogUtils.w("mkdir dir failed : $target")
                return false
            }

            val childs = source.listFiles()
            if (childs == null) {
                LogUtils.w("copy source not a readable directory: $source")
                return false
            }
            if (childs.isEmpty()) {
                return true
            }

            //LogUtils.i("copy dir "+source.getPath()+" to "+target.getPath());
            for (child in childs) {
                // 复制到对应的目标路径
                val copy = copy(child, File(target, child.name))
                if (!copy) {
                    LogUtils.w("copy failed : $source to $target")
                    return false
                }
            }
            return true
        } else if (source.isFile) {
            //LogUtils.i("copy file "+source.getPath()+" to "+target.getPath());
            val targetTempFile = File(target.parentFile, target.name + "" + System.currentTimeMillis())
            if (!copyFile(source, targetTempFile)) {
                LogUtils.w("copy failed : $source to $target")
                return false
            }
            return rename(targetTempFile, target)
        } else {
            LogUtils.w("copy source failed : " + source.path)
            return false
        }
    }

    /**
     * 复制文件
     *
     * @return 不存在源文件，则返回true
     * @see .deleteDir 删除文件夹
     */
    @JvmStatic
    private fun copyFile(source: File, target: File): Boolean {
        if (!source.exists()) {
            LogUtils.i("copyFile source not exists : $source to $target")
            return true
        }

        if (source.length() == 0L) {
            return createNewFile(target)
        }

        val mkdir = mkdirs(target.parentFile)
        if (!mkdir) {
            LogUtils.w("mkdir dir failed : " + target.parentFile)
            return false
        }

        var result = false
        var fileChannel: FileChannel? = null
        var out: FileChannel? = null
        var inStream: FileInputStream? = null
        var outStream: FileOutputStream? = null
        try {
            inStream = FileInputStream(source)
            outStream = FileOutputStream(target)
            fileChannel = inStream.channel
            out = outStream.channel
            val length = fileChannel!!.transferTo(0, fileChannel.size(), out)
            if (length >= 0) {
                result = true
            } else {
                LogUtils.w("copyFile transferTo failed : $source to $target")
                result = false
            }
        } catch (e: IOException) {
            LogUtils.w(e)
        } finally {
            fileChannel.closeQuietly()
            out.closeQuietly()
            inStream.closeQuietly()
            outStream.closeQuietly()
        }
        return result
    }

    /**
     * 复制数据流到文件
     */
    @JvmStatic
    fun copy(stream: InputStream, target: File): Boolean {
        val mkdir = mkdirs(target.parentFile)
        if (!mkdir) {
            return false
        }

        var result = false
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(target)
            IOUtils.copy(stream, fos)
            result = true
        } catch (e: IOException) {
            LogUtils.e(e)
        } finally {
            fos.closeQuietly()
        }
        return result
    }

    @JvmStatic
    fun formatFileSize(size: Long): String {
        if (size > FILE_SIZE_GB * 1.2) {
            return "${fileSizeFormat.format((size * 100 / FILE_SIZE_GB) * 0.01)}GB"
        }
        if (size > FILE_SIZE_MB * 1.2) {
            return "${fileSizeFormat.format((size * 100 / FILE_SIZE_MB) * 0.01)}MB"
        }
        if (size > FILE_SIZE_KB) {
            return "${fileSizeFormat.format((size * 100 / FILE_SIZE_KB) * 0.01)}KB"
        }
        return "${fileSizeFormat.format(size)}B"
    }

}