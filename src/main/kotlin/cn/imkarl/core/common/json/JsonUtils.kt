package cn.imkarl.core.common.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.Reader
import java.lang.reflect.Type

/**
 * JSON相关工具类
 * @author imkarl
 */
object JsonUtils {

    @JvmStatic
    val gson: Gson by lazy { createGsonBuilder().create() }
    private val gsonPretty: Gson by lazy { createGsonBuilder().setPrettyPrinting().create() }

    fun createGsonBuilder(): GsonBuilder {
        return GsonBuilder()

            // 排除字段
            .addFieldExclusionStrategy { field, isSerialize ->
                field.getAnnotation(Exclusion::class.java)?.let { exclusion ->
                    if (isSerialize) {
                        exclusion.serialize
                    } else {
                        exclusion.deserialize
                    }
                } ?: false
            }
            // 排除类
            .addClassExclusionStrategy { clazz, isSerialize ->
                clazz.getAnnotation(Exclusion::class.java)?.let { exclusion ->
                    if (isSerialize) {
                        exclusion.serialize
                    } else {
                        exclusion.deserialize
                    }
                } ?: false
            }

            .registerTypeAdapter(Boolean::class.java, JsonDeserializer<Boolean> { json, _, _ ->
                if (!json.isJsonPrimitive) {
                    return@JsonDeserializer false
                }

                return@JsonDeserializer json.asJsonPrimitive.let {
                    when {
                        it.isBoolean -> it.asBoolean
                        it.isNumber -> it.asNumber.toDouble().toInt() == 1
                        it.isString -> {
                            val str = it.asString
                            var result: Boolean
                            try {
                                result = str.toDouble().toInt() == 1
                            } catch (e: Exception) {
                                result = str?.toBoolean() ?: false
                            }
                            result
                        }
                        else -> false
                    }
                }
            })
    }

    @JvmStatic
    fun <T> fromJson(json: String?, typeToken: TypeToken<T>): T? {
        if (json == null) {
            return null
        }
        return fromJson(json, typeToken.type)
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: String?): T? {
        if (json == null) {
            return null
        }
        return fromJson(json, object : TypeToken<T>() {})
    }

    @JvmStatic
    fun <T> fromJson(json: JsonElement?, typeToken: TypeToken<T>): T? {
        if (json == null) {
            return null
        }
        return fromJson(json, typeToken.type)
    }

    @JvmStatic
    fun <T> fromJson(json: InputStream?, typeToken: TypeToken<T>): T? {
        if (json == null) {
            return null
        }
        return fromJson(json.reader(), typeToken.type)
    }

    @JvmStatic
    fun <T> fromJson(json: Reader?, typeToken: TypeToken<T>): T? {
        if (json == null) {
            return null
        }
        return fromJson(json, typeToken.type)
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: JsonElement?): T? {
        if (json == null) {
            return null
        }
        return fromJson(json, object : TypeToken<T>() {})
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: InputStream?): T? {
        if (json == null) {
            return null
        }
        return fromJson(json.reader(), object : TypeToken<T>() {})
    }

    @JvmStatic
    inline fun <reified T> fromJson(json: Reader?): T? {
        if (json == null) {
            return null
        }
        return fromJson(json, object : TypeToken<T>() {})
    }

    @JvmStatic
    fun <T> fromJson(json: String?, typeOfT: Type): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json, typeOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: JsonElement?, typeOfT: Type): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json, typeOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: InputStream?, typeOfT: Type): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json.reader(), typeOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: Reader?, typeOfT: Type): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json, typeOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: String?, classOfT: Class<T>): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json, classOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: JsonElement?, classOfT: Class<T>): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json, classOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: InputStream?, classOfT: Class<T>): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json.reader(), classOfT)
    }

    @JvmStatic
    fun <T> fromJson(json: Reader?, classOfT: Class<T>): T? {
        if (json == null) {
            return null
        }
        return gson.fromJson(json, classOfT)
    }

    @JvmStatic
    fun toJson(src: Any?): String {
        return gson.toJson(src)
    }

    @JvmStatic
    fun toJsonElement(src: Any?): JsonElement {
        return gson.toJsonTree(src)
    }

    @JvmStatic
    fun toJsonPrettyPrinting(src: Any?): String {
        return gsonPretty.toJson(src)
    }

}
