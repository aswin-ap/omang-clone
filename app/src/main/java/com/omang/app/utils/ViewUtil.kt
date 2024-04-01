package com.omang.app.utils

import android.text.format.DateUtils
import android.util.Log
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertLocaleTimestampToLocale
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object ViewUtil {

    fun isTimeBefore(date: String?): Boolean {
        try {
            val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            val date1 = srcDf.parse(date)
            val currentDate = srcDf.format(Date())
            val currentTime = srcDf.parse(getUtcTime())
            return currentTime?.before(date1) ?: true
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }

    fun returnBefore(incomingDate: String?): Boolean {
        val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val currentDate = srcDf.format(Date())

        val pIncomingDate = incomingDate?.let { srcDf.parse(it) } ?: return false
        val pCurrentDate = srcDf.parse(currentDate)

        if (pIncomingDate == pCurrentDate)
            return true

        return pCurrentDate.before(pIncomingDate)
    }

    fun getUtcTime(): String {
        val localTime = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(localTime)
    }

    fun getLocalTime(): String {
        val localDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        return localDateTime.format(formatter)
    }

    fun getUtcTimeWithMSec(): String {
        val localTime = Date(System.currentTimeMillis())
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(localTime)
    }

    fun getTimeInMillis(time1: String, time2: String): Long {
        // parse the date string into Date object
        try {
            val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            var date1 = srcDf.parse(time1)
            var date2 = srcDf.parse(time2)
            val destMOnth: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")
            val date1Str = destMOnth.format(date1)
            val date2Str = destMOnth.format(date2)
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a")
            date1 = simpleDateFormat.parse(date1Str)
            date2 = simpleDateFormat.parse(date2Str)
            val difference = date2.time - date1.time
            Timber.tag("date1Str").e(" :: %s", date1Str)
            Timber.tag("date2Str").e(" :: %s", date2Str)
            return difference
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return 0
    }

    fun getDiffBetweenTwoTimes(time1: String, time2: String): String {
        var timeDifference = ""

        try {
            val srcDf: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            var date1 = srcDf.parse(time1)
            var date2 = srcDf.parse(time2)
            val destMOnth: DateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US)
            val date1Str = destMOnth.format(date1)
            val date2Str = destMOnth.format(date2)
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.US)
            date1 = simpleDateFormat.parse(date1Str)
            date2 = simpleDateFormat.parse(date2Str)
            val difference = date2.time - date1.time
            val days = (difference / (1000 * 60 * 60 * 24)).toInt()
            var hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60)).toInt()
            val min =
                (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours).toInt() / (1000 * 60)
            val sec =
                (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours - 1000 * 60 * min) / 1000
            hours = if (hours < 0) -hours else hours
            Log.e("======= Hours", " :: $hours")
            Log.e("======= days", " :: $days")
            Log.e("======= min", " :: $min")
            Log.e("======= sec", " :: $sec")
            if (days > 0) {
                timeDifference = days.toString() + "d "
            }
            if (hours > 0) {
                timeDifference = timeDifference + hours + "h "
            }
            if (min > 0) {
                timeDifference = timeDifference + min + "min "
            }
            if (sec > 0) {
                timeDifference = timeDifference + sec + "s "
            }
            return timeDifference
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        return timeDifference
    }

    fun getServerFormattedTime(time: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date(time))
    }

    fun getCurrentEpochTime(): Long {
        return System.currentTimeMillis()
    }

    fun String.isCurrentDateAfterForceUpdateDate(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        try {
            val forceUpdateDateTime = dateFormat.parse(this)
            val currentDate = Calendar.getInstance().time

            return currentDate.after(forceUpdateDateTime)
        } catch (e: Exception) {
            e.printStackTrace()

        }
        return false
    }


}
