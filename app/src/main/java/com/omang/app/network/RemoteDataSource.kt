package com.omang.app.network

import com.omang.app.data.model.analytics.AnalyticsData
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.deviceUpdate.StatusRequest
import com.omang.app.data.model.feed.FeedRequest
import com.omang.app.data.model.modeMeter.MoodMeterRequest
import com.omang.app.data.model.myProfile.ProfileRequest
import com.omang.app.data.model.rating.RatingRequest
import com.omang.app.data.model.resources.ResourceRequest
import com.omang.app.data.model.techSupport.IssueRequest
import com.omang.app.data.model.test.TestSubmitRequest
import com.omang.app.ui.techSupport.rate.RateRequest
import okhttp3.MultipartBody
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val omangApiService: OmangApiService,
) {
    suspend fun registerDevice(
        imeiNo: String,
        simNo: String,
        fcmToken: String,
        secret: String,
        brand: String,
        model: String,
        appVersion: String,
        deviceOS : Int
    ) = omangApiService.registerDevice(
        imeiNo,
        simNo,
        fcmToken,
        secret,
        brand,
        model,
        appVersion,
        deviceOS
    )

    suspend fun assignUser(
        userId: Int,
        simNo: String,
        secret: String,
    ) = omangApiService.assignUser(
        userId,
        simNo,
        secret,
    )

    suspend fun getAppUpdates() = omangApiService.getUpdates()

    suspend fun getDeviceUpdates() = omangApiService.getDeviceUpdates()

    suspend fun getDeleteUpdates(deleteUpdateRequest: DeleteUpdateRequest) =
        omangApiService.getDeleteUpdates(deleteUpdateRequest)

    suspend fun statusUpdate(statusRequest: StatusRequest) =
        omangApiService.statusUpdate(statusRequest)

    suspend fun getSummaryData() = omangApiService.getSummaryData()

    suspend fun getSubjectData(resourceRequest: ResourceRequest) =
        omangApiService.getSubjectData(resourceRequest)

    suspend fun getWebsiteData(resourceRequest: ResourceRequest) =
        omangApiService.getWebsiteData(resourceRequest)

    /*    suspend fun getPushNotification(page: Int, limit: Int) =
            omangApiService.getPushNotification(page, limit)  */
    suspend fun getPushNotification() =
        omangApiService.getPushNotification()

    suspend fun getFeeds(classroomId: Int? = null) =
        omangApiService.getFeeds(classroomId)

    suspend fun getTickets(page: Int, limit: Int, isASC: Boolean, sortBy: String, search: String) =
        omangApiService.getTickets(page, limit, if (isASC) "ASC" else "DESC", sortBy, search)

    suspend fun postIssue(issueRequest: IssueRequest) =
        omangApiService.postIssue(issueRequest)

    suspend fun postMcqResult(testSubmitRequest: TestSubmitRequest) =
        omangApiService.postMcqResult(testSubmitRequest)

    suspend fun imageUpload(imgPath: MultipartBody.Part) =
        omangApiService.imageUpload(imgPath)

    suspend fun getExploreWebData(resourceRequest: ResourceRequest) =
        omangApiService.getExploreWebData(resourceRequest)

    suspend fun getIndividualWebData(webId: Int, isExplore: Boolean) =
        omangApiService.getIndividualWebData(webId, isExplore)

    suspend fun getMcqData() =
        omangApiService.getMcqData()

    suspend fun getMyLibraryResources(resourceRequest: ResourceRequest) =
        omangApiService.getMyLibraryData(resourceRequest)

    suspend fun postRating(id: String, request: RateRequest) =
        omangApiService.postRating(id, request)

    suspend fun updateProfilePic(profileRequest: ProfileRequest) =
        omangApiService.updateProfilePic(profileRequest)

    suspend fun getMyWebPlatformData(resourceRequest: ResourceRequest) =
        omangApiService.getExploreWebData(resourceRequest)


    /*
    * chat */
    suspend fun getChatHistory(roomId: Int, page: Int, limit: Int) =
        omangApiService.getChatHistory(roomId, page, limit)


    suspend fun submitMoodMeterData(moodMeterRequest: MoodMeterRequest) =
        omangApiService.submitMoodMeterData(moodMeterRequest)

    suspend fun deleteNotifications(deleteUpdateRequest: DeleteUpdateRequest?) =
        omangApiService.deleteNotification(deleteUpdateRequest ?: DeleteUpdateRequest())

    suspend fun postFeed(feedRequest: FeedRequest) =
        omangApiService.postFeed(feedRequest)


    suspend fun postAnalytics(analyticsData: AnalyticsData) =
        omangApiService.postAnalytics(analyticsData)

    suspend fun getDiagnosisStatus() = omangApiService.getDiagnosisStatus()
    suspend fun getProfileUpdate() = omangApiService.getProfileUpdate()
    suspend fun postResourceRating(ratingRequest: RatingRequest) = omangApiService.postResourceRating(ratingRequest)
}