package cn.imkarl.core.common.collections

import java.util.*

/**
 * 字母数字比较器
 */
class AlphanumComparator<T>(
    private val transform: (T) -> String
) : Comparator<T> {

    companion object {
        private val alphanumRegex = "[0-9]+".toRegex()
    }

    override fun compare(o1: T?, o2: T?): Int {
        if (o1 == null && o2 == null) {
            return 0
        } else if (o1 == null) {
            return -1
        } else if (o2 == null) {
            return 1
        }

        val str1 = transform(o1)
        val str2 = transform(o2)
        val strNodeArr1 = str1.splitAlphanum()
        val strNodeArr2 = str2.splitAlphanum()

        if (strNodeArr1.isEmpty() || strNodeArr2.isEmpty()) {
            return str1.compareTo(str2)
        }

        val minLen = Math.min(strNodeArr1.size, strNodeArr2.size)
        for (i in 0 until minLen) {
            val strNode1 = strNodeArr1[i].toIntOrNull()
            val strNode2 = strNodeArr2[i].toIntOrNull()

            if (strNodeArr1[i] == strNodeArr2[i]) {
                continue
            } else if (strNode1 != null && strNode2 != null) {
                return if (strNode1 == strNode2) {
                    if (strNodeArr1[i].length < strNodeArr2[i].length) {
                        -1
                    } else if (strNodeArr1[i].length == strNodeArr2[i].length) {
                        0
                    } else {
                        1
                    }
                } else {
                    if (strNode1 > strNode2) {
                        1
                    } else {
                        -1
                    }
                }
            } else {
                return strNodeArr1[i].compareTo(strNodeArr2[i])
            }
        }

        return str1.compareTo(str2)
    }

    /**
     * 以字母数字进行分割
     */
    private fun String.splitAlphanum(): List<String> {
        val mathResults = alphanumRegex.findAll(this)
        val ranges = mutableListOf<IntRange>()
        var index = 0
        for (matchResult in mathResults) {
            if (matchResult.range.first != index) {
                ranges.add(IntRange(index, matchResult.range.first))
            }
            ranges.add(IntRange(matchResult.range.first, matchResult.range.last + 1))
            index = matchResult.range.last + 1
        }
        return ranges.map { this.substring(it.first, it.last) }
    }

}