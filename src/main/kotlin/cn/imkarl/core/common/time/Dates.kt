package cn.imkarl.core.common.time

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class Dates private constructor(val milliseconds: Long){

    companion object {
        fun of(milliseconds: Long): Dates {
            return Dates(
                if (milliseconds > 1_0000_0000_0000) {
                    milliseconds
                } else {
                    milliseconds * 1000
                }
            )
        }

        fun parse(dateTime: String?): Dates? {
            if (dateTime.isNullOrBlank()) {
                return null
            }

            val milliseconds = try {
                if (dateTime.matches("^[0-9]{10}\$|^[0-9]{13}\$".toRegex())) {
                    dateTime.toLongOrNull() ?: -1L
                } else if (dateTime.contains("[0-9]T[0-9]".toRegex())) {
                    if (dateTime.isNotBlank()) {
                        OffsetDateTime.parse(dateTime).toInstant().toEpochMilli()
                    } else {
                        -1L
                    }
                } else if (dateTime.contains(",")) {
                    SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(dateTime).time
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime).time
                }
            } catch (_: Throwable) {
                -1L
            }
            if (milliseconds < 0) {
                return null
            }
            return Dates(milliseconds)
        }


        /**
         * 今天的起始时间
         */
        fun today(): Dates {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return of(calendar.timeInMillis)
        }
    }

    fun format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(pattern).format(milliseconds)
    }


    fun toCalendar(zone: TimeZone = TimeZone.getDefault()): Calendar {
        return Calendar.getInstance().apply {
            this.timeInMillis = milliseconds
            this.timeZone = zone
        }
    }

    fun toDate(): Date {
        return Date(milliseconds)
    }

    fun toDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milliseconds), zone)
    }

}