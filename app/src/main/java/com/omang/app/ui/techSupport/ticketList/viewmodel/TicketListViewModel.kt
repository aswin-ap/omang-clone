package com.omang.app.ui.techSupport.ticketList.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.omang.app.BuildConfig
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity
import com.omang.app.data.database.user.User
import com.omang.app.data.model.techSupport.NavigationModel
import com.omang.app.data.model.techSupport.TechSupportsItem
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.data.repository.UserRepository
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.InternetSpeedChecker
import com.omang.app.utils.connectivity.NetworkConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TicketListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val resourceRepository: ResourceRepository,
    private var networkConnection: NetworkConnection,
) : ViewModel() {

    @Inject
    lateinit var internetSpeedChecker: InternetSpeedChecker

    private val _ticketsLiveData = MutableLiveData<List<TicketsEntity>>()
    val ticketsLiveData: LiveData<List<TicketsEntity>> get() = _ticketsLiveData

    private val _issueStatus = MutableLiveData<String>()
    val issueStatus: LiveData<String> get() = _issueStatus

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _imageUpload = MutableLiveData<TicketImage>()
    val imageUpload: LiveData<TicketImage> = _imageUpload

    private fun getInternetStatus() {
        viewModelScope.launch {
            networkConnection.observeNetworkConnection().collectLatest {
                Timber.d("NetworkStatus : $it")
                if (it) {
                    getTicketsFromAPI()
                }
            }
        }
    }

    private fun getTicketsFromAPI() {
//        _isSyncing.value = NetworkLoadingState.LOADING

        viewModelScope.launch {
            resourceRepository.getTickets(1, 200, false, "", "").collect { result ->
                when (result) {
                    is NetworkResult.Error -> {
//                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e("${result.code} ${result.message}")
                    }

                    is NetworkResult.Failure -> {
//                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e(result.exception)
                    }

                    is NetworkResult.Loading -> {
//                        _isSyncing.value = NetworkLoadingState.LOADING

                    }

                    is NetworkResult.Success -> {
                        result.data.let {
//                            _isSyncing.value = NetworkLoadingState.SUCCESS
                            insertTickets(result.data.data.techSupports)

                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun saveIssueLocally(
        issue: String,
        email: String,
        phone: String,
        image: String?
    ): String {
        val uid = UUID.randomUUID().toString()
        val navigation = resourceRepository.getLastTenNavigation()
        val gson = Gson()
        resourceRepository.insertTicket(
            TicketsEntity(
                issue = issue,
                roomId = 0,
                phone = phone,
                email = email,
                createdAt = ViewUtil.getServerFormattedTime(System.currentTimeMillis()),
                downloadSpeed = "",
                isClosed = false,
                closedAt = "",
                reopenedAt = "",
                id = uid,
                isOffline = true,
                message = "",
                ticketImage = "",
                rating = null,
                feedback = "",
                navigation = gson.toJson(navigation.map {
                    NavigationModel(
                        appVersion = BuildConfig.VERSION_NAME,
                        comment = it.comment,
                        page = it.page,
                        whenTime = ViewUtil.getServerFormattedTime(it.createdTime.time)
                    )
                })
            )
        )

        return uid
    }

    private fun postIssue(localId: String, imageId: Int?) {
        viewModelScope.launch {
            _isSyncing.value = NetworkLoadingState.LOADING
            val tickets = resourceRepository.getCurrentLocalTicket(localId)
            val navigation = resourceRepository.getLastTenNavigation()
            resourceRepository.postIssue(
                issue = tickets.issue,
                email = tickets.email,
                phone = tickets.phone,
                ticketPostedAt = tickets.createdAt,
                appTicketId = tickets.id,
                imageId = imageId,
                navigation = navigation.map {
                    NavigationModel(
                        appVersion = BuildConfig.VERSION_NAME,
                        it.comment,
                        page = it.page,
                        ViewUtil.getServerFormattedTime(it.createdTime.time)
                    )
                }
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
                        result.data.let {
                            val ticket = it.data[0]
                            resourceRepository.deleteTicketById(ticket.appTicketId)
                            /*resourceRepository.insertTicket(
                                TicketsEntity(
                                    id = ticket.id.toString(),
                                    issue = ticket.issue,
                                    downloadSpeed = ticket.downloadSpeed,
                                    isClosed = ticket.isClosed,
                                    phone = ticket.phone,
                                    createdAt = ticket.createdAt,
                                    message = ticket.message,
                                    closedAt = ticket.closedAt,
                                    email = tickets.email,
                                    isOffline = false,
                                    )
                            )*/
                            _isSyncing.value = NetworkLoadingState.SUCCESS
                            _issueStatus.value = ""

                        }
                    }

                    else -> {}
                }
            }
        }
    }

    data class TicketImage(val id: Int, val fileUrl: String)

//    2023-11-08T11:15:17Z
//    2023-11-08T12:51:06Z

//    "2023-11-08T13:01:36Z"  tech-support	310	json	201	2433462	18923162208

    fun saveIssue(
        directory: File,
        image: String?,
        email: String,
        issue: String,
        phone: String,
        hasInternetConnection: Boolean
    ) {
        viewModelScope.launch {
            var url: String? = null
            if (image != null) {
                url = saveImageToAppStorage(directory, image)
            }

            val uid = saveIssueLocally(email = email, phone = phone, issue = issue, image = url)
            if (hasInternetConnection) {
                if (url != null) {
                    uploadImageToServer(localId = uid, url)
                } else {
                    postIssue(uid, null)
                }

            } else {
                _isSyncing.value = NetworkLoadingState.SUCCESS
                _issueStatus.value = ""
            }
        }
    }

    private fun uploadImageToServer(localId: String, image: String) {
        _isSyncing.value = NetworkLoadingState.LOADING

        /*
                val requestFile = image.toRequestBody("image/jpeg".toMediaTypeOrNull())

                Timber.e("requestFile : requestFile: $requestFile")
                val imagePart =
                    MultipartBody.Part.createFormData(
                        "file",
                        File(image).name.toString(),
                        `requestFile`
                    )
        */

        /*        val imageFile = File(image)
                val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                Timber.e("requestFile : requestFile: $requestFile")
                val imagePart =
                    MultipartBody.Part.createFormData(
                        "file",
                        File(image).name.toString(),
                        requestFile
                    )*/

        val imageFile = File(image)
        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        Timber.e("requestFile : requestFile: $requestFile")
        val imagePart =
            MultipartBody.Part.createFormData(
                "file",
                File(image).name.toString(),
                requestFile
            )

        viewModelScope.launch {
            resourceRepository.imageUpload(imagePart).collect { result ->
                when (result) {

                    is NetworkResult.Success -> {
                        postIssue(localId, result.data.data.id)
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

    private suspend fun saveImageToAppStorage(directory: File, url: String): String {
        val id = System.currentTimeMillis()
        val folder = File(directory, "/ticket/")
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                Log.e("ERROR", "Cannot create a directory!")
            } else {
                folder.mkdirs()
            }
        }
        val thumbnail = BitmapFactory.decodeFile(url)
        val file = File(folder, "ticket_$id.jpg")
        val fOut = FileOutputStream(file)
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
        fOut.flush()
        fOut.close()
        return file.absolutePath
    }

    fun getTicketsFromDB() {
        viewModelScope.launch {
            _ticketsLiveData.postValue(resourceRepository.getTickets())
            //getInternetStatus()
            if (networkConnection.hasInternet()) {
                getTicketsFromAPI()
            }
        }
    }

    private fun insertTickets(tickets: List<TechSupportsItem>) {
        viewModelScope.launch {
            resourceRepository.insertTickets(tickets)
            _ticketsLiveData.postValue(resourceRepository.getTickets())

        }
    }

    fun getUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            _user.postValue(userRepository.getUsers())
        }
    }

    override fun onCleared() {
        super.onCleared()
//        networkConnectionManager.stopListenNetworkState()

    }
}