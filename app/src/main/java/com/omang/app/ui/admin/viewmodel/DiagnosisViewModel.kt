package com.omang.app.ui.admin.viewmodel

import android.app.Application
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.ui.admin.model.AppDiagnosisStatus
import com.omang.app.ui.admin.model.DiagnosticsStatus
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.hasInternetConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import javax.inject.Inject


@HiltViewModel
class DiagnosisViewModel @Inject constructor(private val application: Application) :
    BaseViewModel(application) {
    private val _diagnosisStatus = MutableLiveData<List<AppDiagnosisStatus>>()
    val diagnosisStatus: LiveData<List<AppDiagnosisStatus>> = _diagnosisStatus

    private val _loadingStatus = MutableLiveData<NetworkLoadingState>()
    val loadingStatus: LiveData<NetworkLoadingState> = _loadingStatus
    private val _progress = MutableLiveData(false)
    val progress: LiveData<Boolean> get() = _progress
    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData
    private var diagnosisDataList: List<AppDiagnosisStatus> = mutableListOf()
    lateinit var csdkStatus: Flow<Boolean?>
    lateinit var pinnedStatus: Flow<Boolean?>
    var webStatus: Boolean = false
    var internetSpeedStatus: String = ""
    var apiStatusCode: Int = 0
    var apiResponseStatus: Int = -1

    fun runDiagnosis() {
        viewModelScope.launch {
            async { getCsdkStatus() }.await()
        }
    }

    private suspend fun getCsdkStatus() {
        getDeviceStatus().collectLatest { deviceStatus ->
            diagnosisDataList = deviceStatus
            _diagnosisStatus.postValue(diagnosisDataList)
        }
    }

    private suspend fun getDeviceStatus(): Flow<List<AppDiagnosisStatus>> {
        csdkStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.CSDK_LICENSE_STATUS)
        val deviceOwnerStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.DEVICE_OWNER_STATUS)
        pinnedStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.PINNED_STATUS)
        val lockedStatus =
            dataManagerRepository.getFromDataStore(DataStoreKeys.DEVICE_LOCK_STATUS)
        apiResponseStatus = if (application.hasInternetConnection()) {
            getDiagnosisAPIStatus()
        } else {
            0
        }
        val webAccessStatus = performWebRequest()
        //val speedStatus = getInternetSpeed()
        Timber.v("apiResponseStatus after call, $apiStatusCode")
        return combine(
            csdkStatus,
            pinnedStatus,
        ) { csdkStatus, pinnedStatus ->
            listOf(
                AppDiagnosisStatus(
                    0,
                    if (csdkStatus == true) DiagnosticsStatus.SUCCESS else DiagnosticsStatus.FAILED,

                    ),
                AppDiagnosisStatus(
                    1,
                    if (pinnedStatus == true) DiagnosticsStatus.SUCCESS else DiagnosticsStatus.FAILED,
                ),
                AppDiagnosisStatus(
                    2,
                    if (apiResponseStatus == 200) {
                        DiagnosticsStatus.SUCCESS
                    } else if (apiResponseStatus == 0) {
                        DiagnosticsStatus.LOADING
                    } else {
                        DiagnosticsStatus.FAILED
                    },
                ),
                AppDiagnosisStatus(
                    3,
                    if (webAccessStatus) DiagnosticsStatus.SUCCESS else DiagnosticsStatus.FAILED,

                    ),
                /* AppDiagnosisStatus(
                     4,
                     speedStatus,

                     ),*/
            )
        }.stateIn(viewModelScope)
    }

    /*private fun getInternetSpeed() : String{
        viewModelScope.launch(Dispatchers.IO) {
            internetSpeedChecker.checkInternetSpeed(application.applicationContext).also {
                when (it) {
                    is Resource.Error -> {
                        _loadingStatus.postValue(NetworkLoadingState.ERROR)
                      //  _internetSpeed.postValue(it.message!!)
                    }

                    is Resource.Loading -> _loadingStatus.postValue(NetworkLoadingState.LOADING)
                    is Resource.Success -> {
                        _loadingStatus.postValue(NetworkLoadingState.SUCCESS)
                        internetSpeedStatus = InternetSpeedChecker.getConvertedSpeed(it.data!!)
                       *//* _internetSpeed.postValue(
                            application.applicationContext.getString(
                                R.string.internet_speed_title, speed
                            )
                        )*//*
                    }

                    else -> {}
                }
            }
        }
        return internetSpeedStatus
    }*/

    private suspend fun performWebRequest(): Boolean {
        return withContext(Dispatchers.IO) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            if (application.hasInternetConnection()) {
                try {
                    val address: InetAddress = InetAddress.getByName("google.com")
                    when {
                        address.isReachable(5000) -> { // timeout in milliseconds
                            webStatus = true
                        }

                        else -> {
                            webStatus = false
                        }
                    }
                } catch (e: IOException) {
                    webStatus = false
                }
            } else {
                webStatus = false
                _uiMessageStateLiveData.postValue(UIMessageState.StringResourceMessage(
                    false,
                    R.string.no_internet
                ))

            }
             webStatus
        }
    }

    private fun getDiagnosisAPIStatus(): Int {
        viewModelScope.launch {
            if (application.hasInternetConnection()) {
                deviceRepository.getDiagnosisStatus().collect { result ->
                    val index = 2
                    if (index in diagnosisDataList.indices) {
                        diagnosisDataList[index].value =
                            DiagnosticsStatus.LOADING
                    }else{
                        Timber.tag("Diagnosis Loading Index Exception").e("Index $index is out of bounds")

                    }
                    when (result) {
                        is NetworkResult.Success -> {
                            _progress.value = false
                            result.let {
                                apiStatusCode = result.data
                                if (index in diagnosisDataList.indices) {
                                    diagnosisDataList[index].value =
                                        if (result.data == 200) DiagnosticsStatus.SUCCESS else DiagnosticsStatus.FAILED
                                }else{
                                    Timber.tag("Diagnosis Success Index Exception").e("Index $index is out of bounds")

                                }

                                Timber.e("API Status----${apiStatusCode} ")
                            }
                        }

                        is NetworkResult.Error -> {
                            _progress.value = false
                            Timber.e("${result.code} ${result.message}")
                            _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                false, result.message
                            )
                            if (index in diagnosisDataList.indices) {
                                diagnosisDataList[index].value =
                                    DiagnosticsStatus.FAILED
                            }else{
                                Timber.tag("Diagnosis Error Index Exception").e("Index $index is out of bounds")

                            }
                        }

                        is NetworkResult.Failure -> {
                            _progress.value = false
                            Timber.e(result.exception)
                            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                                true, R.string.something_went_wrong
                            )
                            if (index in diagnosisDataList.indices) {
                                diagnosisDataList[index].value =
                                    DiagnosticsStatus.LOADING
                            }else{
                                Timber.tag("Diagnosis Failure Index Exception").e("Index $index is out of bounds")

                            }
                        }

                        is NetworkResult.Loading -> {
                            _progress.value = true
                            if (index in diagnosisDataList.indices) {
                                diagnosisDataList[index].value =
                                    DiagnosticsStatus.LOADING
                            }else{
                                Timber.tag("Diagnosis Loading Index Exception").e("Index $index is out of bounds")

                            }
                        }

                        else -> {}
                    }

                    _diagnosisStatus.postValue(diagnosisDataList)

                }
            } else {
                apiStatusCode = 0
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringResourceMessage(
                        false,
                        R.string.no_internet
                    ))
            }
        }
        return apiStatusCode
    }

}


