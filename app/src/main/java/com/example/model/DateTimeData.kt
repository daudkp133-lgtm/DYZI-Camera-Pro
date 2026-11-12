package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class TimeSource {
    DEVICE,
    MANUAL
}

enum class DateFormatOption(val pattern: String, val label: String) {
    DAY_MONTH_YEAR_DASH("dd-MMM-yyyy", "10-Nov-2026"),
    DAY_MONTH_YEAR_SLASH("dd/MM/yyyy", "10/11/2026"),
    YEAR_MONTH_DAY_DASH("yyyy-MM-dd", "2026-11-10"),
    MONTH_DAY_YEAR_SLASH("MM/dd/yyyy", "11/10/2026"),
    FULL_DATE("EEEE, d MMMM yyyy", "Tuesday, 10 November 2026")
}

enum class TimeFormatOption(val pattern: String, val label: String, val is24Hour: Boolean) {
    TIME_12H_SEC("h:mm:ss a", "8:15:55 pm", false),
    TIME_12H_NO_SEC("h:mm a", "8:15 pm", false),
    TIME_24H_SEC("HH:mm:ss", "20:15:55", true),
    TIME_24H_NO_SEC("HH:mm", "20:15", true)
}

data class DateTimeData(
    val source: TimeSource = TimeSource.DEVICE,
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateFormat: DateFormatOption = DateFormatOption.DAY_MONTH_YEAR_DASH,
    val timeFormat: TimeFormatOption = TimeFormatOption.TIME_12H_SEC,
    val timezoneId: String = TimeZone.getDefault().id,
    // When in manual mode, user can set explicit components:
    val manualYear: Int = 2026,
    val manualMonth: Int = 11, // 1-12
    val manualDay: Int = 10,
    val manualHour: Int = 20, // 0-23
    val manualMinute: Int = 15,
    val manualSecond: Int = 55
) {
    fun getEffectiveMillis(): Long {
        return if (source == TimeSource.DEVICE) {
            System.currentTimeMillis()
        } else {
            val cal = Calendar.getInstance(TimeZone.getTimeZone(timezoneId))
            cal.set(Calendar.YEAR, manualYear)
            cal.set(Calendar.MONTH, manualMonth - 1)
            cal.set(Calendar.DAY_OF_MONTH, manualDay)
            cal.set(Calendar.HOUR_OF_DAY, manualHour)
            cal.set(Calendar.MINUTE, manualMinute)
            cal.set(Calendar.SECOND, manualSecond)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }
    }

    fun formatFormattedDateTime(): String {
        val millis = getEffectiveMillis()
        val tz = TimeZone.getTimeZone(timezoneId)
        val combinedPattern = "${dateFormat.pattern} ${timeFormat.pattern}"
        val sdf = SimpleDateFormat(combinedPattern, Locale.US).apply {
            timeZone = tz
        }
        val result = sdf.format(Date(millis))
        return if (timeFormat.pattern.contains("a")) {
            // Lowercase am/pm as in user reference e.g. "8:15:55 pm"
            result.replace("AM", "am").replace("PM", "pm")
        } else {
            result
        }
    }

    fun formatDateOnly(): String {
        val millis = getEffectiveMillis()
        val sdf = SimpleDateFormat(dateFormat.pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone(timezoneId)
        }
        return sdf.format(Date(millis))
    }

    fun formatTimeOnly(): String {
        val millis = getEffectiveMillis()
        val sdf = SimpleDateFormat(timeFormat.pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone(timezoneId)
        }
        val result = sdf.format(Date(millis))
        return if (timeFormat.pattern.contains("a")) {
            result.replace("AM", "am").replace("PM", "pm")
        } else {
            result
        }
    }
}
