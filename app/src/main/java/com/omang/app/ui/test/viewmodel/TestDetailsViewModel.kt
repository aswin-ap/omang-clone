package com.omang.app.ui.test.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.omang.app.R
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.database.test.TestEntity
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.floorToInt
import com.omang.app.utils.extensions.toJson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TestDetailsViewModel @Inject constructor(val app: Application) :
    BaseViewModel(app) {

    private val _testDetailLiveData = MutableLiveData<TestEntity>()
    val testDetailLiveData: LiveData<TestEntity> = _testDetailLiveData

    private val _questionsLiveData = MutableLiveData<List<QuestionEntity>>()
    val questionsLiveData: LiveData<List<QuestionEntity>> = _questionsLiveData

    private val _chartDetailLiveData = MutableLiveData<PieData>()
    val chartDetailLiveData: LiveData<PieData> = _chartDetailLiveData

    private val _uiMessageStateLiveData = MutableLiveData<UIMessageState>(UIMessageState.Empty())
    val uiMessageStateLiveData: LiveData<UIMessageState> get() = _uiMessageStateLiveData

    private val _isSyncing = MutableLiveData<NetworkLoadingState>()
    val isSyncing: LiveData<NetworkLoadingState> = _isSyncing

    //fetches the details of a test
    fun fetchTestData(testId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            resourceRepository.getTestDetails(testId).also { testData ->
                _testDetailLiveData.postValue(testData)
                fetchTestQuestions(testData.questionsId)
                fetchChartData(testData)
            }
        }
    }

    private fun fetchChartData(testData: TestEntity) {
        val pieEntryList = mutableListOf<PieEntry>()
        val totalCount = testData.questionsId.size.toFloat()
        val attempted = testData.attemptsCount.toFloat()
        val correct = testData.correctAttempts.toFloat()
        val wrong = testData.wrongAttempts.toFloat()
        val unAttempted = totalCount - attempted

        if (wrong != 0f) {
            val percentage =
                ((wrong / totalCount * 100) * 100 / 100).floorToInt().toFloat()
            pieEntryList.add(PieEntry(percentage, "Wrong", 0f)) //wrong
        }
        if (unAttempted != 0f) {
            val percentage =
                ((unAttempted / totalCount * 100) * 100 / 100).floorToInt().toFloat()
            pieEntryList.add(PieEntry(percentage, "Unattempted", 1f)) //unattempted
        }

        if (correct != 0f) {
            val percentage =
                ((correct / totalCount * 100) * 100 / 100).floorToInt().toFloat()
            pieEntryList.add(PieEntry(percentage, "Correct", 2f)) //correct
        }

        val dataSet = PieDataSet(pieEntryList, "")
        var chartColours = listOf(
            Color.rgb(217, 83, 79), Color.rgb(122, 121, 121), Color.rgb(92, 184, 92)
        )

        if (wrong == 0f && unAttempted == 0f && correct != 1f) {
            chartColours = listOf(
                green
            )
        } else if (wrong == 0f && unAttempted != 0f && correct == 0f) {
            chartColours = listOf(
                grey
            )
        } else if (wrong == 0f && unAttempted != 0f) {
            chartColours = listOf(
                grey, green
            )
        } else if (wrong != 0f && unAttempted == 0f && correct == 0f) {
            chartColours = listOf(
                red
            )
        } else if (wrong != 0f && unAttempted == 0f) {
            chartColours = listOf(
                red, green
            )
        } else if (wrong != 0f && correct == 0f) {
            chartColours = listOf(
                red, grey
            )
        } else if (wrong != 0f) {
            chartColours = listOf(
                red, grey, green
            )
        }

        dataSet.colors = chartColours
        PieData(dataSet).also {
            _chartDetailLiveData.postValue(it)
        }
    }

    //fetches the questions of a test
    private suspend fun fetchTestQuestions(testIds: List<Int>) {
            try {
                resourceRepository.getQuestionsList(testIds).also { questionsList ->
                    questionsList.forEach { questionEntity ->
                        //Checks whether the question is multiple answer question
                        val count = questionEntity.options.count { it.isAnswer }
                        if (count > 1) {
                            //iterates the options and find the selected options from the
                            //test entity "questions" field and adds it into selected options array
                            //in question entity
                            testDetailLiveData.value!!.questions
                                .firstOrNull { it.question == questionEntity.id }
                                ?.let { attemptedQuestion ->
                                    questionEntity.options.forEach {
                                        if (attemptedQuestion.selectedOptions.contains(it.id)) {
                                            questionEntity.selectedOptions.add(it)
                                        }
                                    }
                                    questionEntity.isMultiQuestion = true
                                }
                        } else {
                            //iterates the options and find the selected option from the
                            //test entity "questions" field
                            testDetailLiveData.value?.questions
                                ?.firstOrNull { it.question == questionEntity.id }
                                ?.let { attemptedQuestion ->
                                    questionEntity.options
                                        .firstOrNull { it.id == attemptedQuestion.selectedOptions.first() }
                                        ?.isSelected = true
                                }
                        }
                    }
                    Timber.d("Questions list: ${questionsList.toJson()}")
                    _questionsLiveData.postValue(questionsList)
                }
            } catch (exception: Exception) {
                Timber.e("Exception while fetching test details: $exception")
                _uiMessageStateLiveData.postValue(
                    UIMessageState.StringResourceMessage(
                        false,
                        R.string.something_went_wrong
                    )
                )
            }
    }

    fun resetUIUpdate() {
        _uiMessageStateLiveData.value = UIMessageState.Empty()
    }

    companion object {
        private val red = Color.rgb(217, 83, 79)
        private val grey = Color.rgb(122, 121, 121)
        private val green = Color.rgb(92, 184, 92)
    }
}


