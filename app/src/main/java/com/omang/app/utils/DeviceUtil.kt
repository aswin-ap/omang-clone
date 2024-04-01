package com.omang.app.utils

import android.os.Build
import android.os.SystemClock
import android.view.View
import com.omang.app.BuildConfig

object DeviceUtil {
    fun getAppVersion(): String {
        val devVersion = BuildConfig.VERSION_NAME
        return if (devVersion.contains("-debug-staging"))
            devVersion.replace("-debug-staging", "")
        else if (devVersion.contains("-debug-prod"))
            devVersion.replace("-debug-prod", "")
        else if (devVersion.contains("-release-staging"))
            devVersion.replace("-release-staging", "")
        else
            devVersion
    }

    fun getOS(): Int {
        return Build.VERSION.RELEASE.toInt()

    }
}

class SafeClickListener(
    private var defaultInterval: Int = 1000,
    private val onSafeCLick: (View) -> Unit,
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}



