package cn.imkarl.core.common.collections

import cn.imkarl.core.common.lang.cast
import org.bson.Document
import kotlin.collections.toList
import kotlin.collections.toSet
import kotlin.collections.toTypedArray
import kotlin.jvm.java

inline fun <reified T> Document.getValue(key: String): T {
    if (T::class == List::class.java) {
        return this.getList(key, Any::class.java).toList() as T
    }
    if (T::class == Set::class.java) {
        return this.getList(key, Any::class.java).toSet() as T
    }
    if (T::class == Array::class.java) {
        return this.getList(key, Any::class.java).toTypedArray() as T
    }
    return this[key]?.toString().cast<T>()
}
