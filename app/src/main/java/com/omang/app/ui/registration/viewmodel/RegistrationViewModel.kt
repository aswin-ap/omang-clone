package com.omang.app.ui.registration.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.BuildConfig
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.fcm.Payload
import com.omang.app.data.model.updates.DeviceUpdatesData
import com.omang.app.data.model.userAssign.Student
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.dataStore.DataStoreKeys.FCM_TOKEN
import com.omang.app.dataStore.DataStoreKeys.IS_USER_ASSIGNED
import com.omang.app.di.AppDispatchers
import com.omang.app.di.Dispatcher
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.utils.DeviceUtil
import com.omang.app.utils.FcmTokenFetcher
import com.omang.app.utils.UIMessageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import javax.inject.Inject

typealias IMEI = String
typealias SIM_NO = String
typealias USER_ID = Int
typealias USER_NAME = String

data class RegistrationUiState(
    var imei: IMEI,
    var simNo: SIM_NO,
    var userId: USER_ID,
    var userName: USER_NAME,
    var password: String,
)

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    application: Application,
    @Dispatcher(AppDispatchers.IO) private val dispatcher: CoroutineDispatcher,
) : BaseViewModel(application) {
    private val _registrationState = MutableLiveData(
        RegistrationUiState("", "", 0, "", "")
    )

    val registrationState: LiveData<RegistrationUiState> get() = _registrationState

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _progress = MutableLiveData(false)
    val progress: LiveData<Boolean> get() = _progress

    private val _pin = MutableLiveData(false)
    val pin: LiveData<Boolean> get() = _pin

    val accessToken: LiveData<String?> get() = _accessToken
    private val _accessToken = MutableLiveData<String?>(null)

    private val _deviceUpdatesResponse = MutableLiveData<DeviceUpdatesData>()
    val deviceUpdatesResponse: LiveData<DeviceUpdatesData> = _deviceUpdatesResponse

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
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

    private fun getDeviceUpdates() {
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
                            false, R.string.something_went_wrong
                        )
                    }

                    is NetworkResult.Loading -> {}
                }
            }
        }
    }

    fun registerDevice() {
        if (registrationState.value!!.imei.isEmpty()) {
            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                false, R.string.scan_barcode_error_message
            )
            return
        }
        if (registrationState.value!!.simNo.isEmpty()) {
            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                false, R.string.sim_validation
            )
            return
        }
        if (registrationState.value!!.password.isEmpty()) {
            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                false, R.string.scan_password_error_message
            )
            return
        }

        viewModelScope.launch { ->
            registrationState.value?.let { registrationState ->
                dataManagerRepository.saveToDataStore(DataStoreKeys.IMEI_NO, registrationState.imei)
                dataManagerRepository.getFromDataStore(FCM_TOKEN).asLiveData()
                val fcm = dataManagerRepository.getFromDataStore(FCM_TOKEN).stateIn(this)
                registrationRepository.registerDevice(
                    registrationState.imei,
                    registrationState.simNo,
                    fcm.value?:"",
                    registrationState.password,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    DeviceUtil.getAppVersion(),
                    DeviceUtil.getOS()
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Error -> {
                            _progress.value = false
                            Timber.e("${result.code} ${result.message}")
                            _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                false,
                                result.message
                            )
                        }

                        is NetworkResult.Failure -> {
                            _progress.value = false
                            Timber.e(result.exception)
                            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                                false, R.string.something_went_wrong
                            )
                        }

                        is NetworkResult.Loading -> {
                            _progress.value = true
                        }

                        is NetworkResult.Success -> {
                            _progress.value = false

                            result.data.accessToken?.let { token ->
                                sharedPref.accessToken = token
                            }

                            result.data.refreshToken?.let { token ->
                                sharedPref.refreshToken = token
                            }

                            _accessToken.value = result.data.accessToken
                            _registrationState.value = _registrationState.value!!.copy(
                                imei = "", simNo = "", password = ""
                            )

                            _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                true, "Device Registered Successfully"
                            )

                            if (!BuildConfig.DEBUG) {
                                initPinDevice()
                            }
                        }
                    }
                }
            }
        }
    }

    fun assignUser() {
        if (registrationState.value!!.userId == 0) {
            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                false, resId = R.string.scan_barcode_user_assign_error_message
            )
            return
        }
        if (registrationState.value!!.password.isEmpty()) {
            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                false, R.string.scan_password_error_message
            )
            return
        }
        viewModelScope.launch { ->
            registrationState.value?.let { registrationState ->
                userRepository.assignUser(
                    registrationState.userId,
                    registrationState.simNo,
                    registrationState.password,
                ).collect { result ->
                    when (result) {
                        is NetworkResult.Error -> {
                            _progress.value = false
                            Timber.e("${result.code} ${result.message}")
                            _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                                false, result.message
                            )
                        }

                        is NetworkResult.Failure -> {
                            _progress.value = false
                            Timber.e(result.exception)
                            _uiMessageStateLiveData.value = UIMessageState.StringResourceMessage(
                                false, R.string.something_went_wrong
                            )
                        }

                        is NetworkResult.Loading -> {
                            _progress.value = true
                        }

                        is NetworkResult.Success -> {
                            _progress.value = false
                            result.data.student?.let {
                                insertUser(it)
                                dataManagerRepository.saveToDataStore(
                                    IS_USER_ASSIGNED, true
                                )
                            }
                            Timber.e("first name --> " + result.data.student?.firstName + " last name -->" + result.data.student?.lastName)

                            _uiMessageStateLiveData.value = UIMessageState.ScreenTransaction(
                                true, R.string.user_assigned_successfully
                            )
                        }
                    }
                }
            }
        }
    }

    private fun insertUser(student: Student) {
        viewModelScope.launch {
            userRepository.clearUser()
            userRepository.saveUser(student)
        }
    }

    private suspend fun initPinDevice() {
        delay(1000)
        if (!isDeviceOwner()) {
            _pin.postValue(true)
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun updateState(registrationState: RegistrationUiState) {
        _registrationState.value = registrationState
    }

    fun updateToken() {
        viewModelScope.launch(dispatcher) {
            _accessToken.postValue(sharedPref.accessToken)
        }
    }

    fun updatePassword(password: String = "") {
        _registrationState.value = _registrationState.value!!.copy(password = password)
    }

    fun updateImei(imei: IMEI = "") {
        _registrationState.value = _registrationState.value!!.copy(imei = imei)
    }

    fun updateSimNo(simNo: SIM_NO = "") {
        _registrationState.value = _registrationState.value!!.copy(simNo = simNo)
    }

    fun updateUserId(userId: USER_ID = 0, userName: USER_NAME = "") {
        _registrationState.value = _registrationState.value!!.copy(userId = userId)
        _registrationState.value = _registrationState.value!!.copy(userName = userName)
    }

    fun saveFCMToken() {
        viewModelScope.launch(dispatcher) {
            dataManagerRepository.saveToDataStore(FCM_TOKEN, FcmTokenFetcher.fetchFcmToken())

        }
    }

    fun isImeiValid(imei: String): Boolean {
        val imeiRegex = Regex("^\\d{15}$")

        /*  // Check length and numeric using regex
          if (!imei.matches(imeiRegex)) {
              return false
          }*/

        // Additional checks (e.g., Luhn algorithm)
        /*    val total = imei.dropLast(1).reversed().mapIndexed { index, digit ->
                val num = digit.toString().toInt()
                if (index % 2 == 0) num else (num * 2 % 10) + (num * 2 / 10)
            }.sum()

            val checkDigit = (10 - (total % 10)) % 10
            return checkDigit == imei.last().toString().toInt()
    */
        return imei.matches(imeiRegex)
    }


    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }

}