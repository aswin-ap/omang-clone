package com.omang.app.ui.appupdate.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.user.User
import com.omang.app.data.model.appupdate.AppUpdate
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.fcm.Payload
import com.omang.app.data.model.updates.DeviceUpdatesData
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.utils.DeviceUtil
import com.omang.app.utils.DownloadResult
import com.omang.app.utils.FileUtil
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.hasInternetConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.schedule


@HiltViewModel
class AppUpdateViewModel @Inject constructor(
    val app: Application,
) : BaseViewModel(app) {

    @Inject
    lateinit var networkConnectionManager: NetworkConnection

    private val _downloadFile = MutableLiveData<DownloadResult>()
    val downloadFile: LiveData<DownloadResult> get() = _downloadFile

    private val _appUpdateResponse = MutableLiveData<AppUpdate?>()
    val appUpdateResponse: MutableLiveData<AppUpdate?> = _appUpdateResponse

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _progress = MutableLiveData(false)
    val progress: LiveData<Boolean> get() = _progress

    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    private val _deviceUpdatesResponse = MutableLiveData<DeviceUpdatesData>()
    val deviceUpdatesResponse: LiveData<DeviceUpdatesData> = _deviceUpdatesResponse

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageReceived(payload: Payload) {
        Timber.tag("HomeViewModel: onMessageReceived").v("update type ${payload.updateType}")
        savePayloadBody(payload)
        when (payload.updateType) {
            100 -> getDeviceUpdates()
            101 -> getDeviceUpdates()
            102 -> getDeviceUpdates()
            103 -> getDeviceUpdates()
            104, 105, 111, 116 -> getDeviceUpdates()
            else -> Timber.e("--> ${payload.updateType}")
        }
    }

    fun getDeviceUpdates() {
        viewModelScope.launch {
            deviceRepository.getDeviceUpdates().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data.let { data ->
                            val deleteUpdateRequest = DeleteUpdateRequest()

                            _deviceUpdatesResponse.value = data // priority

                            for (added in data.added!!) {
                                for (detail in added!!.details!!) {
                                    detail?.classroom?.let {
                                        detail.id.let { it1 -> deleteUpdateRequest.ids.add(it1) } // add id to remove from updates
                                    }
                                }
                            }

                            data.priority?.forEach { item ->
                                deleteUpdateRequest.ids.add(item!!.id) // add id to remove from updates
                            }

                            /*
                        * delete download api call for remove content from updates api*/
                            if (deleteUpdateRequest.ids.isNotEmpty()) {
                                getDeleteUpdates(deleteUpdateRequest)
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        Timber.e("${result.code} ${result.message}")
                        _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                            false, result.message
                        )
                    }

                    is NetworkResult.Failure -> {
                        Timber.e(result.exception)
                        _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                            true, R.string.something_went_wrong
                        )
                    }

                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun observeNetworkStatus() {
        // networkConnectionManager.startListenNetworkState()
        networkConnectionManager.observeNetworkConnection().onEach {
            _networkStatus.postValue(it)
            Timber.d("NetworkStatus : $it")
        }.launchIn(viewModelScope)
    }

    fun getUserData() {
        viewModelScope.launch {
            _user.value = userRepository.getUsers()
        }
    }

    fun getAppUpdates() {
        viewModelScope.launch() { ->
            _progress.value = true
//            delay(2000)
            deviceRepository.getAppUpdates().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _progress.value = false
                        result.data.let { data ->
                            _appUpdateResponse.postValue(data.appUpdate)
                        }
                    }

                    is NetworkResult.Error -> {
                        _progress.value = false
                        Timber.e("${result.code} ${result.message}")
                        if (result.code == 3535) {
                            _appUpdateResponse.postValue(null)

                        } else {
                            _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                true, result.message
                            )
                        }
                    }

                    is NetworkResult.Failure -> {
                        _progress.value = false
                        if (result.exception.message?.contains("timeout") == true) {
                            _uiMessageStateLiveData.postValue(
                                UIMessageState.StringResourceMessage(
                                    true, R.string.timeout_exception
                                )
                            )
                        } else {
                            _uiMessageStateLiveData.postValue(
                                UIMessageState.StringResourceMessage(
                                    true, R.string.something_went_wrong
                                )
                            )
                        }
                    }

                    is NetworkResult.Loading -> {
                        _progress.value = true
                    }
                }
            }
        } /*else {
                _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                    false, R.string.no_internet
                )
            }*//*else if (null == networkStatus.value){
                _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                    false, "net illa"
                )
            }*/
    }


    /**
     * Download the apk and saves it into the specified path
     * @param url URL of the file
     * @param fileName Name of the file
     * @param path Path of the file to be saved
     */
    fun downloadApk() {

        val path = FileUtil.getApkPath(getApplication()) /*toString()*/
        val currentAppVersion = DeviceUtil.getAppVersion()
        val appUpdateResponse = _appUpdateResponse.value
        val apkUrl = getApkUrl(appUpdateResponse)
        val fileName = getFileName(appUpdateResponse)
        val downloadableFile = File("$path/$fileName")

        Timber.tag("AppUpdate").d(
            "current app version: $currentAppVersion, path : $path APKURL: $apkUrl, filename: $fileName file: $downloadableFile"
        )
        viewModelScope.launch(Dispatchers.IO) {
            appUpdateResponse?.let {
                //compares app versions
                if (compareVersionNames(
                        currentAppVersion, appUpdateResponse.appLatestVersion!!
                    ) != -1
                ) {
                    _uiMessageStateLiveData.postValue(
                        UIMessageState.StringMessage(
                            false, "No updates available"
                        )
                    )
                } else if (downloadableFile.exists()) {
                    //latest file already exists
                    _downloadFile.postValue(
                        DownloadResult.AlreadyDownloaded(
                            downloadableFile
                        )
                    )
                } else {
                    if (networkStatus.value!!) {
                        PRDownloader.download(apkUrl, path, fileName).build()
                            .setOnStartOrResumeListener {
                                _downloadFile.postValue(DownloadResult.OnStartListener)
                            }
                            .setOnPauseListener {
                                _downloadFile.postValue(DownloadResult.OnPauseListener)
                            }.setOnCancelListener {
                                _downloadFile.postValue(DownloadResult.OnCancelListener)
                            }.setOnProgressListener { progress ->
                                val currentProgress =
                                    (progress.currentBytes * 100 / progress.totalBytes).toInt()
                                _downloadFile.postValue(
                                    DownloadResult.OnProgressListener(
                                        currentProgress
                                    )
                                )
                            }.start(object : OnDownloadListener {
                                override fun onDownloadComplete() {
                                    _downloadFile.postValue(
                                        DownloadResult.OnDownloadComplete(
                                            downloadableFile
                                        )
                                    )
                                }

                                override fun onError(error: Error?) {
                                    addToNavigation(
                                        event = DBConstants.Event.ACTION,
                                        page = AppUpdateActivity::class.java.name,
                                        comment = "App download failed",
                                    )
                                    _downloadFile.postValue(DownloadResult.OnError(error))
                                }
                            })
                    } else {
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringResourceMessage(
                                false, R.string.no_internet
                            )
                        )
                    }
                }
            }
        }
    }

    //get the apk url based on the android version
    private fun getApkUrl(appUpdateResponse: AppUpdate?): String {
        appUpdateResponse?.let { data ->
            if (isDeviceAndroid13())
                return data.android13Path.toString()
            else
                return data.android10Path.toString()
        }
        return ""
    }

    //returns the filename based on the android version
    private fun getFileName(appUpdateResponse: AppUpdate?): String {
        appUpdateResponse?.let { data ->
            if (isDeviceAndroid13()) {
                return data.android13Path!!.substring(
                    data.android13Path.lastIndexOf(
                        '/',
                        data.android13Path.lastIndex
                    ) + 1
                )

            } else {
                return data.android10Path!!.substring(
                    data.android10Path.lastIndexOf(
                        '/',
                        data.android10Path.lastIndex
                    ) + 1
                )
            }

        }
        return ""
    }

    override fun onCleared() {
        super.onCleared()
        //  networkConnectionManager.stopListenNetworkState()
        EventBus.getDefault().unregister(this)
    }

    //Checks whether the device is android 12 or android 10
    // TODO eee mandan logic..
    private fun isDeviceAndroid13(): Boolean = Build.MODEL.toString().contains("TB300XU")
            || Build.MODEL.toString().contains("TB310XU")

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    companion object {
        /**
         * To compare app versions
         * @return // 1 : Old version is higher then new // -1: Old version is lesser than new // 0: Both are equal
         * @param oldVersionName Old version code
         * @param newVersionName New Version code
         * */
        fun compareVersionNames(oldVersionName: String, newVersionName: String): Int {
            var res = 0
            val oldNumbers =
                oldVersionName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val newNumbers =
                newVersionName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()

            // To avoid IndexOutOfBounds
            val maxIndex = oldNumbers.size.coerceAtMost(newNumbers.size)
            for (i in 0 until maxIndex) {
                val oldVersionPart = oldNumbers[i].toInt()
                val newVersionPart = newNumbers[i].toInt()
                if (oldVersionPart < newVersionPart) {
                    res = -1
                    break
                } else if (oldVersionPart > newVersionPart) {
                    res = 1
                    break
                }
            }

            // If versions are the same so far, but they have different length...
            if (res == 0 && oldNumbers.size != newNumbers.size) {
                res = if (oldNumbers.size > newNumbers.size) 1 else -1
            }
            Timber.e("compareVersionNames --> $res : current version $oldVersionName : new version $newVersionName")
            return res
        }
    }

    fun allowAppInstallation() {
        csdkRepository.getCSDKHandler().hideOrEnableInstallation(false)
    }

}
