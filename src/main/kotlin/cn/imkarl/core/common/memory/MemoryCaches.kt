package cn.imkarl.core.common.memory

import kotlinx.coroutines.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 内存缓存
 */
object MemoryCaches {

    private val scope by lazy { CoroutineScope(Dispatchers.IO + SupervisorJob()) }

    private val memCache: MutableMap<String, Pair<Long, Any>> = java.util.HashMap()

    init {
        // 定时清理过期的缓存数据
        scope.launch(Dispatchers.IO) {
            while (true) {
                delay(1.seconds)
                try {
                    memCache.forEach { key, (expireTime, _) ->
                        if (expireTime < System.currentTimeMillis()) {
                            memCache.remove(key)
                        }
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }


    /**
     * 存储K - V
     */
    operator fun set(key: String, value: Any?): MemoryCaches {
        set(key, value, Long.MAX_VALUE)
        return this
    }

    /**
     * 存储K - V
     * @param duration 存活时长 (毫秒)
     */
    fun set(key: String, value: Any?, duration: Long): MemoryCaches {
        if (key.isBlank()) {
            return this
        }
        if (value == null) {
            memCache.remove(key)
            return this
        }
        memCache[key] = if (duration <= 0L || duration == Long.MAX_VALUE) {
            Pair(Long.MAX_VALUE, value)
        } else {
            Pair(System.currentTimeMillis() + duration, value)
        }
        return this
    }

    /**
     * 存储K - V
     * @param duration 存活时长
     */
    fun set(key: String, value: Any?, duration: Duration): MemoryCaches {
        set(key, value, duration.inWholeMilliseconds)
        return this
    }


    /**
     * 根据key取对应数据
     */
    inline operator fun <reified T : Any> get(key: String): T? {
        return get(key, T::class)
    }

    /**
     * 根据key取对应数据；如果不存在，则调用supplier获取数据并缓存
     */
    inline fun <reified T : Any> getOrPut(key: String, supplier: () -> Pair<T, Duration>): T {
        return get<T>(key) ?: run {
            val (data, duration) = supplier.invoke()
            set(key, data, duration)
            data
        }
    }

    /**
     * 根据key取对应数据
     */
    fun <T : Any> get(key: String, clazz: KClass<T>): T? {
        if (key.isBlank()) return null
        val pair = memCache[key]
        if (pair == null || pair.first < System.currentTimeMillis()) {
            memCache.remove(key)
            return null
        }
        if (pair.second.javaClass != clazz.java
            && !clazz.java.isInstance(pair.second)
            && !pair.second::class.isSubclassOf(clazz)) {
            return null
        }
        return pair.second as? T
    }


    /**
     * 根据key删除对应缓存
     */
    fun delete(key: String): MemoryCaches {
        if (key.isNotBlank()) {
            memCache.remove(key)
        }
        return this
    }

    /**
     * 批量删除
     */
    fun delete(vararg keys: String): MemoryCaches {
        keys.forEach { key ->
            memCache.remove(key)
        }
        return this
    }



    /**
     * 刷新已存储的Key的存活时间 (秒)
     */
    fun updateExpire(key: String, expire: Long): MemoryCaches {
        if (key.isBlank()) {
            return this
        }
        val pair = memCache[key] ?: return this
        memCache.remove(key)
        if (pair.first >= System.currentTimeMillis()) {
            set(key, pair.second, expire)
        }
        return this
    }

    /**
     * 判断 key 是否存在有效缓存
     */
    fun exists(key: String): Boolean {
        if (key.isBlank()) return false
        val pair = memCache[key] ?: return false
        if (pair.first < System.currentTimeMillis()) {
            memCache.remove(key)
            return false
        }
        return true
    }

}
