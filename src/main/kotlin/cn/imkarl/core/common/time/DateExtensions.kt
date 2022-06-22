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
    return calendar
}
