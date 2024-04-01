package com.omang.app.utils.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omang.app.data.model.test.Mcq
import com.omang.app.data.model.test.TestSubmitRequest
import com.omang.app.data.repository.ResourceRepository
import com.omang.app.network.RemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber
import kotlin.math.roundToInt

/*
* this work manager is used for sending the offline test data to the server
* */
@HiltWorker
class TestWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dataSource: RemoteDataSource,
    private val resourceRepository: ResourceRepository

) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val testResults = resourceRepository.getAllAttemptedOfflineTests()
        if (testResults.isEmpty()) {
            Timber.tag("Test").d("Test is empty")
            return Result.failure()
        }

        val mcqList = testResults.map { test ->
            Mcq(
                id = test.generalMcqId,
                score = test.score.roundToInt(),
                attemptsCount = test.attemptsCount,
                correctAttempts = test.correctAttempts,
                wrongAttempts = test.wrongAttempts,
                questions = test.questions,
                attendedOn = test.createdOn,
                mcqStudId = null,
                classroom = test.classroomId,
                unit = test.unitId
            )
        }
        TestSubmitRequest(
            mcqs = mcqList
        ).also {
            Timber.tag("Test").d("TestSubmitRequest: $it")
            val response = dataSource.postMcqResult(it)
            if (response.isSuccessful) {
                Timber.tag("Test").d("Test Submitted successfully")
                resourceRepository.setTestsStatusToAttempted(response.body()?.mcqResultData!!)
            }
        }
        return Result.success()
    }
}