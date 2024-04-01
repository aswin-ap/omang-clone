package com.omang.app.ui.base.viewmodel

import android.app.Activity
import android.app.Application
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.navigation.MobileNavigationEntity
import com.omang.app.data.database.user.User
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.deviceStatus.DeviceStatus
import com.omang.app.data.model.deviceUpdate.StatusRequest
import com.omang.app.data.model.fcm.Payload
import com.omang.app.data.model.home.NetworkConnectionStatus
import com.omang.app.data.model.rating.RatingResponse
import com.omang.app.data.repository.AnalyticsRepository
import com.omang.app.data.repository.CSDKRepository
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.data.repository.DeviceRepository
import com.omang.app.data.repository.RegistrationRepository
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.data.repository.SocketRepository
import com.omang.app.data.repository.UserRepository
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.dataStore.SharedPref
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myLibrary.viewmodel.Progress
import com.omang.app.utils.DeviceUtil
import com.omang.app.utils.DownloadManger
import com.omang.app.utils.FileUtil
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.InternetSpeedChecker
import com.omang.app.utils.csdk.CSDKConstants
import com.omang.app.utils.csdk.DeviceSystemAdministrator
import com.omang.app.utils.extensions.toJson
import com.omang.app.utils.socket.SocketStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.system.exitProcess

@HiltViewModel
open class BaseViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    @Inject
    lateinit var dataManagerRepository: DataManagerRepository

    @Inject
    lateinit var deviceRepository: DeviceRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var csdkRepository: CSDKRepository

    @Inject
    lateinit var socketRepository: SocketRepository

    @Inject
    lateinit var resourceRepository: ResourceRepository

    @Inject
    lateinit var registrationRepository: RegistrationRepository

    @Inject
    lateinit var sharedPref: SharedPref

    @Inject
    lateinit var analyticsRepository: AnalyticsRepository

    @Inject
    lateinit var internetSpeedChecker: InternetSpeedChecker

    private var kioskJob: Job? = null

    private val _deviceLockStatus = MutableLiveData<Boolean>()
    val deviceLockStatus: LiveData<Boolean> get() = _deviceLockStatus

    private val _userStatus = MutableLiveData<Boolean>()
    val userStatus: LiveData<Boolean> get() = _userStatus

    private val _baseProgress = MutableLiveData<NetworkLoadingState?>()
    val baseProgress: LiveData<NetworkLoadingState?> = _baseProgress

    val _socketStatus = MutableLiveData<SocketStatus>()
    val socketStatus: LiveData<SocketStatus> = _socketStatus

    private val _isRatingSuccess = MutableLiveData<RatingResponse>()
    val isRatingSuccess: LiveData<RatingResponse> = _isRatingSuccess

    private val _isConnectionStatus = MutableLiveData<NetworkConnectionStatus>()
    val isConnectionStatus: LiveData<NetworkConnectionStatus> = _isConnectionStatus

    open fun checkDeviceLockStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(
                DataStoreKeys.DEVICE_LOCK_STATUS
            ).stateIn(this).collect {
                it?.let {
                    Timber.tag("Device Lock").d("Device lock status $it")
                    _deviceLockStatus.postValue(it)
                }
            }
        }
    }

    open fun checkUserStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(
                DataStoreKeys.USER_ACTIVE_STATUS
            ).stateIn(this).collect {
                it?.let {
                    Timber.tag("user Status").d("User status $it")
                    _userStatus.postValue(it)
                }
            }
        }
    }

    fun getDeviceLockMessage() = runBlocking {
        val deferredValue = async(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(DataStoreKeys.DEVICE_LOCK_MESSAGE).first()
        }
        val value = deferredValue.await()
        value
    }

    /**
     * @param isLocked = True if device is locked else false
     */
    fun setDeviceLockStatus(isLocked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.DEVICE_LOCK_STATUS,
                isLocked
            )
            updateDeviceStatus()
        }
    }

    /**
     * @param isLocked = True if device is locked else false
     */
    fun setUserActiveStatus(isLocked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.USER_ACTIVE_STATUS,
                isLocked
            )
            updateDeviceStatus()
        }
    }

    /**
     * @param isAssigned = True if device is assigned else false
     */
    fun setDeviceAssignedStatus(isAssigned: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.IS_USER_ASSIGNED,
                isAssigned
            )
            if (!isAssigned)
                clearDb()
            updateDeviceStatus()
        }
    }

    private fun clearDb() {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.clearAllDataFromDb()
        }
    }

    fun addToNavigation(
        page: String,
        event: DBConstants.Event,
        comment: String? = null,
        subjectId: Int? = null,
        contentId: Int? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.insertMobileNavigationEntry(
                MobileNavigationEntity(
                    page = page,
                    event = event.value,
                    comment = comment,
                    subjectId = subjectId,
                    contentId = contentId
                )
            )
        }
    }

    fun getDeviceImei() = runBlocking {
        val deferredValue = async(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(DataStoreKeys.IMEI_NO).first()
        }
        val value = deferredValue.await()
        value
    }

    //Saves the payload body by the updateType
    fun savePayloadBody(payload: Payload) {
        viewModelScope.launch(Dispatchers.IO) {
            when (payload.updateType) {
                100 ->
                    dataManagerRepository.saveToDataStore(
                        DataStoreKeys.DEVICE_LOCK_MESSAGE,
                        payload.body ?: ""
                    )

                else -> {
                    Timber.tag("savePayloadBody").d("Payload type : ${payload.updateType}")
                }
            }
        }
    }

    fun getDeleteUpdates(deleteUpdateRequest: DeleteUpdateRequest) {
        viewModelScope.launch {
            deviceRepository.getDeleteUpdates(deleteUpdateRequest)
        }
    }

    private fun updateDeviceStatus(statusRequest: StatusRequest) {
        viewModelScope.launch {
            deviceRepository.statusUpdate(statusRequest).collect()
        }
    }

    fun updateDeviceStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            getDeviceStatus().collectLatest { deviceStatus ->
                val statusRequest = StatusRequest().apply {
                    appVersion = DeviceUtil.getAppVersion()
                    csdkStatus = deviceStatus.csdkStatus
                    deviceOwnerStatus = deviceStatus.deviceOwner
                    pinnedStatus = deviceStatus.pinnedStatus
                    lockedStatus = deviceStatus.lockedStatus
                    userStatus = deviceStatus.userStatus
                }
                updateDeviceStatus(statusRequest)
            }
        }
    }

    private suspend fun getDeviceStatus(): Flow<DeviceStatus> {
        val csdkStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.CSDK_LICENSE_STATUS)
        val deviceOwnerStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.DEVICE_OWNER_STATUS)
        val pinnedStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.PINNED_STATUS)
        val lockedStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.DEVICE_LOCK_STATUS)
        val userStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.USER_ACTIVE_STATUS)
        return combine(
            csdkStatus,
            deviceOwnerStatus,
            pinnedStatus,
            lockedStatus,
            userStatus
        ) { csdkStatus, deviceOwnerStatus, pinnedStatus, lockedStatus, userStatus ->
            DeviceStatus(
                if (csdkStatus == true) "on" else "off",
                if (deviceOwnerStatus == true) "on" else "off",
                if (pinnedStatus == true) "on" else "off",
                if (lockedStatus == true) "on" else "off",
                if (userStatus == true) "on" else "off",
            )
        }.stateIn(viewModelScope)
    }

    fun runKiosk(activity: Activity, completion: (Int?, Throwable?) -> Unit) {
        kioskJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                csdkRepository.getCSDKHandler().enableKioskMode(activity, true)
                completion(CSDKConstants().EXCECUTION_SUCCESS, null)
            } catch (e: Exception) {
                e.printStackTrace()
                completion(CSDKConstants().EXCECUTION_ERROR, e)
            }

            kioskJob?.invokeOnCompletion { throwable ->
                Timber.e("${throwable?.message}")
            }
        }
    }

    fun exitKiosk(activity: Activity, completion: (Int?, Throwable?) -> Unit) {
        kioskJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                csdkRepository.getCSDKHandler().enableKioskMode(activity, false)
                completion(CSDKConstants().EXCECUTION_SUCCESS, null)

            } catch (e: Exception) {
                e.printStackTrace()
                completion(CSDKConstants().EXCECUTION_ERROR, e)

            }

            kioskJob?.invokeOnCompletion { throwable ->
                Timber.e("${throwable?.message}")
            }
        }
    }

    fun isDeviceAdmin(activity: Activity?): Boolean {
        val dpm =
            activity?.getSystemService(AppCompatActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val mDeviceAdmin = ComponentName(activity, DeviceSystemAdministrator::class.java)
        return dpm.isAdminActive(mDeviceAdmin)
    }

    fun isDeviceOwner(): Boolean {
        val isDeviceOwner = csdkRepository.getCSDKHandler().isDeviceOwner()
        setDeviceOwnerStatus(isDeviceOwner)
        Timber.e("is Device owner $isDeviceOwner")
        return isDeviceOwner
    }

    fun setDeviceOwner() {
        return csdkRepository.getCSDKHandler().setDeviceOwner()
    }

    fun removeCSDkOwner() {
        return csdkRepository.getCSDKHandler().removeDeviceOwner()
    }

    private fun setDeviceOwnerStatus(boolean: Boolean) {
        viewModelScope.launch {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.DEVICE_OWNER_STATUS,
                boolean
            )
        }
    }


    fun isCSDKLicenseActivated(): Boolean {
        val isCSDKLicenseActivated = csdkRepository.getCSDKHandler().isCSDKActivated
        Timber.v("CSDK License status $isCSDKLicenseActivated")
        setCSDKLicenseStatus(isCSDKLicenseActivated)
        return isCSDKLicenseActivated
    }

    fun setCSDKLicenseStatus(boolean: Boolean) {
        viewModelScope.launch {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.CSDK_LICENSE_STATUS,
                boolean
            )
        }
    }

    fun activateCDSK() {
        csdkRepository.getCSDKHandler().activateCSDKLicense()

    }

    fun setPinnedStatus(boolean: Boolean) {
        viewModelScope.launch {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.PINNED_STATUS,
                boolean
            )
        }
    }

    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        exitProcess(0)
    }

    override fun onCleared() {
        super.onCleared()
    }

    /*
    * below are for testing csdk 5.0 */
    fun runTestCSDK5(status: Boolean) {
//        csdkRepository.getCSDKHandler().hideOrShowActions(status)

    }

    fun test(status: Boolean) {
//        csdkRepository.getCSDKHandler().hideOrShowSettings(status)
    }

    fun setTestStatus(boolean: Boolean) {
        viewModelScope.launch {
            dataManagerRepository.saveToDataStore(
                DataStoreKeys.TEST_STATUS,
                boolean
            )
        }
    }

    fun getTestStatus() = runBlocking {
        val deferredValue = async(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(DataStoreKeys.TEST_STATUS).first()
        }
        val value = deferredValue.await()
        value
    }

    //Sends the offline test to server
    /*    fun sendTestResults() {
                try {
                    viewModelScope.launch(Dispatchers.IO) {
                        val offlineList = resourceRepository.getAllAttemptedOfflineTests()
                        if (offlineList.isNotEmpty()) {
                        Timber.tag("Test").d("Sending test...")
                            val mcqList = mutableListOf<Mcq>()
                            offlineList.forEach { offlineEntity ->
                                mcqList.add(
                                    Mcq(
                                    id = offlineEntity.generalMcqId,
                                        score = offlineEntity.score.roundToInt(),
                                        attemptsCount = offlineEntity.attemptsCount,
                                        correctAttempts = offlineEntity.correctAttempts,
                                        wrongAttempts = offlineEntity.wrongAttempts,
                                        questions = offlineEntity.questions,
                                        attendedOn = offlineEntity.createdOn,
                                        mcqStudId = null,
                                        classroom = offlineEntity.classroomId,
                                        unit = offlineEntity.unitId
                                    )
                                )
                            }

                            TestSubmitRequest(
                                mcqs = mcqList
                            ).also {
                                Timber.tag("Test").d("TestSubmitRequest: $it")
                                resourceRepository.postMcqTestResults(it).collect { result ->
                                    when (result) {
                                        is NetworkResult.Error -> {
                                            _baseProgress.postValue(NetworkLoadingState.ERROR)
                                        }

                                        is NetworkResult.Failure -> {
                                            _baseProgress.postValue(NetworkLoadingState.ERROR)
                                        }

                                        is NetworkResult.Loading -> {}

                                    is NetworkResult.Success -> {
                                        //TODO - update the tests to attempted
                                        resourceRepository.setTestsStatusToAttempted(result.data.mcqResultData)
                                        _baseProgress.postValue(NetworkLoadingState.SUCCESS)
                                    }
                                }
                            }
                        }
                    } else {
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Timber.e("Exception while sending test data: $e")
                _baseProgress.postValue(NetworkLoadingState.ERROR)
            }
        }*/

    fun downloadImageToDevice(url: String): Flow<Pair<Progress, DownloadStatus>?> {
        val fileName = url.substring(
            url.lastIndexOf(
                '/',
                url.lastIndex
            ) + 1
        )
        return DownloadManger.download(
            url,
            fileName,
            FileUtil.getImageUrlPath(getApplication())
        ).map {
            when (it) {
                is DownloadManger.DownloadMangerStatus.Initialized -> {
                    Pair(0, DownloadStatus.INTIALISED)
                }

                DownloadManger.DownloadMangerStatus.Cancelled -> {
                    null
                }

                is DownloadManger.DownloadMangerStatus.Downloaded -> {
                    resourceRepository.fileInsert(FileEntity(filePath = it.path))
                    Pair(100, DownloadStatus.FINISHED)
                }

                is DownloadManger.DownloadMangerStatus.OnError -> {
                    Pair(0, DownloadStatus.ERROR)
                }

                is DownloadManger.DownloadMangerStatus.OnProgress -> {
                    Pair(it.progress, DownloadStatus.PROGRESS)
                }

                DownloadManger.DownloadMangerStatus.Paused -> {
                    null
                }

                DownloadManger.DownloadMangerStatus.Started -> {
                    Pair(0, DownloadStatus.STARTED)
                }

            }
        }
    }

    fun downloadEmojiToDevice(url: String): Flow<Pair<Progress, DownloadStatus>?> {
        val fileName = url.substring(
            url.lastIndexOf(
                '/',
                url.lastIndex
            ) + 1
        )
        return DownloadManger.download(
            url,
            fileName,
            FileUtil.getEmojiUrlPath(getApplication())
        ).map {
            when (it) {
                is DownloadManger.DownloadMangerStatus.Initialized -> {
                    Pair(0, DownloadStatus.INTIALISED)
                }

                DownloadManger.DownloadMangerStatus.Cancelled -> {
                    null
                }

                is DownloadManger.DownloadMangerStatus.Downloaded -> {
                    resourceRepository.fileInsert(FileEntity(filePath = it.path))
                    Pair(100, DownloadStatus.FINISHED)
                    //DownloadUpdate(progress = 100, downloadStatus = DownloadStatus.FINISHED)
                }

                is DownloadManger.DownloadMangerStatus.OnError -> {
                    Timber.tag("ERROR").e(it.error!!.serverErrorMessage)
                    Pair(0, DownloadStatus.ERROR)
                }

                is DownloadManger.DownloadMangerStatus.OnProgress -> {
                    Pair(it.progress, DownloadStatus.PROGRESS)
                }

                DownloadManger.DownloadMangerStatus.Paused -> {
                    null
                }

                DownloadManger.DownloadMangerStatus.Started -> {
                    Pair(0, DownloadStatus.STARTED)
                }
            }
        }
    }

    fun downloadPsmToDevice(url: String): Flow<Pair<Progress, DownloadStatus>?> {
        val fileName = url.substring(
            url.lastIndexOf(
                '/',
                url.lastIndex
            ) + 1
        )
        return DownloadManger.download(
            url,
            fileName,
            FileUtil.getPsmUrlPath(getApplication())
        ).map {
            when (it) {
                is DownloadManger.DownloadMangerStatus.Initialized -> {
                    Pair(0, DownloadStatus.INTIALISED)
                }

                DownloadManger.DownloadMangerStatus.Cancelled -> {
                    null
                }

                is DownloadManger.DownloadMangerStatus.Downloaded -> {
                    Pair(100, DownloadStatus.FINISHED)
                    //DownloadUpdate(progress = 100, downloadStatus = DownloadStatus.FINISHED)
                }

                is DownloadManger.DownloadMangerStatus.OnError -> {
                    Timber.tag("ERROR").e(it.error!!.serverErrorMessage)
                    Pair(0, DownloadStatus.ERROR)
                }

                is DownloadManger.DownloadMangerStatus.OnProgress -> {
                    Pair(it.progress, DownloadStatus.PROGRESS)
                }

                DownloadManger.DownloadMangerStatus.Paused -> {
                    null
                }

                DownloadManger.DownloadMangerStatus.Started -> {
                    Pair(0, DownloadStatus.STARTED)
                }
            }
        }
    }

    fun clearValues() {
        _baseProgress.postValue(null)
    }

    fun socketInit() {
        socketStatus()
        socketRepository.socketInit()
    }

    fun socketDisconnect() {
        socketStatus()
        socketRepository.socketDisconnect()
    }

    fun socketReconnect() {
        socketStatus()
        socketRepository.socketReconnect()
    }

    fun socketStatus() {
        // below will get the callbacks for all socket connection and event statuses
        socketRepository.statusListener { status ->
            _socketStatus.value = status
        }
    }

    internal fun setSocketStatus(flag: Boolean) {
        sharedPref.isSocketConnected = flag
    }

    internal fun isSocketConnected(): Boolean {
        return sharedPref.isSocketConnected
    }

    fun joinRoom(roomId: Int, callback: (Boolean) -> Unit) {
        socketRepository.joinRoom(roomId, callback)
    }

    fun leaveRoom() {
        socketRepository.leaveRoom()
    }

    fun sendMessage(
        message: String,
        timestamp: String = ViewUtil.getServerFormattedTime(System.currentTimeMillis())
    ) {
        socketRepository.sendMessage(message, timestamp)
    }

    fun sendBulkStatus(
        messageId: Int,
        timestamp: String = ViewUtil.getServerFormattedTime(System.currentTimeMillis())
    ) {
        socketRepository.sendBulkStatus(messageId, timestamp)
    }

    fun isOnWifi(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    fun isOnMobileNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
    }

    fun wifi(flag: Boolean) {
        Timber.tag("WIFI").v("wifiStatus $!flag")
        csdkRepository.getCSDKHandler().wifi(!flag)
    }

    fun data(flag: Boolean) {
        Timber.tag("WIFI").v("dataStatus $flag")
        csdkRepository.getCSDKHandler().data(flag)
    }

    //function to save the feed dialog data when picking image
    fun savePostData(postTo: String, selectedClassRoom: Int, description: String) {
        sharedPref.selectedPostToItem = postTo
        sharedPref.selectedClassRoomItem = selectedClassRoom
        sharedPref.postDescription = description
    }

    //function to save the feed dialog data when picking image
    fun clearPostData() {
        sharedPref.selectedPostToItem = ""
        sharedPref.selectedClassRoomItem = 0
        sharedPref.postDescription = ""
    }

    fun getDeviceInfo(value: CSDKConstants.Device): String {
        Timber.e("getDeviceInfo")
        return csdkRepository.getCSDKHandler().getDeviceInfo(value)
    }

    fun insertWatchData(analyticsEntity: AnalyticsEntity) {
        viewModelScope.launch {
            analyticsRepository.insertWatch(analyticsEntity)
        }
    }

    fun insertLocation(analyticsEntity: AnalyticsEntity) {
        viewModelScope.launch {
            analyticsRepository.insertLocation(analyticsEntity)
        }
    }

    fun insertSleepAwake(analyticsEntity: AnalyticsEntity) {
        viewModelScope.launch {
            analyticsRepository.insertSleepAwake(analyticsEntity)
        }
    }

    fun insertPSM(analyticsEntity: AnalyticsEntity) {
        viewModelScope.launch {
            analyticsRepository.insertPSM(analyticsEntity)
        }
    }

    fun insertManualCleaning(analyticsEntity: AnalyticsEntity) {
        viewModelScope.launch {
            analyticsRepository.insertManualCleaning(analyticsEntity)
        }
    }

    fun getAllAnalytics() {
        viewModelScope.launch {
            val allData = analyticsRepository.getAllAnalytics()
            allData.forEach { entity ->
                val json = entity.toJson()
                println(json)
            }
        }
    }

    fun AnalyticsEntity.toJson(): String {
        return Gson().toJson(this)
    }

    fun setFirebaseKeys(user: User) {
        val crashlytics = Firebase.crashlytics
        crashlytics.setCustomKeys {
            key("user_name", "${user.firstName} ${user.lastName}")
            key("user_id", user.id)
            key("accession_no", user.accessionNumber ?: "Unknown")
            key("school", user.school?.name ?: "Unknown")
            key("imei", getDeviceImei() ?: "Unknown")
        }
    }

    fun testInstallation(flag: Boolean) {
//        csdkRepository.getCSDKHandler().test(flag)
    }

    fun rateResources(type: String, id: Int, rating: Float) {
        _baseProgress.postValue(NetworkLoadingState.LOADING)
        viewModelScope.launch (Dispatchers.IO){
            resourceRepository.postResourceRating(type, id, rating).collect {ratingResponse ->
                when (ratingResponse) {
                    is NetworkResult.Error -> {
                        _baseProgress.postValue(NetworkLoadingState.ERROR)
                    }

                    is NetworkResult.Failure -> {
                        _baseProgress.postValue(NetworkLoadingState.ERROR)
                    }

                    is NetworkResult.Loading -> {
                        _baseProgress.postValue(NetworkLoadingState.LOADING)
                    }

                    is NetworkResult.Success -> {
                        ratingResponse.data.let { response ->
                            _baseProgress.postValue(NetworkLoadingState.SUCCESS)
                            if (response.message == "SUCCESS") {
                                Timber.tag("RATE RESPONSE").e("API Rating Response ${response.message}")
                                if(type == "lesson"){
                                    updateLessonRate(response.data.entityId,response.data.ratingValue)
                                }
                                _isRatingSuccess.postValue(response)

                            }
                        }


                    }

                    else -> {}
                }

            }
        }

    }
    private fun updateLessonRate(id: Int, rating: Float) {
        viewModelScope.launch {
            resourceRepository.updateLessonRating(id,rating)
        }

    }
    fun getAvailableNetworkStatus(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var type: String? = null
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                type = when {
                    capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        "WIFI"
                    }

                    capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        "MOBILE"
                    }

                    else -> {
                        "NONE"
                    }
                }
                _isConnectionStatus.postValue(type?.let { NetworkConnectionStatus(true, it) })
            }

            override fun onLost(network: android.net.Network) {
                _isConnectionStatus.postValue(NetworkConnectionStatus(false, "NONE"))
            }
        })
    }
}
