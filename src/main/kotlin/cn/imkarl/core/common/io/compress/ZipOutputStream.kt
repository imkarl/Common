/*
 * Copyright  2001-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package cn.imkarl.core.common.io.compress

import java.io.*
import java.util.*
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipException

/**
 * Reimplementation of [ java.util.zip.ZipOutputStream][java.util.zip.ZipOutputStream] that does handle the extended
 * functionality of this package, especially internal/external file
 * attributes and extra fields with different layouts for local file
 * data and central directory entries.
 *
 *
 * This class will try to use [ RandomAccessFile][RandomAccessFile] when you know that the output is going to go to a
 * file.
 *
 *
 * If RandomAccessFile cannot be used, this implementation will use
 * a Data Descriptor to store size and CRC information for [ ][.DEFLATED] entries, this means, you don't need to
 * calculate them yourself.  Unfortunately this is not possible for
 * the [STORED][.STORED] method, here setting the CRC and
 * uncompressed size information is required before [ ][.putNextEntry] can be called.
 *
 */
class ZipOutputStream : FilterOutputStream {
    /**
     * Current entry.
     *
     * @since 1.1
     */
    private var entry: ZipEntry? = null

    /**
     * The file comment.
     *
     * @since 1.1
     */
    private var comment = ""

    /**
     * Compression level for next entry.
     *
     * @since 1.1
     */
    private var level = Deflater.DEFAULT_COMPRESSION

    /**
     * Has the compression level changed when compared to the last
     * entry?
     *
     * @since 1.5
     */
    private var hasCompressionLevelChanged = false

    /**
     * Default compression method for next entry.
     *
     * @since 1.1
     */
    private var method = DEFLATED

    /**
     * List of ZipEntries written so far.
     *
     * @since 1.1
     */
    private val entries: Vector<ZipEntry> = Vector<ZipEntry>()

    /**
     * CRC instance to avoid parsing DEFLATED data twice.
     *
     * @since 1.1
     */
    private val crc = CRC32()

    /**
     * Count the bytes written to out.
     *
     * @since 1.1
     */
    private var written: Long = 0

    /**
     * Data for local header data
     *
     * @since 1.1
     */
    private var dataStart: Long = 0

    /**
     * Offset for CRC entry in the local file header data for the
     * current entry starts here.
     *
     * @since 1.15
     */
    private var localDataStart: Long = 0

    /**
     * Start of central directory.
     *
     * @since 1.1
     */
    private var cdOffset = ZipLong(0)

    /**
     * Length of central directory.
     *
     * @since 1.1
     */
    private var cdLength = ZipLong(0)

    /**
     * Holds the offsets of the LFH starts for each entry.
     *
     * @since 1.1
     */
    private val offsets: Hashtable<ZipEntry, ZipLong> = Hashtable<ZipEntry, ZipLong>()

    /**
     * The encoding to use for filenames and the file comment.
     *
     * @return null if using the platform's default character encoding.
     *
     * @since 1.3
     */
    /**
     * The encoding to use for filenames and the file comment.
     *
     *
     * For a list of possible values see [http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html](http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html).
     * Defaults to the platform's default character encoding.
     *
     * @since 1.3
     */
    /**
     * The encoding to use for filenames and the file comment.
     *
     *
     * For a list of possible values see [http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html](http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html).
     * Defaults to the platform's default character encoding.
     *
     * @since 1.3
     */
    var encoding: String? = null

    /**
     * This Deflater object is used for output.
     *
     *
     * This attribute is only protected to provide a level of API
     * backwards compatibility.  This class used to extend [ ] up to
     * Revision 1.13.
     *
     * @since 1.14
     */
    protected var def: Deflater = Deflater(Deflater.DEFAULT_COMPRESSION, true)

    /**
     * This buffer servers as a Deflater.
     *
     *
     * This attribute is only protected to provide a level of API
     * backwards compatibility.  This class used to extend [ ] up to
     * Revision 1.13.
     *
     * @since 1.14
     */
    protected var buf: ByteArray = ByteArray(512)

    /**
     * Optional random access output.
     *
     * @since 1.14
     */
    private var raf: RandomAccessFile? = null

    /**
     * Creates a new ZIP OutputStream filtering the underlying stream.
     *
     * @since 1.1
     */
    constructor(out: OutputStream?) : super(out)

    /**
     * Creates a new ZIP OutputStream writing to a File.  Will use
     * random access if possible.
     *
     * @since 1.14
     */
    constructor(file: File) : super(null) {
        try {
            raf = RandomAccessFile(file, "rw")
            raf!!.setLength(0)
        } catch (e: IOException) {
            if (raf != null) {
                try {
                    raf!!.close()
                } catch (inner: IOException) {
                    // ignore
                }
                raf = null
            }
            out = FileOutputStream(file)
        }
    }

    val isSeekable: Boolean
        /**
         * This method indicates whether this archive is writing to a seekable stream (i.e., to a random
         * access file).
         *
         *
         * For seekable streams, you don't need to calculate the CRC or
         * uncompressed size for [.STORED] entries before
         * invoking [.putNextEntry].
         *
         * @since 1.17
         */
        get() = raf != null

    /**
     * Finishs writing the contents and closes this as well as the
     * underlying stream.
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    fun finish() {
        closeEntry()
        cdOffset = ZipLong(written)
        for (i in entries.indices) {
            writeCentralFileHeader((entries.elementAt(i) as ZipEntry?)!!)
        }
        cdLength = ZipLong(written - cdOffset.value)
        writeCentralDirectoryEnd()
        offsets.clear()
        entries.removeAllElements()
    }

    /**
     * Writes all necessary data for this entry.
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    fun closeEntry() {
        if (entry == null) {
            return
        }

        val realCrc = crc.getValue()
        crc.reset()

        if (entry!!.getMethod() == DEFLATED) {
            def.finish()
            while (!def.finished()) {
                deflate()
            }

            entry!!.setSize(adjustToLong(def.getTotalIn()))
            entry!!.compressedSize = adjustToLong(def.getTotalOut())
            entry!!.setCrc(realCrc)

            def.reset()

            written += entry!!.compressedSize
        } else if (raf == null) {
            if (entry!!.getCrc() != realCrc) {
                throw ZipException(
                    ("bad CRC checksum for entry "
                            + entry!!.getName() + ": "
                            + java.lang.Long.toHexString(entry!!.getCrc())
                            + " instead of "
                            + java.lang.Long.toHexString(realCrc))
                )
            }

            if (entry!!.getSize() != written - dataStart) {
                throw ZipException(
                    ("bad size for entry "
                            + entry!!.getName() + ": "
                            + entry!!.getSize()
                            + " instead of "
                            + (written - dataStart))
                )
            }
        } else { /* method is STORED and we used RandomAccessFile */
            val size = written - dataStart

            entry!!.setSize(size)
            entry!!.compressedSize = size
            entry!!.setCrc(realCrc)
        }

        // If random access output, write the local file header containing
        // the correct CRC and compressed/uncompressed sizes
        if (raf != null) {
            val save = raf!!.filePointer

            raf!!.seek(localDataStart)
            writeOut((ZipLong(entry!!.getCrc())).getBytes())
            writeOut((ZipLong(entry!!.compressedSize)).getBytes())
            writeOut((ZipLong(entry!!.getSize())).getBytes())
            raf!!.seek(save)
        }

        writeDataDescriptor(entry!!)
        entry = null
    }

    /**
     * Begin writing next entry.
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    fun putNextEntry(ze: ZipEntry?) {
        closeEntry()

        entry = ze
        entries.addElement(entry)

        if (entry!!.getMethod() == -1) { // not specified
            entry!!.setMethod(method)
        }

        if (entry!!.getTime() == -1L) { // not specified
            entry!!.setTime(System.currentTimeMillis())
        }

        // Size/CRC not required if RandomAccessFile is used
        if (entry!!.getMethod() == STORED && raf == null) {
            if (entry!!.getSize() == -1L) {
                throw ZipException(
                    ("uncompressed size is required for"
                            + " STORED method when not writing to a"
                            + " file")
                )
            }
            if (entry!!.getCrc() == -1L) {
                throw ZipException(
                    "crc checksum is required for STORED"
                            + " method when not writing to a file"
                )
            }
            entry!!.compressedSize = entry!!.getSize()
        }

        if (entry!!.getMethod() == DEFLATED && hasCompressionLevelChanged) {
            def.setLevel(level)
            hasCompressionLevelChanged = false
        }
        writeLocalFileHeader(entry!!)
    }

    /**
     * Set the file comment.
     *
     * @since 1.1
     */
    fun setComment(comment: String) {
        this.comment = comment
    }

    /**
     * Sets the compression level for subsequent entries.
     *
     *
     * Default is Deflater.DEFAULT_COMPRESSION.
     *
     * @since 1.1
     */
    fun setLevel(level: Int) {
        hasCompressionLevelChanged = (this.level != level)
        this.level = level
    }

    /**
     * Sets the default compression method for subsequent entries.
     *
     *
     * Default is DEFLATED.
     *
     * @since 1.1
     */
    fun setMethod(method: Int) {
        this.method = method
    }

    /**
     * Writes bytes to ZIP entry.
     */
    @Throws(IOException::class)
    override fun write(b: ByteArray, offset: Int, length: Int) {
        if (entry!!.getMethod() == DEFLATED) {
            if (length > 0) {
                if (!def.finished()) {
                    def.setInput(b, offset, length)
                    while (!def.needsInput()) {
                        deflate()
                    }
                }
            }
        } else {
            writeOut(b, offset, length)
            written += length.toLong()
        }
        crc.update(b, offset, length)
    }

    /**
     * Writes a single byte to ZIP entry.
     *
     *
     * Delegates to the three arg method.
     *
     * @since 1.14
     */
    @Throws(IOException::class)
    override fun write(b: Int) {
        val buf = ByteArray(1)
        buf[0] = (b and 0xff).toByte()
        write(buf, 0, 1)
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     * @since 1.14
     */
    @Throws(IOException::class)
    override fun close() {
        finish()

        if (raf != null) {
            raf!!.close()
        }
        if (out != null) {
            out.close()
        }
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out to the stream.
     *
     * @exception  IOException  if an I/O error occurs.
     * @since 1.14
     */
    @Throws(IOException::class)
    override fun flush() {
        if (out != null) {
            out.flush()
        }
    }

    /**
     * Writes next block of compressed data to the output stream.
     *
     * @since 1.14
     */
    @Throws(IOException::class)
    protected fun deflate() {
        val len = def.deflate(buf, 0, buf.size)
        if (len > 0) {
            writeOut(buf, 0, len)
        }
    }

    /**
     * Writes the local file header entry
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    protected fun writeLocalFileHeader(ze: ZipEntry) {
        offsets.put(ze, ZipLong(written))

        writeOut(LFH_SIG.getBytes())
        written += 4

        // version needed to extract
        // general purpose bit flag
        if (ze.getMethod() == DEFLATED && raf == null) {
            // requires version 2 as we are going to store length info
            // in the data descriptor
            writeOut((ZipShort(20)).getBytes())

            // bit3 set to signal, we use a data descriptor
            writeOut((ZipShort(8)).getBytes())
        } else {
            writeOut((ZipShort(10)).getBytes())
            writeOut(ZERO)
        }
        written += 4

        // compression method
        writeOut((ZipShort(ze.getMethod())).getBytes())
        written += 2

        // last mod. time and date
        writeOut(toDosTime(Date(ze.getTime())).getBytes())
        written += 4

        // CRC
        // compressed length
        // uncompressed length
        localDataStart = written
        if (ze.getMethod() == DEFLATED || raf != null) {
            writeOut(LZERO)
            writeOut(LZERO)
            writeOut(LZERO)
        } else {
            writeOut((ZipLong(ze.getCrc())).getBytes())
            writeOut((ZipLong(ze.getSize())).getBytes())
            writeOut((ZipLong(ze.getSize())).getBytes())
        }
        written += 12

        // file name length
        val name = getBytes(ze.getName())
        writeOut((ZipShort(name.size)).getBytes())
        written += 2

        // extra field length
        val extra = ze.getLocalFileDataExtra()
        writeOut((ZipShort(extra.size)).getBytes())
        written += 2

        // file name
        writeOut(name)
        written += name.size.toLong()

        // extra field
        writeOut(extra)
        written += extra.size.toLong()

        dataStart = written
    }

    /**
     * Writes the data descriptor entry
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    protected fun writeDataDescriptor(ze: ZipEntry) {
        if (ze.getMethod() != DEFLATED || raf != null) {
            return
        }
        writeOut(DD_SIG.getBytes())
        writeOut((ZipLong(entry!!.getCrc())).getBytes())
        writeOut((ZipLong(entry!!.getCompressedSize())).getBytes())
        writeOut((ZipLong(entry!!.getSize())).getBytes())
        written += 16
    }

    /**
     * Writes the central file header entry
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    protected fun writeCentralFileHeader(ze: ZipEntry) {
        writeOut(CFH_SIG.getBytes())
        written += 4

        // version made by
        writeOut((ZipShort((0 shl 8) or 20)).getBytes())
        written += 2

        // version needed to extract
        // general purpose bit flag
        if (ze.getMethod() == DEFLATED && raf == null) {
            // requires version 2 as we are going to store length info
            // in the data descriptor
            writeOut((ZipShort(20)).getBytes())

            // bit3 set to signal, we use a data descriptor
            writeOut((ZipShort(8)).getBytes())
        } else {
            writeOut((ZipShort(10)).getBytes())
            writeOut(ZERO)
        }
        written += 4

        // compression method
        writeOut((ZipShort(ze.getMethod())).getBytes())
        written += 2

        // last mod. time and date
        writeOut(toDosTime(Date(ze.getTime())).getBytes())
        written += 4

        // CRC
        // compressed length
        // uncompressed length
        writeOut((ZipLong(ze.getCrc())).getBytes())
        writeOut((ZipLong(ze.getCompressedSize())).getBytes())
        writeOut((ZipLong(ze.getSize())).getBytes())
        written += 12

        // file name length
        val name = getBytes(ze.getName())
        writeOut((ZipShort(name.size)).getBytes())
        written += 2

        // extra field length
        val extra = ze.getCentralDirectoryExtra()
        writeOut((ZipShort(extra.size)).getBytes())
        written += 2

        // file comment length
        var comm = ze.getComment()
        if (comm == null) {
            comm = ""
        }
        val comment = getBytes(comm)
        writeOut((ZipShort(comment.size)).getBytes())
        written += 2

        // disk number start
        writeOut(ZERO)
        written += 2

        // internal file attributes
        writeOut((ZipShort(0)).getBytes())
        written += 2

        // external file attributes
        writeOut((ZipLong(0)).getBytes())
        written += 4

        // relative offset of LFH
        writeOut((offsets.get(ze) as ZipLong).getBytes())
        written += 4

        // file name
        writeOut(name)
        written += name.size.toLong()

        // extra field
        writeOut(extra)
        written += extra.size.toLong()

        // file comment
        writeOut(comment)
        written += comment.size.toLong()
    }

    /**
     * Writes the &quot;End of central dir record&quot;
     *
     * @since 1.1
     */
    @Throws(IOException::class)
    protected fun writeCentralDirectoryEnd() {
        writeOut(EOCD_SIG.getBytes())

        // disk numbers
        writeOut(ZERO)
        writeOut(ZERO)

        // number of entries
        val num = (ZipShort(entries.size)).getBytes()
        writeOut(num)
        writeOut(num)

        // length and location of CD
        writeOut(cdLength.getBytes())
        writeOut(cdOffset.getBytes())

        // ZIP file comment
        val data = getBytes(comment)
        writeOut((ZipShort(data.size)).getBytes())
        writeOut(data)
    }

    /**
     * Retrieve the bytes for the given String in the encoding set for
     * this Stream.
     *
     * @since 1.3
     */
    @Throws(ZipException::class)
    protected fun getBytes(name: String): ByteArray {
        if (encoding == null) {
            return name.toByteArray()
        } else {
            try {
                return name.toByteArray(charset(encoding!!))
            } catch (uee: UnsupportedEncodingException) {
                throw ZipException(uee.message)
            }
        }
    }

    /**
     * Write bytes to output or random access file
     *
     * @since 1.14
     */
    /**
     * Write bytes to output or random access file
     *
     * @since 1.14
     */
    @Throws(IOException::class)
    protected fun writeOut(data: ByteArray, offset: Int = 0, length: Int = data.size) {
        if (raf != null) {
            raf!!.write(data, offset, length)
        } else {
            out.write(data, offset, length)
        }
    }

    companion object {
        /**
         * Helper, a 0 as ZipShort.
         *
         * @since 1.1
         */
        private val ZERO = byteArrayOf(0, 0)

        /**
         * Helper, a 0 as ZipLong.
         *
         * @since 1.1
         */
        private val LZERO = byteArrayOf(0, 0, 0, 0)

        /**
         * Compression method for deflated entries.
         *
         * @since 1.1
         */
        val DEFLATED: Int = ZipEntry.DEFLATED

        /**
         * Compression method for deflated entries.
         *
         * @since 1.1
         */
        val STORED: Int = ZipEntry.STORED

        /*
     * Various ZIP constants
     */
        /**
         * local file header signature
         *
         * @since 1.1
         */
        protected val LFH_SIG: ZipLong = ZipLong(0X04034B50L)

        /**
         * data descriptor signature
         *
         * @since 1.1
         */
        protected val DD_SIG: ZipLong = ZipLong(0X08074B50L)

        /**
         * central file header signature
         *
         * @since 1.1
         */
        protected val CFH_SIG: ZipLong = ZipLong(0X02014B50L)

        /**
         * end of central dir signature
         *
         * @since 1.1
         */
        protected val EOCD_SIG: ZipLong = ZipLong(0X06054B50L)

        /**
         * Smallest date/time ZIP can handle.
         *
         * @since 1.1
         */
        private val DOS_TIME_MIN = ZipLong(0x00002100L)

        /**
         * Convert a Date object to a DOS date/time field.
         *
         *
         * Stolen from InfoZip's `fileio.c`
         *
         * @since 1.1
         */
        protected fun toDosTime(time: Date): ZipLong {
            val year = time.getYear() + 1900
            val month = time.getMonth() + 1
            if (year < 1980) {
                return DOS_TIME_MIN
            }
            val value = (((year - 1980) shl 25)
                    or (month shl 21)
                    or (time.getDate() shl 16)
                    or (time.getHours() shl 11)
                    or (time.getMinutes() shl 5)
                    or (time.getSeconds() shr 1)).toLong()

            val result = ByteArray(4)
            result[0] = ((value and 0xFFL)).toByte()
            result[1] = ((value and 0xFF00L) shr 8).toByte()
            result[2] = ((value and 0xFF0000L) shr 16).toByte()
            result[3] = ((value and 0xFF000000L) shr 24).toByte()
            return ZipLong(result)
        }

        /**
         * Assumes a negative integer really is a positive integer that
         * has wrapped around and re-creates the original value.
         *
         * @since 1.17.2.8
         */
        protected fun adjustToLong(i: Int): Long {
            if (i < 0) {
                return 2 * (Int.Companion.MAX_VALUE.toLong()) + 2 + i
            } else {
                return i.toLong()
            }
        }
    }
}

private fun ZipEntry.getLocalFileDataExtra(): ByteArray {
    val extra = getExtra()
    return extra ?: ByteArray(0)
}
private fun ZipEntry.getCentralDirectoryExtra(): ByteArray {
    return ByteArray(0)
}

