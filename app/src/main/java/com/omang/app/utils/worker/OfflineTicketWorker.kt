package com.omang.app.utils.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsDao
import com.omang.app.data.model.techSupport.IssueRequest
import com.omang.app.data.model.techSupport.NavigationModel
import com.omang.app.data.model.techSupport.Ticket
import com.omang.app.data.repository.AnalyticsRepository
import com.omang.app.network.RemoteDataSource
import com.omang.app.utils.ViewUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class OfflineTicketWorker @AssistedInject
constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val remoteDataSource: RemoteDataSource,
    private val analyticsRepository: AnalyticsRepository,
    private val daoTickets: TicketsDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val offlineTickets = daoTickets.getAllLocalTickets(true)

        Timber.tag("worker-report-ticket").e("inside worker")
        for (ticket in offlineTickets) {
            Timber.tag("worker-report-ticket")
                .v("ticket id : ${ticket.id} -> desc : ${ticket.issue} --> ticketPostedAt ${ticket.createdAt}")
        }

        val gson = Gson()
        val ticket = offlineTickets.map {
            val type = object : TypeToken<List<NavigationModel>>() {}.type
            Ticket(
                issue = it.issue,
                email = it.email,
                phone = it.phone,
                ticketPostedAt = it.createdAt,
                appTicketId = it.id,
                latestActivities = gson.fromJson(it.navigation, type)
            )
        }
        if (ticket.isNotEmpty()) {
            val response = remoteDataSource.postIssue(IssueRequest(ticket))
            if (response.isSuccessful) {
                val ids = offlineTickets.map { it.id }
                daoTickets.deleteTicketById(ids)
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
        return Result.success()
    }
}

