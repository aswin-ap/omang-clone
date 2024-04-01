package com.omang.app.ui.myWebPlatforms.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.model.explore.Added
import com.omang.app.data.model.explore.ExploreDataResponse
import com.omang.app.data.model.explore.ExploreIndividualWebDataResponse
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.dataStore.SharedPref
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragmentDirections
import com.omang.app.utils.Resource
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.InternetSpeedChecker
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.hasInternetConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class MyWebPlatformViewModel @Inject constructor(
    val app: Application,
    private val resourceRepository: ResourceRepository,
    private val dataManagerRepository: DataManagerRepository,
    private val sharedPref: SharedPref
) : ViewModel() {

    @Inject
    lateinit var networkConnection: NetworkConnection

    @Inject
    lateinit var internetSpeedChecker: InternetSpeedChecker

    private val _webPlatformsLiveData = MutableLiveData<List<MyWebPlatformEntity>>()
    val webPlatformsLiveData: LiveData<List<MyWebPlatformEntity>> get() = _webPlatformsLiveData

    private val _exploreLiveData = MutableLiveData<List<MyWebPlatformEntity>>()
    val exploreLiveData: LiveData<List<MyWebPlatformEntity>> get() = _exploreLiveData

    private val _exploreLastUpdate = MutableLiveData<String>()
    val exploreLastUpdate: LiveData<String> get() = _exploreLastUpdate


    private val _individualWebResponse = MutableLiveData<ExploreIndividualWebDataResponse>()
    val individualWebResponse: LiveData<ExploreIndividualWebDataResponse> = _individualWebResponse

    private val _exploreDataResponse = MutableLiveData<List<ExploreDataResponse>>()
    val exploreDataResponse: LiveData<List<ExploreDataResponse>> = _exploreDataResponse

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    init {
        getAllMyWebPlatforms()
        getExploreItems()
    }

    private fun getAllMyWebPlatforms() {
        viewModelScope.launch {
            resourceRepository.getMyWebPlatforms().also { myWebPlatformEntities ->
                _webPlatformsLiveData.postValue(myWebPlatformEntities)
            }
        }
    }

    private fun getExploreItems() {
        viewModelScope.launch {
            resourceRepository.getMyWebPlatformItems().collect { myWebPlatformEntities ->
                _exploreLiveData.postValue(myWebPlatformEntities)
            }
        }
    }

    fun observeNetworkStatus() {
        viewModelScope.launch {
            networkConnection.observeNetworkConnection().collectLatest {
                Timber.d("NetworkStatus : $it")
                _networkStatus.postValue(it)
            }
        }
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
                                                MyWebPlatformsFragmentDirections.actionMyWebPlatformFragmentToWebViewerFragment(
                                                    webItem,
                                                    0,
                                                    0,
                                                    DBConstants.AnalyticsMenu.MY_WEB_PLATFORM.value
                                                ).also { direction ->
                                                    navController.navigate(direction)
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
                        _uiMessageStateLiveData.value =
                            UIMessageState.StringResourceMessage(
                                false,
                                R.string.something_went_wrong
                            )
                        Timber.e(networkResult.exception)
                    }

                    is NetworkResult.Loading -> {
                        _isSyncing.value = NetworkLoadingState.LOADING
                    }

                }
            }
        }
    }

    fun getMyWebPlatformData() {
        viewModelScope.launch {
            if (app.hasInternetConnection()) {
                resourceRepository.getMyWebPlatformData().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            _isSyncing.value = NetworkLoadingState.SUCCESS
                            sharedPref.myWebPlatformUpdate = false
                            //saves the last synced date
                            dataManagerRepository.saveToDataStore(
                                DataStoreKeys.MY_WEB_PLATFORM_LAST_UPDATED_DATE,
                                ViewUtil.getUtcTime()
                            )
                            result.data.let {

                                val exploreDataList = it
                                _exploreDataResponse.postValue(listOf(exploreDataList))

                                /*inserting data in added*/
                                exploreDataList.data?.let { exploreData ->
                                    insertExploreData(exploreData.added)
                                } ?: kotlin.run {
                                    _uiMessageStateLiveData.value =
                                        UIMessageState.StringResourceMessage(
                                            false,
                                            R.string.no_data_sync
                                        )
                                }
                                fetchLastUpdateTime()

                                /*deleting data in deleted*/
                                exploreDataList.data?.let { exploreDataList1 ->
                                    exploreDataList1.deleted.forEach { id ->
                                        resourceRepository.deleteMyWebPlatform(id)
                                    }
                                }

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
            } else {
                _isSyncing.postValue(NetworkLoadingState.ERROR)
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringResourceMessage(
                        false,
                        R.string.no_internet
                    )
                )
            }
        }
    }

    fun checkUpdates() = sharedPref.myWebPlatformUpdate

    private fun insertExploreData(myWebPlatformItems: List<Added>) {
        viewModelScope.launch {
            resourceRepository.insertMyWebPlatform(myWebPlatformItems)
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    override fun onCleared() {
        super.onCleared()
//        networkConnectionManager.stopListenNetworkState()
    }

    fun fetchLastUpdateTime() {
        viewModelScope.launch {
            val exploreDate = getLastDate()
            _exploreLastUpdate.value = exploreDate.toString()
        }
    }

    private suspend fun getLastDate(): String? {
        return withContext(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(DataStoreKeys.MY_WEB_PLATFORM_LAST_UPDATED_DATE)
                .first()
        }
    }
}