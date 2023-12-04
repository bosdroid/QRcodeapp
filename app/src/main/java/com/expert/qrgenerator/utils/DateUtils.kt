package com.expert.qrgenerator.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val DATE_FORMAT = "yyyy-MM-dd"
    fun getCurrentDate(): String {
        val time = Calendar.getInstance().time
        val df = SimpleDateFormat(DATE_FORMAT, Locale.US)
        return df.format(time)
    }
}