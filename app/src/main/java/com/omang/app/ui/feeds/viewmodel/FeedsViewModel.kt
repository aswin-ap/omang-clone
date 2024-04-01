package com.omang.app.ui.feeds.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.feed.CreatedBy
import com.omang.app.data.model.feed.FeedRequest
import com.omang.app.data.model.feed.NotificationItem
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.feeds.fragments.FeedsFragmentDirections
import com.omang.app.ui.home.activity.ControlNavigation.safeNavigateWithArgs
import com.omang.app.ui.home.fragments.HomeFragmentDirections
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.home.viewmodel.PostData
import com.omang.app.utils.NotificationUtil
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.hasInternetConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FeedsViewModel @Inject constructor(
    val app: Application
) : BaseViewModel(app) {
    private var networkJob: Job? = null

    private val _feedLiveData = MutableLiveData<List<FeedEntity>?>()
    val feedLiveData: LiveData<List<FeedEntity>?> get() = _feedLiveData

    private val _postLiveData = MutableLiveData<PostData>()
    val postLiveData: LiveData<PostData> = _postLiveData

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private var _currentPageNumber = 1

    var postToValue = "Choose Post to"
    var classRoomId = 0
    var postDescription = ""
    var selectedImageUrl = ""

    fun getFeeds() {
        getNotificationLogsFromAPI()
        getNotificationLogsFromDB()
    }

    private fun getNotificationLogsFromDB() = viewModelScope.launch(Dispatchers.IO) {
        resourceRepository.getAllNotifications().collect { feedList ->
            feedList.sortedByDescending { it.createdAt }.also {
                _feedLiveData.postValue(it)
            }
        }
    }

    private fun getNotificationLogsFromAPI() {
        viewModelScope.launch {
            resourceRepository.getPushNotificationsLog().collect { result ->
                when (result) {
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

                    is NetworkResult.Success -> {
                        result.data.let {
                            result.data.let {
                                insertNotification(
                                    result.data.data.notificationLogs,
                                    result.data.data.pagination?.totalPages!!
                                )
                                _isSyncing.postValue(NetworkLoadingState.SUCCESS)
                            }
                        }
                    }
                }
            }
        }
    }

    /*private fun insertNotification(notifications: List<NotificationItem>, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val feedIdList = notifications.map { it.id }
            val isContentsExist =
                async { resourceRepository.isPublicFeedsExists(feedIdList) }.await()
            if (!isContentsExist) {
                resourceRepository.insertPushNotificationLog(notifications)
                _currentPageNumber += 1
                if (_currentPageNumber <= page && page != 0)
                    getNotificationLogsFromAPI()
                else
                    networkJob?.cancel()
            }
        }
    }*/

    private fun insertNotification(notifications: List<NotificationItem>, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.insertPushNotificationLog(notifications)
        }
    }

    /**
     * deletes all notifications in the device
     */
    fun clearAllNotifications(deleteUpdateRequest: DeleteUpdateRequest? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            deviceRepository.deleteNotifications(deleteUpdateRequest).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _isSyncing.postValue(NetworkLoadingState.SUCCESS)
                        resourceRepository.clearAllLocalNotifications()
                        _feedLiveData.postValue(null)
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringMessage(
                                false, "Feeds cleared successfully"
                            )
                        )
                    }

                    is NetworkResult.Error -> {
                        _isSyncing.postValue(NetworkLoadingState.ERROR)
                        Timber.e("${result.code} ${result.message}")
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringMessage(
                                false, result.message
                            )
                        )
                    }

                    is NetworkResult.Failure -> {
                        _isSyncing.postValue(NetworkLoadingState.ERROR)
                        Timber.e(result.exception)
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringResourceMessage(
                                true, R.string.something_went_wrong
                            )
                        )
                    }

                    is NetworkResult.Loading -> _isSyncing.postValue(NetworkLoadingState.LOADING)
                }
            }
        }
    }

    //get initial details for post
    fun getPostDetails() {
        viewModelScope.launch {
            var studentName = ""
            var imageUrl = ""
            val classroomMap = LinkedHashMap<String, Int>()
            val classRoomClubsEntities =
                async { resourceRepository.fetchMyClassroomsAndClubsList() }.await()
            val userData = async { userRepository.getUsers() }.await()
            // Populate the HashMap with classroom names and corresponding IDs
            for (classroom in classRoomClubsEntities) {
                classroomMap[classroom.name] = classroom.id
            }
            //adds the Post to option in first
            val newClassroomMap = linkedMapOf("Choose classroom" to 0) + classroomMap

            imageUrl = userData?.avatar.toString()
            studentName = "${userData?.firstName} ${userData?.lastName}"
            _postLiveData.postValue(
                PostData(
                    name = studentName,
                    imageUrl = imageUrl,
                    classRooms = newClassroomMap,
                    selectedImages = selectedImageUrl
                )
            )
        }
    }

    fun postFeed() {
        viewModelScope.launch {
            if (postToValue == "Choose Post to") {
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringMessage(
                        false, "Please select post to"
                    )
                )
            } else if ((postToValue == "Classroom or Clubs" || postToValue == "Teachers") && classRoomId == 0) {
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringMessage(
                        false, "Please select Classroom and club"
                    )
                )
            } else if (!ValidationUtil.isNotNullOrEmpty(postDescription)) {
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringMessage(
                        false, "Please enter post description"
                    )
                )
            } else {
                if (app.hasInternetConnection()) {
                    _isSyncing.value = NetworkLoadingState.LOADING
                    //if the request has contains the image, send the image to upload API
                    // then  makes the post request of feed else directly posts the feed
                    if (ValidationUtil.isNotNullOrEmpty(selectedImageUrl)) {
                        val imageFile = File(selectedImageUrl)
                        val requestFile =
                            imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        Timber.e("requestFile : requestFile: $requestFile")
                        val imagePart =
                            MultipartBody.Part.createFormData(
                                "file",
                                File(selectedImageUrl).name.toString(),
                                requestFile
                            )

                        resourceRepository.imageUpload(imagePart).collect { result ->
                            when (result) {

                                is NetworkResult.Success -> {
                                    _isSyncing.value = NetworkLoadingState.SUCCESS
                                    createFeed(
                                        postToValue,
                                        classRoomId,
                                        result.data.data.id,
                                        postDescription
                                    )
                                }

                                is NetworkResult.Error -> {
                                    _isSyncing.value = NetworkLoadingState.ERROR
                                    Timber.e("${result.code} ${result.message}")
                                    _uiMessageStateLiveData.postValue(
                                        UIMessageState.StringResourceMessage(
                                            true,
                                            R.string.something_went_wrong
                                        )
                                    )
                                }

                                is NetworkResult.Failure -> {
                                    _isSyncing.value = NetworkLoadingState.ERROR
                                    Timber.e(result.exception)
                                    _uiMessageStateLiveData.postValue(
                                        UIMessageState.StringResourceMessage(
                                            true, R.string.something_went_wrong
                                        )
                                    )
                                }

                                is NetworkResult.Loading -> {
                                    _isSyncing.value = NetworkLoadingState.LOADING
                                }

                            }
                        }
                    } else {
                        createFeed(postToValue, classRoomId, null, postDescription)
                    }
                } else {
                    _uiMessageStateLiveData.postValue(
                        UIMessageState.StringMessage(
                            true,
                            "No network available to post a feed. Please try again later"
                        )
                    )
                }
            }
        }
    }

    private fun createFeed(
        postToValue: String,
        classRoomId: Int,
        imageId: Int? = null,
        description: String
    ) {
        viewModelScope.launch {
            //calculates the sent to value
            val sentToValue = when (postToValue) {
                "Teachers" -> "TEACHERS"
                "Classroom or Clubs" -> "CLASSROOM"
                "My investor" -> "INVESTOR"
                else -> ""
            }
            val hasClassroomId = postToValue == "Teachers" || postToValue == "Classroom or Clubs"
            deviceRepository.postFeed(
                FeedRequest(
                    sentTo = sentToValue,
                    classroom = if (hasClassroomId) classRoomId else null,
                    image = imageId,
                    description = description
                )
            ).collect { result ->
                when (result) {
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

                    is NetworkResult.Success -> {
                        _isSyncing.value = NetworkLoadingState.SUCCESS
                        insertLatestNotification(result.data.data)
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringMessage(
                                true,
                                "Feed posted successfully"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun insertLatestNotification(data: com.omang.app.data.model.feed.PostData?) {
        data?.let { postData ->
            insertNotification(
                listOf(
                    NotificationItem(
                        id = postData.id,
                        resource = postData.resource,
                        name = postData.name,
                        description = postData.description,
                        file = postData.file,
                        createdBy = CreatedBy(
                            firstName = postData.createdBy?.firstName,
                            lastName = postData.createdBy?.lastName,
                            avatar = postData.createdBy?.avatar
                        ),
                        createdAt = postData.createdAt,
                        sentTo = postData.sentTo,
                        classroom = postData.classroom,
                        updateType = postData.updateType,
                    )
                ), 0
            )
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    override fun onCleared() {
        super.onCleared()
        networkJob?.cancel()
    }

    fun navigate(type: Int?, id: Int?, navController: NavController) {
        type?.let { feedType ->
            when (feedType) {
                NotificationUtil.MY_WEBPLATFORM.type -> {
                    navController.navigate(R.id.action_navigation_notifications_to_myWebPlatformsFragment2)
                }

                NotificationUtil.WEB_PLATFORM.type -> {
                    navController.navigate(R.id.action_navigation_notifications_to_navigation_web_platform)
                }

                NotificationUtil.MY_LIBRARY.type -> {
                   // navController.navigate(R.id.action_navigation_notifications_to_navigation_library)
                   navController.safeNavigateWithArgs(FeedsFragmentDirections.actionNavigationNotificationsToNavigationLibrary())
                }

                NotificationUtil.CLASSROOM.type -> {
                    navController.navigate(R.id.action_navigation_notifications_to_navigation_classroom)
                }

                NotificationUtil.CLASSROOM_RESOURCE.type, NotificationUtil.MCQ_TEST.type, NotificationUtil.PSM.type -> {
                    id?.let { classroomId ->
                        FeedsFragmentDirections.actionNavigationNotificationsToSubjectContentsFragment(
                            classroomId
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                else -> {}
            }
        }
    }
}