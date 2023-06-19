package cn.imkarl.core.common.lang

import cn.imkarl.core.common.json.JsonUtils


/**
 * 将字符串转换为指定类型
 * @param defValue 默认值
 */
inline fun <reified T> String?.cast(defValue: T): T {
    if (this.isNullOrBlank()) {
        return defValue
    }

    return when(T::class) {
        String::class -> this
        Boolean::class -> this.equals("true", ignoreCase = true) || this.equals("yes", ignoreCase = true) || this.equals("y", ignoreCase = true) || this == "1"
        Double::class -> this.toDoubleOrNull() ?: defValue
        Float::class -> this.toFloatOrNull() ?: defValue
        Int::class -> this.toIntOrNull() ?: defValue
        Long::class -> this.toLongOrNull() ?: defValue
        else -> try { JsonUtils.fromJson<T>(this) } catch (throwable: Throwable) { defValue }
    } as T
}

/**
 * 将字符串转换为指定类型
 */
inline fun <reified T> String?.cast(): T {
    return when(T::class) {
        String::class -> (this ?: "") as T
        Boolean::class -> this.cast(false) as T
        Double::class -> this.cast(0.0) as T
        Float::class -> this.cast(0F) as T
        Int::class -> this.cast(0) as T
        Long::class -> this.cast(0L) as T
        else -> (try { JsonUtils.fromJson<T>(this) } catch (throwable: Throwable) { null }) as T
    }
}

