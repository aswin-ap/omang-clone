package com.omang.app.utils.csdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class CSDKLicenseReceiver : BroadcastReceiver() {
    private var licenseReceiverListener: LicenseReceiverListener? = null

    fun setLicenseReceiverListener(listener: LicenseReceiverListener) {
        licenseReceiverListener = listener
    }

    interface LicenseReceiverListener {
        fun onLicenseReceived(result: Int)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        Timber.tag("CSDK").e("action --> $action")

        if (action == CSDKConstants().ACTION) {
            val code: Int = intent.getIntExtra(CSDKConstants().ERROR_CODE, CSDKConstants().ERROR_DEFAULT)
            licenseReceiverListener?.onLicenseReceived(code)

        }
    }
}