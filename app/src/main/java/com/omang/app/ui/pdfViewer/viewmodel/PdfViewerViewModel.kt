package com.omang.app.ui.pdfViewer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.omang.app.BuildConfig
import com.omang.app.data.repository.MediaRepository
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.utils.FileUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@HiltViewModel
class PdfViewerViewModel @Inject constructor(
    application: Application,
    private val mediaRepository: MediaRepository,
) : BaseViewModel(application) {
    private val _uiState = MutableLiveData<PdfStatus>()
    val uiState: LiveData<PdfStatus> get() = _uiState
    private var _file: File? = null

    var isFullScreen: Boolean = false

    fun fetchFile(fetchFileUrl: String) {
        viewModelScope.launch {
            try {
                val fileName = FileUtil.getFileNameFromUrl(fetchFileUrl)
                mediaRepository.fetchFile(getApplication(), fileName)?.let {
                    withContext(Dispatchers.IO) {
                        decryptFile(it)
                    }
                } ?: kotlin.runCatching {
                    _uiState.postValue(PdfStatus.OnError(FileNotFoundException("File not found")))
                }
            } catch (e: FileNotFoundException) {
                Timber.tag("DECRYPTION").e("${e.printStackTrace()}")
                _uiState.postValue(PdfStatus.OnError(e))
            }
        }
    }

    private suspend fun decryptFile(encryptedFile: File) {
        try {
            _uiState.postValue(PdfStatus.Encrypt(0))
            // Read encrypted data from file
            val encryptedData = encryptedFile.readBytes()

            // Extract the salt, IV, and encrypted bytes from the encrypted data
            val salt = encryptedData.sliceArray(0..15)
            val iv = encryptedData.sliceArray(16..31)
            val encryptedBytes = encryptedData.sliceArray(32 until encryptedData.size)

            val keySpec = PBEKeySpec(BuildConfig.DECYPHER_KEY.toCharArray(), salt, 100000, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val secretKey = SecretKeySpec(secretKeyFactory.generateSecret(keySpec).encoded, "AES")

            // Create the decipher instance
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ivParams = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams)

            // Decrypt the data
            val decryptedBytes = cipher.doFinal(encryptedBytes)

            // Save decrypted data to a new file
            val outPutFile = File(
                FileUtil.getTempFilePath(getApplication()),
                "temp_${encryptedFile.name}"
            )
            withContext(Dispatchers.IO) {
                Files.write(Paths.get(outPutFile.toURI()), decryptedBytes)
            }
            _file = outPutFile
            _uiState.postValue(PdfStatus.Initialized(outPutFile))

        } catch (e: Exception) {
            Timber.tag("DECRYPTION").e("${e.printStackTrace()}")
            _uiState.postValue(PdfStatus.OnError(e))
        }
    }

    fun loadPdf(pdfViewer: PDFView, file: File) {
        pdfViewer.fromFile(file)
            .defaultPage(0)
            .scrollHandle(DefaultScrollHandle(getApplication()))
            .swipeHorizontal(true)
            .enableDoubletap(true)
            .enableAntialiasing(true)
            .spacing(20)
            .onError {
                _uiState.postValue(PdfStatus.OnError(Exception(it.message)))
            }
            .onLoad {
                _uiState.postValue(PdfStatus.Loaded(it))
            }
            .load()

    }

    override fun onCleared() {
        super.onCleared()
        _file?.delete()
    }

}

sealed class PdfStatus {
    data class Initialized(val file: File) : PdfStatus()
    data class Encrypt(val progress: Int) : PdfStatus()
    data class OnError(val error: Exception) : PdfStatus()
    data class Loaded(val value: Int) : PdfStatus()
}