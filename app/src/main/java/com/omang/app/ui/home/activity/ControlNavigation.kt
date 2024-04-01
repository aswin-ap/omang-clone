package com.omang.app.ui.home.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import timber.log.Timber
import java.util.concurrent.Semaphore
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import java.lang.IllegalArgumentException


object ControlNavigation {
    private var mLastClickTime = 0L
    private val navigationSemaphore = Semaphore(1)
    fun isClickRecently(): Boolean {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return true
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        return false
    }
    fun navigate(destinationId: Int, findNavController: NavController) {
        try {
            navigationSemaphore.acquire()
            findNavController.navigate(destinationId)
        } catch (e: Exception) {
            Timber.i("Navigation Exception: ${e.message}")

        } finally {
            navigationSemaphore.release()
        }
    }

    fun NavController.safeNavigateWithArgs(direction: NavDirections) {
        try {
            navigate(direction)
        } catch (e: IllegalArgumentException) {
            Timber.tag("NAVIGATION").e("EXCEPTION IS ${e.message}")
        }

    }
}
