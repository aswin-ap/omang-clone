package com.omang.app.ui.splash.activity


import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.omang.app.BuildConfig
import com.omang.app.data.database.DBConstants
import com.omang.app.databinding.ActivitySplashBinding
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.registration.activity.RegistrationActivity
import com.omang.app.ui.splash.viewmodel.SplashViewModel
import com.omang.app.utils.FullscreenHelper
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.csdk.CSDKConstants
import com.omang.app.utils.csdk.CSDKLicenseReceiver
import com.omang.app.utils.csdk.DeviceSystemAdministrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class LauncherActivity : AppCompatActivity() {
    private var permissions1 = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var permissions2 = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.FOREGROUND_SERVICE
    )

    private val viewModel: SplashViewModel by viewModels()
    private val csdkLicenseReceiver = CSDKLicenseReceiver()

    @Inject
    lateinit var toast: ToastMessage
    private lateinit var binding: ActivitySplashBinding

    private var isToNextScreenCalled = false
    private var isEnablePerShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullscreenHelper.enableFullscreen(this)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initCSDK()
        observe()
        observeScreenTransaction()

        Timber.e("Package id --> " + BuildConfig.APPLICATION_ID)
    }

    override fun onResume() {
        super.onResume()
        isEnablePerShowed = false
        viewModel.getInternetStatus()
//        viewModel.getNavigationLogs()
    }

    private fun observe() {
        with(viewModel) {
            internet.observe(this@LauncherActivity) { internet ->
                if (internet) {
                    Timber.tag("ERROR_3").e("observe internet : $internet ")
                    binding.tvError.visibility = View.GONE

                } else {
                    Timber.tag("ERROR_3").e("observe internet else: $internet ")
                    binding.tvError.visibility = View.VISIBLE

                }
                initLogic(internet)
            }
        }
    }

    private fun initLogic(hasInternet : Boolean){
        if (!isEnablePerShowed) {
            isEnablePerShowed = true
            enableDeviceAdminAppPermission(hasInternet)

        }
    }

    private fun initCSDK() {
        lifecycleScope.launch {
            Timber.v("viewModel.isCSDKLicenseActivated() : ${viewModel.isCSDKLicenseActivated()}")
            if (viewModel.isCSDKLicenseActivated()) {
                toast.showToast("Safe Guard is activated")
                return@launch

            } else {

                csdkLicenseReceiver.setLicenseReceiverListener(object :
                    CSDKLicenseReceiver.LicenseReceiverListener {
                    override fun onLicenseReceived(result: Int) {
                        Timber.tag("CSDK").d("License received $result")

                        when (result) {
                            CSDKConstants().ERROR_NONE -> {
                                toast.showToast("SAFE GUARD LICENSE ACTIVATED")
                                viewModel.setCSDKLicenseStatus(true)
                                return
                            }

                            CSDKConstants().ERROR_INVALID_LICENSE -> toast.showToast("ERROR:112")
                            CSDKConstants().ERROR_NETWORK_DISCONNECTED -> toast.showToast("ERROR:113")
                            else -> toast.showToast(
                                "ERROR:114-$result-" + intent.getStringExtra(CSDKConstants().ERROR_DESC)
                            )
                        }
                    }
                })

                val dynamicIntentFilter = IntentFilter(CSDKConstants().ACTION)
                registerReceiver(csdkLicenseReceiver, dynamicIntentFilter)

                viewModel.activateCDSK()
            }
        }
    }

    private fun observeScreenTransaction() {
        with(viewModel) {
            isUserAssigned.observe(this@LauncherActivity) { isUserAssigned ->
                Timber.tag("ERROR_3").e("observeScreenTransaction $isUserAssigned")
                toNextScreen(isUserAssigned)

            }
        }
    }

    private fun noInternetScenario() {
        Timber.e("noInternetScenario")
        Timber.e("noInternetScenario 1 ${viewModel.isDeviceAdmin(this)} ")
        Timber.e("noInternetScenario 2 ${viewModel.checkPermissions(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2,
            this
        )} ")
        Timber.e("noInternetScenario 3 ${viewModel.isDefaultDialer(this)} ")
        Timber.e("noInternetScenario 4 ${Settings.System.canWrite(this)} ")
        Timber.e("noInternetScenario 5 ${viewModel.isUsageAccessEnabled(this)} ")
        Timber.e("noInternetScenario 6 ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true} ")
        if (viewModel.isDeviceAdmin(this)
            && viewModel.checkPermissions(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2,
                this
            )
            && viewModel.isDefaultDialer(this)
            && Settings.System.canWrite(this)
            && viewModel.isUsageAccessEnabled(this)
//            && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
            && viewModel.hasOverlayPermission(this) /*else true*/
        ) {
            Timber.e("noInternetScenario 1")
            runKiosk()

        }
    }

    private fun enableDeviceAdminAppPermission(hasInternet: Boolean) {
        Timber.e("enableDeviceAdminAppPermission")
        if (!viewModel.isDeviceAdmin(this)) {
            val mDeviceAdmin = ComponentName(this, DeviceSystemAdministrator::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
            startForResult.launch(intent)

        } else if (!viewModel.checkPermissions(
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2,
                this
            )
        ) {
            viewModel.requestPermission(
                applicationContext,
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) permissions1 else permissions2
            )

        } else if (!viewModel.isDefaultDialer(this)){
//            viewModel.requestDialerRole(this)


            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager?
            val intent = roleManager!!.createRequestRoleIntent(RoleManager.ROLE_DIALER)
//            startActivity(intent)
            startForResult.launch(intent)

        } else if (!Settings.System.canWrite(this)) {
            viewModel.launchSystemSettingWritePermission(this)

        } else if (!viewModel.isUsageAccessEnabled(this)) {
            viewModel.launchReadNetworkHistoryAccess(this)

        } /*else if (!viewModel.hasOverlayPermission(this)) {
            viewModel.launchOverlayPermission(this)
        }*/ // TODO hided for m8 .. blah blah blah

        // FIXME: Environment.isExternalStorageManager() return always same value
        /* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                viewModel.launchAllFileAccess(this)

            }

        }*/ else {
            Timber.e("TO REGISTRATION")

            if (hasInternet) {
                runKiosk()

            }else{
                noInternetScenario()

            }

        }
    }

    private fun runKiosk() {
        Timber.tag("ERROR_3").e("runKiosk")

        val handler = Handler(Looper.getMainLooper())

        lifecycleScope.launch {
            if (viewModel.isDeviceOwner()) {
                viewModel.runKiosk(this@LauncherActivity) { result, error ->
                    if (result != null) {
                        handler.post {
                            when (result) {
                                CSDKConstants().EXCECUTION_SUCCESS -> {
                                    toast.showCenterAlignedToast("Restriction are run successfully.")
                                    viewModel.setPinnedStatus(true)
                                    viewModel.toNextScreen()
                                }

                                CSDKConstants().EXCECUTION_ERROR -> {
                                    toast.showCenterAlignedToast("Error in restriction running $error")
                                }

                                else -> {
                                    toast.showCenterAlignedToast("Unknown error $error")
                                }
                            }
                        }
                    }
                }
            } else {
                Timber.tag("ERROR_3").e("runKiosk else")
                viewModel.toNextScreen()

            }
        }
    }

    private var startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            Timber.v("device owner permission --> " + result.resultCode)

        } else
            toast.showToast("device admin perms cancelled!")
    }

    private fun toNextScreen(isUserAssigned: Boolean) {
        Timber.tag("ERROR_3").e("toNextScreen")

        if (isToNextScreenCalled) return

        isToNextScreenCalled = true

        val i = if (!isUserAssigned) {
            Intent(applicationContext, RegistrationActivity::class.java)

        } else {
            addToMobileNavigation()
            Intent(applicationContext, AppUpdateActivity::class.java)

        }

        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(csdkLicenseReceiver)

        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
        }
    }

    private fun addToMobileNavigation() {
        viewModel.addToNavigation(
            page = SplashActivity::class.java.name,
            event = DBConstants.Event.VISIT,
            comment = "Visited splash page"
        )
        Timber.d("Splash : Inserted to db")
    }

}