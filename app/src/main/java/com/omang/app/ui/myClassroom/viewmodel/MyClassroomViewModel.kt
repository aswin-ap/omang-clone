package com.omang.app.ui.myClassroom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.navigation.MobileNavigationEntity
import com.omang.app.data.database.test.TestEntity
import com.omang.app.data.model.test.Mcq
import com.omang.app.data.model.test.TestSubmitRequest
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MyClassroomViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    private var networkConnectionManager: NetworkConnection
) : ViewModel() {

    private lateinit var offlineTestData: TestEntity

    private val _offlineTestDataList = MutableLiveData<TestEntity?>()
    val offlineTestDataList: LiveData<TestEntity?> = _offlineTestDataList

    private val _testStatus = MutableLiveData<Boolean>()
    val testStatus: LiveData<Boolean> = _testStatus

    private val _testSyncing = MutableLiveData<NetworkLoadingState?>()
    val testSyncing: LiveData<NetworkLoadingState?> = _testSyncing

    private val _myClassroomsLiveData = MutableLiveData<List<MyClassroomEntity>>()
    val myClassroomsLiveData: LiveData<List<MyClassroomEntity>> get() = _myClassroomsLiveData

    private val _myClubsLiveData = MutableLiveData<List<MyClassroomEntity>>()
    val myClubsLiveData: LiveData<List<MyClassroomEntity>> get() = _myClubsLiveData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    init {
        getMyClubs()
        getMyClassrooms()
    }
    private fun getMyClassrooms() {
        try {
            viewModelScope.launch {
                resourceRepository.getMyClassrooms().collect { myClassroomEntities ->
                    _myClassroomsLiveData.postValue(myClassroomEntities)
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

    private fun getMyClubs() {
        viewModelScope.launch {
            resourceRepository.getMyClubs().collect { myClassroomEntities ->
                try {
                    _myClubsLiveData.postValue(myClassroomEntities)
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

    fun addToNavigation(
        page: String,
        event: DBConstants.Event,
        comment: String? = null,
        subjectId: Int? = null,
        contentId: Int? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.insertMobileNavigationEntry(
                MobileNavigationEntity(
                    page = page,
                    event = event.value,
                    comment = comment,
                    subjectId = subjectId,
                    contentId = contentId
                )
            )
        }
    }

    /*To get the details of test that are done at offline and not submitted to server
    Fetch all the test details that are not submitted from the test table
    * */
    fun getTestOfflineStatus() {
        viewModelScope.launch {
            async {
                networkConnectionManager.observeNetworkConnection()
                    .collect {
                        if (it) {
                            viewModelScope.launch(Dispatchers.IO) {
                                val offlineList = resourceRepository.getAllAttemptedOfflineTests()
                                if (offlineList.isNotEmpty()) {
                                    Timber.tag("Test Offline Details").d("Attempted Offline Test are: ${offlineList.toJson()}")
                                    offlineList.forEach { offlineEntity ->
                                        _offlineTestDataList.postValue(offlineEntity)
                                        offlineTestData = offlineEntity
                                    }
                                }
                            }

                        } else {

                        }
                    }
            }.await()
        }

    }

    /*Submit all the tests that are done through offline to server
    * */
    fun sendTestResultsToServer() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                _testSyncing.postValue(NetworkLoadingState.LOADING)
                if (offlineTestData.toJson().isNotEmpty()) {
                    val mcqList = mutableListOf<Mcq>()
                    Timber.tag("Test Submit").d("Attempted Test are: ${offlineTestData.toJson()}")
                    mcqList.add(
                        Mcq(
                            id = offlineTestData.generalMcqId,
                            score = offlineTestData.score.roundToInt(),
                            attemptsCount = offlineTestData.attemptsCount,
                            correctAttempts = offlineTestData.correctAttempts,
                            wrongAttempts = offlineTestData.wrongAttempts,
                            questions = offlineTestData.questions,
                            attendedOn = offlineTestData.createdOn,
                            mcqStudId = null,
                            classroom = offlineTestData.classroomId,
                            unit = offlineTestData.unitId
                        )
                    )
                    TestSubmitRequest(
                        mcqs = mcqList
                    ).also {
                        Timber.tag("Test").d("TestSubmitRequest: $it")
                        resourceRepository.postMcqTestResults(it).collect { result ->
                            when (result) {
                                is NetworkResult.Error -> {
                                    _testSyncing.postValue(NetworkLoadingState.ERROR)
                                }

                                is NetworkResult.Failure -> {
                                    _testSyncing.postValue(NetworkLoadingState.ERROR)
                                }

                                is NetworkResult.Loading -> {}

                                is NetworkResult.Success -> {
                                    //TODO - update the tests to attempted
                                    resourceRepository.setTestsStatusToAttempted(result.data.mcqResultData)
                                    _testSyncing.postValue(NetworkLoadingState.SUCCESS)
                                }
                            }
                        }
                    }
                } else {
                    _testSyncing.postValue(NetworkLoadingState.SUCCESS)
                    return@launch
                }
            }
        } catch (e: Exception) {
            Timber.e("Exception while sending test data: $e")
            _testSyncing.postValue(NetworkLoadingState.ERROR)
        }
    }
}