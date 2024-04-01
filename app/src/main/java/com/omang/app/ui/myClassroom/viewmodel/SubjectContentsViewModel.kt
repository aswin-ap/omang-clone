package com.omang.app.ui.myClassroom.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.psm.PsmEntity
import com.omang.app.data.model.bubble.BubbleData
import com.omang.app.data.model.myclassroom.ClassroomSyncRequest
import com.omang.app.data.model.myclassroom.Unit
import com.omang.app.data.model.resources.Added
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.FileUtil
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SubjectContentsViewModel @Inject constructor(
    val app: Application
) : BaseViewModel(app) {

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _classroomDetails = MutableLiveData<MyClassroomEntity>()
    val classroomDetails: LiveData<MyClassroomEntity> = _classroomDetails

    private var classroomDocIdList: List<Int> = emptyList()
    private var classroomVideoIdList: List<Int> = emptyList()
    private var classroomWebsiteIdList: List<Int> = emptyList()
    private var classroomMcqList: List<Int> = emptyList()
    private var unitWithLessonsIds: List<UnitWithLessons> = emptyList()
    private var unitIdList: MutableList<Unit> = mutableListOf()
    private var mcqList: List<Int> = emptyList()


    fun getSubjectData(classroomId: Int) {
        viewModelScope.launch {
            if (app.hasInternetConnection()) {
                _isSyncing.postValue(NetworkLoadingState.LOADING)
            /*   val classroomSyncRequest = ClassroomSyncRequest().apply {
                    category = "classroomChanges"
                    resourceId = classroomId
                    books = classroomDocIdList
                    videos = classroomVideoIdList
                    mcqs = classroomMcqList
                    webPlatforms = classroomWebsiteIdList
                    units = unitIdList
                }*/
                //Timber.tag("SUBJECT SYNC CALL").e("Sync Request is ${classroomSyncRequest.toJson()}")
                resourceRepository.getSubjectData(classroomId).collect { result ->
                    when (result) {
                        is NetworkResult.Success -> {
                            if (BubbleData.classroomList.contains(classroomId)) {
                                BubbleData.classroomList.remove(classroomId)
                            }
                            result.data.data?.let { resoureData ->
                                try {
                                    val added = resoureData.added

                                    val downloadList = mutableListOf<String>()
                                    added.units.forEach { unit ->
                                        unit.mcqs.forEach { mcqItem ->
                                            mcqItem.questions.forEach { questionItem ->
                                                if (ValidationUtil.isNotNullOrEmpty(questionItem.questionUrl)) {
                                                    downloadList.add(questionItem.questionUrl!!)
                                                }
                                                questionItem.options.forEach { optionItem ->
                                                    if (ValidationUtil.isNotNullOrEmpty(optionItem.optionUrl)) {
                                                        downloadList.add(optionItem.optionUrl!!)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    added.mcqs.notAttendedMCQs.forEach { mcqItem ->
                                        mcqItem.questions.forEach { questionItem ->
                                            if (ValidationUtil.isNotNullOrEmpty(questionItem.questionUrl)) {
                                                downloadList.add(questionItem.questionUrl!!)
                                            }
                                            questionItem.options.forEach { optionItem ->
                                                if (ValidationUtil.isNotNullOrEmpty(optionItem.optionUrl)) {
                                                    downloadList.add(optionItem.optionUrl!!)
                                                }
                                            }
                                        }
                                    }

                                    added.mcqs.attendedMCQs.forEach { mcqItem ->
                                        mcqItem.questions.forEach { questionItem ->
                                            if (ValidationUtil.isNotNullOrEmpty(questionItem.questionURL)) {
                                                downloadList.add(questionItem.questionURL!!)
                                            }
                                            questionItem.options.forEach { optionItem ->
                                                if (ValidationUtil.isNotNullOrEmpty(optionItem.optionURL)) {
                                                    downloadList.add(optionItem.optionURL!!)
                                                }
                                            }
                                        }
                                    }

//                            insert book/video and it's relation to the classroom
                                    resourceRepository.insertResources(added.books)
                                    resourceRepository.insertResources(added.videos)
                                    resourceRepository.insertClassroomWithResourcesRelation(
                                        resoureData
                                    )

//                            inserting unit
                                    resourceRepository.insertUnits(added.units)

//                            inserting lesson and it's relation to the unit, inserting weblinks
                                    resourceRepository.insertLessons(resoureData)

//                            insert classroom websites
                                    resourceRepository.insertClassroomWebsites(
                                        added.webPlatforms,
                                        DBConstants.WebsiteContent.CLASSROOM.value
                                    )

//                            insert mapping classroom and websites
                                    resourceRepository.insertClassroomWebsiteRelation(resoureData)

                                    resourceRepository.insertUnitTests(resoureData)

                                    resourceRepository.insertClassroomTest(resoureData)
                                    //inserts classroom psm
                                    added.psm?.let {
                                        resourceRepository.insertPsm(
                                            PsmEntity(
                                                psmId = classroomId,
                                                psmResponse = it.toJson()
                                            )
                                        )
                                        downloadPsm(it.url!!)
                                    }


                                    //saves the last synced date of the corresponding class
                                    //resourceRepository.updateLastUpdatedDateId(classroomId)

                                    updateClassroomContentCount(resoureData.added, classroomId)

//                                _isSyncing.value = NetworkLoadingState.LOADING

                                    recursiveImageDownload(downloadList)
                                } catch (e: Exception) {
                                    Timber.e("Sync Failed ---> ${e.toString()}")
                                    _isSyncing.value = NetworkLoadingState.ERROR
                                    _uiMessageStateLiveData.postValue(
                                        UIMessageState.StringResourceMessage(
                                            false, R.string.something_went_wrong
                                        )
                                    )
                                }
                            } ?: run {
                                _isSyncing.value = NetworkLoadingState.SUCCESS
                            }
                        }

                        is NetworkResult.Error -> {
                            _isSyncing.value = NetworkLoadingState.ERROR
                            Timber.e("${result.code} ${result.message}")
                        }

                        is NetworkResult.Failure -> {
                            if (result.exception.message?.contains("timeout") == true) {
                                _uiMessageStateLiveData.postValue(
                                    UIMessageState.StringResourceMessage(
                                        true, R.string.timeout_exception
                                    )
                                )
                            } else {
                                _uiMessageStateLiveData.postValue(
                                    UIMessageState.StringResourceMessage(
                                        true, R.string.something_went_wrong
                                    )
                                )
                            }
                            _isSyncing.value = NetworkLoadingState.ERROR

                        }

                        is NetworkResult.Loading -> TODO()

                    }
                }
            } else {
                _isSyncing.postValue(NetworkLoadingState.ERROR)
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringResourceMessage(
                        false, R.string.no_internet
                    )
                )
            }
        }
    }

    private suspend fun downloadPsm(url: String) {
        if (!FileUtil.isPsmAlreadyDownloadedInsideImagesFolder(
                getApplication(), url
            )
        ) {
            downloadPsmToDevice(url).distinctUntilChanged()
                .collect { result ->
                    if (result?.second == DownloadStatus.FINISHED) {
                        Timber.tag("Psm classroom Download").d("$url -> FINISHED")
                        _isSyncing.value = NetworkLoadingState.SUCCESS
                        return@collect
                    } else if (result?.second == DownloadStatus.ERROR) {
                        Timber.tag("Psm classroom Download").d("$url -> FAILED")
                        _isSyncing.value = NetworkLoadingState.SUCCESS
                        _uiMessageStateLiveData.value = UIMessageState.StringMessage(
                            false, "Psm download failed, please try again later!"
                        )
                        return@collect
                    }
                }
        }
    }

    private suspend fun updateClassroomContentCount(added: Added, classroomId: Int) {
        _classroomDetails.value?.let { myClassroomEntity ->
            var lessonsCount = 0
            added.units.forEach {
                if (it.lessons.isNotEmpty()) {
                    lessonsCount += it.lessons.size
                }
            }
            resourceRepository.updateMyClassroomCount(
                myClassroomEntity.also {
                    it.contents.books = added.books.size
                    it.contents.videos = added.videos.size
                    it.contents.lessons = lessonsCount
                    it.contents.webPlatforms = added.webPlatforms.size
                    it.updatedAt = ViewUtil.getUtcTime()
                }
            )
        }
        fetchClassroomDetails(classroomId)
    }

    fun fetchClassroomDetails(classroomId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.getClassroomDetailsById(classroomId).also {
                _classroomDetails.postValue(it)
            }
        }
    }

    //downloads all images to device using recursive function
    private suspend fun recursiveImageDownload(downloadList: MutableList<String>) {
        Timber.tag("Image Download").d("Download list: $downloadList")
        if (downloadList.isNotEmpty()) {
            if (FileUtil.isFileAlreadyDownloadedInsideImagesFolder(
                    getApplication(), downloadList.first()
                )
            ) {
                Timber.tag("Image Download").d("Download Skipped")
                downloadList.removeFirst()
                recursiveImageDownload(downloadList)
            } else {
                downloadImageToDevice(downloadList.first()).distinctUntilChanged()
                    .collect { result ->
                        if (result?.second == DownloadStatus.FINISHED) {
                            Timber.tag("Image Download")
                                .d("${downloadList.first()} -> FINISHED")
                            downloadList.removeFirst()
                            recursiveImageDownload(downloadList)
                        } else if (result?.second == DownloadStatus.ERROR) {
                            Timber.tag("Image Download").d("${downloadList.first()} -> FAILED")
                            downloadList.removeFirst()
                            recursiveImageDownload(downloadList)
                        }
                    }
            }
        } else {
            _isSyncing.value = NetworkLoadingState.SUCCESS
            Timber.tag("Image Download").d("DOWNLOAD COMPLETED 💥")
        }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }


    fun fetchClassroomResourceIds(classroomId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                resourceRepository.getDocumentIdByClassroomId(classroomId).also {
                    classroomDocIdList = it
                    Timber.tag("SUBJECT VIEWMODEL")
                        .e("Classroom Doc Id List----$classroomDocIdList")
                }
            }.await()
            async {
                resourceRepository.getVideoIdByClassroomId(classroomId).also {
                    classroomVideoIdList = it
                    Timber.tag("SUBJECT VIEWMODEL")
                        .e("Classroom Video Id List----$classroomVideoIdList")

                }
            }.await()

            async {
                resourceRepository.getWebsiteIdByClasroomId(classroomId).also {
                    classroomWebsiteIdList = it
                    Timber.tag("SUBJECT VIEWMODEL")
                        .e("Classroom Website Id List----$classroomWebsiteIdList")

                }
            }.await()

            async {
                resourceRepository.getUnitsWithLessonsByClassroomId(classroomId)
                    .also { unitlessons ->
                        unitWithLessonsIds = unitlessons
                        unitlessons.forEach { unitWithLessons ->
                            resourceRepository.getMcqId(unitWithLessons.unit.id, classroomId).also {
                                mcqList = it
                                Timber.tag("SUBJECT VIEWMODEL")
                                    .e("Classroom MCQ Id List----$mcqList")
                            }
                            val unit = Unit()
                            unit.id = unitWithLessons.unit.id
                            unit.lessons = unitWithLessons.lessons.map {
                                it.id
                            }
                            unit.mcqs = mcqList.map { it }
                            unitIdList.add(unit)
                        }

                        Timber.tag("SUBJECT VIEWMODEL")
                            .e("Classroom Lesson And Unit Id List----$unitIdList")
                    }
            }.await()

            async {
                resourceRepository.getClassroomMcqIds(classroomId).also{
                    classroomMcqList = it
                    Timber.tag("SUBJECT VIEWMODEL")
                        .e("Classroom General Mcq Id List----$classroomMcqList")
                }
            }.await()
        }

    }

        /*    fun getResourcesByClassroomId(classroomId: Int) {
            viewModelScope.launch {
                resourceRepository.getResourcesByClassroomId(classroomId).also { resourceEntity ->
                    Timber.d("Resources for Classroom $classroomId:")
                    resourceEntity.forEach { resource ->
                        Timber.d("Resource ID: ${resource.id}, Name: ${resource.name}, Type: ${resource.type}")
                        // Add more properties as needed
                    }
                }
            }
        }

        fun getWebsiteByClassroomID(classroomId: Int) {
            viewModelScope.launch {
                resourceRepository.getWebsiteByClassroomID(classroomId).also { websiteEntity ->
                    Timber.d("Resources for Classroom $classroomId:")
                    websiteEntity.forEach { website ->
                        Timber.d("Resource ID: ${website.id}, Name: ${website.name}, Type: ${website.logo}")
                        // Add more properties as needed
                    }
                }
            }
        }

        fun getUnitsWithLessonsByClassroomId(classroomId: Int) {
            viewModelScope.launch {
                resourceRepository.getUnitsWithLessonsByClassroomId(classroomId)
                    .also { unitWithLessons ->
                        Timber.d("Units with Lessons: ${unitWithLessons.toJson()}")
                    }
            }
        }

        fun getLessonWeblinks(classroomId: Int, lessonId: Int) {
            viewModelScope.launch {
                val lessonWeblinksMappingEntity =
                    resourceRepository.fetchLessonIds(classroomId, lessonId)

                if (lessonWeblinksMappingEntity != null) {
                    resourceRepository.getMyWebPlatforms(lessonWeblinksMappingEntity.websiteIds)
                        .also { lessonsWithWeblinks ->
                            Timber.d("Lessons with weblinks: ${lessonsWithWeblinks.toJson()}")
                        }
                }
            }
        }*/

}