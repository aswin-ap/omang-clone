package com.omang.app.ui.techSupport.ticketList.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omang.app.data.NetworkResult
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketDetailsViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository
) : ViewModel() {

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _postStatus = MutableLiveData<Boolean>()
    val postStatus: LiveData<Boolean> = _postStatus

    fun postRating(id: String, ratingValue: Float, text: String) {
        viewModelScope.launch {
            resourceRepository.postRating(id, ratingValue, text).collect {
                when (it) {
                    is NetworkResult.Error -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                    }

                    is NetworkResult.Failure -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                    }

                    is NetworkResult.Loading -> {
                        _isSyncing.value = NetworkLoadingState.LOADING
                    }

                    is NetworkResult.Success -> {
                        if (it.data.message == "SUCCESS") {
                            _postStatus.postValue(true)
                            _isSyncing.value = NetworkLoadingState.SUCCESS
                        }

                    }
                }

            }

        }

    }


}