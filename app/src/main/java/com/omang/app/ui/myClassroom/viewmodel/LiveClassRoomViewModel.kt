package com.omang.app.ui.myClassroom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omang.app.data.repository.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LiveClassRoomViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
) : ViewModel() {

    private val _networkStatus = MutableLiveData<Boolean>()
    val networkStatus: LiveData<Boolean> = _networkStatus

    fun observeNetworkStatus() {

    }

    override fun onCleared() {
        super.onCleared()

    }

}


