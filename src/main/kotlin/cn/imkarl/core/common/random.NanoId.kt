package cn.imkarl.core.common

import java.security.SecureRandom
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln


/**
 * The default random number generator used by this class.
 * Creates cryptographically strong NanoId Strings.
 */
private val DEFAULT_NUMBER_GENERATOR: SecureRandom = SecureRandom()

/**
 * The default alphabet used by this class.
 * Creates url-friendly NanoId Strings using 64 unique symbols.
 */
private val DEFAULT_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()

/**
 * The default size used by this class.
 * Creates NanoId Strings with slightly more unique values than UUID v4.
 */
private const val DEFAULT_SIZE = 21

/**
 * 生成一个url友好的、伪随机生成的 NanoId 字符串。
 *
 * 生成的 NanoId 字符串将有21个符号
 * NanoId 字符串是使用加密强伪随机数生成器生成的。
 *
 * @return 一个随机生成的NanoId字符串
 */
fun randomNanoId(): String {
    return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, DEFAULT_SIZE)
}

/**
 * Static factory to retrieve a NanoId String.
 *
 * The string is generated using the given random number generator.
 *
 * @param random   The random number generator.
 * @param alphabet The symbols used in the NanoId String.
 * @param size     The number of symbols in the NanoId String.
 * @return A randomly generated NanoId String.
 */
internal fun randomNanoId(random: Random, alphabet: CharArray, size: Int): String {
    require(!(alphabet.isEmpty() || alphabet.size >= 256)) { "alphabet must contain between 1 and 255 symbols." }
    require(size > 0) { "size must be greater than zero." }
    val mask = (2 shl floor(ln((alphabet.size - 1).toDouble()) / ln(2.0)).toInt()) - 1
    val step = ceil(1.6 * mask * size / alphabet.size).toInt()
    val idBuilder = StringBuilder()
    while (true) {
        val bytes = ByteArray(step)
        random.nextBytes(bytes)
        for (i in 0 until step) {
            val alphabetIndex = bytes[i].toInt() and mask
            if (alphabetIndex < alphabet.size) {
                idBuilder.append(alphabet[alphabetIndex])
                if (idBuilder.length == size) {
                    return idBuilder.toString()
                }
            }
        }
    }
}
