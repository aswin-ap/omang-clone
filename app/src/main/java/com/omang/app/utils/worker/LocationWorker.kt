package com.omang.app.utils.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.location.LocationDao
import com.omang.app.data.database.location.LocationEntity
import com.omang.app.data.repository.AnalyticsRepository
import com.omang.app.ui.base.viewmodel.BaseViewModel
import com.omang.app.utils.ViewUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber


@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val analyticsRepository: AnalyticsRepository
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val loc = LocationManger(context = context).getLocationUpdates()

        analyticsRepository.insertLocation(
            AnalyticsEntity(
                latitude = loc?.lastLocation?.latitude,
                longitude = loc?.lastLocation?.longitude,
                createdTime = ViewUtil.getCurrentEpochTime(),
                startTime = ViewUtil.getUtcTimeWithMSec()
            )
        )

        return Result.success()
    }
}