package cn.imkarl.core.common.collections

private val alphanumComparator by lazy {
    AlphanumComparator<String> { it }
}

/**
 * 按照字母数字顺序比较字符串
 */
fun String.compareToByAlphanum(other: String): Int {
    return alphanumComparator.compare(this, other)
}
