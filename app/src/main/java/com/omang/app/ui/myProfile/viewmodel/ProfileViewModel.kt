package com.omang.app.ui.myProfile.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.user.User
import com.omang.app.data.model.myProfile.ProfilePayLoad
import com.omang.app.data.model.techSupport.ImageUpload
import com.omang.app.data.model.userAssign.Student
import com.omang.app.data.model.userAssign.UserAssignResponse
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.FileUtil
import com.omang.app.utils.Resource
import com.omang.app.utils.connectivity.InternetSpeedChecker
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import timber.log.Timber
import java.io.File
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val app: Application
) : BaseViewModel(app) {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _classroomList = MutableLiveData<List<String>>()
    val classroomList: LiveData<List<String>> = _classroomList

    private val _clubsList = MutableLiveData<List<String>>()
    val clubsList: LiveData<List<String>> = _clubsList

    private val _imageUpload = MutableLiveData<ImageUpload>()
    val imageUpload: LiveData<ImageUpload> = _imageUpload

    private val _internetSpeed = MutableLiveData<String>()
    val internetSpeed: LiveData<String> = _internetSpeed

    private val _loadingStatus = MutableLiveData<NetworkLoadingState>()
    val loadingStatus: LiveData<NetworkLoadingState> = _loadingStatus

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    fun getUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            _user.postValue(userRepository.getUsers())
            _clubsList.postValue(resourceRepository.getMyClubsList())
            _classroomList.postValue(resourceRepository.getMyClassroomsList())
        }
    }

    fun getInternetSpeed() {
        viewModelScope.launch(Dispatchers.IO) {
            _loadingStatus.postValue(NetworkLoadingState.LOADING)
            internetSpeedChecker.checkInternetSpeed(app.applicationContext).also {
                when (it) {
                    is Resource.Error -> {
                        _loadingStatus.postValue(NetworkLoadingState.ERROR)
                        _internetSpeed.postValue(it.message!!)
                    }

                    is Resource.Loading -> _loadingStatus.postValue(NetworkLoadingState.LOADING)
                    is Resource.Success -> {
                        _loadingStatus.postValue(NetworkLoadingState.SUCCESS)
                        val speed = InternetSpeedChecker.getConvertedSpeed(it.data!!)
                        _internetSpeed.postValue(
                            app.applicationContext.getString(
                                R.string.internet_speed_title, speed
                            )
                        )
                    }
                }
            }
        }
    }

    fun uploadNewProfilePic(image: String) {
        _isSyncing.value = NetworkLoadingState.LOADING

        val imageFile = File(image)
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart =
            MultipartBody.Part.createFormData(
                "file",
                File(image).name.toString().trim('[', ']'),
                requestFile
            )

        viewModelScope.launch {
            resourceRepository.imageUpload(imagePart).collect { result ->

                when (result) {
                    is NetworkResult.Success -> {
                        result.data.let {
                            _imageUpload.postValue(it)
                        }
                        _isSyncing.value = NetworkLoadingState.SUCCESS
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

                    else -> {}
                }
            }
        }
    }

    fun updateProfilePic(avatarId: Int) {
        _isSyncing.value = NetworkLoadingState.LOADING
        viewModelScope.launch {
            resourceRepository.updateProfilePic(avatarId).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data.student.let {
                            if (it != null) {
                                updateImageData(it)
                            }
                        }
                        _isSyncing.value = NetworkLoadingState.SUCCESS
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

                    else -> {}
                }
            }
        }
    }

    private fun updateImageData(imageData: Student) {
        viewModelScope.launch {
            downloadProfilePic(imageData)
        }

    }

    private suspend fun downloadProfilePic(student: Student) {
        student.avatar?.let {
            if (!FileUtil.isEmojiAlreadyDownloadedInsideImagesFolder(
                    getApplication(app.applicationContext),
                    student.avatar
                )
            ) {
                downloadEmojiToDevice(student.avatar).distinctUntilChanged()
                    .collect { result ->
                        if (result?.second == DownloadStatus.FINISHED) {
                            userRepository.saveUser(student)
                            _user.postValue(userRepository.getUsers())
                            val apiUrl = student.avatar
                            Timber.tag("passed param URL --> $apiUrl")
                            EventBus.getDefault()
                                .post(ProfilePayLoad(apiUrl))
                            Timber.tag("Profile Image Download -- > Success")

                        } else if (result?.second == DownloadStatus.ERROR) {
                            Timber.tag("Profile Image Download -- > Fail")

                        }
                    }
            }
        }
    }
}


