package com.omang.app.utils.deviceCallbacks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.widget.Toast
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.utils.call.service.CallService
import com.omang.app.utils.extensions.goAsync
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class DeviceReceivers : BroadcastReceiver() {

    @Inject
    lateinit var dataManagerRepository: DataManagerRepository

    lateinit var onScreenOffEvents: (() -> Unit)

    override fun onReceive(context: Context, intent: Intent) {

        Timber.tag("DeviceReceivers").v("onReceive ${intent.action}")

        when (intent.action) {
            /*Intent.ACTION_BOOT_COMPLETED,*/
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Timber.e("ACTION_MY_PACKAGE_REPLACED")
                val launchIntent =
                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                launchIntent?.let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }

            Intent.ACTION_SCREEN_OFF,
            Intent.ACTION_REBOOT,
            Intent.ACTION_SHUTDOWN -> {
                Timber.e("ACTION SCREEN IS TURNED OFF")
                goAsync {
                    // update the datastore to show the psm
                    dataManagerRepository.saveToDataStore(DataStoreKeys.PSM_STATUS, true)
                }
                onScreenOffEvents.invoke()
            }
        }
    }
}