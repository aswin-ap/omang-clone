package com.omang.app.ui.explore.viewmodel

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
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.dataStore.DataStoreKeys.EXPLORE_LAST_UPDATED_DATE
import com.omang.app.dataStore.SharedPref
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.explore.fragment.WebPlatformsFragmentDirections
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.Resource
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.InternetSpeedChecker
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ExploreViewModel @Inject constructor(
    val app: Application,
) : BaseViewModel(app) {

    @Inject
    lateinit var networkConnection: NetworkConnection

  /*  @Inject
    lateinit var internetSpeedChecker: InternetSpeedChecker

    private val resourceRepository: ResourceRepository,
    private val dataManagerRepository: DataManagerRepository,
    private val sharedPref: SharedPref*/

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    private val _exploreDataResponse = MutableLiveData<List<ExploreDataResponse>>()
    val exploreDataResponse: LiveData<List<ExploreDataResponse>> = _exploreDataResponse

    private val _exploreLiveData = MutableLiveData<List<MyWebPlatformEntity>>()
    val exploreLiveData: LiveData<List<MyWebPlatformEntity>> get() = _exploreLiveData

    private val _exploreLastUpdate = MutableLiveData<String>()
    val exploreLastUpdate: LiveData<String> get() = _exploreLastUpdate

    private val _setFavoriteData = MutableLiveData<List<MyWebPlatformEntity>>()
    val setFavoriteData: LiveData<List<MyWebPlatformEntity>> = _setFavoriteData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData


   /* init {
        getExploreItems()
    }*/

    fun observeNetworkStatus() {
        viewModelScope.launch {
            networkConnection.observeNetworkConnection().collectLatest {
                Timber.d("NetworkStatus : $it")
                _networkStatus.postValue(it)
            }
        }
    }

    fun getExploreItems() {
        viewModelScope.launch {
            resourceRepository.getExploreItems().collect { myWebPlatformEntities ->
                _exploreLiveData.postValue(myWebPlatformEntities)
            }
        }
    }

    fun setFavorite(id: Int) {
        viewModelScope.launch {
            resourceRepository.setFavorite(id)
        }
    }

    fun removeFavorite(id: Int) {
        viewModelScope.launch {
            resourceRepository.removeFavorite(id)
        }
    }

    fun setTimeStamp(id: Int, timeData: String) {
        viewModelScope.launch {
            resourceRepository.setTimeStamp(id, timeData)
        }
    }

    fun getIndividualWebData(webId: Int, navController: NavController) {
        Timber.e("calling api ")
        _isSyncing.value = NetworkLoadingState.LOADING
        viewModelScope.launch {
            resourceRepository.getIndividualWebData(webId, true).collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Success -> {
                        networkResult.data.let { response ->
                            val webItem = response.data
                            internetSpeedChecker.checkInternetSpeed(app.applicationContext).also {
                                when (it) {
                                    is Resource.Error -> {
                                        _uiMessageStateLiveData.postValue(
                                            UIMessageState.StringMessage(
                                                false,
                                                it.message.toString()
                                            )
                                        )
                                        _isSyncing.value = NetworkLoadingState.SUCCESS
                                    }

                                    is Resource.Loading -> {}
                                    is Resource.Success -> {
                                        _isSyncing.value = NetworkLoadingState.SUCCESS
                                        it.data?.let { speed ->
                                            if (speed >= (webItem.threshold ?: 0)) {
                                                WebPlatformsFragmentDirections.actionWebPlatformFragmentToWebViewerFragment(
                                                    webItem,
                                                    0,
                                                    0,
                                                    DBConstants.AnalyticsMenu.EXPLORE.value
                                                ).also {
                                                    try {
                                                        navController.navigate(it)
                                                    } catch (e: IllegalArgumentException) {
                                                        Timber.tag("EXPLORE ALL NAVIGATION")
                                                            .e("Exception Message is ${e.message}")
                                                    }
                                                }
                                            } else {
                                                _uiMessageStateLiveData.value =
                                                    UIMessageState.StringMessage(
                                                        false,
                                                        "This website requires threshold of " +
                                                                "${webItem.threshold} kbps"
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

    fun getExploreWebData() {
//        235
        viewModelScope.launch {
            resourceRepository.getExploreItems().collect { myWebPlatformEntities ->
                val json = myWebPlatformEntities.toJson()
                log("request 1 : $json")

            }
        }

        viewModelScope.launch {
            log("check 1 : ${app.hasInternetConnection()}")
            if (app.hasInternetConnection()) {
                resourceRepository.getExploreWebData().collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            result.data.let {
                                //saves the last synced date
                                log("Success 1 : ${result.data.toJson()}")

                                dataManagerRepository.saveToDataStore(
                                    EXPLORE_LAST_UPDATED_DATE,
                                    ViewUtil.getUtcTime()
                                )
                                val exploreDataList = it
                                _exploreDataResponse.postValue(listOf(exploreDataList))
                                _isSyncing.value = NetworkLoadingState.SUCCESS

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

                                /*deleting data in deleted*/
                                exploreDataList.data?.let { exploreDataList1 ->
                                    exploreDataList1.deleted.forEach { id ->
                                        resourceRepository.deleteWebPlatform(id)
                                    }
                                }

                                sharedPref.exploreUpdate = false
                                fetchLastUpdateTime()
                            }
                        }

                        is NetworkResult.Error -> {
                            log("Error 1 : ${result.code} ${result.message}")

                            _isSyncing.value = NetworkLoadingState.ERROR
                            Timber.e("${result.code} ${result.message}")

                        }

                        is NetworkResult.Failure -> {
                            _isSyncing.value = NetworkLoadingState.ERROR
                            Timber.e(result.exception)
                            log("Failure 1 : ${result.exception}")

                        }

                        is NetworkResult.Loading -> {
                            _isSyncing.value = NetworkLoadingState.LOADING
                            log("loading")

                        }

                    }
                }
            } else {
                _isSyncing.postValue(NetworkLoadingState.ERROR)
                _uiMessageStateLiveData.value =
                    UIMessageState.StringResourceMessage(
                        false,
                        R.string.no_internet
                    )
            }
        }
    }

    fun checkUpdates() = sharedPref.exploreUpdate

    fun getLogs() = sharedPref.logs

    private fun insertExploreData(myWebPlatformItems: List<Added>) {
        viewModelScope.launch {
            resourceRepository.insertExploreData(myWebPlatformItems)
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun fetchFavItems() {
        viewModelScope.launch {
            resourceRepository.fetchFavItems()
                .collect { myWebPlatformEntities ->
                    _setFavoriteData.postValue(myWebPlatformEntities)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
//        networkConnection.stopListenNetworkState()
    }

    fun fetchLastUpdateTime() {
        viewModelScope.launch {
            val exploreDate = getLastDate()
            _exploreLastUpdate.value = exploreDate.toString()
        }
    }

    private suspend fun getLastDate(): String? {
        return withContext(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(EXPLORE_LAST_UPDATED_DATE).first()
        }
    }

    fun log(log: String) {
        var logs = sharedPref.logs
        logs += "\n${ViewUtil.getUtcTimeWithMSec()} :   : " + log
        sharedPref.logs = logs
    }
}