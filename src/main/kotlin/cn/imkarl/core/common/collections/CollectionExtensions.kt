package cn.imkarl.core.common.collections


/**
 * 查找子集合
 * @return 没有找到返回 -1
 */
fun <T> List<T>.findSubArray(subArray: List<T>): Int {
    if (subArray.isEmpty()) return -1 // 空数组是任何数组的子数组
    if (this.size < subArray.size) return -1 // 如果主数组比子数组小，则不可能包含它

    for (i in 0 .. this.size - subArray.size) {
        var match = true
        for (j in subArray.indices) {
            if (this[i + j] != subArray[j]) {
                match = false
                break
            }
        }
        if (match) {
            return i
        }
    }
    return -1
}


/**
 * 查找子数组
 * @return 没有找到返回 -1
 */
fun <T> Array<T>.findSubArray(subArray: Array<T>): Int {
    if (subArray.isEmpty()) return -1 // 空数组是任何数组的子数组
    if (this.size < subArray.size) return -1 // 如果主数组比子数组小，则不可能包含它

    for (i in 0 .. this.size - subArray.size) {
        var match = true
        for (j in subArray.indices) {
            if (this[i + j] != subArray[j]) {
                match = false
                break
            }
        }
        if (match) {
            return i
        }
    }
    return -1
}
