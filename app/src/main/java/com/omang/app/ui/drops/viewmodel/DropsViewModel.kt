package com.omang.app.ui.drops.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omang.app.data.database.user.User
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DropsViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    private val userRepository: UserRepository,
) : ViewModel() {


    private val _displayData = MutableLiveData<User>()
    val displayData: LiveData<User> = _displayData

    fun setData() {
        viewModelScope.launch {
            _displayData.postValue(userRepository.getUsers())
        }
    }

}