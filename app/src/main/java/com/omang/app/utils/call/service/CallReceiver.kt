package com.omang.app.utils.call.service

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.omang.app.dataStore.SharedPref
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CallReceiver : InCallService() {

    @Inject
    lateinit var sharedPref: SharedPref

    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)

        val phone = call?.details?.handle?.schemeSpecificPart
        Timber.e("spam detected from $phone")
        phone?.let {
            if (sharedPref.techPhones.contains(phone)) {
                this.applicationContext.startService(
                    Intent(
                        this,
                        CallService::class.java
                    ).putExtra("phone", phone)
                )
            }
        }
    }

    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
        this.stopService(Intent(this, CallService::class.java))
    }

}