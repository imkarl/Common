/*
 * Copyright  2001-2002,2004-2005 The Apache Software Foundation
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

/**
 * Utility class that represents a four byte integer with conversion
 * rules for the big endian byte order of ZIP files.
 *
 */
class ZipLong : Cloneable {
    /**
     * Get value as Java int.
     *
     * @since 1.1
     */
    var value: Long
        private set

    /**
     * Create instance from a number.
     *
     * @since 1.1
     */
    constructor(value: Long) {
        this.value = value
    }

    /**
     * Create instance from the four bytes starting at offset.
     *
     * @since 1.1
     */
    /**
     * Create instance from bytes.
     *
     * @since 1.1
     */
    @JvmOverloads
    constructor(bytes: ByteArray, offset: Int = 0) {
        value = (bytes[offset + 3].toInt() shl 24).toLong() and 0xFF000000L
        value += ((bytes[offset + 2].toInt() shl 16) and 0xFF0000).toLong()
        value += ((bytes[offset + 1].toInt() shl 8) and 0xFF00).toLong()
        value += (bytes[offset].toInt() and 0xFF).toLong()
    }

    /**
     * Get value as two bytes in big endian byte order.
     *
     * @since 1.1
     */
    fun getBytes(): ByteArray {
        val result = ByteArray(4)
        result[0] = ((value and 0xFFL)).toByte()
        result[1] = ((value and 0xFF00L) shr 8).toByte()
        result[2] = ((value and 0xFF0000L) shr 16).toByte()
        result[3] = ((value and 0xFF000000L) shr 24).toByte()
        return result
    }

    /**
     * Override to make two instances with same value equal.
     *
     * @since 1.1
     */
    override fun equals(o: Any?): Boolean {
        if (o == null || o !is ZipLong) {
            return false
        }
        return value == o.value
    }

    /**
     * Override to make two instances with same value equal.
     *
     * @since 1.1
     */
    override fun hashCode(): Int {
        return value.toInt()
    }
}
