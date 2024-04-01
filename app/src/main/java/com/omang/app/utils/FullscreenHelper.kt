package com.omang.app.utils

import android.app.Activity
import android.view.View
import android.view.Window
import android.view.WindowManager

class FullscreenHelper {
    companion object {
        fun enableFullscreen(activity: Activity) {
            // Hide the status bar

            activity.requestWindowFeature(Window.FEATURE_NO_TITLE)
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

           /* activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

            // Set the activity to fullscreen
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )*/
        }
    }
}