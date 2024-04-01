package com.omang.app.ui.home.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkInfo
import coil.load
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.omang.app.BuildConfig
import com.omang.app.R
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.databinding.ActivityHomeBinding
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.camera.activity.CameraActivity
import com.omang.app.ui.home.viewmodel.Bubble
import com.omang.app.ui.home.viewmodel.HomeViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.FullscreenHelper
import com.omang.app.utils.NotificationUtil
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.csdk.CSDKConstants
import com.omang.app.utils.csdk.CSDKLicenseReceiver
import com.omang.app.utils.deviceCallbacks.DeviceReceivers
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hideDeviceStatusDialog
import com.omang.app.utils.extensions.hideUserStatusDialog
import com.omang.app.utils.extensions.loadLocalEmoji
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.moodMeterDialog
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.showDeviceStatusDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.showUserStatusDialog
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.signal.NetworkSignalObserver
import com.omang.app.utils.signal.SignalObserver
import com.omang.app.utils.socket.SocketStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject


enum class CurrentActivity {
    HOME, PROFILE, TECH_SUPPORT, OTHER, TEST, EXPLORE, PDF
}

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private val WIFI_SETTINGS_REQUEST_CODE = 500

    val viewModel: HomeViewModel by viewModels()
    lateinit var dialog: Dialog

    @Inject
    lateinit var toast: ToastMessage

    private val csdkLicenseReceiver = CSDKLicenseReceiver()
    lateinit var binding: ActivityHomeBinding
    lateinit var navController: NavController
    private lateinit var batteryUpdateReceiver: BroadcastReceiver
    private var toolbar: Toolbar? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    val statusHandler = Handler(Looper.getMainLooper())
    private var wifiSelectionHandler = Handler(Looper.getMainLooper())
    private var flag: Boolean = false
    private var wifiSelectionRunnable = Runnable {
        val intent = Intent(this@HomeActivity, HomeActivity::class.java)
        val bundle = Bundle()
        bundle.putString("OPEN_FROM_WIFI_SETTINGS", "true")
        intent.putExtras(bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private val wifiSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                wifiSelectionHandler.removeCallbacks(wifiSelectionRunnable)
            }
        }

    private var deviceReceivers = DeviceReceivers()

    var adminFlag = false

    private var userName = ""
    private var appStarttime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullscreenHelper.enableFullscreen(this)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = Dialog(this@HomeActivity)

        val navView: BottomNavigationView = binding.navView

        toolbar = binding.lyToolbar.tbAppbar
        setSupportActionBar(toolbar)
        firebaseAnalytics = Firebase.analytics
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)
        navView.menu.findItem(R.id.navigation_camera).setOnMenuItemClickListener {
            startActivity(Intent(this@HomeActivity, CameraActivity::class.java))
            true
        }

        //removes the badges, if it is present
        //clears the post data when navigating to the bottom nav fragments
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_notifications -> {
                    navView.removeBadge(R.id.navigation_notifications)
                    viewModel.clearPostData()
                }

                R.id.testFragment -> {
                    navView.removeBadge(R.id.navigation_test)
                    viewModel.clearPostData()
                }

                R.id.navigation_home -> {
                    viewModel.clearPostData()
                }

                R.id.navigation_camera -> {
                    viewModel.clearPostData()
                }
            }
        }

        val bundle = intent.extras
        if (bundle != null) {
            if (bundle.containsKey("OPEN_FROM_WIFI_SETTINGS")) {
                showWifiSelectionTimeoutPopup()
            }
        }

        initCSDK()
        initSocket()
        viewModel.getSummaryData()
        viewModel.getAvailableNetworkStatus(this)
        observeUserData()
        observeData()
        bindView()
        registerReceivers()
        getFCMToken()
        viewModel.locationWorker(this)
        viewModel.updateDeviceStatus()

        viewModel.sharedPref.logs = "" // clearing log in app restart
    }

    private fun registerReceivers() {
        //register battery receiver
        batteryUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                binding.lyToolbar.txtBatIndicator.text = "$batteryPercentage%"
                binding.lyToolbar.imgBattery.setImageResource(
                    setBatteryIconResource(
                        batteryPercentage

                    )
                )
            }
        }
        registerReceiver(batteryUpdateReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        deviceReceivers.onScreenOffEvents = {
            viewModel.handlePsm()
        }

        IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SHUTDOWN)
            addAction(Intent.ACTION_REBOOT)
            addAction(Intent.ACTION_SCREEN_ON)
        }.also {
            //register device-unlock status for PSM
            registerReceiver(deviceReceivers, it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMoodMeter(name: String?) {

        val calendar = Calendar.getInstance()
        val currentDay = calendar[Calendar.DAY_OF_MONTH]
        if (name != null) {
            moodMeterDialog(
                dialog,
                this,
                name
            ) { modeId, modeName ->

                viewModel.setLastModeDate(currentDay)
//                viewModel.setMoodMeterTime(ViewUtil.getUtcTime(), modeId)
                viewModel.insertClickedData(ViewUtil.getUtcTime(), modeId, modeName)
            }
        }
    }

    private fun initSocket() {
        viewModel.socketInit()
    }


    private fun networkObserver() {
        val syncDialog = Dialog(this, R.style.mDialogTheme)
        viewModel.isSyncing.observe(this) { state ->
            when (state) {
                NetworkLoadingState.LOADING -> {
                    loadingDialog(syncDialog, true, this)
                }

                NetworkLoadingState.SUCCESS -> {
                    loadingDialog(syncDialog, false, this)
                }

                NetworkLoadingState.ERROR -> {
                    loadingDialog(syncDialog, false, this)
                }
            }
        }

    }

    private fun observeData() {
        with(viewModel) {
            isConnectionStatus.observe(this@HomeActivity) {
                if (it.networkStatus) {
                    when (it.networkType) {
                        "WIFI" -> {
                            setViewWifiNetwork()
                        }

                        "MOBILE" -> {
                            setViewMobileNetwork()
                        }

                        "NONE" -> {
                            setViewDisableNetwork()
                        }
                    }
                } else {
                    setViewDisableNetwork()
                }
            }

            lifecycleScope.launch {
                NetworkSignalObserver(this@HomeActivity).observe().collect {
//                    Timber.d("Signal strength is $it")
                    withContext(Dispatchers.Main) {
                        updateSignal(it)
                    }
                }

            }

            deviceUpdatesResponse.observe(this@HomeActivity) { response ->
                response.priority?.let { priorityList ->
                    priorityList.forEach { item ->
                        if (item?.updateType == NotificationUtil.MANUAL_CLEAN_DEVICE.value()) {
                            try {
                                manualCleaning(
                                    item.details.storageCleanUp.idealFreeStorageInPercentage,
                                    item.details.storageCleanUp.deleteThresholdInDays
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            handleUpdates(item?.updateType!!)
                        }
                    }
                }
            }

            deviceLockStatus.observe(this@HomeActivity) {
                it?.let { isLocked ->
                    Timber.tag("HomeActivity").d("Device lock status $it")
                    if (isLocked) showDeviceStatusDialog(
                        getString(R.string.device_lock),
                        viewModel.getDeviceLockMessage()
                    ) else
                        hideDeviceStatusDialog()
                }
            }

            userStatus.observe(this@HomeActivity) {
                it?.let { isLocked ->
                    Timber.tag("HomeActivity").d("Device lock status $it")
                    if (!isLocked) showUserStatusDialog(
                        userName + " " + getString(R.string.user_locked), ""
                    ) else
                        hideUserStatusDialog()
                }
            }

            alert.observe(this@HomeActivity) { detailsItems ->
                detailsItems?.let {
                    customNotification(it, true)
                }
            }

            networkStatus.observe(this@HomeActivity) { hasInternet ->
                if (hasInternet && !viewModel.isSocketConnected()) {
                    viewModel.socketInit()

                }
                if (hasInternet) {
                    viewModel.startTicketWorker(this@HomeActivity)
                    viewModel.startAnalyticsWorker(this@HomeActivity)
                    viewModel.startTestWorker(this@HomeActivity)
                }
            }

            uiMessageStateLiveData.observe(this@HomeActivity) {
                it?.let {
                    when (it) {
                        is UIMessageState.StringMessage -> {
                            showToast(it.message)
                            resetUIUpdate()
                        }

                        is UIMessageState.StringResourceMessage -> {
                            showToast(getString(it.resId))
                            resetUIUpdate()
                        }

                        else -> {
                            return@let
                        }
                    }
                }
            }

            socketStatus.observe(this@HomeActivity) {
                it?.let {

                    Timber.tag("WebSocket").e("received $it")
                    binding.apply {
                        when (it) {
                            SocketStatus.Connected -> {
                                viewModel.setSocketStatus(true)
                                tvStatusLogic(it)
                            }

                            SocketStatus.Connecting -> {
                                viewModel.setSocketStatus(false)
                                tvStatusLogic(it)
                            }

                            SocketStatus.Disconnected -> {
                                viewModel.setSocketStatus(false)
                                tvStatusLogic(it)
                            }

                            is SocketStatus.NewNotification -> {
                                Timber.tag("WebSocket")
                                    .e("message = ${it.newNotificationResponse.data?.message?.messageText}")
                                Timber.tag("WebSocket")
                                    .e("firstName = ${it.newNotificationResponse.data?.message?.user?.firstName}")
                                tvNotification.text = it.newNotificationResponse.data?.let { data ->
                                    val user = data.message?.user
                                    val messageText = data.message?.messageText
                                    if (messageText.isNullOrEmpty()) {
                                        ""
                                    } else {
                                        "${user?.firstName ?: ""} :\n${messageText}"
                                    }
                                } ?: ""
                                tvNotification.visible()
                            }

                            else -> {
                            }
                        }
                    }
                }
            }

            psmStatus.observe(this@HomeActivity) { psmData ->
                if (psmData.status)
                    showPsmDialog(psmData.image)
            }

            bubble.observe(this@HomeActivity) {
                Timber.e("notification bubble: $it")
                showNotificationBadge(it)
            }

            showMoodMeterDialog.observe(this@HomeActivity) {
                showMoodMeter(it)
            }

            baseProgress.observe(this@HomeActivity) {
                when (it) {
                    NetworkLoadingState.LOADING -> {}
                    NetworkLoadingState.SUCCESS -> getSummaryData()
                    NetworkLoadingState.ERROR -> {}
                    null -> {}
                }
            }

            //if testWork is completed, we are calling summary API to update the classroom progress
            liveTestWorkStatus.observe(this@HomeActivity) {
                if (it.state == WorkInfo.State.SUCCEEDED) {
                    getSummaryData()
                }
            }
        }
    }

    private fun setViewWifiNetwork() {
        binding.apply {
            lyToolbar.apply {
                ivNetwork.visible()
                ivNetwork.setImageResource(R.drawable.wifi_blue)
                if (isSimCardInserted()) {
                    ivSim.visible()
                    ivSim.setImageResource(R.drawable.sim_inserted_blue)
                }

            }
        }
    }

    private fun setViewMobileNetwork() {
        binding.apply {
            lyToolbar.apply {
                ivSim.visible()
                ivNetwork.gone()
                ivSim.setImageResource(R.drawable.mobile_data_blue)
            }
        }
    }

    private fun setViewDisableNetwork() {
        binding.apply {
            lyToolbar.apply {
                if (isSimCardInserted()) {
                    ivSim.visible()
                    ivSim.setImageResource(R.drawable.sim_inserted_blue)
                } else {
                    ivSim.visible()
                    ivSim.setImageResource(R.drawable.sim_not_inserted)
                }
                ivNetwork.gone()
            }
        }
    }


    private fun updateSignal(status: SignalObserver.Status) {
        binding.lyToolbar.ivSignal.setImageResource(viewModel.getSignalDrawable(status))
    }

    //adds the notification badge in bottom navigation
    private fun showNotificationBadge(it: Bubble?) {
        when (it!!) {
            Bubble.CLASSROOM -> {}
            Bubble.NOTIFICATION -> {
                addBadge(R.id.navigation_notifications)
            }

            Bubble.TEST -> {
                addBadge(R.id.navigation_test)
            }
        }
    }

    fun openWifi() {
        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))
        wifiSelectionHandler.postDelayed(wifiSelectionRunnable, 30000)
    }

    @Deprecated(
        "Deprecated in Java", ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            WIFI_SETTINGS_REQUEST_CODE -> wifiSelectionHandler.removeCallbacks(wifiSelectionRunnable)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    //handle the priority response
    private fun handleUpdates(id: Int) {
        when (id) {
            NotificationUtil.LOCK_DEVICE.value() -> viewModel.setDeviceLockStatus(true)
            NotificationUtil.UNLOCK_DEVICE.value() -> viewModel.setDeviceLockStatus(false)
            NotificationUtil.USER_ASSIGN.value() -> userAssign()
            NotificationUtil.USER_UNASSIGN.value() -> userUnAssign()
            NotificationUtil.PIN_DEVICE.value() -> {
                showToast("check-gate-for:$id")
                pinDevice()

            }
            NotificationUtil.UNPIN_DEVICE.value() -> {
                showToast("check-gate-for:$id")
                unpinDevice()
            }
            NotificationUtil.USER_ACTIVATE.value() -> viewModel.setUserActiveStatus(true)
            NotificationUtil.USER_DEACTIVATE.value() -> viewModel.setUserActiveStatus(false)
            NotificationUtil.APP_UPDATE.value() -> { // transaction to notice app update to user
                val i = Intent(applicationContext, AppUpdateActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(i)
                finish()
            }

            NotificationUtil.ANALYTICS.value() -> {
                viewModel.startAnalyticsWorkerImmediate(this@HomeActivity)
            }

            else -> Timber.v("--> Priority Type :${id}")
        }
    }

    private fun userUnAssign() {
        viewModel.setDeviceAssignedStatus(false)
        showDeviceStatusDialog(
            getString(R.string.unassign_user),
            getString(R.string.unassign_description, viewModel.getDeviceImei())
        )
    }

    private fun userAssign() {
        viewModel.setDeviceAssignedStatus(true)
        viewModel.getSummaryData()
        hideDeviceStatusDialog()
    }

    override fun onResume() {
        super.onResume()

        appStarttime = ViewUtil.getUtcTimeWithMSec()

        try {
            wifiSelectionHandler.removeCallbacks(wifiSelectionRunnable)
        } catch (e: Exception) {
            // ignore
        }

        viewModel.socketReconnect()
        viewModel.checkDeviceLockStatus()
        viewModel.checkUserStatus()
        networkObserver()

        viewModel.checkForDateMoodMeterOncePerDay()
        viewModel.submitMoodMeterData()
//        viewModel.sendTestResultsToServer()
    }

    override fun onPause() {
        super.onPause()
        viewModel.socketDisconnect()
        viewModel.insertSleepAwake(
            AnalyticsEntity(
                startTime = appStarttime,
                endTime = ViewUtil.getUtcTimeWithMSec()
            )
        )
    }

    fun unPinDevice() {
        val handler = Handler(Looper.getMainLooper())
        viewModel.setPinnedStatus(false)
        viewModel.updateDeviceStatus()
        lifecycleScope.launch {
            viewModel.exitKiosk(this@HomeActivity) { result, error ->
                if (result != null) {
                    handler.post {
                        when (result) {
                            CSDKConstants().EXCECUTION_SUCCESS -> {
                                toast.showCenterAlignedToast("Device unpinned")
                            }

                            CSDKConstants().EXCECUTION_ERROR -> {
                                toast.showCenterAlignedToast("Error in unpinning $error")
                            }

                            else -> toast.showCenterAlignedToast("Unknown error : UNPIN $error")

                        }
                    }
                }
            }
        }
    }

    private fun getFCMToken() {
        // Generate FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Timber.d("FCM token : $token")
                // Handle the FCM token
                // For example, you can store the token in SharedPreferences or send it to your backend.
                // Implement your logic here...
            } else {
                // Handle token generation error
                // Implement error handling logic here...
            }
        }
    }

    private fun initCSDK() {
        lifecycleScope.launch {
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

    fun csdkUtil() {
//        viewModel.getSummaryData()

        /*if (viewModel.getTestStatus() == true) {
            viewModel.setTestStatus(false)
            viewModel.runTestCSDK5(false)

        } else {
            viewModel.setTestStatus(true)
            viewModel.runTestCSDK5(true)

        }*/

        /*
        * only for testing in csdk 5.0
        */

        /*        viewModel.setDeviceOwner()
                unpinDevice()*/

//        viewModel.socketInit()

//        Timber.e("hasInternetConnection ${this@HomeActivity.hasInternetConnection()}")
//        Timber.e(" IMEI : ${viewModel.getDeviceInfo(CSDKConstants.Device.IMEI)}")
//        Timber.e(" convertTimestampToLocale : " + "${convertTimestampToLocale("2023-12-14T10:03:35.895Z", DateTimeFormat.TO_LOCALE_TIME_N_DATE)}")

//        viewModel.fetchLocation(this@HomeActivity)
//        viewModel.setPeriodicWorker(this)
//        viewModel.startAnalyticsWorker(this)

        /*        this@HomeActivity.applicationContext.startService(
                    Intent(
                        applicationContext,
                        CallService::class.java
                    ).putExtra("phone", "9879879879")
                )*/

        /*     viewModel.testSystem(!taskBar)
             taskBar = !taskBar*/

//        viewModel.getAllAnalytics()

//        viewModel.testInstallation(false)

//        Timber.e("storage in percentage ${FileUtil.getStorageInPercentage(applicationContext)}")

//        viewModel.fileCleaning()

//        viewModel.testInstallation(true)
/*
        Firebase.crashlytics.log("clicked on csdk button")
        throw RuntimeException("test crash from  dev")*/

        navigateToLegacyWV()
    }


    fun restartApp() {
        viewModel.restartApp(this)
    }

    private fun observeUserData() {
        viewModel.getUserData()
        viewModel.user.observe(this@HomeActivity) {
            it?.let { userData ->
                val firstName = userData.firstName
                val lastName = userData.lastName
                val school = userData.school
                val point = userData.dropPoints

                binding.apply {
                    lyToolbar.tvUserName.text = "$firstName $lastName"
                    lyToolbar.tvSchool.text = school?.name
                    lyToolbar.tvSchool.isSelected = true
                    lyToolbar.txtDropBalance.text = point.toString()

                    lyToolbar.ivUser.load(userData.avatar)

                    if (userData.avatar != null) {
                        userData.avatar.let {
                            lyToolbar.ivUser.loadLocalEmoji(userData.avatar)
                        }
                    }
                }

                userName = "$firstName $lastName"
                firebaseAnalytics.setUserProperty(ANALYTICS_IMEI, viewModel.getDeviceImei())
                firebaseAnalytics.setUserProperty(ANALYTICS_USER_NAME, "$firstName $lastName")
                firebaseAnalytics.setUserProperty(
                    ANALYTICS_ENVIRONMENT,
                    BuildConfig.FLAVOR + BuildConfig.BUILD_TYPE
                )
                viewModel.setFirebaseKeys(userData)

            }
        }

        viewModel.updateDpPill.observe(this@HomeActivity) {
            it?.let {
                Timber.tag("set in UI param URL").v("--> $it")
                binding.lyToolbar.ivUser.loadLocalEmoji(it)
            }
        }
    }

    private fun bindView() {
        binding.apply {
            lyToolbar.apply {
                cvBack.setOnClickListener {
                    if (!viewModel.backNavigationDisabled)
                        navController.popBackStack()
                }
                imgCubetlogo.setOnClickListener {
                    showMessagePopup()
                }
                rlNotification.setOnClickListener {
                    navController.navigate(R.id.navigation_notifications)
                }
                ivSettings.setOnClickListener {
                    showSettingsPopup()
                }
                cvUser.setOnClickListener {
                    //viewModel.sendTestResultsToServer()
                    navController.safeNavigateToProfile()
                }
                ivHome.setSafeOnClickListener {
                    navController.navigate(R.id.navigation_add)
                }

                /*  imgBrightness.setOnClickListener {
                       showAdjustBrightnessPopup()
                   }*/
                /*  cvDropBalance.setOnClickListener {
                       navController.navigate(R.id.action_navigation_home_to_navigation_profile2)
                  }*/
                /*  btnWifi.setOnClickListener {
                       showWifiSettings()
                      }*/
                /*  btnCamera.setOnClickListener {
                       startActivity(Intent(this@HomeActivity, CameraActivity::class.java))
                   }*/
                tbAppbar.setNavigationOnClickListener {
                    navController.popBackStack()
                }
                ivBackProfile.setOnClickListener {
                    navController.popBackStack()
                }
                when (getActiveNetworkType()) {
                    NetworkType.WIFI -> {
                        ivNetwork.visible()
                        ivNetwork.setImageResource(R.drawable.wifi_blue)
                    }

                    NetworkType.MOBILE -> {
                        ivNetwork.visible()
                        ivNetwork.setImageResource(R.drawable.mobile_data_blue)
                        ivSim.gone()
                    }

                    NetworkType.NONE -> {
                        ivNetwork.gone()
                    }
                }
                if (isSimCardInserted()) {
                    ivSim.visible()
                    ivSim.setImageResource(R.drawable.sim_inserted_blue)
                } else {
                    ivSim.visible()
                    ivSim.setImageResource(R.drawable.sim_not_inserted)
                }

                imgOrientation.setOnClickListener {
                    onChangeOrientation()
                }
            }

            fabSupport.setOnClickListener {
                navigateToTechSupport()
            }

            tvNotification.setOnClickListener {
                navigateToTechSupport()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity") // Suppress warning (use responsibly!)
    private fun onChangeOrientation() {
        val currentOrientation = resources.configuration.orientation

        val desiredOrientation =
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (desiredOrientation != requestedOrientation) {
            requestedOrientation = desiredOrientation
        }
    }

    @SuppressLint("SourceLockedOrientationActivity") // Suppress warning (use responsibly!)
    private fun onChangeOrientationToDefault() {
        val currentOrientation = resources.configuration.orientation
        if (currentOrientation != Configuration.ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun isSimCardInserted(): Boolean {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.simState == TelephonyManager.SIM_STATE_READY
    }

    private fun getActiveNetworkType(): NetworkType {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities =
            connectivityManager.getNetworkCapabilities(network)
        return when {

            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkType.WIFI
            }

            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                NetworkType.WIFI
            }

            capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                NetworkType.MOBILE
            }


            else -> {
                NetworkType.NONE
            }
        }
    }

    private fun navigateToTechSupport() {
        binding.apply {
            tvNotification.text = ""
            tvNotification.gone()
            navController.navigate(R.id.action_anyFragment_to_technical_support_fragment)

        }
    }

    private fun navigateToLegacyWV() {
        binding.apply {
            navController.navigate(R.id.action_anyFragment_to_legacy_web_viewer)
        }
    }

    /**
     * Configure the toolbar title
     */
    fun configureToolbar(
        currentActivity: CurrentActivity = CurrentActivity.HOME,
        titleResourceId: Int = R.string.title_home,
        title: String = "",
    ) {
        Timber.e("Current activity $currentActivity")
        if (::binding.isInitialized) {
            when (currentActivity) {
                CurrentActivity.HOME -> {
                    binding.lyToolbar.apply {
                        cvBack.gone()
                        ivBackProfile.gone()
                        cvUser.visible()
                        cvTest.gone()
                        ivBackProfile
                        imgOrientation.gone()
                    }
                    binding.fabSupport.visibility = View.VISIBLE
                    adminFlag = true
                    onChangeOrientationToDefault()

                }

                CurrentActivity.PROFILE -> {
                    binding.lyToolbar.apply {
                        cvBack.gone()
                        cvUser.visible()
                        cvTest.gone()
                        ivBackProfile.visible()
                        tvCurrentActivity.text = getString(titleResourceId)
                        imgOrientation.gone()

                    }
                    binding.fabSupport.visibility = View.VISIBLE
                    adminFlag = false
                    onChangeOrientationToDefault()

                }

                CurrentActivity.TECH_SUPPORT -> {
                    binding.lyToolbar.apply {
                        cvUser.gone()
                        ivBackProfile.gone()
                        cvBack.visible()
                        cvTest.gone()
                        tvCurrentActivity.text = if (title != "")
                            title
                        else
                            getString(titleResourceId)
                        imgOrientation.gone()
                    }
                    binding.fabSupport.visibility = View.GONE
                    adminFlag = false
                    onChangeOrientationToDefault()

                }

                CurrentActivity.TEST -> {
                    binding.lyToolbar.apply {
                        cvUser.gone()
                        ivBackProfile.gone()
                        cvBack.gone()
                        cvTest.visible()
                        tvCurrentActivity.text = getString(titleResourceId)
                        imgOrientation.gone()
                    }
                    binding.fabSupport.gone()
                    adminFlag = false
                    onChangeOrientationToDefault()

                }

                CurrentActivity.EXPLORE -> {
                    binding.lyToolbar.apply {
                        cvUser.gone()
                        ivBackProfile.gone()
                        cvBack.visible()
                        cvTest.gone()
                        tvCurrentActivity.text = if (title != "")
                            title
                        else
                            getString(titleResourceId)
                        imgOrientation.visible()

                    }
                    binding.fabSupport.visibility = View.VISIBLE
                    adminFlag = false

                }

                CurrentActivity.PDF -> {
                    binding.lyToolbar.apply {
                        cvUser.gone()
                        ivBackProfile.gone()
                        cvBack.visible()
                        cvTest.gone()
                        tvCurrentActivity.text = if (title != "")
                            title
                        else
                            getString(titleResourceId)
                        imgOrientation.visible()

                    }
                    binding.fabSupport.visibility = View.VISIBLE
                    adminFlag = false

                }

                CurrentActivity.OTHER -> {
                    binding.lyToolbar.apply {
                        cvUser.gone()
                        ivBackProfile.gone()
                        cvBack.visible()
                        cvTest.gone()
                        tvCurrentActivity.text = if (title != "")
                            title
                        else
                            getString(titleResourceId)
                        imgOrientation.gone()

                    }
                    binding.fabSupport.visibility = View.VISIBLE
                    adminFlag = false
                    onChangeOrientationToDefault()

                }

            }
        }
    }

    fun hideBottomNavigation(flag: Boolean) {
        viewModel.canNavigateBack(flag)
        if (::binding.isInitialized) {
            binding.apply {
                if (flag) {
                    navView.gone()
                    ivHome.gone()
                } else {
                    ivHome.visible()
                    navView.visible()
                }
            }
        }
    }

    fun setDeviceFullScreen(flag: Boolean) {
        if (::binding.isInitialized) {
        binding.apply {
            if (flag) {
                navView.gone()
                lyToolbar.root.gone()
                ivHome.gone()
                ivPlus.gone()
            } else {
                navView.visible()
                lyToolbar.root.visible()
                ivHome.visible()
                ivPlus.visible()
            }
        }
    }
    }

    fun showPsmByClassroomId(classroomId: Int) {
        viewModel.getPsmByClassroom(classroomId)
    }

    private fun unlockDevice() {

    }

    private fun pinDevice() {
        viewModel.setDeviceOwner()
        runOnUiThread {
            showPinDialog()
        }
    }

    private fun unpinDevice() {
        runOnUiThread {
            try {
                showUnpinDialog()

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }


    fun removeWebViewInstance(webView: WebView?) {
        webView?.destroy()
    }

    private fun addBadge(navigationId: Int) {
        with(binding.navView) {
            getOrCreateBadge(navigationId).apply {
                number += 1
                isVisible = true
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryUpdateReceiver)
            unregisterReceiver(csdkLicenseReceiver)
            unregisterReceiver(deviceReceivers)
            wifiSelectionHandler.removeCallbacks(wifiSelectionRunnable)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun WebView.run() {

    }

    companion object {
        private const val ANALYTICS_USER_NAME = "analytics_user_name"
        private const val ANALYTICS_IMEI = "analytics_imei"
        private const val ANALYTICS_ENVIRONMENT = "analytics_environment"
        var selectedClassroomId: Int? = null
    }

    enum class NetworkType {
        WIFI,
        MOBILE,
        NONE
    }

}




