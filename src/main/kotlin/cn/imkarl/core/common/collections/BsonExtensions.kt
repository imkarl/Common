package cn.imkarl.core.common.collections

import cn.imkarl.core.common.lang.cast
import org.bson.Document
import kotlin.reflect.full.isSubclassOf


inline fun <reified T> Document.getValue(key: String): T {
    if (T::class.isSubclassOf(List::class)) {
        return this.getList(key, Any::class.java).toList() as T
    }
    if (T::class.isSubclassOf(Set::class)) {
        return this.getList(key, Any::class.java).toSet() as T
    }
    if (T::class.isSubclassOf(Collection::class)) {
        return this.getList(key, Any::class.java) as T
    }
    if (T::class.isSubclassOf(Array::class)) {
        return this.getList(key, Any::class.java).toTypedArray() as T
    }
    return this[key]?.toString().cast<T>()
}
