package com.omang.app.ui.home.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.database.psm.PsmEntity
import com.omang.app.data.database.user.User
import com.omang.app.data.model.bubble.BubbleData
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.fcm.Payload
import com.omang.app.data.model.home.NetworkConnectionStatus
import com.omang.app.data.model.modeMeter.ModeMeterEntity
import com.omang.app.data.model.modeMeter.MoodMeterRequest
import com.omang.app.data.model.modeMeter.MoodsList
import com.omang.app.data.model.myProfile.ProfilePayLoad
import com.omang.app.data.model.summary.McqSummary
import com.omang.app.data.model.summary.MoodMeter
import com.omang.app.data.model.summary.MyClassroomItem
import com.omang.app.data.model.updates.DeletedItems
import com.omang.app.data.model.updates.DetailsItem
import com.omang.app.data.model.updates.DeviceUpdatesData
import com.omang.app.data.model.userAssign.Doe
import com.omang.app.data.model.userAssign.Psm
import com.omang.app.data.model.userAssign.Student
import com.omang.app.data.model.userAssign.UserAssignResponse
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.activity.getPsmByEntity
import com.omang.app.ui.home.activity.getTodaysPsm
import com.omang.app.ui.home.fragments.HomeFragmentDirections
import com.omang.app.utils.FileUtil
import com.omang.app.utils.NotificationUtil
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.toJson
import com.omang.app.utils.signal.SignalObserver
import com.omang.app.utils.worker.AnalyticsWorker
import com.omang.app.utils.worker.LocationWorker
import com.omang.app.utils.worker.OfflineTicketWorker
import com.omang.app.utils.worker.TestWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.util.Calendar
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    val app: Application,
) : BaseViewModel(app) {

    private val _deviceUpdatesResponse = MutableLiveData<DeviceUpdatesData>()
    val deviceUpdatesResponse: LiveData<DeviceUpdatesData> = _deviceUpdatesResponse

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _updateDpPill = MutableLiveData<String>()
    val updateDpPill: LiveData<String> = _updateDpPill

    private val _bubble = MutableLiveData<Bubble>()
    val bubble: LiveData<Bubble> = _bubble

    private val _alert = MutableLiveData<List<DetailsItem?>?>()
    val alert: MutableLiveData<List<DetailsItem?>?> = _alert

    private var _backNavigationDisabled: Boolean = false
    val backNavigationDisabled get() = _backNavigationDisabled

    private var _showMoodMeterDialog = MutableLiveData<String?>()
    val showMoodMeterDialog: LiveData<String?> = _showMoodMeterDialog

    private var _getMoodMeterName = MutableLiveData<String?>()
    val getMoodMeterName: MutableLiveData<String?> = _getMoodMeterName

    private val _moodMeterData = MutableLiveData<List<ModeMeterEntity>>()
    val moodMeterData: LiveData<List<ModeMeterEntity>> get() = _moodMeterData

    private var _downloadStatus = MutableLiveData<Boolean>()
    var downloadStatus: LiveData<Boolean> = _downloadStatus

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _moodMeterSubmit = MutableLiveData<UserAssignResponse>()
    val moodMeterSubmit: LiveData<UserAssignResponse> = _moodMeterSubmit

    private val _psmStatus = MutableLiveData<PsmData>()
    val psmStatus: LiveData<PsmData> = _psmStatus

    private val _lastUpdatedLiveData = MutableLiveData<LastUpdatedData>()
    val lastUpdatedLiveData: LiveData<LastUpdatedData> = _lastUpdatedLiveData

    @Inject
    lateinit var networkConnection: NetworkConnection

    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    private val _doeLiveData = MutableLiveData<MyWebPlatformEntity>()
    val doeLiveData: LiveData<MyWebPlatformEntity> = _doeLiveData

    val liveTestWorkStatus = MediatorLiveData<WorkInfo>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected

    /*private val _isConnectionStatus = MutableLiveData<NetworkConnectionStatus>()
    val isConnectionStatus: LiveData<NetworkConnectionStatus> = _isConnectionStatus*/

    private val navigationSemaphore = Semaphore(1)



    fun canNavigateBack(boolean: Boolean) {
        _backNavigationDisabled = boolean
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    fun startListeningInternetStatus() {
        networkConnection.observeNetworkConnection().onEach {
            Timber.tag("NetworkConnection").d("NetworkStatus : $it")
            _networkStatus.postValue(it)
        }.launchIn(viewModelScope)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMessageReceived(payload: Payload) {
        Timber.tag("HomeViewModel: onMessageReceived").v("update type ${payload.updateType}")
        savePayloadBody(payload)
        setBadgeData(payload.updateType)
        when (payload.updateType) {
            100, 101, 102, 103, 104, 105, 110, 111, 112, 116, 114, 115, 113, 120, 125 -> getDeviceUpdates()
            NotificationUtil.USER_PROFILE.type -> getProfileUpdates()
            NotificationUtil.PSM.type -> {
                payload.classroom?.let {
                    getDeviceUpdates()
                } ?: kotlin.run {
                    getProfileUpdates()
                }
            }
//            NotificationUtil.CUSTOM_NOTIFICATION.toInt() -> _alert.postValue(payload)
            else -> Timber.e("--> ${payload.updateType}")
        }
    }

    //updates the badge based the notification id
    private fun setBadgeData(updateType: Int) {
        viewModelScope.launch {
            //to shows the bubble in bottom navigation
            when (updateType) {
                //test
                118 -> _bubble.postValue(Bubble.TEST)
                else -> _bubble.postValue(Bubble.NOTIFICATION)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun updateToNewProfilePic(profilePayLoad: ProfilePayLoad) {
        Handler(Looper.getMainLooper()).post {
            _updateDpPill.value = profilePayLoad.apiUrl
        }
    }

    fun getDeviceUpdates() {
        Timber.d("Update API called--->")
        viewModelScope.launch {
                deviceRepository.getDeviceUpdates().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            result.data.let { data ->
                                val deleteUpdateRequest = DeleteUpdateRequest()

//                                 priority
                                _deviceUpdatesResponse.value = data

//                                added
                                for (added in data.added!!) {
                                    for (detail in added!!.details!!) {
                                        detail?.classroom?.let {
                                            BubbleData.classroomList.add(it)
                                            Timber.e("classroom bubble : ${BubbleData.classroomList}")
                                            detail.id.let { it1 ->
                                                deleteUpdateRequest.ids.add(
                                                    it1
                                                )
                                            } // add id to remove from updates
                                        }
                                    }

                                    handleAddedData(added.updateType)

                                    if (added.updateType == NotificationUtil.CUSTOM_NOTIFICATION.value()) {
                                        if (added.details?.isNotEmpty() == true) {
                                            val detailsItemList = added.details
                                            _alert.postValue(detailsItemList)
                                            Timber.tag("customNotificationData")
                                                .d("_alert ---> $detailsItemList")
                                        }
                                    } else if (added.updateType == NotificationUtil.WEB_PLATFORM.value()) {
                                        insertWebPlatform(added.details)

                                    } else if (added.updateType == NotificationUtil.MY_WEBPLATFORM.value()) {
                                        insertMyWebPlatform(added.details)

                                    }
                                }

//                                update
                                data.updated?.let {
                                    for (update in data.updated) {
                                        if (update?.updateType == NotificationUtil.DOE.value()) {
                                            update.details?.let {
                                                val doe = Doe(
                                                    id = update.details[update.details.lastIndex]!!.resource,
                                                    title = update.details[update.details.lastIndex]!!.name,
                                                    logo = update.details[update.details.lastIndex]!!.logo
                                                )
                                                insertDoe(doe)
                                            }
                                        }
                                    }
                                }

                                /*
                            * delete logic */
                                data.deleted.forEach { deletedItem -> // add id to remove from updates
                                    when (deletedItem.updateType) {
                                        NotificationUtil.CLASSROOM_RESOURCE.value() -> { // CLASSROOM_RESOURCE
                                            deleteClassroomResource(deletedItem)
                                        }

                                        NotificationUtil.CLASSROOM.value() -> {//CLASSROOM
                                            deleteClassroom(deletedItem)
                                        }

                                        NotificationUtil.MY_LIBRARY.value() -> {
                                            deleteMyLibrary(deletedItem)
                                        }

                                        NotificationUtil.MY_WEBPLATFORM.value() -> {
                                            deleteMyWebPlatform(deletedItem)
                                        }

                                        NotificationUtil.WEB_PLATFORM.value() -> {
                                            deleteWebPlatform(deletedItem)
                                        }

                                        NotificationUtil.CUSTOM_NOTIFICATION.value() -> {
                                            deleteFeed(deletedItem)
                                        }

                                        NotificationUtil.PSM.value() -> {
                                            deletePsm(deletedItem)
                                        }
                                    }
                                }

                                data.priority?.forEach { item ->
                                    deleteUpdateRequest.ids.add(item!!.id) // add id to remove from updates
                                }

                                data.updated?.forEach { item ->
                                    item?.details!!.forEach {
                                        deleteUpdateRequest.ids.add(it!!.id)
                                    }
                                }

                                data.deleted.forEach { deletedItem ->
                                    deletedItem.details.forEach {
                                        deleteUpdateRequest.ids.add(it.id) // add id to remove from updates
                                    }
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
                            /*   _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                   false, result.message

                               )*/
                        }

                        is NetworkResult.Failure -> {
                            Timber.e(result.exception)
                            /*     _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                                     false, R.string.something_went_wrong
                                 )*/
                        }

                        is NetworkResult.Loading -> {}
                    }
            }
        }
    }

    private fun handleAddedData(type: Int?) {
        type?.let { updateType ->
            when (updateType) {
                NotificationUtil.CLASSROOM.type -> {
                    getSummaryData()
                }

                NotificationUtil.MY_LIBRARY.value() -> {
                    sharedPref.myLibraryUpdate = true
                }

                NotificationUtil.MY_WEBPLATFORM.value() -> {
                    sharedPref.myWebPlatformUpdate = true
                }

                NotificationUtil.WEB_PLATFORM.value() -> {
                    sharedPref.exploreUpdate = true
                }

                NotificationUtil.PSM.type, NotificationUtil.USER_PROFILE.type -> {
                    getProfileUpdates()
                }
            }
        }
    }

    private suspend fun deleteWebPlatform(deletedItems: DeletedItems) {
        deletedItems.details.forEach { deletedItem ->
            resourceRepository.deleteWebPlatform(deletedItem.resource)
//            won't get deleted content in update api for explore, {"statusCode":200,"message":"SUCCESS","data":{"priority":[],"added":[],"deleted":[],"updated":[]}}
        }
    }

    private suspend fun deleteMyWebPlatform(deletedItems: DeletedItems) {
        deletedItems.details.forEach { deletedItem ->
            resourceRepository.deleteMyWebPlatform(deletedItem.resource)

        }
    }

    private fun insertWebPlatform(details: List<DetailsItem?>?) {
        viewModelScope.launch {
            resourceRepository.insertExploreDataFromUpdate(details)
        }
    }

    private fun insertMyWebPlatform(details: List<DetailsItem?>?) {
        viewModelScope.launch {
            resourceRepository.insertMyWebPlatformFromUpdate(details)
        }
    }

    private suspend fun deleteFeed(deletedItems: DeletedItems) {
        deletedItems.details.forEach { deletedItem ->
            Timber.tag("FeedDelete").d(deletedItem.toJson())
            resourceRepository.deleteClassroomFeed(deletedItem.resource)
            resourceRepository.deletePublicFeed(deletedItem.resource)

        }
    }

    private suspend fun deletePsm(deletedItems: DeletedItems) {
        deletedItems.details.forEach {
            Timber.tag("PsmDelete").d(deletedItems.toJson())
            resourceRepository.deletePsmByClassroomId(it.classroom)
        }
    }

    fun locationWorker(applicationContext: Context) {
        val myWork = PeriodicWorkRequestBuilder<LocationWorker>(
            4, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(applicationContext).enqueue(myWork)
    }

    fun startTicketWorker(applicationContext: Context) {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val reportTicketWorkRequest =
            OneTimeWorkRequestBuilder<OfflineTicketWorker>().setConstraints(constraints).build()

        WorkManager.getInstance(applicationContext).enqueue(reportTicketWorkRequest)

    }

    fun startAnalyticsWorker(applicationContext: Context) {
        val currentDay = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        try {
            val lastUpdatedDay = sharedPref.analyticsFlag

            if (lastUpdatedDay == currentDay) {
                Timber.e("Data already sent once today $lastUpdatedDay")
                return
            }

            val constraints =
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val reportTicketWorkRequest =
                OneTimeWorkRequestBuilder<AnalyticsWorker>()
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(applicationContext).enqueue(reportTicketWorkRequest)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startAnalyticsWorkerImmediate(applicationContext: Context) {
        try {

            Timber.e("startAnalyticsWorkerImmediate")
            val constraints =
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val reportTicketWorkRequest =
                OneTimeWorkRequestBuilder<AnalyticsWorker>()
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(applicationContext).enqueue(reportTicketWorkRequest)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTestWorker(applicationContext: Context) {
        try {
            val constraints =
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val testWorkRequest =
                OneTimeWorkRequestBuilder<TestWorker>()
                    .setConstraints(constraints)
                    .build()

            val workManager = WorkManager.getInstance(applicationContext)

            workManager.enqueue(testWorkRequest)

            val liveTestStatus = workManager.getWorkInfoByIdLiveData(
                testWorkRequest.id
            )

            //adds the livedata to mediator livedata to observe in activity
            liveTestWorkStatus.addSource(liveTestStatus) {
                liveTestWorkStatus.value = it

                //if work is finished or failed, removes the observer
                if (it.state == WorkInfo.State.FAILED) {
                    liveTestWorkStatus.removeSource(liveTestStatus)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSummaryData() {
        viewModelScope.launch {
            resourceRepository.getSummaryData().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _isSyncing.value = NetworkLoadingState.SUCCESS

                        result.data.let {
                            insertUser(it.data.student)
                            insertDoe(it.data.student.school?.doe)
                            insertMyClassroom(it.data.myClassroom)
//                            insertPsm(it.data.student.psm)
                            handleGeneralPsm(it.data.student.psm)
//                            updateTestSummary(it.data.mcq)
                        }
                    }

                    is NetworkResult.Error -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e("${result.code} ${result.message}")

                    }

                    is NetworkResult.Failure -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e(result.exception)

                    }

                    is NetworkResult.Loading -> {
                        _isSyncing.value = NetworkLoadingState.LOADING

                    }
                }
            }
        }
    }

    private fun handleGeneralPsm(psm: List<Psm>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (psm.isNotEmpty()) {
                insertPsm(psm)
            } else
                deleteAllPsm()
        }
    }

    private suspend fun insertPsm(psm: List<Psm>) {
        psm?.let {
            if (it.isNotEmpty()) {
                viewModelScope.launch {
                    PsmEntity(
                        psmId = DBConstants.GENERAL_PSM_ID,
                        psmResponse = psm.toJson()
                    ).also {
                        //saves psm response
                        resourceRepository.insertPsm(it)
                    }
                    downloadPsm(psm)
                }
            }
        }
    }

    /**
     * If PSM exists, deletes general psm when the psm from api is empty.
     */
    private suspend fun deleteAllPsm() {
        resourceRepository.getGeneralPsm()?.let {
            resourceRepository.deleteGeneralPsm()
        }
    }

    private suspend fun downloadPsm(psm: List<Psm>) {
        _isSyncing.value = NetworkLoadingState.LOADING
        val downloadList = mutableListOf<String>()
        psm.forEach { psmItem ->
            if (ValidationUtil.isNotNullOrEmpty(psmItem.url)) {
                downloadList.add(psmItem.url!!)
            }
        }
        //downloads the returned psm from api
        recursivePsmDownload(downloadList)
    }

    //Fetches the details of the DOE
    fun getDoeDetails(webId: Int, navController: NavController) {
        _isSyncing.value = NetworkLoadingState.LOADING
        viewModelScope.launch {
            resourceRepository.getIndividualWebData(webId, false).collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Success -> {
                        networkResult.data.let { response ->
                            val webItem = response.data
                            _isSyncing.value = NetworkLoadingState.SUCCESS
                            HomeFragmentDirections.actionNavigationHomeToWebViewerFragment(
                                webItem, 0, 0, DBConstants.AnalyticsMenu.DOE.value
                            ).also {
                                try {
                                    navigationSemaphore.acquire()
                                    navController.navigate(it)
                                } catch (e: Exception) {
                                    Timber.i("Navigation Exception: ${e.message}")

                                } finally {
                                    navigationSemaphore.release()
                                }
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e("${networkResult.code} ${networkResult.message}")
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringMessage(
                                false,
                                networkResult.message
                            )
                        )
                    }

                    is NetworkResult.Failure -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e(networkResult.exception)
                    }

                    is NetworkResult.Loading -> {
                        _isSyncing.value = NetworkLoadingState.LOADING
                    }

                }
            }
        }
    }

    private fun insertDoe(doe: Doe?) {
        viewModelScope.launch {
            resourceRepository.insertDoe(doe)
            downloadDoeLogo(doe?.logo)
        }
    }

    private suspend fun downloadDoeLogo(doeLogo: String?) {
        doeLogo?.let { url ->
            if (!FileUtil.isEmojiAlreadyDownloadedInsideImagesFolder(getApplication(), url)) {
                downloadImageToDevice(url).distinctUntilChanged().collect { result ->
                    if (result?.second == DownloadStatus.FINISHED) {
                        Timber.tag("Doe Download").d("-> FINISHED")
                        getDoeDetails()
                    } else if (result?.second == DownloadStatus.ERROR) {
                        Timber.tag("Doe Download").d("-> FAILED")
                    }
                }
            }
        }
    }

    fun getDoeDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.getDoeDetails().also {
                it?.let { myWebPlatformEntity ->
                    _doeLiveData.postValue(myWebPlatformEntity)
                }
            }
        }
    }

    private suspend fun updateTestSummary(mcq: McqSummary) {
        dataManagerRepository.saveToDataStore(
            DataStoreKeys.TEST_ATTENDED_COUNT, mcq.attendedCount
        )
        dataManagerRepository.saveToDataStore(
            DataStoreKeys.TEST_NEW_COUNT, mcq.notAttendedCount
        )
        dataManagerRepository.saveToDataStore(
            DataStoreKeys.TEST_EXPIRED_COUNT, mcq.expiredCount
        )
    }

    private fun insertUser(student: Student) {
        viewModelScope.launch {
            userRepository.clearUser()
            userRepository.saveUser(student)
            Timber.e("is debug user ---> ${student.isDebugUser}")
            sharedPref.isDebugUser = student.isDebugUser
            downloadProfilePic(student.avatar)
            getUserData()
            student.settings?.let {
                sharedPref.dsl = it.deviceStorageLimit
                sharedPref.drdd = it.deviceResourceDeletionDays
                sharedPref.acc = it.deviceUnpinningClickCount
                sharedPref.dap = it.deviceUnpinningPassword
                sharedPref.techPhones = it.allowedIncomingPhNos.toString()
            }
        }
    }

    private suspend fun downloadProfilePic(avatar: String?) {
        avatar?.let {
            if (!FileUtil.isEmojiAlreadyDownloadedInsideImagesFolder(
                    getApplication(),
                    avatar
                )
            ) {
                downloadEmojiToDevice(avatar).distinctUntilChanged().collect { result ->
                    if (result?.second == DownloadStatus.FINISHED) {
                        getUserData()
                        Timber.tag("Profile Image Download -- > Success")
                    } else if (result?.second == DownloadStatus.ERROR) {
                        Timber.tag("Profile Image Download -- > Fail")

                    }
                }
            }
        }
    }

    private fun insertMyClassroom(myClassroom: List<MyClassroomItem>) {
        viewModelScope.launch {
            resourceRepository.insertMyClassroom(myClassroom)
        }
    }

    private fun deleteClassroomResource(deletedItems: DeletedItems) {
        deletedItems.details.forEach { detailsItem ->
            when (detailsItem.type) {
                "Book", "Video" -> {
                    viewModelScope.launch {
                        Timber.v("Delete Classroom -> Resource : ${detailsItem.classroom} - ${detailsItem.resource}")
                        resourceRepository.deleteClassroomResourceRelation(
                            detailsItem.classroom, detailsItem.resource
                        )
                    }
                }

                "Website" -> {
                    viewModelScope.launch {
                        resourceRepository.deleteClassroomWebsiteRelation(
                            detailsItem.classroom, detailsItem.resource
                        )
                    }
                }

                "lesson" -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        async {
                            resourceRepository.deleteClassroomLessonWebLinkMapping(
                                detailsItem.classroom, detailsItem.resource
                            )
                        }.await()
                        async {
                            resourceRepository.deleteClassroomLessonMapping(
                                detailsItem.classroom, detailsItem.resource
                            )
                        }.await()
                        async {
                            resourceRepository.deleteClassroomUnitLessonMapping(
                                detailsItem.classroom, detailsItem.resource, detailsItem.unit
                            )
                        }.await()
                    }
                }
            }
        }
    }

    private fun deleteClassroom(deletedItems: DeletedItems) {
        deletedItems.details.forEach { detailsItem ->
            viewModelScope.launch(Dispatchers.Default) {
                Timber.v("Delete Classroom/Club -> : ${detailsItem.resource}")
                async {
                    resourceRepository.deleteClassroomLessonMapping(
                        detailsItem.resource
                    )
                }.await()
                async {
                    resourceRepository.deleteClassroomResourceMapping(
                        detailsItem.resource
                    )
                }.await()
                async {
                    resourceRepository.deleteClassroomWebsiteMapping(
                        detailsItem.resource
                    )
                }.await()
                async {
                    resourceRepository.deleteClassroomLessonWebLinkMapping(
                        detailsItem.resource
                    )
                }.await()
                async {
                    resourceRepository.deleteClassroomUnitLessonMapping(
                        detailsItem.resource
                    )
                }.await()
                resourceRepository.deleteClassRoomOrClub(
                    detailsItem.resource
                )
            }
        }
    }

    private fun deleteMyLibrary(deletedItems: DeletedItems) {
        deletedItems.details.forEach { detailsItem ->
            viewModelScope.launch {
                Timber.v("Delete My Library -> : ${detailsItem.resource}")
                val resourceEntity =
                    async { resourceRepository.getResourceById(detailsItem.resource) }.await()
                resourceEntity?.let { resource ->

//                         update api returns a my library book to deleted, what first?

//                    checking the content isBonus

//                        isBonus true means the respective content is only a my library content,
//                                            false means it is not only a my library content

//                     checking that the content is only a my library or not
                    if (resource.isBonus == DBConstants.BonusContent.TRUE.value) {

//                        the content is only a my library content

//                        perform deleting item from db

                        resourceRepository.deleteResource(detailsItem.resource)

//                        delete file from internal storage

                        resource.file?.let { file ->
                            deleteFile(file)
                        }

                    } else if (resource.isBonus == DBConstants.BonusContent.FALSE.value) {
//                         the content is not only a my library content, it is a classroom content too
                        resourceRepository.updateIsBonusToFalse(detailsItem.resource)

                    }
                }
            }
        }
    }

    private fun deleteFile(deleteFile: String) {
        FileUtil.deleteFile(getApplication(), deleteFile).let { isDeleted ->
            Timber.e("isDeleted : $isDeleted -> $deleteFile ")
        }
    }

    //gets the last updated dates of each category
    fun fetchLastUpdatedContentDates() {
        viewModelScope.launch(Dispatchers.IO) {
            val exploreDate =
                dataManagerRepository.getFromDataStore(DataStoreKeys.EXPLORE_LAST_UPDATED_DATE)
            val myWebPlatformDate =
                dataManagerRepository.getFromDataStore(DataStoreKeys.MY_WEB_PLATFORM_LAST_UPDATED_DATE)
            val myLibraryDate =
                dataManagerRepository.getFromDataStore(DataStoreKeys.MY_LIBRARY_LAST_UPDATED_DATE)
            combine(
                exploreDate, myWebPlatformDate, myLibraryDate
            ) { exploreFlowDate, myWebPlatformFlowDate, myLibraryFlowDate ->
                _lastUpdatedLiveData.postValue(
                    LastUpdatedData(
                        exploreDate = exploreFlowDate ?: "",
                        myWebPlatformDate = myWebPlatformFlowDate ?: "",
                        myLibraryDate = myLibraryFlowDate ?: ""
                    )
                )
            }.stateIn(this)
        }
    }

    fun getSignalDrawable(status: SignalObserver.Status): Int {
        return when (status) {
            SignalObserver.Status.EXCELLENT -> R.drawable.ic_signal_cellular_full_bar
            SignalObserver.Status.GOOD -> R.drawable.ic_signal_cellular_3_bar
            SignalObserver.Status.AVERAGE -> R.drawable.ic_signal_cellular_2_bar
            SignalObserver.Status.WEAK -> R.drawable.ic_signal_cellular_1_bar
            SignalObserver.Status.NOT_AVAILABLE -> R.drawable.ic_signal_cellular_off
        }
    }

    fun getUserData() {
        viewModelScope.launch {
            _user.value = userRepository.getUsers()
        }
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

    fun setLastModeDate(currentDay: Int) {
        sharedPref.moodLastShown = currentDay
    }

    private fun getLastMoodDate(): Int {
        return sharedPref.moodLastShown
    }

    private suspend fun moodMeterEmoji(mood: List<MoodMeter>) {
        _isSyncing.value = NetworkLoadingState.LOADING
        val downloadList = mutableListOf<String>()
        mood.forEach { moodEmoji ->
            if (ValidationUtil.isNotNullOrEmpty(moodEmoji.emoji)) {
                downloadList.add(moodEmoji.emoji)
            }
        }
//        recursiveImageDownload(downloadList)
    }

    private suspend fun recursivePsmDownload(downloadList: MutableList<String>) {
        Timber.tag("Psm Download").d("Download list: $downloadList")
        if (downloadList.isNotEmpty()) {
            if (FileUtil.isPsmAlreadyDownloadedInsideImagesFolder(
                    getApplication(), downloadList.first()
                )
            ) {
                Timber.tag("Psm Download").d("Download Skipped")
                downloadList.removeFirst()
                recursivePsmDownload(downloadList)
            } else {
                downloadPsmToDevice(downloadList.first()).distinctUntilChanged()
                    .collect { result ->
                        if (result?.second == DownloadStatus.FINISHED) {
                            Timber.tag("Psm Download").d("${downloadList.first()} -> FINISHED")
                            downloadList.removeFirst()
                            recursivePsmDownload(downloadList)
                        } else if (result?.second == DownloadStatus.ERROR) {
                            Timber.tag("Psm Download").d("${downloadList.first()} -> FAILED")
                            downloadList.removeFirst()
                            recursivePsmDownload(downloadList)
                        }
                    }
            }
        } else {
            _isSyncing.value = NetworkLoadingState.SUCCESS
            Timber.tag("Psm Download").d("DOWNLOAD COMPLETED 💥")
        }
    }

    fun checkForDateMoodMeterOncePerDay() {
        viewModelScope.launch {

            val firstName = userRepository.getUserFirstName()
            val calendar = Calendar.getInstance()
            val currentDay = calendar[Calendar.DAY_OF_MONTH]
            val lastUpdatedDay: Int = getLastMoodDate()

            if (!firstName.isNullOrEmpty()) {
                if (lastUpdatedDay != currentDay) {
                    _showMoodMeterDialog.postValue(
                        firstName
                    )
                    fileCleaning()
                }
            }
        }
    }

    private fun insertModeMeterData(moodMeterItem: List<MoodMeter>) {
        viewModelScope.launch {
            resourceRepository.insertModeMeterData(moodMeterItem)
        }
    }

    fun insertClickedData(formattedDateTime: String, modeId: Int, modeName: String) {
        viewModelScope.launch {
            resourceRepository.insertClickedData(
                formattedDateTime, modeId, modeName
            )
        }
    }

    fun handlePsm() {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.getGeneralPsm()?.let {
                //fetches the corresponding psm
                val psm = getTodaysPsm(it)
                Timber.tag("PSM").d("General : $psm")
                psm?.let {
                    //saves to analytics if psm is not null
                    insertPSM(
                        AnalyticsEntity(
                            startTime = ViewUtil.getUtcTimeWithMSec(), psmId = psm.id
                        )
                    )
                }
                _psmStatus.postValue(
                    PsmData(
                        status = true,
                        image = psm?.url
                    )
                )
                dataManagerRepository.saveToDataStore(DataStoreKeys.PSM_STATUS, false)
            } ?: kotlin.run {
                /*if psm is empty, we posts the null value, so the default
                  psm will be shown
                 */
                _psmStatus.postValue(
                    PsmData(
                        status = true,
                        image = null
                    )
                )
            }
        }
    }

    fun getPsmByClassroom(classroomId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val psmData = resourceRepository.getPsmByClassroomId(classroomId)
            psmData?.let {
                val psm = getPsmByEntity(it)
                insertPSM(
                    AnalyticsEntity(
                        startTime = ViewUtil.getUtcTimeWithMSec(),
                        classroomId = classroomId,
                        psmId = psm.id
                    )
                )
                _psmStatus.postValue(
                    PsmData(
                        status = true,
                        image = psm.url
                    )
                )
            }
        }
    }

    fun submitMoodMeterData() {
        viewModelScope.launch {
            val offlineMoodList = resourceRepository.getClickedData()
            if (offlineMoodList.isNotEmpty()) {
                val moodList = mutableListOf<MoodsList>()
                offlineMoodList.forEach {
                    moodList.add(
                        MoodsList(
                            mood = it.id, date = it.time
                        )
                    )
                }
                MoodMeterRequest(
                    moods = moodList
                ).also {
                    resourceRepository.submitMoodMeterData(it).collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {

                                _moodMeterSubmit.postValue(result.data)

                                //clear clicked mood data from saved table
                                clearMoodData()

                                // download and update new data
//                                resourceRepository.getMoodEmojis()

                            }

                            is NetworkResult.Error -> {
                                Timber.e("${result.code} ${result.message}")
                            }

                            is NetworkResult.Failure -> {
                                Timber.e(result.exception)
                            }

                            is NetworkResult.Loading -> {}

                        }
                    }
                }
            }
        }
    }

    private fun getProfileUpdates() {
        viewModelScope.launch {
            if (app.hasInternetConnection()) {
                deviceRepository.getProfileUpdate().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            insertUser(result.data.data!!)
//                            insertPsm(result.data.data.psm)
                            handleGeneralPsm(result.data.data.psm)
                        }

                        is NetworkResult.Error -> {
                            Timber.e("${result.code} ${result.message}")
                            _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                false, result.message
                            )
                        }

                        is NetworkResult.Failure -> {
                            Timber.e(result.exception)
                            /*      _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                                 true, R.string.something_went_wrong
                             )*/
                        }

                        is NetworkResult.Loading -> {}
                    }
                }
            }
        }
    }

    private fun clearMoodData() {
        viewModelScope.launch {
            resourceRepository.clearMoodData()
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun fileCleaning() {
        Timber.tag("FILE_CLEANING").e("dsl : ${sharedPref.dsl}")
        Timber.tag("FILE_CLEANING").e("drdd : ${sharedPref.drdd}")
        val dsl = sharedPref.dsl
        viewModelScope.launch {
            Timber.tag("FILE_CLEANING")
                .e("checkpoint 1: ${FileUtil.getStorageInPercentage(getApplication())}")

            if (dsl == 0) {
                return@launch
            }

            if (dsl <= FileUtil.getStorageInPercentage(getApplication())) {
                val drdd = sharedPref.drdd
                val fileEntities = resourceRepository.getAllFile()
                if (fileEntities.isNotEmpty()) {
                    insertManualCleaning(AnalyticsEntity(startTime = ViewUtil.getUtcTimeWithMSec()))
                    for (file in fileEntities) {
                        file.createdTime?.let { ct ->
                            Timber.tag("FILE_CLEANING")
                                .e(
                                    "checkpoint 2: ${
                                        FileUtil.deleteFileIfCreatedBefore(
                                            ct,
                                            drdd
                                        )
                                    }"
                                )
                            if (FileUtil.deleteFileIfCreatedBefore(ct, drdd)) {
                                Timber.tag("FILE_CLEANING").e(
                                    "deletion : ${
                                        FileUtil.deleteFileFromPath(
                                            File(file.filePath)
                                        )
                                    }"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun manualCleaning(incomingDSL: Int, deleteThresholdInDays: Int) {
        Timber.tag("FILE_CLEANING").e("dsl : ${incomingDSL}")
        Timber.tag("FILE_CLEANING").e("drdd : ${deleteThresholdInDays}")
        val dsl = incomingDSL
        viewModelScope.launch {
            Timber.tag("FILE_CLEANING")
                .e("checkpoint 1: ${FileUtil.getStorageInPercentage(getApplication())}")

            if (dsl == 0) {
                return@launch
            }

            if (dsl <= FileUtil.getStorageInPercentage(getApplication())) {
                val fileEntities = resourceRepository.getAllFile()
                if (fileEntities.isNotEmpty()) {
                    insertManualCleaning(AnalyticsEntity(startTime = ViewUtil.getUtcTimeWithMSec()))
                    for (file in fileEntities) {
                        file.createdTime?.let { ct ->
                            Timber.tag("FILE_CLEANING")
                                .e(
                                    "checkpoint 2: ${
                                        deleteThresholdInDays.let {
                                            FileUtil.deleteFileIfCreatedBefore(
                                                ct,
                                                it
                                            )
                                        }
                                    }"
                                )
                            if (
                                FileUtil.deleteFileIfCreatedBefore(
                                    ct,
                                    deleteThresholdInDays
                                )
                            ) {
                                Timber.tag("FILE_CLEANING").e(
                                    "deletion : ${
                                        FileUtil.deleteFileFromPath(
                                            File(file.filePath)
                                        )
                                    }"
                                )
                            }
                        }
                    }
                }
            }
        }
    }


   /* fun getAvailableNetworkStatus() {
        val connectivityManager =
            app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
    }*/

  /*  fun rateResources(type: String, id: Int, rating: Float) {
        viewModelScope.launch {
            resourceRepository.postResourceRating(type, id, rating).collect {
                when (it) {
                    is NetworkResult.Error -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                    }

                    is NetworkResult.Failure -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                    }

                    is NetworkResult.Loading -> {
                        _isSyncing.value = NetworkLoadingState.LOADING
                    }

                    is NetworkResult.Success -> {
                        if (it.data.message == "SUCCESS") {
                            Timber.tag("RATE RESPONSE").e("API Rating Response ${it.data.message}")
                            if(type == "lesson"){
                                updateLessonRate(id,rating)
                            }
                            _isSyncing.value = NetworkLoadingState.SUCCESS
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

    }*/
}

enum class NetworkLoadingState {
    LOADING, SUCCESS, ERROR
}

enum class Bubble {
    CLASSROOM, NOTIFICATION, TEST
}

data class LastUpdatedData(
    val exploreDate: String, val myLibraryDate: String, val myWebPlatformDate: String
)

data class PostData(
    val name: String,
    val imageUrl: String,
    val classRooms: Map<String, Int>,
    val selectedImages: String
)

data class PsmData(
    var status: Boolean,
    var image: String?
)
