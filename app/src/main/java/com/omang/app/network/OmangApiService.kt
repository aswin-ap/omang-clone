package com.omang.app.network

import com.google.gson.JsonObject
import com.omang.app.data.model.analytics.AnalyticsData
import com.omang.app.data.model.appupdate.AppUpdateResponse
import com.omang.app.data.model.deleteUpdates.DeleteUpdateRequest
import com.omang.app.data.model.deleteUpdates.DeleteUpdateResponse
import com.omang.app.data.model.deviceUpdate.StatusRequest
import com.omang.app.data.model.explore.ExploreDataResponse
import com.omang.app.data.model.explore.ExploreIndividualWebDataResponse
import com.omang.app.data.model.myProfile.ProfileRequest
import com.omang.app.data.model.feed.FeedsResponse
import com.omang.app.data.model.registerDevice.NetworkRegistrationResponse
import com.omang.app.data.model.resources.LibraryResourceResponse
import com.omang.app.data.model.resources.ResourceRequest
import com.omang.app.data.model.resources.ResourceResponse
import com.omang.app.data.model.resources.WebsiteResourceResponse
import com.omang.app.data.model.summary.SummaryResponse
import com.omang.app.data.model.techSupport.ImageUpload
import com.omang.app.data.model.techSupport.IssueRequest
import com.omang.app.data.model.techSupport.IssueResponse
import com.omang.app.data.model.techSupport.TicketResponse
import com.omang.app.data.model.test.McqResponse
import com.omang.app.data.model.test.TestSubmitRequest
import com.omang.app.data.model.test.TestSubmitResponse
import com.omang.app.data.model.updates.DeviceUpdatesResponse
import com.omang.app.data.model.userAssign.UserAssignResponse
import com.omang.app.ui.techSupport.rate.RateRequest
import com.omang.app.ui.techSupport.rate.RateResponse
import com.omang.app.data.model.chat.message.ChatResponse
import com.omang.app.data.model.feed.FeedPostResponse
import com.omang.app.data.model.feed.FeedRequest
import com.omang.app.data.model.modeMeter.MoodMeterRequest
import com.omang.app.data.model.profile.ProfileResponse
import com.omang.app.data.model.rating.RatingRequest
import com.omang.app.data.model.rating.RatingResponse
import com.omang.app.ui.admin.model.DiagnosisResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface OmangApiService {

    @FormUrlEncoded
    @POST("device/register")
    suspend fun registerDevice(
        @Field("imeiNo") imeiNo: String,
        @Field("simNo") simNo: String,
        @Field("fcmToken") fcmToken: String,
        @Field("secret") secret: String,
        @Field("brand") brand: String,
        @Field("model") model: String,
        @Field("appVersion") appVersion: String,
        @Field("androidVersion") deviceOS : Int
    ): Response<NetworkRegistrationResponse>

    @FormUrlEncoded
    @POST("user/assign")
    suspend fun assignUser(
        @Field("userId") userId: Int,
        @Field("simNo") simNo: String,
        @Field("secret") secret: String,
    ): Response<UserAssignResponse>

    @GET("device/app-update")
    suspend fun getUpdates(
    ): Response<AppUpdateResponse>

    @PATCH("device/settings")
    suspend fun statusUpdate(@Body statusRequest: StatusRequest): Response<JsonObject>

    @GET("updates")
    suspend fun getDeviceUpdates(): Response<DeviceUpdatesResponse>

    @HTTP(method = "DELETE", path = "updates", hasBody = true)
    suspend fun getDeleteUpdates(@Body deleteUpdateRequest: DeleteUpdateRequest): Response<DeleteUpdateResponse>

    @GET("summary")
    suspend fun getSummaryData(): Response<SummaryResponse>

    /**
     * older version
     * */
/*    @POST("resource")
    suspend fun getSubjectData(
        @Body resourceRequest: ResourceRequest,
    ): Response<ResourceResponse>*/

    @POST("resource/v1")
    suspend fun getSubjectData(
        @Body resourceRequest: ResourceRequest,
    ): Response<ResourceResponse>

    @POST("resource")
    suspend fun getWebsiteData(
        @Body resourceRequest: ResourceRequest,
    ): Response<WebsiteResourceResponse>

/*    @GET("feeds")
    suspend fun getPushNotification(
        @Query("page") page: Int ,
        @Query("limit") limit: Int,
    ): Response<FeedsResponse>*/

    @GET("feeds")
    suspend fun getPushNotification(): Response<FeedsResponse>

/*    @GET("feeds")
    suspend fun getFeeds(
        @Query("page") page: Int ,
        @Query("limit") limit: Int,
        @Query("classroom") classroomId: Int?
    ): Response<FeedsResponse>*/

    @GET("feeds")
    suspend fun getFeeds(
        @Query("classroom") classroomId: Int?
    ): Response<FeedsResponse>

    @GET("tech-support")
    suspend fun getTickets(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sortOrder") sortOrder: String,
        @Query("sortBy") sortBy: String,
        @Query("search") search: String,
    ): Response<TicketResponse>

    @POST("tech-support")
    suspend fun postIssue(
        @Body issueRequest: IssueRequest,
    ): Response<IssueResponse>

    @Multipart
    @POST("uploads")
    suspend fun imageUpload(
        @Part imgPath: MultipartBody.Part
    ): Response<ImageUpload>

    @POST("mcq/result")
    suspend fun postMcqResult(
        @Body testSubmitRequest: TestSubmitRequest,
    ): Response<TestSubmitResponse>

    @POST("resource")
    suspend fun getExploreWebData(
        @Body resourceRequest: ResourceRequest,
    ): Response<ExploreDataResponse>

    @POST("resource")
    suspend fun getMyLibraryData(
        @Body resourceRequest: ResourceRequest,
    ): Response<LibraryResourceResponse>

    @GET("resource/website/{id}")
    suspend fun getIndividualWebData(
        @Path("id") webId: Int,
        @Query("explore") isExplore: Boolean
    ): Response<ExploreIndividualWebDataResponse>

    @GET("resource/mcq")
    suspend fun getMcqData(
        @Query("type") type: String = "all"
    ): Response<McqResponse>

    @PATCH("user")
    suspend fun updateProfilePic(
        @Body profileRequest: ProfileRequest
    ): Response<UserAssignResponse>

    @PATCH("tech-support/rating/{id}")
    suspend fun postRating(
        @Path("id") id: String,
        @Body request: RateRequest
    ): Response<RateResponse>

    @GET("chat/messages")
    suspend fun getChatHistory(
        @Query("roomId") roomId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
    ): Response<ChatResponse>

    @PATCH("user")
    suspend fun submitMoodMeterData(
        @Body moodMeterRequest: MoodMeterRequest
    ):Response<UserAssignResponse>

    @HTTP(method = "DELETE", path = "feeds", hasBody = true)
    suspend fun deleteNotification(@Body deleteUpdateRequest: DeleteUpdateRequest): Response<DeleteUpdateResponse>

    @POST("feeds")
    suspend fun postFeed(
        @Body feedRequest: FeedRequest,
    ): Response<FeedPostResponse>

    @POST("analytics")
    suspend fun postAnalytics(
        @Body analyticsData: AnalyticsData,
    ): Response<JsonObject>

    @GET("device/health-check")
    suspend fun getDiagnosisStatus(): Response<DiagnosisResponse>
    @GET("user/profile")
    suspend fun getProfileUpdate(): Response<ProfileResponse>
    @POST("feedback/rating")
    suspend fun postResourceRating(@Body ratingRequest: RatingRequest): Response<RatingResponse>
}

