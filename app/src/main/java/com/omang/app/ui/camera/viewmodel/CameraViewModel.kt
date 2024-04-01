package com.omang.app.ui.camera.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omang.app.utils.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _countdownTimer = MutableLiveData<String>()
    val countdownTimer: LiveData<String>
        get() = _countdownTimer

    /**
     * Writes the bytearray into the file.
     */
    fun saveImage(photoPath: File, inputData: ByteArray?, onSuccess: () -> Unit) {
        try {
            val outputStream = FileOutputStream(photoPath)
            outputStream.write(inputData)
            outputStream.flush()
            outputStream.close()
            onSuccess()
            Timber.d("Image saved successfully")
        } catch (e: IOException) {
            Timber.e("Error while saving the cropped image")
        }
    }

    /**
     * Renames the Image with given name
     */
    fun renameImage(renamedImage: File, photoPath: File, onSuccess: () -> Unit) {
        try {

            if (photoPath.exists()) {
                photoPath.renameTo(renamedImage)
                onSuccess()
            }

        } catch (e: Exception) {
            Timber.e("Error while rename $e")
        }
    }

    /**
     * Renames the video with given name
     */
    fun renameVideo(renamedFile: File, videoPath: File, onSuccess: () -> Unit) {
        try {

            if (videoPath.exists()) {
                videoPath.renameTo(renamedFile)
                onSuccess()
            }

        } catch (e: Exception) {
            Timber.e("Error while rename $e")
        }
    }

    /**
     * Starts the video recording time from 10 minutes until it reaches 0 and updates the
     * livedata.
     */
    suspend fun startCountDown(totalMinutes: Int = 10) = viewModelScope.launch {
        val totalSeconds = totalMinutes * 60 // 10 minutes by default
        var remainingSeconds = totalSeconds

        while (remainingSeconds >= 0) {
            remainingSeconds--
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val formattedSeconds = String.format("%02d", seconds)
            Log.d("Time", "Time remaining: $minutes min $seconds sec")
            val countDownText = "$minutes : $formattedSeconds"
            _countdownTimer.postValue(countDownText)

            delay(1000) // Wait for 1 second
        }
    }
}