package com.omang.app.ui.survey.survey

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SurveyViewModel @Inject constructor() : ViewModel() {

    private val _usersList = MutableLiveData<Int>(0)
    val userList: LiveData<Int>
        get() = _usersList


    fun setValue(int: Int) {
        _usersList.value = int
    }

}