package cn.imkarl.core.common.io

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.Throws

/**
 * 限制读取最大长度的[FilterInputStream]
 */
class LimitedInputStream(
    stream: InputStream,
    /**
     * 限制最大读取量，单位byte
     */
    private val maxSize: Long
) : FilterInputStream(stream) {
    private var currentPos: Long = 0

    @Throws(IOException::class)
    override fun read(): Int {
        val data = super.read()
        if (data != -1) {
            currentPos++
            checkPos()
        }
        return data
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val count = super.read(b, off, len)
        if (count > 0) {
            currentPos += count.toLong()
            checkPos()
        }
        return count
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val skipped = super.skip(n)
        if (skipped != 0L) {
            currentPos += skipped
            checkPos()
        }
        return skipped
    }

    private fun checkPos() {
        check(currentPos <= maxSize) { "Read limit exceeded" }
    }
}