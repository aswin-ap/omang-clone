package com.omang.app.ui.myClassroom.viewmodel

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.model.explore.ExploreIndividualWebDataResponse
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.fragments.subjectContent.SubjectContentsFragmentDirections
import com.omang.app.ui.webViewer.fragment.WebViewerFragment
import com.omang.app.utils.Resource
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.connectivity.InternetSpeedChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlatformsViewModel @Inject constructor(
    val app: Application,
    private val resourceRepository: ResourceRepository,
) : ViewModel() {

    @Inject
    lateinit var internetSpeedChecker: InternetSpeedChecker

    private var _classRoomId: Int? = null

    private val _platformsLiveData = MutableLiveData<List<MyWebPlatformEntity>>()
    val platformsLiveData: LiveData<List<MyWebPlatformEntity>> get() = _platformsLiveData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData
    private val _webLinks = MutableLiveData<List<MyWebPlatformEntity>>()

    private val _individualWebResponse = MutableLiveData<List<ExploreIndividualWebDataResponse>>()
    val individualWebResponse: LiveData<List<ExploreIndividualWebDataResponse>> =
        _individualWebResponse

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    fun setClassRoomId(id: Int) {
        viewModelScope.launch {
            _classRoomId = id
            Timber.tag("LessonsViewModel").d("classRoomId: $_classRoomId")
        }
    }

    fun getPlatforms() {
        try {
            viewModelScope.launch {
                resourceRepository.getWebsiteByClassroomID(_classRoomId!!)
                    .also { myWebPlatformEntity ->
                        _platformsLiveData.postValue(myWebPlatformEntity)
                    }
            }
        } catch (e: Exception) {
            _uiMessageStateLiveData.postValue(
                UIMessageState.StringResourceMessage(
                    true,
                    R.string.something_went_wrong
                )
            )
            Timber.e("Error fetching db : $e")
        }
    }

    fun getWebsiteData(websiteId: Int, navController: NavController) {
        _isSyncing.value = NetworkLoadingState.LOADING

        viewModelScope.launch {
            resourceRepository.getWebsiteData(websiteId).collect { result ->

                when (result) {
                    is NetworkResult.Success -> {
                        result.data.let {

                            val data = it.data[0]
                            _isSyncing.value = NetworkLoadingState.SUCCESS

                            val bundle = Bundle().apply {
                                putParcelable("weblinkData", data)
                            }

                            val fragment = WebViewerFragment()
                            fragment.arguments = bundle

                            navController.navigate(
                                R.id.action_subjectContentsFragment_to_webViewerFragment,
                                bundle
                            )

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

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun getIndividualWebData(webId: Int, navController: NavController) {
        _isSyncing.value = NetworkLoadingState.LOADING
        viewModelScope.launch {
            resourceRepository.getIndividualWebData(webId, false).collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Success -> {
                        networkResult.data.let { response ->
                            val webItem = response.data
                            internetSpeedChecker.checkInternetSpeed(app.applicationContext).also {
                                when (it) {
                                    is Resource.Error -> {
                                        _uiMessageStateLiveData.postValue(
                                            UIMessageState.StringMessage(
                                                false, it.message.toString()
                                            )
                                        )
                                        _isSyncing.value = NetworkLoadingState.SUCCESS
                                    }

                                    is Resource.Loading -> {}
                                    is Resource.Success -> {
                                        _isSyncing.value = NetworkLoadingState.SUCCESS
                                        it.data?.let { speed ->
                                            if (speed >= (webItem.threshold ?: 0)) {
                                                _classRoomId?.let { classroomId ->
                                                    SubjectContentsFragmentDirections.actionPlatformFragmentToWebViewerFragment(
                                                        webItem,
                                                        0,
                                                        classroomId,
                                                        DBConstants.AnalyticsMenu.CLASSROOM.value
                                                    ).also {
                                                        navController.navigate(it)
                                                    }
                                                }
                                            } else {
                                                _uiMessageStateLiveData.postValue(
                                                    UIMessageState.StringMessage(
                                                        false,
                                                        "This website requires threshold of " +
                                                                "${webItem.threshold} kbps"
                                                    )
                                                )
                                            }
                                        }
                                    }
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

                    else -> {}
                }
            }
        }
    }
}