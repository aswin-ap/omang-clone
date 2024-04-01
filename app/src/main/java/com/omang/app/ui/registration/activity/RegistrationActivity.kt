package com.omang.app.ui.registration.activity

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.omang.app.R
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.registration.viewmodel.RegistrationViewModel
import com.omang.app.utils.FullscreenHelper
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.csdk.CSDKConstants
import com.omang.app.utils.extensions.hideDeviceStatusDialog
import com.omang.app.utils.extensions.showDeviceStatusDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {

    private val viewModel by viewModels<RegistrationViewModel>()

    @Inject
    lateinit var toast: ToastMessage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullscreenHelper.enableFullscreen(this)
        setContentView(R.layout.activity_registration)

        init()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDeviceLockStatus()
//        test()
    }

    private fun observeData() {
        with(viewModel) {
            deviceLockStatus.observe(this@RegistrationActivity) {
                it?.let { isLocked ->
                    Timber.d("Device lock status $it")
                    if (isLocked) showDeviceStatusDialog(
                        getString(R.string.device_lock),
                        getDeviceLockMessage()
                    ) else
                        hideDeviceStatusDialog()
                }
            }

            deviceUpdatesResponse.observe(this@RegistrationActivity) { response ->
                response.priority?.let { priorityList ->
                    priorityList.forEach { item ->
                        handleUpdates(item?.updateType!!)
                    }
                }
            }

        }
    }

    private fun handleUpdates(updateType: Int) {
        when (updateType) {
            100 -> viewModel.setDeviceLockStatus(true)
            101 -> viewModel.setDeviceLockStatus(false)
            102 -> userAssign()
            103 -> userUnAssign()
            105 -> unpinDevice()

            else ->
                Timber.e("--> Priority Type :${updateType}")
        }
    }

    private fun init() {
        viewModel.saveFCMToken()
        viewModel.updateDeviceStatus()

    }

    private fun unpinDevice() {
        showUnpinDialog()

    }

    private fun showUnpinDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_unpin_device)
        dialog.setCancelable(false)

        val btOk: Button = dialog.findViewById(R.id.btOk)
        btOk.setOnClickListener {
            unPinDevice()

        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp
        dialog.show()
    }

    private fun userAssign() {
        viewModel.setDeviceAssignedStatus(true)
        hideDeviceStatusDialog()
        val i = Intent(this, AppUpdateActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        finish()
    }

    private fun userUnAssign() {
        viewModel.setDeviceAssignedStatus(false)
        showDeviceStatusDialog(
            getString(R.string.unassign_user),
            getString(R.string.unassign_description, viewModel.getDeviceImei())
        )
    }

    private fun unPinDevice() {
        val handler = Handler(Looper.getMainLooper())
        lifecycleScope.launch {
            viewModel.exitKiosk(this@RegistrationActivity) { result, error ->
                if (result != null) {
                    handler.post {
                        when (result) {
                            CSDKConstants().EXCECUTION_SUCCESS -> {
                                toast.showCenterAlignedToast("Device unpinned")
                                viewModel.setPinnedStatus(false)
                                viewModel.updateDeviceStatus()
                            }

                            CSDKConstants().EXCECUTION_ERROR -> {
                                toast.showCenterAlignedToast("Error in unpinning $error")
                            }

                            else -> {
                                toast.showCenterAlignedToast("Unknown error : UNPIN $error")
                            }
                        }
                    }
                }
            }
        }
    }

}