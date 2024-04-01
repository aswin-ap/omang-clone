package com.omang.app.ui.gallery.viewmodel

import android.content.ContentResolver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omang.app.data.repository.DeviceRepository
import com.omang.app.di.AppDispatchers
import com.omang.app.di.Dispatcher
import com.omang.app.ui.gallery.model.MediaFile
import com.omang.app.utils.Resource
import com.omang.app.utils.extensions.sortFilesByCreationTime
import com.omang.app.utils.extensions.sortFilesByName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    val repository: DeviceRepository,
    @Dispatcher(AppDispatchers.IO) val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _mediaStatus = MutableLiveData<Resource<Any>>()
    val mediaStatus: MutableLiveData<Resource<Any>> get() = _mediaStatus

    private val _pictureList = MutableLiveData<List<MediaFile>>()
    val pictureList: MutableLiveData<List<MediaFile>> get() = _pictureList

    private val _videoList = MutableLiveData<List<MediaFile>>()
    val videoList: MutableLiveData<List<MediaFile>> get() = _videoList

    private val _docList = MutableLiveData<List<MediaFile>>()
    val docList: MutableLiveData<List<MediaFile>> get() = _docList

    private val _fileStatus = MutableLiveData<FileStatus>()
    val fileStatus: MutableLiveData<FileStatus> get() = _fileStatus

    /**
     * Fetches the medias(picture, video, documents) from the in-app directories
     * and device folders with the help of Async tasks and updates the corresponding live data
     */
    suspend fun fetchGallery(
        picturePath: String?,
        videoPath: String?,
        docPath: String?,
        contentResolver: ContentResolver
    ) {
        _mediaStatus.postValue(Resource.Loading())

//        FIXME
//         /home/cubet/Work/Repos/Omang-kotlin/app/src/main/java/com/omang/app/ui/gallery/viewmodel/GalleryViewModel.kt:58: Error: Expected non-nullable value [NullSafeMutableLiveData from androidx.lifecycle]
//            _pictureList.postValue(fetchMedia(

        try {
            ///fetches photos
            _pictureList.value = (fetchMedia(
                picturePath,
                "jpg"
            ) { repository.fetchPhotosOrVideosFromDevice(contentResolver) }.await()
                    )

            ///fetches videos
            _videoList.value = (fetchMedia(
                videoPath,
                "mp4"
            ) {
                repository.fetchPhotosOrVideosFromDevice(
                    contentResolver,
                    isVideo = true
                )
            }.await()
                    )

            val extension = listOf("pdf", "ods", "xlsx", "txt", "docx")
            //fetches documents
            _docList.value = (fetchMedia(
                docPath,
                "pdf"
            ) { repository.fetchDocumentsFromDevice(contentResolver) }.await())

            Timber.tag("Gallery").e("$docPath")
            _mediaStatus.postValue(Resource.Success(true))
        } catch (exe: Exception) {
            _mediaStatus.postValue(Resource.Error(exe.message.toString()))
        }
    }

    /**
     * Fetches the medias(picture) from the in-app directories
     * and device folders with the help of Async tasks and updates the corresponding live data
     */
    fun fetchPhoto(
        picturePath: String?,
        contentResolver: ContentResolver
    ) {
        viewModelScope.launch {
            _mediaStatus.postValue(Resource.Loading())
            try {
                //fetches photos
                val pictureList = fetchMedia(
                    picturePath,
                    "jpg"
                ) { repository.fetchPhotosFromDevice(contentResolver) }.await()
                _pictureList.postValue(pictureList)
            } catch (exe: Exception) {
                exe.printStackTrace()
                _mediaStatus.postValue(Resource.Error(exe.message.toString()))
            }
        }
    }


    /**
     * Fetches data based on the path, extension and converts the data into list.
     * @param deviceFetch suspend function to fetch the data
     */
    private fun fetchMedia(
        path: String?,
        extension: String,
        deviceFetch: suspend () -> List<MediaFile>
    ): Deferred<List<MediaFile>> {
        return viewModelScope.async(dispatcher) {
            val mediaList: MutableList<MediaFile> = mutableListOf()

            path?.let {
                ///In app repository will always same
                val inAppTask = async { repository.fetchMediaFromInAppDirectory(it, extension) }
                val deviceTask = async { deviceFetch() }

                val inAppList = inAppTask.await()
                val deviceList = deviceTask.await()

                val results = awaitAll(inAppTask, deviceTask)
                val combinedList = results.flatten()

                /* val combinedList = mutableListOf<MediaFile>().apply {
                     if (inAppList.isNotEmpty())
                         addAll(inAppList)
                     if (deviceList.isNotEmpty())
                         addAll(deviceList)
                 }*/
                mediaList.addAll(combinedList)
                Timber.d("Fetched $extension Data is: $mediaList")
            } ?: run {
                val deviceTask = async { deviceFetch() }
                deviceTask.await().let {
                    mediaList.addAll(it)
                }
            }
            return@async mediaList
        }
    }

    fun updateSelectionStatus(mediaType: MEDIATYPE, position: Int) {
        viewModelScope.launch(dispatcher) {
            try {
                when (mediaType) {
                    MEDIATYPE.PHOTO -> updateSelectedStatus(_pictureList, position)
                    MEDIATYPE.VIDEO -> updateSelectedStatus(_videoList, position)
                    MEDIATYPE.DOCUMENT -> updateSelectedStatus(_docList, position)
                }
            } catch (exe: Exception) {
                Timber.e("Exception while updating selected status: $exe")
                _fileStatus.postValue(FileStatus.Error(exe.toString()))
            }
        }
    }

    fun deleteSelectedFiles(type: MEDIATYPE, contentResolver: ContentResolver) {
        try {
            when (type) {
                MEDIATYPE.PHOTO -> {
                    val currentList = _pictureList.value!!.toMutableList()
                    val selectedList = getSelectedItemsFromList(_pictureList.value!!)
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteFiles(selectedList, contentResolver)
                    }
                    val updatedList = getUpdatedList(currentList, selectedList)
                    _pictureList.postValue(updatedList)
                    _fileStatus.postValue(FileStatus.DeleteSuccess)
                }

                MEDIATYPE.VIDEO -> {
                    val currentList = _videoList.value!!.toMutableList()
                    val selectedList = getSelectedItemsFromList(_videoList.value!!)
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteFiles(selectedList, contentResolver)
                    }
                    val updatedList = getUpdatedList(currentList, selectedList)
                    _videoList.postValue(updatedList)
                    _fileStatus.postValue(FileStatus.DeleteSuccess)
                }

                MEDIATYPE.DOCUMENT -> {
                    val currentList = _docList.value!!.toMutableList()
                    val selectedList = getSelectedItemsFromList(_docList.value!!)
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteFiles(selectedList, contentResolver)
                    }
                    val updatedList = getUpdatedList(currentList, selectedList)
                    _docList.postValue(updatedList)
                    _fileStatus.postValue(FileStatus.DeleteSuccess)
                }
            }
        } catch (exe: Exception) {
            Timber.e("Exception while deleting files: $exe")
            _fileStatus.postValue(FileStatus.Error(exe.toString()))
        }
    }

    fun deleteSelectedFile(
        type: MEDIATYPE,
        contentResolver: ContentResolver,
        mediaFile: MediaFile
    ) {
        try {
            when (type) {
                MEDIATYPE.PHOTO -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteFile(mediaFile, contentResolver)
                    }
                }

                MEDIATYPE.VIDEO -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteFile(mediaFile, contentResolver)
                    }
                }

                MEDIATYPE.DOCUMENT -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.deleteFile(mediaFile, contentResolver)
                    }
                }
            }
        } catch (exe: Exception) {
            Timber.e("Exception while deleting files: $exe")
            _fileStatus.postValue(FileStatus.Error(exe.toString()))
        }
    }

    fun sortListByDate(mediaType: MEDIATYPE, isAscending: Boolean) {
        when (mediaType) {
            MEDIATYPE.PHOTO -> {
                val picList = _pictureList.value!!.sortFilesByCreationTime(isAscending)
                _pictureList.postValue(picList)
            }

            MEDIATYPE.VIDEO -> {
                val videoList = _videoList.value!!.sortFilesByCreationTime(isAscending)
                _videoList.postValue(videoList)
            }

            MEDIATYPE.DOCUMENT -> {
                val docList = _docList.value!!.sortFilesByCreationTime(isAscending)
                _docList.postValue(docList)
            }
        }
    }

    fun sortListByName(mediaType: MEDIATYPE, isAscending: Boolean) {
        when (mediaType) {
            MEDIATYPE.PHOTO -> {
                val picList = _pictureList.value!!.sortFilesByName(isAscending)
                _pictureList.postValue(picList)
            }

            MEDIATYPE.VIDEO -> {
                val videoList = _videoList.value!!.sortFilesByName(isAscending)
                _videoList.postValue(videoList)
            }

            MEDIATYPE.DOCUMENT -> {
                val docList = _docList.value!!.sortFilesByName(isAscending)
                _docList.postValue(docList)
            }
        }
    }

    ///Removes the selected items from the list and resets the selected value to false
    private fun getUpdatedList(
        currentList: MutableList<MediaFile>,
        selectedList: List<MediaFile>
    ): List<MediaFile> {
        return currentList.map { mediaFile ->
            mediaFile.apply {
                isSelected = false
            }
        }.toMutableList().apply {
            removeAll(selectedList)
        }
    }

    private fun updateSelectedStatus(
        liveData: MutableLiveData<List<MediaFile>>,
        position: Int
    ) {
        viewModelScope.launch(dispatcher) {
            try {
                liveData.value?.let { list ->
                    //bug
                    //list[position].apply { isSelected = !isSelected }
                    //enableOrDisableDelete(list)
                }
            } catch (e: Exception) {
                Timber.e("Exception while updating selection $e")
                _fileStatus.postValue(FileStatus.Error("Oops, Something went wrong"))
            }
        }
    }

    private fun enableOrDisableDelete(mediaList: List<MediaFile>) {
        mediaList.any { file -> file.isSelected }.also {
            _fileStatus.postValue(FileStatus.DeleteOption(it))
        }
    }

    private fun getSelectedItemsFromList(mediaList: List<MediaFile>): List<MediaFile> =
        mediaList.filter {
            it.isSelected
        }
}

sealed class FileStatus(
    val hasDeleteOption: Boolean = false,
    val message: String? = null
) {
    class DeleteOption(hasDelete: Boolean) : FileStatus(hasDeleteOption = hasDelete)
    object DeleteSuccess : FileStatus()
    data class Error(val error: String?) : FileStatus(message = error)
}

enum class MEDIATYPE {
    PHOTO, VIDEO, DOCUMENT
}