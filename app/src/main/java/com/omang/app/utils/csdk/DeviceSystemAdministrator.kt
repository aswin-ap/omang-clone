package com.omang.app.utils.csdk

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import timber.log.Timber

class DeviceSystemAdministrator : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Timber.v("one-omang is device admin ")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Timber.e("one-omang device admin privilege removed ")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Toast.makeText(context, "Pinned mode exiting", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "DeviceAdminReceiver"
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, DeviceSystemAdministrator::class.java)
        }
    }
}
