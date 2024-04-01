package com.omang.app.ui.myClassroom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.ui.myClassroom.fragments.subjectContent.SubjectContentsFragmentDirections
import com.omang.app.utils.DownloadManger
import com.omang.app.utils.FileUtil
import com.omang.app.utils.UIMessageState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject

data class DownloadUpdate(
    val id: DocId, var progress: Progress = 0,
    var downloadStatus: DownloadStatus,
    var downloadedFiles: Int = 0,
    var childDownloadId: DownloadID = -1
)

data class QueueDownload(
    val id: DocId,
    var downloadId: DownloadID = -1,
    var url: Url,
    var childDownloadId: DownloadID = -1,
    var downloadedFiles: Int = 0
)
typealias Progress = Int
typealias Url = String
typealias DocId = Int
typealias DownloadID = Int

@HiltViewModel
class DocAndVideoViewModel @Inject constructor(
    application: Application,
    private val resourceRepository: ResourceRepository,
) : AndroidViewModel(application) {

    var list = Collections.synchronizedList(mutableListOf<QueueDownload>())
    private var _downloadStatus = MutableLiveData<DownloadUpdate?>()
    val downloadStatus: LiveData<DownloadUpdate?> get() = _downloadStatus

    private val _resourceLiveData = MutableLiveData<List<ResourcesEntity>>()
    val resourceLiveData: LiveData<List<ResourcesEntity>> get() = _resourceLiveData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    fun downloadQueuedFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            recursiveDownload(docId = list.first().id, url = list.first().url)
        }

    }

    fun clearLiveData() {
        _downloadStatus.value = null
    }

    private suspend fun recursiveDownload(docId: DocId, url: Url) {
        downloadFiles(url).distinctUntilChanged().collect {
            it.let {
                if (it != null) {
                    if (it.second == DownloadStatus.ERROR) {
                        list.removeAt(0)
                        _downloadStatus.postValue(DownloadUpdate(docId, it.first, it.second))
                    }else if (it.second == DownloadStatus.FINISHED) {
                        _downloadStatus.postValue(DownloadUpdate(docId, 100, DownloadStatus.FINISHED))
                        list.removeFirst()
                        if (list.isNotEmpty()) {
                            recursiveDownload(docId = list.first().id, url = list.first().url)
                        }
                    } else if(it.first == 100 && it.second == DownloadStatus.PROGRESS){
                        _downloadStatus.postValue(DownloadUpdate(docId, 100, DownloadStatus.FINISHED))
                    }
                    else
                        _downloadStatus.postValue(DownloadUpdate(docId, it.first, it.second))
                }
            }
         /*   if (it?.second == DownloadStatus.FINISHED) {
                Timber.tag("DOWNLOADING----").d("${it.first}{$it.second}")
                _downloadStatus.postValue(DownloadUpdate(docId, 100, DownloadStatus.FINISHED))

                list.removeFirst()
                if (list.isNotEmpty()) {
                    recursiveDownload(docId = list.first().id, url = list.first().url)
                }
            }*/
        }
    }

    private fun downloadFiles(url: String): Flow<Pair<Progress, DownloadStatus>?> {
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
                    Timber.tag("DOWNLOAD STATUS").d("FINISHED")
                    Pair(100, DownloadStatus.FINISHED)
                    //DownloadUpdate(progress = 100, downloadStatus = DownloadStatus.FINISHED)
                }

                is DownloadManger.DownloadMangerStatus.OnError -> {
                    Timber.tag("ERROR").e(it.error!!.serverErrorMessage)
                    Pair(0, DownloadStatus.ERROR)
                }

                is DownloadManger.DownloadMangerStatus.OnProgress -> {
                    Timber.tag("DOWNLOAD STATUS").d("PROGRESS")
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

    fun getDocumentsByClassroomId(classroomId: Int) {
        viewModelScope.launch {
            resourceRepository.getResourcesByClassroomId(classroomId).also { resourceEntity ->
                resourceEntity.filter {
                    it.type == "Book"
                }.also {
                    it.forEach { resourceEntity ->
                        if (FileUtil.isFileAlreadyDownloadedInsideDownloadsFolder(
                                getApplication(),
                                resourceEntity.file!!
                            )
                        )
                            resourceEntity.downloadStatus = DownloadStatus.FINISHED
                    }
                    _resourceLiveData.postValue(it)
                }
            }
        }
    }

    fun getVideosByClassroomId(classroomId: Int) {
        viewModelScope.launch {
            resourceRepository.getResourcesByClassroomId(classroomId).also { resourceEntity ->
                resourceEntity.filter {
                    it.type == "Video"
                }.also {
                    it.forEach { resourceEntity ->
                        if (FileUtil.isFileAlreadyDownloadedInsideDownloadsFolder(
                                getApplication(),
                                resourceEntity.file!!
                            )
                        )
                            resourceEntity.downloadStatus = DownloadStatus.FINISHED
                    }
                    _resourceLiveData.postValue(it)
                }
            }
        }
    }

    fun navigate(navController: NavController, entity: ResourcesEntity, classRoomId: Int) {
        Timber.e("classroom id : $classRoomId")
        if (entity.downloadStatus == DownloadStatus.FINISHED) {
            when (entity.type) {
                "Book" -> {
                    entity.file?.let { file ->
                        SubjectContentsFragmentDirections.actionSubjectContentsFragmentToPdfViewerFragment(
                            entity.id,
                            file,
                            classRoomId,
                            0,
                            0,
                            DBConstants.AnalyticsMenu.CLASSROOM.value
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                "Video" -> {
                    entity.file?.let { file ->
                        SubjectContentsFragmentDirections.actionSubjectContentsFragmentToVideoViewerFragment(
                            entity.id,
                            file,
                            classRoomId,
                            0,
                            0,
                            DBConstants.AnalyticsMenu.CLASSROOM.value
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

}