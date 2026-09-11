package com.example.scrolljourney.data.repository

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())

    fun getDateKey(epochMs: Long): String {
        return formatter.format(Instant.ofEpochMilli(epochMs))
    }
}
