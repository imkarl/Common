package cn.imkarl.core.common.io

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.*
import java.nio.charset.Charset


/**
 * 复制数据
 */
fun InputStream.copyToString(charset: Charset = Charsets.UTF_8): String {
    val out = ByteArrayOutputStream()
    val bufferedInputStream = BufferedInputStream(this)
    bufferedInputStream.copyTo(out)
    return out.toString(charset.name())
}

/**
 * 复制数据
 */
suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) = withContext(dispatcher) {
    val buffer = ByteArray(bufferSize)
    var bytesCopied = 0L
    var bytesAfterYield = 0L
    while (true) {
        val bytes = withContext(Dispatchers.IO) {
            read(buffer).takeIf { it >= 0 }
        } ?: break
        withContext(Dispatchers.IO) {
            out.write(buffer, 0, bytes)
        }
        if (bytesAfterYield >= yieldSize) {
            yield()
            bytesAfterYield %= yieldSize
        }
        bytesCopied += bytes
        bytesAfterYield += bytes
    }
    return@withContext bytesCopied
}


/**
 * 静默关闭
 */
fun Closeable?.closeQuietly() {
    try {
        this?.close()
    } catch (_: Throwable) {
    }
}
