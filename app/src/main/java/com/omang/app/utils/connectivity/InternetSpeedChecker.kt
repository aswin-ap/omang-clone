package com.omang.app.utils.connectivity

import android.content.Context
import com.omang.app.R
import com.omang.app.utils.Resource
import com.omang.app.utils.extensions.hasInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import timber.log.Timber
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.floor
import kotlin.math.roundToInt

class InternetSpeedChecker @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    suspend fun checkInternetSpeed(context: Context): Resource<Int> {
        if (context.hasInternetConnection()) {
            val request = Request.Builder()
                .url(URL)
                .build()
            var kilobytePerSec = 0
            try {
                val startTime = System.currentTimeMillis()
                val response = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }

                if (!response.isSuccessful) {
                    return Resource.Error(context.getString(R.string.something_went_wrong))
                }

                val inputStream = response.body.byteStream()
                val bos = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var fileSize: Long
                withContext(Dispatchers.IO) {
                    while (inputStream.read(buffer) != -1) {
                        bos.write(buffer)
                    }
                    fileSize = bos.size().toLong()
                    inputStream.close()
                }
                val endTime = System.currentTimeMillis()

                // calculate how long it took by subtracting endtime from starttime
                val timeTakenMills =
                    floor((endTime - startTime).toDouble()) // time taken in milliseconds
                val timeTakenInSecs = timeTakenMills / 1000 // divide by 1000 to get time in seconds
                kilobytePerSec = (1024 / timeTakenInSecs).roundToInt()
                val speed = (fileSize / timeTakenMills).roundToInt().toDouble()
                Timber.tag("internet_speed").e("$kilobytePerSec kbps")
                Timber.tag("internet_speed").e("$speed speed")
            } catch (e: Exception) {
                return Resource.Error(context.getString(R.string.something_went_wrong))
            }
            return Resource.Success(kilobytePerSec)
        } else {
            return Resource.Error(context.getString(R.string.no_internet_text))
        }
    }

    companion object {
        private const val URL =
            "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"

        fun getConvertedSpeed(speed: Int): String {
            return when (speed.toString().length) {
                5 -> {
                    "${(speed / 1000.0)} Mbps"
                }

                4 -> {
                    "$speed Kbps"
                }

                else -> {
                    "Not available"
                }
            }
        }
    }
}