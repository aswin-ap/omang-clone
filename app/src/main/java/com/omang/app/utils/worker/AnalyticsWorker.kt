package com.omang.app.utils.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.model.analytics.AnalyticItem
import com.omang.app.data.model.analytics.AnalyticsData
import com.omang.app.data.repository.AnalyticsRepository
import com.omang.app.dataStore.SharedPref
import com.omang.app.network.RemoteDataSource
import com.omang.app.utils.ViewUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.greenrobot.eventbus.EventBus
import java.util.Calendar

/*
* this work manager is used for sending the analytics data to the server
* */
@HiltWorker
class AnalyticsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dataSource: RemoteDataSource,
    private val analyticsRepository: AnalyticsRepository,
    private val sharedPref: SharedPref
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        analyticsSendLogic()
        return Result.success()
    }

    private suspend fun analyticsSendLogic() {
        val analyticsData = analyticsRepository.getAllAnalytics()
        if (analyticsData.isEmpty()) {
            return
        }

        val analyticList = analyticsData.map { aE ->
            AnalyticItem(
                id = aE.id,
                type = aE.type,
                createdTime = aE.createdTime,
                startTime = aE.startTime,
                endTime = aE.endTime,
                resourceId = aE.resourceId,
                webPlatformId = aE.webPlatformId,
                webUrl = aE.webUrl,
                classroomId = aE.classroomId,
                lessonId = aE.lessonId,
                unitId = aE.unitId,
                psmId = aE.psmId,
                menu = aE.menu,
                latitude = aE.latitude,
                longitude = aE.longitude,
                logs = aE.logs,
            )
        }

        if (analyticList.isNotEmpty()) {
            val response =
                dataSource.postAnalytics(AnalyticsData(analytics = analyticList))
            if (response.isSuccessful) {
                analyticsRepository.clearAnalytics()
                sharedPref.analyticsFlag = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            } else {
                analyticsRepository.insertErrorLog(
                    AnalyticsEntity(
                        createdTime = ViewUtil.getCurrentEpochTime(),
                        startTime = ViewUtil.getUtcTimeWithMSec(),
                        logs = response.raw().toString()
                    )
                )
            }
        }
    }

}