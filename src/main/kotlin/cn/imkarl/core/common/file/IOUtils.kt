package cn.imkarl.core.common.file

import cn.imkarl.core.common.log.LogUtils
import java.io.*
import java.nio.charset.Charset

/**
 * IO相关工具类
 * @author imkarl
 */
object IOUtils {

    private const val BUFFER_SIZE = 4 * 1024

    /**
     * 复制数据
     */
    @JvmStatic
    fun copy(source: InputStream, target: OutputStream): Long {
        try {
            var total: Long = 0

            var length: Int
            val buffer = ByteArray(BUFFER_SIZE)
            length = source.read(buffer)
            while (length >= 0) {
                target.write(buffer, 0, length)
                total += length.toLong()
                length = source.read(buffer)
            }
            target.flush()

            return total
        } catch (e: IOException) {
            LogUtils.w(e)
        }
        return -1
    }

    @JvmStatic
    fun copyToString(source: InputStream, charset: Charset = Charsets.UTF_8): String {
        val out = ByteArrayOutputStream()
        val bufferedInputStream = BufferedInputStream(source)
        copy(bufferedInputStream, out)
        return out.toString(charset.name())
    }

}


/**
 * 静默关闭
 */
fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (throwable: Throwable) {
    }
}
