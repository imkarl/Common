package cn.imkarl.core.common.io

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Long.Bytes: Long get() = this

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Long.KBytes: Long get() = this.Bytes * 1024L

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Long.MBytes: Long get() = this.KBytes * 1024L

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Long.GBytes: Long get() = this.MBytes * 1024L



/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Int.Bytes: Long get() = this.toLong().Bytes

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Int.KBytes: Long get() = this.toLong().KBytes

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Int.MBytes: Long get() = this.toLong().MBytes

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Int.GBytes: Long get() = this.toLong().GBytes



/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Double.Bytes: Long get() = (this * 1.Bytes).toLong()

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Double.KBytes: Long get() = (this * 1.KBytes).toLong()

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Double.MBytes: Long get() = (this * 1.MBytes).toLong()

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Double.GBytes: Long get() = (this * 1.GBytes).toLong()



/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Float.Bytes: Long get() = this.toDouble().Bytes

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Float.KBytes: Long get() = this.toDouble().KBytes

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Float.MBytes: Long get() = this.toDouble().MBytes

/**
 * 将数值转换为对应的字节大小（Byte）
 */
val Float.GBytes: Long get() = this.toDouble().GBytes
