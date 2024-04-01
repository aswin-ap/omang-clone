package com.omang.app.ui.myClassroom.viewmodel

import android.app.Application
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.fragments.subjectContent.SubjectContentsFragmentDirections
import com.omang.app.ui.myClassroom.fragments.subjectContent.lessons.LessonsFragment
import com.omang.app.ui.test.MCQType
import com.omang.app.ui.test.TestStartFragment.Companion.CLASSROOM_ID
import com.omang.app.ui.test.TestStartFragment.Companion.UNIT_ID
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.DownloadManger
import com.omang.app.utils.FileUtil
import com.omang.app.utils.Resource
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.Collections
import javax.inject.Inject

data class ChildDownloadUpdate(
    val itemId: Int,
    var progress: Progress = 0,
    var downloadStatus: DownloadStatus,
    var downloadCount: Int = 0
)

@HiltViewModel
class LessonsViewModel @Inject constructor(
    val app: Application,
    private val networkConnection: NetworkConnection
) : BaseViewModel(app) {

    private var _classRoomId: Int? = null

    private var _groupDownloadStatus = MutableLiveData<DownloadUpdate>()
    val groupDownloadStatus: LiveData<DownloadUpdate?> get() = _groupDownloadStatus

    private var _childDownloadStatus = MutableLiveData<ChildDownloadUpdate?>()
    val childDownloadStatus: LiveData<ChildDownloadUpdate?> get() = _childDownloadStatus

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _lessonsLiveData = MutableLiveData<List<UnitWithLessons>>()
    val lessonsLiveData: LiveData<List<UnitWithLessons>> get() = _lessonsLiveData

    private val _webLinks = MutableLiveData<List<MyWebPlatformEntity>>()
    val webLinks: LiveData<List<MyWebPlatformEntity>> get() = _webLinks

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _downloadList = Collections.synchronizedList(mutableListOf<QueueDownload>())
    private var isDownloading = false
    private var isChildDownloading = false

    var lessonId: Int? = null

    private val _internetStatusLiveData = MutableLiveData<Boolean>()
    val internetStatusLiveData: LiveData<Boolean> = _internetStatusLiveData

    fun observeNetworkStatus() {
        viewModelScope.launch {
            networkConnection.observeNetworkConnection().collect {
                Timber.d("Lesson network: $it")
                _internetStatusLiveData.value = it
            }
        }
    }

    fun fetchLessonsAndUnits() {
        viewModelScope.launch {
            resourceRepository.getUnitsWithLessonsByClassroomId(_classRoomId!!)
                .also { unitWithLessons ->
                    Timber.tag("ERROR_06").e("fetching unit with lesson for $unitWithLessons")
                    unitWithLessons.forEach { unit ->
                        var downloadedCount = 0
                        unit.lessons.forEach { lesson ->
                            if (isFileAlreadyDownloaded(lesson.file)) {
                                downloadedCount += 1
                                lesson.downloadStatus = DownloadStatus.FINISHED
                            }
                        }
                        unit.unit.totalFiles = unit.lessons.size
                        unit.unit.downloadedFiles = downloadedCount
                        checkFileDownloaded(unit)
                    }
                    Timber.tag("ERROR_06")
                        .d("Units with Lessons: ${unitWithLessons.toJson()}")
                    _lessonsLiveData.postValue(unitWithLessons)
                }
        }
    }

    private fun checkFileDownloaded(unit: UnitWithLessons) {
        if (unit.unit.downloadedFiles == unit.unit.totalFiles) unit.unit.downloadStatus =
            DownloadStatus.FINISHED
    }

    fun setClassRoomId(id: Int) {
        viewModelScope.launch {
            _classRoomId = id
            Timber.tag("LessonsViewModel").d("classRoomId: $_classRoomId")
        }
    }

    suspend fun bulkDownload(urlList: List<LessonsEntity>, id: DocId, downloadedCount: Int) {
        urlList.forEach {
            //Adds the unit id as id for each QueueDownload items. So it will update the
            //Unit recyclerview
            _downloadList.add(
                QueueDownload(
                    id = id, downloadId = -1, url = it.file, childDownloadId = it.id
                )
            )
        }
        Timber.tag("Download").d("Download list: $_downloadList")
        if (isChildDownloading) {
            _uiMessageStateLiveData.postValue(
                UIMessageState.StringMessage(
                    false, "Download already in progress"
                )
            )
        } else if (isDownloading) {
            _groupDownloadStatus.postValue(
                DownloadUpdate(
                    id,
                    0,
                    DownloadStatus.INTIALISED,
                )
            )
        } else {
            viewModelScope.launch(Dispatchers.IO) {
                recursiveDownload(
                    docId = _downloadList.first().id,
                    url = _downloadList.first().url,
                    downloadedCount = downloadedCount
                )
            }
            isDownloading = true
        }
    }
//
    private suspend fun recursiveDownload(docId: DocId, url: Url, downloadedCount: Int) {
        /* if file already downloaded skip delete the first
        item from list and keeps the downloaded count as same*/
        if (isFileAlreadyDownloaded(url)) {
            _downloadList.removeFirst()
            if (_downloadList.isNotEmpty()) {
                recursiveDownload(
                    docId = _downloadList.first().id,
                    url = _downloadList.first().url,
                    downloadedCount = downloadedCount
                )
            }
        } else {
            downloadFile(url).distinctUntilChanged().collect {
                Timber.tag("Download").d("Download status: ${it?.second}")
                if (it!!.second == DownloadStatus.FINISHED) {
                    //Updates the corresponding lesson as downloaded
                    /*_childDownloadStatus.postValue(
                        ChildDownloadUpdate(
                            _downloadList.first().childDownloadId,
                            0,
                            DownloadStatus.FINISHED
                        )
                    )*/
                    _downloadList.removeFirst()
                    if (_downloadList.isNotEmpty()) {
                        if (_downloadList.count { value -> value.id == docId } >= 1) {
                            //if list is not empty, updates the downloaded count
                            _groupDownloadStatus.postValue(
                                DownloadUpdate(
                                    docId, it.first, DownloadStatus.PROGRESS, downloadedCount + 1
                                )
                            )

                            //recall the function
                            recursiveDownload(
                                docId = _downloadList.first().id,
                                url = _downloadList.first().url,
                                downloadedCount = downloadedCount + 1
                            )
                        } else {
                            //if list is not empty, updates the downloaded count
                            _groupDownloadStatus.postValue(
                                DownloadUpdate(
                                    docId,
                                    it.first,
                                    DownloadStatus.FINISHED,
                                    downloadedCount + 1,
                                    childDownloadId = _downloadList.first().childDownloadId
                                )
                            )
                            //recall the function
                            recursiveDownload(
                                docId = _downloadList.first().id,
                                url = _downloadList.first().url,
                                downloadedCount = _downloadList.first().downloadedFiles
                            )
                        }
                    } else {
                        //mark all files as downloaded
                        _groupDownloadStatus.postValue(
                            DownloadUpdate(
                                docId, it.first, DownloadStatus.FINISHED, downloadedCount + 1
                            )
                        )
                        //adds to mobile navigation
                        withContext(Dispatchers.IO) {
                            addToNavigation(
                                event = DBConstants.Event.DOWNLOAD,
                                page = LessonsFragment::class.java.name,
                                comment = "Unit download completed",
                                subjectId = _classRoomId,
                                contentId = docId
                            )
                        }
                        isDownloading = false
                        return@collect
                    }
                }
                it.let {
                    if (it.second == DownloadStatus.ERROR) {
                        _downloadList.removeFirst()
                        _groupDownloadStatus.postValue(DownloadUpdate(docId, it.first, it.second))
                        //adds to mobile navigation

                        withContext(Dispatchers.IO) {
                            addToNavigation(
                                event = DBConstants.Event.DOWNLOAD,
                                page = LessonsFragment::class.java.name,
                                comment = "Unit download failed",
                                subjectId = _classRoomId,
                                contentId = docId
                            )
                        }
                    } else {
                        _groupDownloadStatus.postValue(
                            DownloadUpdate(
                                docId, it.first, it.second, downloadedCount
                            )
                        )
                    }
                }
            }
        }
    }


    suspend fun individualDownload(url: String, fileId: Int, downloadedFiles: Int) {
        downloadFile(url).distinctUntilChanged().collectLatest {
            isChildDownloading = true
            Timber.tag("Download").d("Download status: ${it?.second}")
            var downloads = downloadedFiles
            it?.let {
                if (it.second == DownloadStatus.FINISHED) {
                    //adds lesson downloaded to mobile navigation
                    addToNavigation(
                        event = DBConstants.Event.DOWNLOAD,
                        page = LessonsFragment::class.java.name,
                        comment = "Lesson download completed",
                        subjectId = _classRoomId,
                        contentId = fileId
                    )
                    downloads += 1
                    isChildDownloading = false
                } else if (it.second == DownloadStatus.ERROR) {
                    //adds lesson download failed to mobile navigation
                    addToNavigation(
                        event = DBConstants.Event.DOWNLOAD,
                        page = LessonsFragment::class.java.name,
                        comment = "Lesson download failed",
                        subjectId = _classRoomId,
                        contentId = fileId
                    )
                    isChildDownloading = false
                }
                _childDownloadStatus.postValue(
                    ChildDownloadUpdate(
                        fileId, it.first, it.second, downloads
                    )
                )
            }
        }
    }

    private fun downloadFile(url: String): Flow<Pair<Progress, DownloadStatus>?> {
        val fileName = url.substring(
            url.lastIndexOf(
                '/', url.lastIndex
            ) + 1
        )
        return DownloadManger.download(
            url, fileName, FileUtil.getDownloadsPathInAppFolder(getApplication())!!
        ).map {
            when (it) {
                is DownloadManger.DownloadMangerStatus.Initialized -> {
                    Pair(0, DownloadStatus.INTIALISED)
                }

                DownloadManger.DownloadMangerStatus.Cancelled -> {
                    null
                }

                is DownloadManger.DownloadMangerStatus.Downloaded -> {
                    resourceRepository.fileInsert(FileEntity(filePath = it.path))
                    Pair(100, DownloadStatus.FINISHED)
                }

                is DownloadManger.DownloadMangerStatus.OnError -> {
                    _uiMessageStateLiveData.postValue(
                        UIMessageState.StringMessage(
                            false, "There is an error downloading $fileName, Please try again."
                        )
                    )
                    Pair(0, DownloadStatus.ERROR)
                }

                is DownloadManger.DownloadMangerStatus.OnProgress -> {
                    Pair(it.progress, DownloadStatus.PROGRESS)
                }

                DownloadManger.DownloadMangerStatus.Paused -> {
                    null
                }

                DownloadManger.DownloadMangerStatus.Started -> {
                    Timber.tag("Download").d("Currently downloading: $fileName")
                    Pair(0, DownloadStatus.STARTED)
                    // DownloadUpdate(downloadStatus = DownloadStatus.STARTED)
                }
            }
        }
    }

    fun getWebsiteData(websiteId: Int, navController: NavController) {
        _isSyncing.value = NetworkLoadingState.LOADING

        viewModelScope.launch {
            resourceRepository.getIndividualWebData(websiteId, false).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data.let { response ->
                            val webItem = response.data
                            _isSyncing.value = NetworkLoadingState.SUCCESS
                            internetSpeedChecker.checkInternetSpeed(app).also {
                                when (it) {
                                    is Resource.Error -> {
                                        _uiMessageStateLiveData.postValue(
                                            UIMessageState.StringMessage(
                                                false, it.message.toString()
                                            )
                                        )
                                    }

                                    is Resource.Loading -> {}
                                    is Resource.Success -> {
                                        it.data?.let { speed ->
                                            if (speed != webItem.threshold) {
                                                _classRoomId?.let { classroomId ->
                                                    lessonId?.let { lessonId ->
                                                        SubjectContentsFragmentDirections.actionSubjectContentsFragmentToWebViewerFragment(
                                                            webItem,
                                                            lessonId,
                                                            classroomId,
                                                            DBConstants.AnalyticsMenu.LESSON.value
                                                        ).also {
                                                            navController.navigate(it)
                                                        }
                                                    }
                                                }
                                            } else {
                                                _uiMessageStateLiveData.postValue(
                                                    UIMessageState.StringMessage(
                                                        false,
                                                        "This website requires threshold of " +
                                                                "${webItem.threshold} "
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e("${result.code} ${result.message}")
                        _uiMessageStateLiveData.postValue(
                            UIMessageState.StringMessage(
                                false,
                                result.message
                            )
                        )

                    }

                    is NetworkResult.Failure -> {
                        _isSyncing.value = NetworkLoadingState.ERROR
                        Timber.e(result.exception)
                    }

                    is NetworkResult.Loading -> {
                        _isSyncing.value = NetworkLoadingState.LOADING

                    }
                }
            }
        }
    }

    fun navigate(navController: NavController, entity: LessonsEntity, unitId: Int) {
        if (entity.downloadStatus == DownloadStatus.FINISHED) {
            when (entity.mimeType) {
                "pdf" -> {
                    _classRoomId?.let { classroomId ->
                        SubjectContentsFragmentDirections.actionSubjectContentsFragmentToPdfViewerFragment(
                            entity.id,
                            entity.file,
                            classroomId,
                            lessonId = entity.id,
                            unitId = unitId,
                            DBConstants.AnalyticsMenu.CLASSROOM.value
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                "jpeg", "png" -> {
                    _classRoomId?.let { classroomId ->
                        SubjectContentsFragmentDirections.actionSubjectContentsFragmentToImageViewerFragment(
                            entity.id,
                            entity.file,
                            classroomId,
                            lessonId = entity.id,
                            unitId = unitId,
                            DBConstants.AnalyticsMenu.CLASSROOM.value
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                "mp4" -> {
                    _classRoomId?.let { classroomId ->
                        SubjectContentsFragmentDirections.actionSubjectContentsFragmentToVideoViewerFragment(
                            entity.id,
                            entity.file,
                            classroomId,
                            lessonId = entity.id,
                            unitId = unitId,
                            DBConstants.AnalyticsMenu.CLASSROOM.value
                        ).also {
                            navController.navigate(it)
                        }
                    }
                }

                else -> {
                    _uiMessageStateLiveData.postValue(
                        UIMessageState.StringMessage(
                            false, "Invalid file type"
                        )
                    )
                }
            }
        } else _uiMessageStateLiveData.postValue(
            UIMessageState.StringMessage(
                false, "Please download the file to view the content"
            )
        )
    }

    private fun isFileAlreadyDownloaded(fileUrl: String): Boolean {
        val path = FileUtil.getDownloadsPathInAppFolder(getApplication())
        val fileName = fileUrl.substring(
            fileUrl.lastIndexOf(
                '/', fileUrl.lastIndex
            ) + 1
        )
        File("$path/$fileName").also {
            return (it.exists())
        }
    }

    fun getLessonWeblinks(lessonId: Int) {
        this.lessonId = lessonId
        viewModelScope.launch {
            val lessonWeblinksMappingEntity =
                _classRoomId?.let { resourceRepository.fetchLessonIds(it, lessonId) }

            if (lessonWeblinksMappingEntity != null) {
                resourceRepository.getMyWebPlatforms(lessonWeblinksMappingEntity.websiteIds)
                    .also { lessonsWithWeblinks ->
                        Timber.d("Lessons with weblinks: ${lessonsWithWeblinks.toJson()}")
                        _webLinks.value = lessonsWithWeblinks
                    }
            }
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun launchTest(findNavController: NavController, unitId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.postValue(NetworkLoadingState.LOADING)
            val checkTestPassedStatus =
                async { resourceRepository.checkIfTestPassedOrNot(unitId, _classRoomId!!) }.await()
            _isSyncing.postValue(NetworkLoadingState.SUCCESS)
            if (checkTestPassedStatus > 0) {
                Timber.d("Test is passed")
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringMessage(
                        false, "Test already passed 🎉"
                    )
                )
            } else {
                withContext(Dispatchers.Main) {
                    findNavController.navigate(
                        R.id.action_subjectContentsFragment_to_unitTestFragment, bundleOf(
                            UNIT_ID to unitId,
                            CLASSROOM_ID to _classRoomId,
                            TestViewModel.TEST_TYPE to MCQType.UNIT.type
                            //  UnitTestFragment.UNIT_NAME to name
                        )
                    )
                }
            }
        }
    }


}