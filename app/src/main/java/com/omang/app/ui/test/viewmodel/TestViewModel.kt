package com.omang.app.ui.test.viewmodel

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.omang.app.R
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.database.test.TestEntity
import com.omang.app.data.model.resources.addOrRemoveElementFromSelectedOptions
import com.omang.app.data.model.resources.updateSelectedId
import com.omang.app.data.model.test.AttemptedQuestion
import com.omang.app.data.model.test.Mcq
import com.omang.app.data.model.test.TestSubmitRequest
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.test.MCQType
import com.omang.app.ui.test.TestFragment
import com.omang.app.utils.FileUtil
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.NetworkConnection
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.properties.Delegates


sealed class CountDownStatus {
    object CountDownInit : CountDownStatus()
    data class CountDownStarted(val time: String) : CountDownStatus()
    object CountDownFinished : CountDownStatus()
}

data class TestResult(
    var percentageObtained: Float = 0f,
    var totalQuestions: Int = 0,
    var attemptedQuestions: Int = 0,
    var correctAnswers: Int = 0,
    var wrongAnswers: Int = 0,
    var testTime: String = "",
    var totalTimeTaken: String = "",
    var isTestPassed: Boolean
)

@HiltViewModel
class TestViewModel @Inject constructor(
    val app: Application,
    private var networkConnectionManager: NetworkConnection
) :
    BaseViewModel(app) {

    private val networkInspectorScope = CoroutineScope(Job())

    private var _unitId: Int? = null
    private var _classRoomId: Int? = null
    private var _MCQType by Delegates.notNull<MCQType>()
    val testType get() = _MCQType

    private lateinit var startTime: String

    private val _testNewLiveData = MutableLiveData<List<TestEntity>?>()
    val testNewLiveData: LiveData<List<TestEntity>?> = _testNewLiveData

    private val _testExpiredLiveData = MutableLiveData<List<TestEntity>?>()
    val testExpiredLiveData: LiveData<List<TestEntity>?> = _testExpiredLiveData

    private val _testAttemptedLiveData = MutableLiveData<List<TestEntity>?>()
    val testAttemptedLiveData: LiveData<List<TestEntity>?> = _testAttemptedLiveData

    private val _testDetailLiveData = MutableLiveData<TestEntity?>()
    val testDetailLiveData: LiveData<TestEntity?> = _testDetailLiveData

    private val _questionsLiveData = MutableLiveData<List<QuestionEntity>>()
    val questionsLiveData: LiveData<List<QuestionEntity>> = _questionsLiveData

    private val _questionAnsweredList = mutableListOf<QuestionEntity>()
    val questionAnsweredList get() = _questionAnsweredList

    private val _testResultLiveData = MutableLiveData<TestResult>()
    val testResultLiveData: LiveData<TestResult> = _testResultLiveData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    private val _testSyncing = MutableLiveData<NetworkLoadingState?>()
    val testSyncing: LiveData<NetworkLoadingState?> = _testSyncing

    private lateinit var countDownTimer: CountDownTimer

    private val _countdownTime = MutableLiveData<CountDownStatus>()
    val countdownTime: LiveData<CountDownStatus>
        get() = _countdownTime

    fun setUnitId(id: Int) {
        if (id != 0)
            _unitId = id
    }

    fun setClassRoomId(id: Int) {
        if (id != 0)
            _classRoomId = id
    }

    fun setTestType(MCQType: MCQType) {
        _MCQType = MCQType
    }

    fun initTestTime() {
        startTime = ViewUtil.getUtcTime()
    }

    fun syncTestsFromAPI(currentFragment: TestFragment.TEST) {
        viewModelScope.launch(Dispatchers.IO) {
            if (app.hasInternetConnection()) {
                //checks device network connection
                resourceRepository.getMcqTestData().collect { networkResult ->
                    when (networkResult) {
                        is NetworkResult.Error -> {
                            _isSyncing.postValue(NetworkLoadingState.ERROR)
                            Timber.e("${networkResult.code} ${networkResult.message}")
                            _uiMessageStateLiveData.postValue(
                                UIMessageState.StringResourceMessage(
                                    false,
                                    R.string.something_went_wrong
                                )
                            )
                        }

                        is NetworkResult.Failure -> {
                            _isSyncing.postValue(NetworkLoadingState.ERROR)
                            Timber.e("${networkResult.exception}")
                            _uiMessageStateLiveData.postValue(
                                UIMessageState.StringResourceMessage(
                                    false,
                                    R.string.something_went_wrong
                                )
                            )
                        }

                        is NetworkResult.Loading -> {
                            _isSyncing.postValue(NetworkLoadingState.LOADING)
                        }

                        is NetworkResult.Success -> {
                            val downloadList = mutableListOf<String>()
                            networkResult.data.data.notAttendedMCQs.forEach { mcqItem ->
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
                            resourceRepository.insertGeneralMcqs(networkResult.data.data)
                            _isSyncing.postValue(NetworkLoadingState.SUCCESS)
                            _isSyncing.postValue(NetworkLoadingState.LOADING)
                            recursiveImageDownload(downloadList, currentFragment)
                        }
                    }
                }
            } else {
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringResourceMessage(
                        false,
                        R.string.no_internet
                    )
                )
            }
        }
    }

    //downloads all images to device using recursive function
    private suspend fun recursiveImageDownload(
        downloadList: MutableList<String>,
        currentFragment: TestFragment.TEST
    ) {
        Timber.tag("Image Download").d("Download list: $downloadList")
        if (downloadList.isNotEmpty()) {
            if (FileUtil.isFileAlreadyDownloadedInsideImagesFolder(
                    getApplication(),
                    downloadList.first()
                )
            ) {
                Timber.tag("Image Download").d("Download Skipped")
                downloadList.removeFirst()
                recursiveImageDownload(downloadList, currentFragment)
            } else {
                downloadImageToDevice(downloadList.first()).distinctUntilChanged()
                    .collect { result ->
                        if (result?.second == DownloadStatus.FINISHED) {
                            Timber.tag("Image Download").d("${downloadList.first()} -> FINISHED")
                            downloadList.removeFirst()
                            recursiveImageDownload(downloadList, currentFragment)
                        } else if (result?.second == DownloadStatus.ERROR) {
                            Timber.tag("Image Download").d("${downloadList.first()} -> FAILED")
                            downloadList.removeFirst()
                            recursiveImageDownload(downloadList, currentFragment)
                        }
                    }
            }
        } else {
            _isSyncing.postValue(NetworkLoadingState.SUCCESS)
            Timber.tag("Image Download").d("DOWNLOAD COMPLETED 💥")
            getTestsBasedOnType(currentFragment)
        }
    }

    private fun getTestsBasedOnType(currentFragment: TestFragment.TEST) {
        when (currentFragment) {
            TestFragment.TEST.EXPIRED -> fetchExpiredGeneralMCQs()
            TestFragment.TEST.ATTEMPTED -> fetchAttemptedGeneralMCQs()
            TestFragment.TEST.NEW -> fetchNewGeneralMCQs()
        }
    }

    fun fetchTestsFromDb(testId: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_MCQType == MCQType.UNIT) {
                //fetches the number of remaining tests to check if its last
                val remainingTests = async {
                    resourceRepository.getNotAttemptedMcqCountByClassroomAndUnit(
                        _classRoomId!!,
                        _unitId!!
                    )
                }.await()
                Timber.tag("getUnitMainMcqTest").d("remainingTests : $remainingTests")
                resourceRepository.getUnitMainMcqTest(_unitId!!, _classRoomId!!).also {
                    //fetchin resourceRepository.getUnitMainMcqTest(_unitId!!, _classRoomId!!).also {g the first test data from the list
                    it?.let { testEntity ->
                        Timber.d("getUnitMainMcqTest: ${testEntity.toJson()}")
                        fetchTestData(testEntity.generalMcqId!!, remainingTests)
                    } ?: kotlin.run {
                        _testDetailLiveData.postValue(null)
                    }
                }
            } else {
                testId?.let {
                    fetchTestData(it, 0)
                }
            }
        }
    }

    fun fetchNewGeneralMCQs() {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                resourceRepository.getClassroomTestsByType(
                    _classRoomId!!,
                    DBConstants.TestType.NEW.value
                )
            }.await()
                .also { testList ->
                    Timber.d("testList : -> ${testList.map { it.id }}")
                    _testNewLiveData.postValue(testList)
                   /* testList.filter { ViewUtil.returnBefore(it.endTime) }.also {
                        _testLiveData.postValue(it)
                    }
                    testList.filter { !ViewUtil.returnBefore(it.endTime) }.onEach { test ->
                        resourceRepository.updateTestToExpired(test.generalMcqId!!)
                    }*/
                }
        }
    }

    fun fetchExpiredGeneralMCQs() {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                resourceRepository.getClassroomTestsByType(
                    _classRoomId!!,
                    DBConstants.TestType.EXPIRED.value
                )
            }.await().also { testList ->
                _testExpiredLiveData.postValue(testList)
            }
        }
    }

    fun fetchAttemptedGeneralMCQs() {
        viewModelScope.launch(Dispatchers.IO) {
            async {
                resourceRepository.getClassroomTestsByType(
                    _classRoomId!!,
                    DBConstants.TestType.ATTEMPTED.value
                )
            }.await().also { testList ->
                _testAttemptedLiveData.postValue(testList)
            }
        }
    }

    //fetches the details of a test
    fun fetchTestData(testId: Int, remainingTests: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSyncing.postValue(NetworkLoadingState.LOADING)
            resourceRepository.getTestDetails(testId).also { testData ->
                testData.also {
                    it.isOneTestRemains = remainingTests == 1
                }
                _testDetailLiveData.postValue(testData)
                fetchTestQuestions(testData.questionsId)
            }
        }
    }

    //fetches the questions of a test
    private suspend fun fetchTestQuestions(testIds: List<Int>) {
        resourceRepository.getQuestionsList(testIds).also { questionsList ->
            questionsList.forEach { questionEntity ->
                //Checks whether the question is multiple answer question
                val count = questionEntity.options.count { it.isAnswer }
                if (count > 1)
                    questionEntity.isMultiQuestion = true
            }
            _questionsLiveData.postValue(questionsList)
            //adds the questions to another list to handle answer logic
            _questionAnsweredList.clear()
            _questionAnsweredList.addAll(questionsList)
            _isSyncing.postValue(NetworkLoadingState.SUCCESS)
        }
    }

    fun updateTestAnswerByOptionId(questionId: Int, optionId: Int) {
        /* Updates the selected options as selected on corresponding question
        in _questionAnsweredList */
        val questionEntity = _questionAnsweredList.find { it.id == questionId }
        if (questionEntity!!.isMultiQuestion) {
            val option = questionEntity.options.find { it.id == optionId }
            questionEntity.addOrRemoveElementFromSelectedOptions(option!!)
        } else {
            questionEntity.options.updateSelectedId(optionId)
        }
        Timber.d("Answer list: ${_questionAnsweredList.toJson()}")
    }

    fun calculateResult() {
        viewModelScope.launch {
            _isSyncing.postValue(NetworkLoadingState.LOADING)
            //TODO: calculation of test total time and attempted time
            var totalQuestions: Int
            var attemptedQuestions = 0
            var correctAnswers = 0
            var wrongAnswers: Int
            var percentage: Float
            var totalScore = 0
            var totalScoreObtained = 0.0f
            var correctAnswerCount = 0
            val attemptedQuestionList = mutableListOf<AttemptedQuestion>()

            _testDetailLiveData.value?.let { testEntity ->
                totalQuestions = testEntity.questionsId.size
                //sets the wrong answer as total questions
                wrongAnswers = totalQuestions
                _questionAnsweredList.map { questionEntity ->
                    //adds the question score
                    totalScore += questionEntity.score
                    //if it it a multiple selection question
                    if (questionEntity.isMultiQuestion) {
                        if (questionEntity.selectedOptions.isNotEmpty()) {
                            //creates attemptedQuestion
                            val attemptedMultiQuestion = AttemptedQuestion()
                            val selectedOptionsList = arrayListOf<Int>()
                            attemptedMultiQuestion.question = questionEntity.id
                            //adds attempted question by 1
                            attemptedQuestions += 1
                            //fetches the correct answers
                            val correctAnswerList = questionEntity.options.filter { it.isAnswer }
                            //validates each selectedOption of user
                            questionEntity.selectedOptions.forEachIndexed { _, optionItem ->
                                //TODO: Update the logic
                                selectedOptionsList.add(optionItem.id)
                                if (correctAnswerList.isEmpty()) {
                                    //Sets the score as 0, might happen in some cases when
                                    // the question has no isAnswer key as "true"
                                    attemptedMultiQuestion.score = 0
                                } else {
                                    //find the average of total score
                                    val averageScore =
                                        questionEntity.score.div(correctAnswerList.size.toFloat())
                                    //Checks whether the selected option is correct
                                    correctAnswerList.forEach { correctOptionItem ->
                                        //if selected option is correct :
                                        if (optionItem.id == correctOptionItem.id) {
                                            //adds the current question score to totalScoreObtained
                                            correctAnswerCount += 1
                                            //calculates the averageScore
                                            totalScoreObtained += averageScore
                                            //TODO: User Input texts
                                        }
                                    }
                                }
                            }
                            //adds the selected options to attemptedQuestion entity
                            attemptedMultiQuestion.selectedOptions = selectedOptionsList
                            //if both count is correct, It is a correct answer
                            if (correctAnswerCount == correctAnswerList.count { it.isAnswer }) {
                                //adds the correctAnswers by 1
                                correctAnswers++
                                wrongAnswers--
                                attemptedMultiQuestion.score = questionEntity.score
                            } else {
                                attemptedMultiQuestion.score = totalScoreObtained.toInt()
                            }
                            //Adds the attemptedQuestion object to list
                            attemptedQuestionList.add(attemptedMultiQuestion)
                        } else {
                            //removes the correctAnswers by 1
                            wrongAnswers--
                        }
                    } else {
                        if (questionEntity.options.any { it.isSelected }) {
                            //creates attemptedQuestion
                            val attemptedQuestion = AttemptedQuestion()
                            //adds attempted question by 1
                            attemptedQuestions += 1
                            //Gets the selected option details
                            val selectedOption = questionEntity.options.find { it.isSelected }
                            selectedOption?.let { optionItem ->
                                //Checks whether the selected option is correct
                                questionEntity.options.any { it.isAnswer && it.id == optionItem.id }
                                    .also { isCorrect ->
                                        if (isCorrect) {
                                            //adds the correctAnswers by 1
                                            correctAnswers++
                                            //removes the correctAnswers by 1
                                            wrongAnswers--
                                            //adds the current question score to totalScoreObtained
                                            totalScoreObtained += questionEntity.score
                                            attemptedQuestion.score = questionEntity.score
                                            //TODO: User Input texts
                                        } else {
                                            //If it is wrong answer, sets the score as 0
                                            attemptedQuestion.score = 0
                                        }
                                    }
                                attemptedQuestion.question = questionEntity.id
                                attemptedQuestion.selectedOptions = listOf(optionItem.id)
                                //Adds the attemptedQuestion object to list
                                attemptedQuestionList.add(attemptedQuestion)
                            } ?: false
                        } else {
                            //removes the correctAnswers by 1
                            wrongAnswers--
                        }
                    }
                }
                percentage = ((totalScoreObtained / totalScore) * 100)
                //if no questions attended, wrongAnswer will be zero
                if (attemptedQuestions == 0) wrongAnswers = 0
                //If totalScore and attemptedScore is same test pass else fail
                val isTestPassed = totalScoreObtained.toInt() == totalScore
                //If percentage is greater than or equals to 60 test pass else fail
//                val isTestPassed = percentage >= 60.0f
                Timber.tag("Test Details")
                    .d(
                        "totalScore: $totalScore totalScoreObtained: $totalScoreObtained percentage: $percentage" +
                                "isTestPassed: $isTestPassed attemptedQuestions: $attemptedQuestions correctAnswers: $correctAnswers" +
                                "wrongAnswers: $wrongAnswers attemptedQuestionList: ${attemptedQuestionList.toJson()}"
                    )

                if (testType == MCQType.UNIT) {
                    //fetches the corresponding unitTestRelationEntity
                    val unitTestRelationEntity = async {
                        resourceRepository.getUnitTestRelationEntityByUnitAndTest(
                            _testDetailLiveData.value?.generalMcqId!!, _unitId!!, _classRoomId!!
                        )
                    }.await()

                    //updates given mcq as attended in unitTestRelationEntity table
                    resourceRepository.updateTestDetails(
                        unitTestRelationEntity.apply {
                            isAttempted = DBConstants.IsTestAttempted.TRUE.value
                            testStatus = if (isTestPassed) DBConstants.TestStatus.PASSED.value
                            else DBConstants.TestStatus.FAILED.value
                        }
                    )
                }

                _isSyncing.postValue(NetworkLoadingState.SUCCESS)

                _testResultLiveData.postValue(
                    TestResult(
                        totalQuestions = totalQuestions,
                        attemptedQuestions = attemptedQuestions,
                        correctAnswers = correctAnswers,
                        wrongAnswers = wrongAnswers,
                        percentageObtained = percentage,
                        totalTimeTaken = if (testType == MCQType.CLASSROOM) ViewUtil.getDiffBetweenTwoTimes(
                            startTime,
                            ViewUtil.getUtcTime()
                        ) else "",
                        testTime = if (testType == MCQType.CLASSROOM) ViewUtil.getDiffBetweenTwoTimes(
                            testEntity.startTime,
                            testEntity.endTime
                        ) else "",
                        isTestPassed = isTestPassed
                    )
                )

                //Inserts the test result in room
                if (attemptedQuestions != 0) {
                    resourceRepository.updateAttemptedTest(
                        testEntity.apply {
                            createdOn = ViewUtil.getUtcTime()
                            unitId = this@TestViewModel._unitId
                            this.classroomId = _classRoomId
                            score = totalScoreObtained
                            this.percentage = percentage
                            attemptsCount = attemptedQuestions
                            correctAttempts = correctAnswers
                            wrongAttempts = wrongAnswers
                            questions = attemptedQuestionList
                            type = DBConstants.TestType.ATTEMPTED.value
                        }
                    )
                }
            }
        }
    }

    fun sendTests(findNavController: NavController) {
        viewModelScope.launch {
            if (app.hasInternetConnection()) {
                sendTestResultsToServer()
            } else {
                _testSyncing.postValue(NetworkLoadingState.SUCCESS)
//                findNavController.popBackStack(R.id.testStartFragment, true)
            }
        }
    }

    private fun sendTestResultsToServer() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                _testSyncing.postValue(NetworkLoadingState.LOADING)
                val offlineList = resourceRepository.getAllAttemptedOfflineTests()
                if (offlineList.isNotEmpty()) {
                    val mcqList = mutableListOf<Mcq>()
                    offlineList.forEach { offlineEntity ->
                        Timber.tag("Test").d("Attempted Test are: ${offlineEntity.toJson()}")
                        mcqList.add(
                            Mcq(
                                id = offlineEntity.generalMcqId,
                                score = offlineEntity.score.roundToInt(),
                                attemptsCount = offlineEntity.attemptsCount,
                                correctAttempts = offlineEntity.correctAttempts,
                                wrongAttempts = offlineEntity.wrongAttempts,
                                questions = offlineEntity.questions,
                                attendedOn = offlineEntity.createdOn,
                                mcqStudId = null,
                                classroom = offlineEntity.classroomId,
                                unit = offlineEntity.unitId
                            )
                        )
                    }

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

    private fun startCountDown(timeInMilli: Long) {
        viewModelScope.launch {
            _countdownTime.postValue(CountDownStatus.CountDownInit)
            countDownTimer = object : CountDownTimer(timeInMilli, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val time: String
                    val seconds = (millisUntilFinished / 1000).toInt() % 60
                    val minutes = (millisUntilFinished / (1000 * 60) % 60).toInt()
                    val hours = (millisUntilFinished / (1000 * 60 * 60) % 24).toInt()
                    time = if (hours == 0 && minutes == 0) {
                        if (seconds < 10) {
                            "00:0$seconds"
                        } else {
                            "00:$seconds"
                        }
                    } else if (hours == 0) {
                        if (minutes < 10) {
                            if (seconds < 10) {
                                "0$minutes:0$seconds"
                            } else {
                                "0$minutes:$seconds"
                            }
                        } else {
                            if (seconds < 10) {
                                "$minutes:0$seconds"
                            } else {
                                "$minutes:$seconds"
                            }
                        }
                    } else {
                        if (hours < 10) {
                            if (minutes < 10) {
                                if (seconds < 10) {
                                    "0$hours:0$minutes:0$seconds"
                                } else {
                                    "0$hours:0$minutes:$seconds"
                                }
                            } else {
                                "0$hours:$minutes:$seconds"
                            }
                        } else {
                            if (seconds < 10) {
                                "$hours:$minutes:0$seconds"
                            } else {
                                "$hours:$minutes:$seconds"
                            }
                        }
                    }
                    _countdownTime.postValue(CountDownStatus.CountDownStarted(time))
                }

                override fun onFinish() {
                    _countdownTime.postValue(CountDownStatus.CountDownFinished)
                }
            }.start()
        }
    }

    fun cancelCountDown() {
        if (testType == MCQType.CLASSROOM)
            countDownTimer.cancel()
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    fun resetTimer() {
        _countdownTime.value = CountDownStatus.CountDownInit
    }

    fun startTimer() {
        _testDetailLiveData.value?.let {
            if (ValidationUtil.isNotNullOrEmpty(it.endTime)) {
                val timeInMilliSeconds = ViewUtil.getTimeInMillis(
                    it.startTime, it.endTime
                )
                startCountDown(timeInMilliSeconds)
            }
        }
    }

    public override fun onCleared() {
        super.onCleared()
        Timber.e("com.omang.app.ui.test.viewmodel.TestViewModel killed ")
    }

    fun clearSync() {
        viewModelScope.launch {
            _testSyncing.value = null
        }
    }

    companion object {
        const val TEST_TYPE = "test_type"
    }
}


