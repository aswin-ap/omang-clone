package com.omang.app.utils.call.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.omang.app.ui.call.CallerUI
import timber.log.Timber

class CallService : Service() {

    private val CALL_TAG = "PHONE_CALL"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.tag(CALL_TAG).e("onStartCommand")
        if (intent?.extras != null) {
            val phone = intent.getStringExtra("phone")
            if (phone != null) {
                CallerUI.showWindow(applicationContext, phone)
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        CallerUI.callEndUI(applicationContext)

    }

}
