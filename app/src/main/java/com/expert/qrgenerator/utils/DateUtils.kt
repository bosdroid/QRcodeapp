package com.expert.qrgenerator.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val DATE_FORMAT = "dd-MMM-yyyy"
    fun getCurrentDate(): String {
        val time = Calendar.getInstance().time
        val df = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return df.format(time)
    }
}