package com.omang.app.data.repository

import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.LocalDataSource
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.network.RemoteDataSource
import com.omang.app.utils.ViewUtil
import javax.inject.Inject

class AnalyticsRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) {

    suspend fun insertWatch(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.WATCH.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertLocation(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.LOCATION.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertSleepAwake(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.SLEEP_AWAKE.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertUnusual(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.UNUSUAL_ACTIVITIES.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertNetwork(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.NETWORK.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertErrorLog(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.ERROR_LOG.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertPSM(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.PSM.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun insertManualCleaning(analyticsEntity: AnalyticsEntity) {
        analyticsEntity.createdTime = ViewUtil.getCurrentEpochTime()
        analyticsEntity.type = DBConstants.AnalyticsType.MANUAL_CLEANUP.value
        localDataSource.insertAnalytics(analyticsEntity)
    }

    suspend fun getAllAnalytics() = localDataSource.getAllAnalytics()

    suspend fun clearAnalytics() = localDataSource.clearAnalytics()

}
