package cn.imkarl.core.common.time

import java.util.*

/**
 * 今天的起始时间
 */
fun today(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}


/**
 * 将10位或13位时间戳，转换为13位的精确到毫秒的时间戳
 */
fun Long.toMilliseconds(): Long {
    return if (this > 1_0000_0000_0000) {
        this
    } else {
        this * 1000
    }
}
