package com.omang.app.ui.myLibrary.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.model.resources.LibraryResourceResponse
import com.omang.app.data.model.summary.MyLibraryItem
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.dataStore.DataStoreKeys
import com.omang.app.dataStore.SharedPref
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.viewmodel.DownloadUpdate
import com.omang.app.ui.myClassroom.viewmodel.QueueDownload
import com.omang.app.ui.myLibrary.fragments.MyLibraryFragmentDirections
import com.omang.app.utils.DownloadManger
import com.omang.app.utils.FileUtil
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.hasInternetConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject

data class QueueDownload(
    val id: DocId,
    var downloadId: DownloadID = -1,
    var url: Url,
)
typealias Progress = Int
typealias Url = String
typealias DocId = Int
typealias DownloadID = Int

@HiltViewModel
class MyLibraryViewModel @Inject constructor(
    val app: Application,
    val resourceRepository: ResourceRepository,
    private val dataManagerRepository: DataManagerRepository,
    private val sharedPref: SharedPref
) : AndroidViewModel(app) {

    var list = Collections.synchronizedList(mutableListOf<QueueDownload>())
    private var _downloadStatus = MutableLiveData<DownloadUpdate?>()
    val downloadStatus: LiveData<DownloadUpdate?> get() = _downloadStatus

    private val _myLibraryDocsLiveData = MutableLiveData<List<ResourcesEntity>>()
    val myLibraryDocsLiveData: LiveData<List<ResourcesEntity>> get() = _myLibraryDocsLiveData

    private val _myLibraryVideosLiveData = MutableLiveData<List<ResourcesEntity>>()
    val myLibraryVideosLiveData: LiveData<List<ResourcesEntity>> get() = _myLibraryVideosLiveData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _downloadList = Collections.synchronizedList(mutableListOf<QueueDownload>())

    private val _exploreLastUpdate = MutableLiveData<String>()
    val exploreLastUpdate: LiveData<String> get() = _exploreLastUpdate

    init {
        getAllBooks()
        getAllVideos()
    }
    private fun getAllBooks() {
        viewModelScope.launch {
            resourceRepository.fetchMyLibrary().also { myLibraryEnities ->
                myLibraryEnities.filter {
                    it.type == "Book"
                }.also {
                    try {
                        it.forEach { resourceEntity ->
                            if (FileUtil.isFileAlreadyDownloadedInsideDownloadsFolder(
                                    getApplication(),
                                    resourceEntity.file ?: ""
                                )
                            )
                                resourceEntity.downloadStatus = DownloadStatus.FINISHED
                        }
                        _myLibraryDocsLiveData.postValue(it)
                    } catch (e: Exception) {
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringResourceMessage(
                                true,
                                R.string.something_went_wrong
                            )
                        )
                        Timber.e("Error fetching db : $e")
                    }
                }
            }
        }
    }

    private fun getAllVideos() {
        try {
            viewModelScope.launch {
                resourceRepository.fetchMyLibrary().also { myLibraryEntities ->
                    myLibraryEntities.filter {
                        it.type == "Video"
                    }.also {
                        it.forEach { resourceEntity ->
                            if (FileUtil.isFileAlreadyDownloadedInsideDownloadsFolder(
                                    getApplication(),
                                    resourceEntity.file ?: ""
                                )
                            )
                                resourceEntity.downloadStatus = DownloadStatus.FINISHED
                        }
                        _myLibraryVideosLiveData.postValue(it)
                    }
                }
            }
        } catch (e: Exception) {
            _uiMessageStateLiveData.postValue(
                UIMessageState.StringResourceMessage(
                    true,
                    R.string.something_went_wrong
                )
            )
            Timber.e("Error fetching db : $e")
        }
    }

    fun navigate(navController: NavController, entity: ResourcesEntity) {
        if (entity.downloadStatus == DownloadStatus.FINISHED) {
            when (entity.type) {
                "Book" -> {
                    entity.file?.let { file ->
                        MyLibraryFragmentDirections.actionMyLibraryFragmentToDocumentViewerFragment2(
                            entity.id,
                            file,
                            0,
                            0,
                            0,
                            DBConstants.AnalyticsMenu.MY_LIBRARY.value
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                "Video" -> {
                    entity.file?.let { file ->
                        MyLibraryFragmentDirections.actionMyLibraryFragmentToVideoViewerFragment3(
                            entity.id,
                            file,
                            0,
                            0,
                            0,
                            DBConstants.AnalyticsMenu.MY_LIBRARY.value
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                else -> {
                    _uiMessageStateLiveData.postValue(
                        UIMessageState.StringMessage(
                            false,
                            "Invalid file type"
                        )
                    )
                }
            }
        } else
            _uiMessageStateLiveData.postValue(
                UIMessageState.StringMessage(
                    false,
                    "Please download the file to view the content"
                )
            )
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun downloadQueuedFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            recursiveDownload(docId = list.first().id, url = list.first().url)
        }

    }

    private suspend fun recursiveDownload(docId: DocId, url: Url) {
        downloadFiles(url).distinctUntilChanged().collect {
            it.let {
                if (it != null) {
                    Timber.tag("FIRST").v("${it.first}")
                    Timber.tag("SECOND").v("${it.second}")
                    if (it.second == DownloadStatus.ERROR) {
                        list.removeAt(0)
                        _downloadStatus.postValue(DownloadUpdate(docId, it.first, it.second))
                    } else
                        _downloadStatus.postValue(DownloadUpdate(docId, it.first, it.second))
                }
            }
            if (it!!.second == DownloadStatus.FINISHED) {
                list.removeFirst()
                if (list.isNotEmpty()) {
                    recursiveDownload(docId = list.first().id, url = list.first().url)
                }
            }
        }
    }

    fun downloadFiles(url: String): Flow<Pair<Progress, DownloadStatus>?> {
        val fileName = url.substring(
            url.lastIndexOf(
                '/',
                url.lastIndex
            ) + 1
        )
        return DownloadManger.download(
            url,
            fileName,
            FileUtil.getDownloadsPathInAppFolder(getApplication())!!
        ).map {
            when (it) {
                is DownloadManger.DownloadMangerStatus.Initialized -> {
                    list[0].downloadId = it.id
                    Pair(0, DownloadStatus.INTIALISED)
                }

                DownloadManger.DownloadMangerStatus.Cancelled -> {
                    null
                }

                is DownloadManger.DownloadMangerStatus.Downloaded -> {
                    resourceRepository.fileInsert(FileEntity(filePath = it.path))
                    Pair(100, DownloadStatus.FINISHED)
                    //DownloadUpdate(progress = 100, downloadStatus = DownloadStatus.FINISHED)
                }

                is DownloadManger.DownloadMangerStatus.OnError -> {
                    Timber.tag("ERROR").e(it.error!!.serverErrorMessage)
                    Pair(0, DownloadStatus.ERROR)
                }

                is DownloadManger.DownloadMangerStatus.OnProgress -> {
                    Pair(it.progress, DownloadStatus.PROGRESS)
                }

                DownloadManger.DownloadMangerStatus.Paused -> {
                    null
                }

                DownloadManger.DownloadMangerStatus.Started -> {
                    Pair(0, DownloadStatus.STARTED)

                    // DownloadUpdate(downloadStatus = DownloadStatus.STARTED)
                }
            }
        }
    }

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing
    fun getLibraryData() {
        viewModelScope.launch {
            if (app.hasInternetConnection()) {
                _isSyncing.postValue(NetworkLoadingState.LOADING)
                resourceRepository.getLibraryResourcesData().collect { result ->
                    when (result) {

                        is NetworkResult.Success -> {
                            result.data.let {

                                val exploreDataList = it
                                // _exploreDataResponse.postValue(listOf(exploreDataList))
                                _isSyncing.value = NetworkLoadingState.SUCCESS

                                it.data?.let { _ ->
                                    insertLibraryData(it)
                                    getAllBooks()
                                    getAllVideos()
                                } ?: kotlin.run {
                                    _uiMessageStateLiveData.value =
                                        UIMessageState.StringResourceMessage(
                                            false,
                                            R.string.no_data_sync
                                        )
                                }
//                            write code to fetch data from room while updated API response
//                            fetchExploreItems()
                                
                            }
                            sharedPref.myLibraryUpdate = false
                            //saves the last synced date
                            dataManagerRepository.saveToDataStore(
                                DataStoreKeys.MY_LIBRARY_LAST_UPDATED_DATE,
                                ViewUtil.getUtcTime()
                            )
                            fetchLastUpdateTime()
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
            } else {
                _isSyncing.postValue(NetworkLoadingState.ERROR)
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringResourceMessage(
                        false,
                        R.string.no_internet
                    )
                )
            }
        }
    }

    fun checkUpdates() = sharedPref.myLibraryUpdate

    private suspend fun insertLibraryData(it: LibraryResourceResponse) {
        it.data?.let { libraryData ->
            resourceRepository.insertMyLibrary(libraryData.added.map {
                MyLibraryItem(
                    id = it.id, name = it.name, logo = it.logo, file = it.file,
                    type = it.type, description = it.description
                )
            })
        }
    }

    fun bulkDownload(downloadList: List<ResourcesEntity>?) {
        downloadList?.forEach {

        }

    }

    fun fetchLastUpdateTime() {
        viewModelScope.launch {
            val exploreDate = getLastDate()
            _exploreLastUpdate.value = exploreDate.toString()
        }
    }

    private suspend fun getLastDate(): String? {
        return withContext(Dispatchers.IO) {
            dataManagerRepository.getFromDataStore(DataStoreKeys.MY_LIBRARY_LAST_UPDATED_DATE)
                .first()
        }
    }

}