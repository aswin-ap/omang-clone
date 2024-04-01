package com.omang.app.ui.appupdate.activity

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.omang.app.BuildConfig
import com.omang.app.R
import com.omang.app.data.database.DBConstants
import com.omang.app.data.model.appupdate.AppUpdate
import com.omang.app.databinding.ActivityAppUpdateBinding
import com.omang.app.ui.appupdate.viewmodel.AppUpdateViewModel
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.utils.DeviceUtil
import com.omang.app.utils.DownloadResult
import com.omang.app.utils.FullscreenHelper
import com.omang.app.utils.NotificationUtil
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil.isCurrentDateAfterForceUpdateDate
import com.omang.app.utils.appUpdate.OmangPackageInstaller
import com.omang.app.utils.deviceCallbacks.DeviceReceivers
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.hideDeviceStatusDialog
import com.omang.app.utils.extensions.hideUserStatusDialog
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.showDeviceStatusDialog
import com.omang.app.utils.extensions.showSettingsPopup
import com.omang.app.utils.extensions.showSnackBar
import com.omang.app.utils.extensions.showUserStatusDialog
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/*class where handles the app update */
@AndroidEntryPoint
class AppUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppUpdateBinding
    val viewModel: AppUpdateViewModel by viewModels()
    private var pi: PackageInstaller? = null

    private var isForceUpdate: Boolean = false

    @Inject
    lateinit var toast: ToastMessage

    private var userName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FullscreenHelper.enableFullscreen(this)
        Timber.e("----------AppUpdateActivity-------------")
        binding = ActivityAppUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //viewModel.getUser()
        initView()
        viewModel.observeNetworkStatus()
        observeData()

        binding.tvLog.movementMethod = ScrollingMovementMethod()

    }

    private fun observeUserData() {
        viewModel.getUserData()
        viewModel.user.observe(this@AppUpdateActivity) {
            it?.let { userData ->
                userName = "${userData.firstName} ${userData.lastName}"
            }
        }
    }

    /**
     * Function to observe the observable variables inside the ViewModel
     */
    private fun observeData() {
        observeUserData()
        with(viewModel) {
            downloadFile.observe(this@AppUpdateActivity) { result ->
                when (result) {
                    is DownloadResult.OnCancelListener -> TODO()
                    is DownloadResult.OnDownloadComplete -> {
                        binding.tvLog.append("\nDownload completed")

                        Timber.d("Download Completed")
                        binding.root.showSnackBar("Download Completed", true)
                        binding.downloadLayout.invisible()
                        disableOrEnableButtons(true)
                        installApk(result.apkFile)
                    }

                    is DownloadResult.OnError -> {
//                        disableOrEnableButtons(true)
                        binding.downloadLayout.invisible()
                        binding.btnSkip.isEnabled = true

                        binding.btnSkip.visibility =
                            if (isForceUpdate) View.GONE else View.VISIBLE

                        binding.btnUpdateNow.isUpdate(false)
                        binding.btnUpdateNow.visible()

                        binding.root.showSnackBar(
                            resources.getString(R.string.download_error)
                        )

                        Timber.e("Download error: ${result.error?.connectionException}")
                    }

                    is DownloadResult.OnPauseListener -> TODO()
                    is DownloadResult.OnProgressListener -> {
                        lifecycleScope.launch {
                            Timber.d("${result.progress}")
                            if (result.progress != 100) {
                                binding.pbDownload.progress = result.progress
                                binding.tvPercentage.text =
                                    getString(R.string.percentage, result.progress)
                                binding.tvLog.append("\n -> ${result.progress}")

                            } else binding.downloadLayout.invisible()
                        }
                    }

                    DownloadResult.OnStartListener -> {
                        binding.tvLog.append("\nfile download started")
                        binding.downloadLayout.visible()
                        disableOrEnableButtons(false)
                        Timber.d("Download ---> Start")
                    }

                    is DownloadResult.AlreadyDownloaded -> {
                        binding.tvLog.append("\nAlready downloaded")

                        binding.downloadLayout.invisible()
                        installApk(result.apkFile)
                    }
                }
            }

            networkStatus.observe(this@AppUpdateActivity) { hasInternet ->
                if (hasInternet) {
                    binding.btnUpdateNow.isUpdate(true)
                    getAppUpdates()
                } else {
                    handleInternetUi()
                }
            }

            //Observe api response
            appUpdateResponse.observe(this@AppUpdateActivity) {
                updateUi(it)
            }

            //observe message livedata
            uiMessageStateLiveData.observe(this@AppUpdateActivity) {
                it?.let {
                    when (it) {
                        is UIMessageState.StringMessage -> {
                            binding.pbLoading.gone()
                            showMessage(it.message, it.status)
                            viewModel.resetUIUpdate()
                            if (it.status) {
                                binding.apply {
                                    tvError.visible()
                                    tvError.text = getString(R.string.something_went_wrong)
                                    btnSkip.visible()
                                }
                            }
                        }

                        is UIMessageState.StringResourceMessage -> {
                            binding.pbLoading.gone()
                            showMessage(getString(it.resId), it.status)
                            viewModel.resetUIUpdate()
                            if (it.status) {
                                binding.apply {
                                    tvError.visible()
                                    tvError.text = getString(it.resId)
                                    btnSkip.visible()
                                }
                            }
                        }

                        else -> {
                            return@let
                        }
                    }
                }
            }

            //progressbar live data
            progress.observe(this@AppUpdateActivity) { value ->
                if (value) showLoading()
            }

            deviceUpdatesResponse.observe(this@AppUpdateActivity) { response ->
                response.priority?.let { priorityList ->
                    priorityList.forEach { item ->
                        handleUpdates(item?.updateType!!)
                    }
                }
            }

            deviceLockStatus.observe(this@AppUpdateActivity) {
                it?.let { isLocked ->
                    if (isLocked) showDeviceStatusDialog(
                        getString(R.string.device_lock), viewModel.getDeviceLockMessage()
                    ) else hideDeviceStatusDialog()
                }
            }

            userStatus.observe(this@AppUpdateActivity) {
                it?.let { isLocked ->
                    Timber.tag("HomeActivity").d("Device lock status $it")
                    if (!isLocked) showUserStatusDialog(
                        userName + " " + getString(R.string.user_locked), ""
                    ) else
                        hideUserStatusDialog()
                }
            }
        }
    }

    private fun Button.isUpdate(flag: Boolean) {
        if (flag) {
            text = "Update"
            binding.btnUpdateNow.setBackgroundColor(getColor(R.color.deep_blue))
        } else {
            text = "Retry"
            binding.btnUpdateNow.setBackgroundColor(getColor(R.color.orange))

        }

        isEnabled = true

    }

    private fun handleUpdates(updateType: Int) {
        when (updateType) {
            NotificationUtil.LOCK_DEVICE.value() -> viewModel.setDeviceLockStatus(true)
            NotificationUtil.UNLOCK_DEVICE.value() -> viewModel.setDeviceLockStatus(false)
            NotificationUtil.USER_ASSIGN.value() -> userAssign()
            NotificationUtil.USER_UNASSIGN.value() -> userUnAssign()
            NotificationUtil.USER_ACTIVATE.value() -> viewModel.setUserActiveStatus(true)
            NotificationUtil.USER_DEACTIVATE.value() -> viewModel.setUserActiveStatus(false)
            else -> Timber.v("--> Priority Type :${updateType}")
        }
    }

    //Updates the app-update ui
    private fun updateUi(data: AppUpdate?) {
        val currentAppVersion = DeviceUtil.getAppVersion()
        data?.let {

            data.forceUpdateDate?.let {
                isForceUpdate = data.isForceUpdate && it.isCurrentDateAfterForceUpdateDate()
            }

            binding.apply {
                if (AppUpdateViewModel.compareVersionNames(
                        currentAppVersion, data.appLatestVersion!!
                    ) != -1
                ) {
                    updateNoUpdatesUI()
                } else {
                    tvVersion.text =
                        resources.getString(
                            R.string.new_version,
                            it.appLatestVersion.toString()
                        )
                    tvDescription.text = it.releaseNote
                    ivUpdateDone.gone()
                    btnSkip.text = getString(R.string.skip)
                    btnSkip.visibility = if (isForceUpdate) View.GONE else View.VISIBLE
                    hideLoading()
                }
            }
        } ?: kotlin.run {
            updateNoUpdatesUI()
        }
    }

    private fun handleInternetUi() {

        binding.apply {
            tvError.visible()
            tvError.text = getString(R.string.no_internet_text)
            btnSkip.visible()
            ivUpdateDone.gone()
            btnUpdateNow.invisible()
            pbLoading.gone()
            tvWhatsNew.gone()
        }
        /*      val i = Intent(this, HomeActivity::class.java)
           i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
           startActivity(i)*/
    }

    private fun updateNoUpdatesUI() {
        binding.apply {
            tvError.visible()
            tvError.text = getString(R.string.no_updates)
            btnSkip.visible()
            ivUpdateDone.visible()
            btnSkip.text = getString(R.string.go_to_home)
            pbLoading.gone()
            tvWhatsNew.gone()
        }
        //TODO : Remove intent
        /*  val intent = Intent(applicationContext, HomeActivity::class.java)
          intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          startActivity(intent)
          finish()*/
    }

    private fun showMessage(message: String, status: Boolean) {
        binding.root.showSnackBar(message, status)
    }

    /**
     * Used to initialize the views. Like set click...etc
     */
    private fun initView() {

        viewModel.getDeviceUpdates()
        binding.pbDownload.max = 100
        binding.btnUpdateNow.setOnClickListener {

            if (applicationContext.hasInternetConnection()) {
                binding.btnUpdateNow.gone()
                viewModel.downloadApk()
            } else {
                binding.root.showSnackBar(
                    resources.getString(R.string.no_internet)
                )
            }
        }
        binding.ivSettings.setOnClickListener {
            showSettingsPopup()
        }
        binding.btnSkip.setOnClickListener {
            viewModel.addToNavigation(
                event = DBConstants.Event.CLICK,
                page = AppUpdateActivity::class.java.name,
                comment = "Clicked skip button",
            )
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        }
    }

    private var wifiSelectionHandler = Handler(Looper.getMainLooper())
    private var wifiSelectionRunnable = Runnable {
        val intent = Intent(this@AppUpdateActivity, AppUpdateActivity::class.java)
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

    fun openWifi() {

        wifiSettingsLauncher.launch(Intent(Settings.ACTION_WIFI_SETTINGS))

        wifiSelectionHandler.postDelayed(wifiSelectionRunnable, 30000)
    }

    private fun userAssign() {
        viewModel.setDeviceAssignedStatus(true)
        hideDeviceStatusDialog()
        val i = Intent(this, HomeActivity::class.java)
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

    private fun lockDevice() {
        viewModel.setDeviceLockStatus(true)
    }

    private fun unlockDevice() {
        viewModel.setDeviceLockStatus(false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkDeviceLockStatus()
        viewModel.checkUserStatus()
    }

    private fun showLoading() {
        binding.apply {
            tvUpdateDate.gone()
            tvWhatsNew.text = getString(R.string.check_update)
            tvVersion.gone()
            tvUpdateQuote.gone()
            tvDescription.gone()
            downloadLayout.gone()
            btnUpdateNow.gone()
            tvError.gone()
            btnSkip.gone()
            ivUpdateDone.gone()
            pbLoading.visible()
        }
    }

    private fun hideLoading() {
        binding.apply {
            tvUpdateDate.visible()
            tvWhatsNew.visible()
            tvWhatsNew.text = getString(R.string.whats_new_ver)
            tvVersion.visible()
            //tvUpdateQuote.visible()
            tvDescription.visible()
            btnUpdateNow.visible()
            downloadLayout.gone()
            pbLoading.gone()
        }
    }

    //enable status of the update and skip button
    private fun disableOrEnableButtons(enable: Boolean) {
        binding.apply {
            btnSkip.isEnabled = enable
            btnSkip.visibility = View.GONE
            btnUpdateNow.isEnabled = enable
        }
    }

    private fun installApk(apkFile: File) {
        viewModel.allowAppInstallation()
        Timber.v("APK file : $apkFile")
        Timber.v("APK file can read: ${apkFile.canRead()}")
        Timber.v("flavour  ${BuildConfig.FLAVOR}")

        binding.apply {
            tvError.text = "Installing the new version, Please wait a moment. "
            tvError.visible()
        }

        if (BuildConfig.FLAVOR == "android13") {

            /*
            * as the condition says, this is where the tb310xu's app update being init..  */


            /*
            * very imp, only God and me knows how this been fixed and working in prod or any envs.
            * please take responsibility while changing anything in here, a miss interchanged line may occur
            * unexpected turns of events
            * */

            install(apkFile)

        } else {
            OmangPackageInstaller.installPackage(
                applicationContext,
                apkFile
            )
        }
    }


    private val mSessionCallback: PackageInstaller.SessionCallback =
        object : PackageInstaller.SessionCallback() {
            override fun onCreated(sessionId: Int) {
                Timber.e("Session create,id:$sessionId\n")
                binding.tvLog.append("\nSession create, id: $sessionId")
            }

            override fun onBadgingChanged(sessionId: Int) {
                binding.tvLog.append("\nonBadgingChanged : " + sessionId.toString())
            }

            override fun onActiveChanged(sessionId: Int, active: Boolean) {
                Timber.e("Session active changed,id:$sessionId, active:$active\n")
                binding.tvLog.append("\nSession active changed, id: $sessionId, active: $active")
            }

            override fun onProgressChanged(sessionId: Int, progress: Float) {
                Timber.e("Progress changed,id:$sessionId, progress:$progress\n")
                binding.tvLog.append("\nProgress changed, id: $sessionId, progress: $progress")

            }

            override fun onFinished(sessionId: Int, success: Boolean) {
                Timber.e("Install task finished,id:$sessionId, success:$success\n")
                binding.tvLog.append("\nInstall task finished,id:$sessionId, success:$success")
            }
        }

    private fun install(file: File) {

        val params = SessionParams(
            SessionParams.MODE_FULL_INSTALL
        )

        try {
            Timber.e("packageName $packageName")
            Timber.e("apk trying to install $file")
            binding.tvLog.append("\npackageName: $packageName")
            binding.tvLog.append("\napk trying to install: $file")

            installPackage(file, packageName, params, applicationContext, mSessionCallback)
        } catch (e: IOException) {
            Timber.e("Install error:" + Log.getStackTraceString(e))
            binding.tvLog.append("\n" + e)

        } finally {
        }
    }

    @Throws(IOException::class)
    private fun copyStream(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(50 * 1024)
        var numRead: Int
        while (`in`.read(buffer).also { numRead = it } != -1) {
            out.write(buffer, 0, numRead)
        }
    }

    @Throws(IOException::class)
    private fun installPackage(
        source: File,
        packageName: String,
        params: SessionParams,
        context: Context,
        sessionCallback: PackageInstaller.SessionCallback
    ) {
        pi = context.packageManager.packageInstaller

        pi!!.registerSessionCallback(sessionCallback)
        val sessionId = pi!!.createSession(params)
        pi!!.openSession(sessionId).use { session ->
            try {
                FileInputStream(source).use { `in` ->
                    session.openWrite(source.name, 0, -1).use { out ->
                        copyStream(
                            `in`,
                            out
                        )
                    }
                }
            } catch (e: IOException) {
                session.abandon()
                binding.tvLog.append(binding.tvLog.text.toString() + "\nexception : ${e}")
            }

            val ACTION_DONE = "com.lenovo.ACTION_PACKAGE_INSTALL_DONE"

            val intent = Intent(context.applicationContext, DeviceReceivers::class.java)
//            val intent = Intent(ACTION_DONE)

            intent.setPackage(packageName)
            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                sessionId,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            session.commit(pendingIntent.intentSender)
        }
    }

}