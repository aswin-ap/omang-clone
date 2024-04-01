package com.omang.app.ui.chat.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.user.User
import com.omang.app.data.model.chat.message.Message
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    application: Application,
/*    private val userRepository: UserRepository,
    private val resourceRepository: ResourceRepository,
    private var networkConnectionManager: NetworkConnectionManager,
    private var socketRepository: SocketRepository*/
) : BaseViewModel(application) {

/*
    private val _socketStatus = MutableLiveData<SocketStatus>()
    val socketStatus: LiveData<SocketStatus> = _socketStatus*/

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _messageResponse = MutableLiveData<List<Message>>()
    val messageResponse: LiveData<List<Message>> get() = _messageResponse

/*    fun socketStatus() {
        socketRepository.statusListener { status ->
            _socketStatus.value = status
        }
    }*/



    fun getUserData() {
        viewModelScope.launch {
            _user.value = userRepository.getUsers()
        }
    }

    fun getChatHistory(roomId: Int) {
        viewModelScope.launch {
            resourceRepository.getChatHistory(roomId, 1, 100).collect() { result ->

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
                            _isSyncing.value = NetworkLoadingState.SUCCESS
                            _messageResponse.value = it.data.messages
                            socketStatus()
                        }
                    }
                }
            }
        }
    }
}