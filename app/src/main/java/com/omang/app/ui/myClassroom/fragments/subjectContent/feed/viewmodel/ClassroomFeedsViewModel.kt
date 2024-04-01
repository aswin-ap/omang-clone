package com.omang.app.ui.myClassroom.fragments.subjectContent.feed.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.data.model.feed.NotificationItem
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ClassroomFeedsViewModel @Inject constructor(
    val app: Application
) : BaseViewModel(app) {

    private var networkJob: Job? = null

    private val _feedLiveData = MutableLiveData<List<FeedEntity>?>()
    val feedLiveData: LiveData<List<FeedEntity>?> get() = _feedLiveData

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private var _classroomId: Int? = null

    private var _currentPageNumber = 1
    fun setClassroomId(id: Int) {
        _classroomId = id
    }

    fun getFeedsFromAPI() {
        networkJob = viewModelScope.launch(Dispatchers.IO) {
            if (app.hasInternetConnection()) {
                if (isActive) {
                    resourceRepository.getFeedsByClassroomId(
                        page = _currentPageNumber,
                        classroomId = _classroomId!!
                    ).collect { result ->
                        when (result) {
                            is NetworkResult.Error -> {
                                _isSyncing.postValue(NetworkLoadingState.ERROR)
                                _uiMessageStateLiveData.postValue(
                                    UIMessageState.StringMessage(
                                        false, result.message
                                    )
                                )
                                Timber.e("${result.code} ${result.message}")
                            }

                            is NetworkResult.Failure -> {
                                _isSyncing.postValue(NetworkLoadingState.ERROR)
                                _uiMessageStateLiveData.postValue(
                                    UIMessageState.StringResourceMessage(
                                        false, R.string.something_went_wrong
                                    )
                                )
                                Timber.e(result.exception)
                            }

                            is NetworkResult.Loading -> {
                                _isSyncing.postValue(NetworkLoadingState.LOADING)
                            }

                            is NetworkResult.Success -> {
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
    }

    fun getFeedsByClassroomId() {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.getFeedsByClassroomId(_classroomId!!).collect {
                Timber.tag("ClassroomFeed").d(it.toJson())
                val feedList = it.map { classroomEntity ->
                    FeedEntity(
                        id = classroomEntity.id,
                        resourceId = classroomEntity.resourceId,
                        title = classroomEntity.title,
                        description = classroomEntity.description,
                        imageUrl = classroomEntity.imageUrl,
                        createdAt = classroomEntity.createdAt,
                        createdBy = classroomEntity.createdBy,
                        postedTo = classroomEntity.postedTo,
                        classroomDetails = classroomEntity.classroomDetails,
                        feedType = null
                    )
                }
                _feedLiveData.postValue(feedList)
            }
        }
    }
    /*    private suspend fun insertNotification(notifications: List<NotificationItem>, page: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                val feedIdList = notifications.map { it.id }
                val isContentsExist =
                    async { resourceRepository.isFeedsExistsInClassroom(feedIdList) }.await()
                if (!isContentsExist) {
                    resourceRepository.insertClassroomFeeds(notifications, _classroomId!!)
                    _currentPageNumber += 1
                    if (_currentPageNumber <= page && page != 0)
                        getFeedsFromAPI()
                    else
                        networkJob?.cancel()
                }
            }
        }*/

    private suspend fun insertNotification(notifications: List<NotificationItem>, page: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.insertClassroomFeeds(notifications, _classroomId!!)
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    override fun onCleared() {
        super.onCleared()
        networkJob?.cancel()
    }

}
