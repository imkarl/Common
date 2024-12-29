package cn.imkarl.core.common.json

import cn.imkarl.core.common.lang.cast
import com.google.gson.JsonObject


inline fun <reified T> JsonObject.getPrimitive(name: String): T? {
    return try {
        this.get(name)?.asString.cast<T>()
    } catch (_: Throwable) {
        null
    }
}

inline fun <reified T> JsonObject.getPrimitive(name: String, defValue: T): T {
    return try {
        this.get(name)?.asString.cast(defValue)
    } catch (_: Throwable) {
        defValue
    }
}
