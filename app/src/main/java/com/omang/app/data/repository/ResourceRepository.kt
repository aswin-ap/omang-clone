package com.omang.app.data.repository

import com.omang.app.data.NetworkResult
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.LocalDataSource
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.myClassroom.relation.classroomLesson.ClassroomLessonRelationEntity
import com.omang.app.data.database.myClassroom.relation.classroomResource.ClassroomResourcesRelationEntity
import com.omang.app.data.database.myClassroom.relation.classroomWebsite.ClassroomWebsiteRelationEntity
import com.omang.app.data.database.myClassroom.relation.lessonWeblinks.LessonWeblinksMappingEntity
import com.omang.app.data.database.myClassroom.relation.unitLesson.UnitLessonRelationEntity
import com.omang.app.data.database.myClassroom.relation.unitTest.UnitTestRelationEntity
import com.omang.app.data.database.myClassroom.unit.UnitEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity
import com.omang.app.data.database.myClassroom.unit.lessons.createdBy.CreatedByEntity
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.database.navigation.MobileNavigationEntity
import com.omang.app.data.database.psm.PsmEntity
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.database.test.TestEntity
import com.omang.app.data.model.chat.message.ChatResponse
import com.omang.app.data.model.explore.Added
import com.omang.app.data.model.explore.ExploreDataResponse
import com.omang.app.data.model.explore.ExploreIndividualWebDataResponse
import com.omang.app.data.model.feed.FeedsResponse
import com.omang.app.data.model.feed.NotificationItem
import com.omang.app.data.model.feed.asClassroomFeedEntity
import com.omang.app.data.model.feed.asEntity
import com.omang.app.data.model.modeMeter.ModeMeterEntity
import com.omang.app.data.model.modeMeter.MoodDataEntity
import com.omang.app.data.model.modeMeter.MoodMeterRequest
import com.omang.app.data.model.myProfile.ProfileRequest
import com.omang.app.data.model.rating.RatingRequest
import com.omang.app.data.model.rating.RatingResponse
import com.omang.app.data.model.resources.CreatedBy
import com.omang.app.data.model.resources.Data
import com.omang.app.data.model.resources.LibraryResourceResponse
import com.omang.app.data.model.resources.MediaItem
import com.omang.app.data.model.resources.ResourceRequest
import com.omang.app.data.model.resources.ResourceResponse
import com.omang.app.data.model.resources.UnitsItem
import com.omang.app.data.model.resources.WebsiteItem
import com.omang.app.data.model.resources.WebsiteResourceResponse
import com.omang.app.data.model.summary.MoodMeter
import com.omang.app.data.model.summary.MyClassroomItem
import com.omang.app.data.model.summary.MyLibraryItem
import com.omang.app.data.model.summary.SummaryResponse
import com.omang.app.data.model.summary.asEntity
import com.omang.app.data.model.techSupport.ImageUpload
import com.omang.app.data.model.techSupport.IssueRequest
import com.omang.app.data.model.techSupport.IssueResponse
import com.omang.app.data.model.techSupport.NavigationModel
import com.omang.app.data.model.techSupport.TechSupportsItem
import com.omang.app.data.model.techSupport.Ticket
import com.omang.app.data.model.techSupport.TicketResponse
import com.omang.app.data.model.techSupport.asEntity
import com.omang.app.data.model.test.McqResponse
import com.omang.app.data.model.test.TestSubmitRequest
import com.omang.app.data.model.test.TestSubmitResponse
import com.omang.app.data.model.test.asEntityOptions
import com.omang.app.data.model.test.toAttemptedQuestionList
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.data.model.updates.DetailsItem
import com.omang.app.data.model.userAssign.Doe
import com.omang.app.data.model.userAssign.UserAssignResponse
import com.omang.app.network.RemoteDataSource
import com.omang.app.ui.techSupport.rate.RateRequest
import com.omang.app.ui.techSupport.rate.RateResponse
import com.omang.app.ui.test.MCQType
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.connectivity.InternetSpeedChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import org.json.JSONObject
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class ResourceRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val analyticsRepository: AnalyticsRepository
) {

    @Inject
    lateinit var internetSpeedChecker: InternetSpeedChecker

    fun getSummaryData(): Flow<NetworkResult<SummaryResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val summaryResponse = remoteDataSource.getSummaryData()
            if (summaryResponse.isSuccessful) {
                summaryResponse.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }

            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = summaryResponse.raw().toString()
                )
            )

            return@flow emit(
                NetworkResult.Error(
                    summaryResponse.message(), summaryResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun insertMyLibrary(myLibraryItems: List<MyLibraryItem>) {
        val myLibraryEntities = myLibraryItems.map { resourcesEntity ->
            resourcesEntity.asEntity()
        }
        localDataSource.insertMyLibrary(myLibraryEntities)
    }

    suspend fun updateIsBonusToFalse(resourceId: Int) =
        localDataSource.updateIsBonusToFalse(resourceId)

    suspend fun deleteResource(resourceId: Int) = localDataSource.deleteResource(resourceId)

    suspend fun insertMyWebPlatform(myWebPlatformItems: List<Added>) {
        val exploreEntity = myWebPlatformItems.map { myWebPlatformItem ->
            MyWebPlatformEntity(
                id = myWebPlatformItem.id,
                name = myWebPlatformItem.name,
                url = myWebPlatformItem.url,
                logo = myWebPlatformItem.logo,
                altUrl = null,
                timeStamp = null,
                type = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value,
                isDeleted = 0
            )
        }
        localDataSource.insertMyWebPlatform(exploreEntity)
    }

    suspend fun insertExploreData(myWebPlatformItems: List<Added>) {
        val exploreEntity = myWebPlatformItems.map { myWebPlatformItem ->
            MyWebPlatformEntity(
                id = myWebPlatformItem.id,
                name = myWebPlatformItem.name,
                url = myWebPlatformItem.url,
                logo = myWebPlatformItem.logo,
                altUrl = null,
                timeStamp = null,
                type = DBConstants.WebsiteContent.EXPLORE.value,
                isDeleted = 0
            )
        }
        localDataSource.insertMyWebPlatform(exploreEntity)
    }

    suspend fun insertExploreDataFromUpdate(myWebPlatformItems: List<DetailsItem?>?) {
        try {
            val exploreEntity = myWebPlatformItems!!.map { myWebPlatformItem ->
                MyWebPlatformEntity(
                    id = myWebPlatformItem!!.resource,
                    name = myWebPlatformItem.name,
                    url = null,
                    logo = myWebPlatformItem.logo,
                    altUrl = null,
                    timeStamp = null,
                    type = DBConstants.WebsiteContent.EXPLORE.value,
                    isDeleted = 0
                )
            }

            localDataSource.insertMyWebPlatform(exploreEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    suspend fun insertMyWebPlatformFromUpdate(myWebPlatformItems: List<DetailsItem?>?) {
        try {
            val exploreEntity = myWebPlatformItems!!.map { myWebPlatformItem ->
                MyWebPlatformEntity(
                    id = myWebPlatformItem!!.resource,
                    name = myWebPlatformItem.name,
                    url = null,
                    logo = myWebPlatformItem.logo,
                    altUrl = null,
                    timeStamp = null,
                    type = DBConstants.WebsiteContent.EXPLORE.value,
                    isDeleted = 0
                )
            }

            localDataSource.insertMyWebPlatform(exploreEntity)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getWebsiteByClassroomID(classroomId: Int): List<MyWebPlatformEntity> =
        localDataSource.getWebsiteByClassroomID(classroomId)

    suspend fun removeFavorite(id: Int): Unit = localDataSource.removeFavorite(id)

    suspend fun setFavorite(id: Int): Unit = localDataSource.setFavorite(id)

    fun fetchFavItems(): Flow<List<MyWebPlatformEntity>> = localDataSource.fetchFavItems()


    suspend fun setTimeStamp(id: Int, timeData: String): Unit =
        localDataSource.setTimeStamp(id, timeData)

    suspend fun insertClassroomWebsites(websiteItems: List<WebsiteItem>, type: Int) {
        val websiteEntities = websiteItems.map { websiteItem ->
            MyWebPlatformEntity(
                id = websiteItem.id,
                name = websiteItem.name,
                logo = websiteItem.logo,
                url = null,
                altUrl = null,
                timeStamp = null,
                type = type,
                isDeleted = 0
            )
        }
        localDataSource.insertMyWebPlatform(websiteEntities)

    }

    suspend fun getMyWebPlatforms(): List<MyWebPlatformEntity> =
        localDataSource.fetchAllMyWebPlatforms()

    fun getExploreItems(): Flow<List<MyWebPlatformEntity>> = localDataSource.getExploreItems()

    suspend fun getMyWebPlatformItems(): Flow<List<MyWebPlatformEntity>> =
        localDataSource.getMyWebPlatformItems()

    suspend fun getMyWebPlatforms(websiteIds: List<Int>): List<MyWebPlatformEntity> =
        localDataSource.getWebsitesByIds(websiteIds)

    suspend fun insertMyClassroom(myClassrooms: List<MyClassroomItem>) {
        val myClassroomEntities = myClassrooms.map { myClassroomItem ->
            myClassroomItem.asEntity()
        }
        myClassroomEntities.forEach { myClassroomEntity ->
            localDataSource.insertMyClassroom(myClassroomEntity)
        }
    }

    suspend fun insertClassroomWithResourcesRelation(data: Data) {
        val classroomId = data.id

        val classroomResourcesRelationEntities = mutableListOf<ClassroomResourcesRelationEntity>()

        for (book in data.added.books) {
            Timber.e("inserting book classroom : $classroomId - resource : ${book.id}")
            classroomResourcesRelationEntities.add(
                ClassroomResourcesRelationEntity(
                    classroomId = classroomId, resourceId = book.id
                )
            )
        }

        for (video in data.added.videos) {
            Timber.e("inserting video classroom : $classroomId - resource : ${video.id}")
            classroomResourcesRelationEntities.add(
                ClassroomResourcesRelationEntity(
                    classroomId = classroomId, resourceId = video.id
                )
            )
        }

        localDataSource.insertClassroomResourcesRelations(classroomResourcesRelationEntities)

    }

    suspend fun deleteWebPlatform(resourceId: Int) = localDataSource.deleteWebPlatform(resourceId)
    suspend fun deleteMyWebPlatform(resourceId: Int) =
        localDataSource.deleteMyWebPlatform(resourceId)

    suspend fun deleteClassroomResourceRelation(classroomId: Int, resourceId: Int): Int =
        localDataSource.deleteClassroomResourceRelation(classroomId, resourceId)

    fun getMyClassrooms(): Flow<List<MyClassroomEntity>> = localDataSource.fetchAllClassrooms()
    fun getMyClubs(): Flow<List<MyClassroomEntity>> = localDataSource.fetchAllClubs()
    suspend fun getMyClubsList(): List<String> = localDataSource.fetchAllClubsList()
    suspend fun getMyClassroomsList(): List<String> = localDataSource.fetchAllClassroomsList()
    suspend fun fetchMyClassroomsAndClubsList() = localDataSource.fetchMyClassroomsAndClubsList()

    suspend fun getUnitsWithLessonsByClassroomId(classroomId: Int): List<UnitWithLessons> =
        localDataSource.getUnitsWithLessonsByClassroomId(classroomId)

    suspend fun fetchLessonIds(classroomId: Int, lessonId: Int): LessonWeblinksMappingEntity? =
        localDataSource.fetchLessonIds(classroomId, lessonId)


    fun getSubjectData(id: Int): Flow<NetworkResult<ResourceResponse>> {
        return flow {

            val resourceRequest = ResourceRequest()
            resourceRequest.category = "classroomChanges"
            resourceRequest.resourceId = id

            val subjectResponse = remoteDataSource.getSubjectData(resourceRequest)

            if (subjectResponse.isSuccessful) {
                subjectResponse.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = subjectResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    subjectResponse.message(), subjectResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun postRating(
        id: String, ratingValue: Float, comment: String
    ): Flow<NetworkResult<RateResponse>> {
        return flow {

            val request = RateRequest(rating = ratingValue.toInt(), feedback = comment)


            val response = remoteDataSource.postRating(id, request)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getWebsiteData(id: Int): Flow<NetworkResult<WebsiteResourceResponse>> {
        return flow {

            val resourceRequest = ResourceRequest()
            resourceRequest.category = "webPlatform"
            resourceRequest.resourceId = id

            val websiteResponse = remoteDataSource.getWebsiteData(resourceRequest)

            if (websiteResponse.isSuccessful) {
                websiteResponse.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = websiteResponse.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    websiteResponse.message(), websiteResponse.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun insertResources(mediaItems: List<MediaItem>) {
        val resourceEntities = mediaItems.map { mediaData ->
            Timber.e(" mediaData.id : ${mediaData.id}")
            ResourcesEntity(
                id = mediaData.id,
                name = mediaData.name,
                description = mediaData.description,
                logo = mediaData.logo,
                file = mediaData.file,
                type = mediaData.type,
                isBonus = DBConstants.BonusContent.FALSE.value
            )
        }
        localDataSource.insertAllResources(resourceEntities)

    }

    suspend fun getResourcesByClassroomId(classroomId: Int): List<ResourcesEntity> =
        localDataSource.getResourcesByClassroomId(classroomId)

    suspend fun fetchMyLibrary(): List<ResourcesEntity> = localDataSource.fetchMyLibrary()

    /*
    * inserting unit */
    suspend fun insertUnits(unitsItems: List<UnitsItem>) {
        val lessonEntities = unitsItems.map { unitsItem ->
            UnitEntity(
                id = unitsItem.id,
                name = unitsItem.name,
                description = unitsItem.description,
                objective = unitsItem.objective

            )
        }
        localDataSource.insertAllUnits(lessonEntities)
    }

    private fun buildFullName(createdBy: CreatedBy?): String {
        val firstName = createdBy?.firstName ?: ""
        val lastName = createdBy?.lastName ?: ""

        return "$firstName $lastName".trim()
    }

    /*
    * inserting lessons*/
    suspend fun insertLessons(data: Data) {
        data.added.units.forEach { unit ->
            val lessonEntities = unit.lessons.map { lessonItem ->
                Timber.e("lesson id : ${lessonItem.id}")
                LessonsEntity(
                    id = lessonItem.id,
                    name = lessonItem.name,
                    description = lessonItem.description ?: "",
                    logo = lessonItem.logo,
                    file = lessonItem.file,
                    mimeType = lessonItem.file.substringAfterLast('.'),
                    createdBy = if (lessonItem.createdBy == null) CreatedByEntity(
                        name = "", avatar = ""
                    ) else CreatedByEntity(
                        name = buildFullName(lessonItem.createdBy),
                        avatar = lessonItem.createdBy.avatar ?: ""
                    )
                )
            }
            localDataSource.insertAllLessons(lessonEntities)

            val unitLessonRelationEntities = mutableListOf<UnitLessonRelationEntity>()
            // Map each lesson to its corresponding unit using the IDs
            for (lesson in unit.lessons) {
                unitLessonRelationEntities.add(
                    UnitLessonRelationEntity(
                        unitId = unit.id,
                        lessonId = lesson.id,
                        classroomId = data.id ,// classroom id
                    )
                )

                // inserting lesson's weblinks
                insertClassroomWebsites(
                    lesson.webPlatforms, DBConstants.WebsiteContent.WEBLINKS.value
                )

                /*mapping lesson and weblinks*/
                val lessonWeblinksMappingEntities = mutableListOf<LessonWeblinksMappingEntity>()
                val weblinksIds = mutableListOf<Int>()
                for (weblinks in lesson.webPlatforms) {
                    weblinksIds.add(weblinks.id)
                }
                lessonWeblinksMappingEntities.add(
                    LessonWeblinksMappingEntity(
                        classroomId = data.id, lessonId = lesson.id, websiteIds = weblinksIds
                    )
                )

                localDataSource.insertLessonWeblinksMapping(lessonWeblinksMappingEntities)
            }

            localDataSource.insertUnitLessonRelation(unitLessonRelationEntities)

            val classroomLessonRelationEntities = mutableListOf<ClassroomLessonRelationEntity>()
            for (lesson in unit.lessons) {
                classroomLessonRelationEntities.add(
                    ClassroomLessonRelationEntity(
                        classroomId = data.id, lessonId = lesson.id
                    )
                )
            }

            localDataSource.insertClassroomLesson(classroomLessonRelationEntities)

        }
    }

    /*
    * classroom website relation*/
    suspend fun insertClassroomWebsiteRelation(data: Data) {
        val classroomId = data.id

        val classroomWebsiteRelationEntity = mutableListOf<ClassroomWebsiteRelationEntity>()

        for (website in data.added.webPlatforms) {
            Timber.e("inserting website classroom : $classroomId - resource : ${website.id}")
            classroomWebsiteRelationEntity.add(
                ClassroomWebsiteRelationEntity(
                    classroomId = classroomId, websiteId = website.id
                )
            )
        }

        localDataSource.insertClassroomWebsiteRelation(classroomWebsiteRelationEntity)

    }

    suspend fun deleteClassroomWebsiteRelation(classroomId: Int, websiteId: Int): Int =
        localDataSource.deleteClassroomWebsiteRelation(classroomId, websiteId)

    /*
    * Push notification*//*    fun getPushNotificationsLog(page: Int, limit: Int): Flow<NetworkResult<FeedsResponse>> {
            return flow {
                emit(NetworkResult.Loading())
                val response =
                    remoteDataSource.getPushNotification(page, limit)

                if (response.isSuccessful) {
                    response.body()?.let { data ->
                        return@flow emit(NetworkResult.Success(data))
                    }
                }
                return@flow emit(
                    NetworkResult.Error(
                        response.message(),
                        response.code()
                    )
                )
            }.flowOn(Dispatchers.IO).catch { exceptions ->
                emit(NetworkResult.Failure(exceptions))
            }
        }*/

    fun getPushNotificationsLog(): Flow<NetworkResult<FeedsResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val response = remoteDataSource.getPushNotification()

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getFeedsByClassroomId(
        page: Int, limit: Int = 20, classroomId: Int
    ): Flow<NetworkResult<FeedsResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val response = remoteDataSource.getFeeds(classroomId)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun insertPushNotificationLog(notifications: List<NotificationItem>) {
        val notificationEntities = notifications.map { notificationEntity ->
            notificationEntity.asEntity()
        }
        localDataSource.insertNotification(notificationEntities)
    }

    fun getAllNotifications() = localDataSource.getAllNotifications()

    /*
    * Unit test table operations*/
    suspend fun insertUnitTests(data: Data) {
        val questionEntities = mutableListOf<QuestionEntity>()
        val testEntities = mutableListOf<TestEntity>()
        val unitTestRelationEntities = mutableListOf<UnitTestRelationEntity>()

        data.added.units.forEach { unit ->
            unit.mcqs.map { mcqItem ->
                testEntities.add(
                    TestEntity(
                        generalMcqId = mcqItem.id,
                        classroomId = data.id,
                        unitId = unit.id,
                        startTime = "",
                        endTime = "",
                        createdOn = "",
                        name = mcqItem.name,
                        mcqType = DBConstants.TestContent.UNIT.value,
                        instructions = mcqItem.instructions,
                        questionsId = mcqItem.questions.map { it.id },
                        isSentToApi = DBConstants.IsTestSentToApi.FALSE.value
                    )
                )

                unitTestRelationEntities.add(
                    UnitTestRelationEntity(
                        classroomId = data.id,
                        unitId = unit.id,
                        testId = mcqItem.id,
                        isAttempted = DBConstants.IsTestAttempted.FALSE.value,
                        testStatus = DBConstants.TestStatus.NOT_ATTEMPTED.value
                    )
                )

                mcqItem.questions.forEach { questionItem ->
                    questionEntities.add(
                        QuestionEntity(
                            id = questionItem.id,
                            question = questionItem.question,
                            score = questionItem.score,
                            questionType = questionItem.questionType,
                            questionUrl = questionItem.questionUrl,
                            options = questionItem.options,
                        )
                    )
                }
            }
        }

        Timber.tag("test insertion").d(
            "questionEntities $questionEntities, testEntities $testEntities" + "unitTestRelationEntities $unitTestRelationEntities"
        )

        localDataSource.insertQuestions(questionEntities)
        localDataSource.insertTestList(testEntities)
        localDataSource.insertUnitTestMapping(unitTestRelationEntities)
    }

    suspend fun insertGeneralMcqs(data: McqResponse.Data) {
        val testEntities = mutableListOf<TestEntity>()
        val questionEntities = mutableListOf<QuestionEntity>()
        //adds attempted MCQS
        data.attendedMCQs.forEach { attendedMCQ ->
            testEntities.add(
                TestEntity(
                    generalMcqId = attendedMCQ.id,
                    startTime = attendedMCQ.startTime ?: "",
                    endTime = attendedMCQ.endTime ?: "",
                    createdOn = attendedMCQ.attendedOn,
                    name = attendedMCQ.name,
                    instructions = "",
                    mcqType = if (attendedMCQ.classroomId != null) MCQType.UNIT.type else MCQType.CLASSROOM.type,
                    type = DBConstants.TestType.ATTEMPTED.value,
                    questionsId = attendedMCQ.questions.map { it.id },
                    isSentToApi = DBConstants.IsTestSentToApi.TRUE.value,
                    classroomId = attendedMCQ.classroomId,
                    unitId = attendedMCQ.unitId,
                    score = attendedMCQ.scoreObtained.toFloat(),
                    percentage = 0.0f, // TODO: check with graph,
                    attemptsCount = attendedMCQ.attemptsCount,
                    correctAttempts = attendedMCQ.correctAttempts,
                    wrongAttempts = attendedMCQ.wrongAttempts,
                    questions = attendedMCQ.questions.toAttemptedQuestionList()
                )
            )

            //adds the questions to questionEntity list
            attendedMCQ.questions.forEach { questionItem ->
                questionEntities.add(
                    QuestionEntity(
                        id = questionItem.id,
                        question = questionItem.question,
                        score = questionItem.score,
                        questionType = questionItem.questionType,
                        questionUrl = questionItem.questionURL,
                        options = questionItem.options.asEntityOptions()
                    )
                )
            }
        }

        //adds expired MCQS
        data.expiredMCQs.forEach { expiredMCQ ->
            /*     testEntities.add(
                     TestEntity(
                         generalMcqId = expiredMCQ.mcqId,
                         startTime = "",
                         endTime = "",
                         //expiredOn saved in createdOn
                         createdOn = expiredMCQ.expiredOn,
                         name = expiredMCQ.title,
                         instructions = "",
                         mcqType = MCQType.CLASSROOM.type,
                         type = DBConstants.TestType.EXPIRED.value,
                         questionsId = listOf(),
                         isSentToApi = DBConstants.IsTestSentToApi.FALSE.value
                     )
                 )*/
        }

        //adds New MCQS
        data.notAttendedMCQs.forEach { notAttendedMCQ ->
            testEntities.add(
                TestEntity(
                    generalMcqId = notAttendedMCQ.id,
                    startTime = notAttendedMCQ.startTime,
                    endTime = notAttendedMCQ.endTime,
                    createdOn = "",
                    name = notAttendedMCQ.name,
                    instructions = notAttendedMCQ.instructions,
                    mcqType = MCQType.CLASSROOM.type,
                    type = DBConstants.TestType.NEW.value,
                    questionsId = notAttendedMCQ.questions.map { it.id },
                    isSentToApi = DBConstants.IsTestSentToApi.FALSE.value,
//                    generalMcqId = notAttendedMCQ.mcqStudId
                )
            )

            //adds the questions to questionEntity list
            notAttendedMCQ.questions.forEach { questionItem ->
                questionEntities.add(
                    QuestionEntity(
                        id = questionItem.id,
                        question = questionItem.question,
                        score = questionItem.score,
                        questionType = questionItem.questionType,
                        questionUrl = questionItem.questionUrl,
                        options = questionItem.options,
                    )
                )
            }
        }

        localDataSource.insertQuestions(questionEntities)
        localDataSource.insertTestList(testEntities)
    }

    suspend fun insertClassroomTest(data: Data) {
        val testEntities = mutableListOf<TestEntity>()
        val questionEntities = mutableListOf<QuestionEntity>()

        //adds attempted MCQS
        data.added.mcqs.attendedMCQs.forEach { attendedMCQ ->
            testEntities.add(
                TestEntity(
                    generalMcqId = attendedMCQ.id,
                    startTime = attendedMCQ.startTime ?: "",
                    endTime = attendedMCQ.endTime ?: "",
                    createdOn = "",
                    name = attendedMCQ.name,
                    instructions = "",
                    mcqType = DBConstants.TestContent.CLASSROOM.value,
                    type = DBConstants.TestType.ATTEMPTED.value,
                    questionsId = attendedMCQ.questions.map { it.id },
                    isSentToApi = DBConstants.IsTestSentToApi.TRUE.value,
                    classroomId = data.id,
                    unitId = attendedMCQ.unitId,
                    score = attendedMCQ.scoreObtained.toFloat(),
                    percentage = 0.0f, // TODO: check with graph,
                    attemptsCount = attendedMCQ.attemptsCount,
                    correctAttempts = attendedMCQ.correctAttempts,
                    wrongAttempts = attendedMCQ.wrongAttempts,
                    questions = attendedMCQ.questions.toAttemptedQuestionList()
                )
            )

            //adds the questions to questionEntity list
            attendedMCQ.questions.forEach { questionItem ->
                questionEntities.add(
                    QuestionEntity(
                        id = questionItem.id,
                        question = questionItem.question,
                        score = questionItem.score,
                        questionType = questionItem.questionType,
                        questionUrl = questionItem.questionURL,
                        options = questionItem.options.asEntityOptions()
                    )
                )
            }
        }

        //adds expired MCQS
        data.added.mcqs.expiredMCQs.forEach { expiredMCQ ->
            testEntities.add(
                TestEntity(
                    generalMcqId = expiredMCQ.id,
                    classroomId = data.id,
                    startTime = "",
                    endTime = "",
                    //expiredOn saved in createdOn
                    createdOn = expiredMCQ.expiredOn,
                    name = expiredMCQ.name,
                    instructions = "",
                    mcqType = DBConstants.TestContent.CLASSROOM.value,
                    type = DBConstants.TestType.EXPIRED.value,
                    questionsId = listOf(),
                    isSentToApi = DBConstants.IsTestSentToApi.FALSE.value
                )
            )
        }

        //adds New MCQS
        data.added.mcqs.notAttendedMCQs.forEach { notAttendedMCQ ->
            testEntities.add(
                TestEntity(
                    generalMcqId = notAttendedMCQ.id,
                    classroomId = data.id,
                    startTime = notAttendedMCQ.startTime,
                    endTime = notAttendedMCQ.endTime,
                    createdOn = "",
                    name = notAttendedMCQ.name,
                    instructions = notAttendedMCQ.instructions,
                    mcqType = DBConstants.TestContent.CLASSROOM.value,
                    type = DBConstants.TestType.NEW.value,
                    questionsId = notAttendedMCQ.questions.map { it.id },
                    isSentToApi = DBConstants.IsTestSentToApi.FALSE.value,
//                    generalMcqId = notAttendedMCQ.mcqStudId
                )
            )

            //adds the questions to questionEntity list
            notAttendedMCQ.questions.forEach { questionItem ->
                questionEntities.add(
                    QuestionEntity(
                        id = questionItem.id,
                        question = questionItem.question,
                        score = questionItem.score,
                        questionType = questionItem.questionType,
                        questionUrl = questionItem.questionUrl,
                        options = questionItem.options,
                    )
                )
            }
        }

        localDataSource.insertQuestions(questionEntities)
        localDataSource.insertClassroomTest(testEntities)
    }

    suspend fun getUnitTests(unitId: Int): List<TestEntity> =
        localDataSource.getTestsByUnitId(unitId)

    fun getUnitMainMcqTest(unitId: Int, classroomId: Int): TestEntity? =
        localDataSource.getUnitMainMcq(unitId, classroomId)

    suspend fun checkIfTestPassedOrNot(unitId: Int, classroomId: Int): Int =
        localDataSource.checkIfTestPassed(unitId, classroomId)

    suspend fun getTestDetails(testId: Int): TestEntity = localDataSource.getTestById(testId)

    suspend fun getQuestionsList(id: List<Int>): List<QuestionEntity> =
        localDataSource.getTestQuestionsById(id)


    /*
    * Tech-support*/


    /*
    * get tech-support*/

    fun getTickets(
        page: Int,
        limit: Int,
        isASC: Boolean,
        sortBy: String,
        search: String,
    ): Flow<NetworkResult<TicketResponse>> {
        return flow {

            val response = remoteDataSource.getTickets(page, limit, isASC, sortBy, search)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun postIssue(
        issue: String,
        email: String,
        phone: String,
        appTicketId: String,
        ticketPostedAt: String,
        imageId: Int?,
        navigation: List<NavigationModel>
    ): Flow<NetworkResult<IssueResponse>> {
        return flow {
            val issueRequest = IssueRequest()
            issueRequest.tickets = listOf(
                Ticket(
                    issue = issue,
                    email = email,
                    phone = phone,
                    ticketPostedAt = ticketPostedAt,
                    appTicketId = appTicketId,
                    resources = if (imageId == null) listOf() else listOf(imageId),
                    latestActivities = navigation
                )
            )

            val response = remoteDataSource.postIssue(issueRequest)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun imageUpload(imgPath: MultipartBody.Part): Flow<NetworkResult<ImageUpload>> {
        return flow {
            val response = remoteDataSource.imageUpload(imgPath)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }


    suspend fun insertTickets(tickets: List<TechSupportsItem>) {
        val ticketEntities = tickets.map { ticketEntity ->
            ticketEntity.asEntity()
        }
        localDataSource.insertTickets(ticketEntities)
    }

    suspend fun insertTicket(ticket: TicketsEntity) {
        localDataSource.insertTickets(listOf(ticket))
    }

    suspend fun getTickets() = localDataSource.getTickets()

    suspend fun getUnitTestRelationEntityByUnitAndTest(testId: Int, unitId: Int, classroomId: Int) =
        localDataSource.getUniTestEntityByUnitAndTestId(
            unitId, testId, classroomId
        )

    suspend fun updateTestDetails(unitTestRelationEntity: UnitTestRelationEntity) {
        localDataSource.updateUniTestRelationEntity(unitTestRelationEntity)
    }

    suspend fun insertMobileNavigationEntry(mobileNavigationEntity: MobileNavigationEntity) {
        localDataSource.insertMobileNavigation(mobileNavigationEntity)
    }

    suspend fun getMobileNavigationEntries() = localDataSource.getMobileNavigationList()

    suspend fun getLastTenNavigation() = localDataSource.getLastTenNavigation()

    fun postMcqTestResults(testSubmitRequest: TestSubmitRequest): Flow<NetworkResult<TestSubmitResponse>> {
        return flow {
            val response = remoteDataSource.postMcqResult(testSubmitRequest)

            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getAllAttemptedOfflineTests() = localDataSource.getAllAttemptedOfflineTests()
    suspend fun setTestsStatusToAttempted(offlineTestEntity: List<TestSubmitResponse.McqResultData>) {
        localDataSource.updateListOfTestToAttemptedAndSent(
            offlineTestEntity
        )
    }

    suspend fun updateAttemptedTest(testEntity: TestEntity) = localDataSource.updateTest(testEntity)

    /*
* drop all table*/
    suspend fun clearAllDataFromDb() = localDataSource.clearAllTables()

    fun getIndividualWebData(
        webId: Int, isExplore: Boolean
    ): Flow<NetworkResult<ExploreIndividualWebDataResponse>> {
        return flow {
            val response = remoteDataSource.getIndividualWebData(webId, isExplore)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }

            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )

            val errorBody = response.errorBody()?.string()
            val errorMessage = JSONObject(
                errorBody ?: "Something went wrong.. Please contact Aunty A"
            ).optString("error", "")

            return@flow emit(
                NetworkResult.Error(
                    errorMessage, response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getExploreWebData(): Flow<NetworkResult<ExploreDataResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val exploreIdList = localDataSource.getExploreItemIds()
            val resourceRequest = ResourceRequest()
            resourceRequest.category = "webPlatform"
            resourceRequest.resourceIds = exploreIdList // TODO may need to comment this.
            Timber.tag("Resource Id").d("Explore Id: $exploreIdList")

            val response = remoteDataSource.getExploreWebData(resourceRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }

            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )

            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )

        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getMcqTestData(): Flow<NetworkResult<McqResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val response = remoteDataSource.getMcqData()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )


        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    fun getLibraryResourcesData(): Flow<NetworkResult<LibraryResourceResponse>> {
        return flow {
            val myLibraryId = localDataSource.getAllMyLibraryContentIds()
            val resourceRequest = ResourceRequest()
            resourceRequest.category = "myLibrary"
            resourceRequest.resourceIds = myLibraryId
            Timber.tag("Resource Id").d("My Library Id: $myLibraryId")
            val response = remoteDataSource.getMyLibraryResources(resourceRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
                return@flow emit(
                    NetworkResult.Error(
                        response.message(), response.code()
                    )
                )
            }

        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun getCurrentLocalTicket(localId: String): TicketsEntity {
        return localDataSource.getTicketById(localId)
    }

    suspend fun deleteTicketById(localId: String) = localDataSource.deleteTicketById(localId)

    fun getNewMcqTests() = localDataSource.getAllNewTests()
    fun getExpiredMcqTests() = localDataSource.getAllExpiredTests()
    fun getAttemptedMcqTests() = localDataSource.getAllAttemptedTests()

    fun updateProfilePic(avatarId: Int): Flow<NetworkResult<UserAssignResponse>> {
        return flow {
            emit(NetworkResult.Loading())

            val profileRequest = ProfileRequest()
            profileRequest.avatar = avatarId

            val profileResult = remoteDataSource.updateProfilePic(profileRequest)
            if (profileResult.isSuccessful) {
                profileResult.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = profileResult.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    profileResult.message(), profileResult.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }

    }

    /*
    chat*/
    fun getChatHistory(roomId: Int, page: Int, limit: Int): Flow<NetworkResult<ChatResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val response = remoteDataSource.getChatHistory(roomId, page, limit)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = response.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )

        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun deleteClassRoomOrClub(resourceId: Int): Int =
        localDataSource.deleteMyClassRoomOrClub(resourceId)

    suspend fun checkResourceExistsInClassRoom(id: Int) =
        localDataSource.checkIfResourceExistInClassroom(id)

    suspend fun getResourceById(id: Int) = localDataSource.getResourceById(id)

    fun getMyWebPlatformData(): Flow<NetworkResult<ExploreDataResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val myWebPlatformIdList = localDataSource.getMyWebPlatformItemIds()
            val resourceRequest = ResourceRequest()
            resourceRequest.category = "myWebPlatform"
            resourceRequest.resourceIds = myWebPlatformIdList
            Timber.tag("Resource Id").d("My WebPlatform Id: $myWebPlatformIdList")

            val response = remoteDataSource.getMyWebPlatformData(resourceRequest)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
                return@flow emit(
                    NetworkResult.Error(
                        response.message(), response.code()
                    )
                )
            } else {
                return@flow emit(
                    NetworkResult.Error(
                        response.message(), response.code()
                    )
                )
            }

        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun insertModeMeterData(moodMeterItem: List<MoodMeter>) {
        val moodMeterEntity = moodMeterItem.map { moodMeter ->
            ModeMeterEntity(
                emoji = moodMeter.emoji,
                colour = moodMeter.color,
                displayName = moodMeter.displayName,
                systemName = moodMeter.systemName,
                id = moodMeter.id,
            )
        }
        localDataSource.insertModeMeterData(moodMeterEntity)
    }

    suspend fun insertClickedData(formattedDateTime: String, modeId: Int, modeName: String) {

        val moodDataEntity = MoodDataEntity(
            id = modeId,
            time = formattedDateTime,
            displayName = modeName,
            columnId = UUID.randomUUID().toString()
        )

        localDataSource.insertClickedData(moodDataEntity)
    }

    //for getting data from RoomDB

    suspend fun getClickedData(): List<MoodDataEntity> = localDataSource.getClickedData()

    fun submitMoodMeterData(moodMeterRequest: MoodMeterRequest): Flow<NetworkResult<UserAssignResponse>> {
        return flow {
            emit(NetworkResult.Loading())

            val moodResult = remoteDataSource.submitMoodMeterData(moodMeterRequest)

            if (moodResult.isSuccessful) {
                moodResult.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = moodResult.raw().toString()
                )
            )
            return@flow emit(
                NetworkResult.Error(
                    moodResult.message(), moodResult.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }

    suspend fun clearMoodData() {
        localDataSource.clearMoodData()
    }

    suspend fun getRowCount(): Int = localDataSource.getRowCount()

    suspend fun updateLastUpdatedDateId(classroomId: Int) {
        localDataSource.setLastUpdatedDate(classroomId)
    }

    suspend fun getLastUpdatedDateById(classroomId: Int) =
        localDataSource.getLastUpdatedDateById(classroomId)

    suspend fun insertDoe(doeResponse: Doe?) {
        Timber.e("Doe insertion ${doeResponse?.id}")
        localDataSource.clearDoe()
        doeResponse?.let { doe ->
            MyWebPlatformEntity(
                id = doe.id,
                name = doe.title,
                logo = doe.logo,
                url = null,
                altUrl = null,
                timeStamp = null,
                type = DBConstants.WebsiteContent.DOE.value,
                isDeleted = 0
            )
        }.also {
            localDataSource.insertDoe(it!!)
        }
    }

    suspend fun getDoeDetails() = localDataSource.getDoe()

    suspend fun clearAllLocalNotifications() = localDataSource.clearLocalNotifications()

    fun getFeedsByClassroomId(classroomId: Int) = localDataSource.getFeedsByClassroomId(classroomId)

    suspend fun insertClassroomFeeds(notifications: List<NotificationItem>, classroomId: Int) {
        val notificationEntities = notifications.map { notificationEntity ->
            notificationEntity.asClassroomFeedEntity(classroomId)
        }
        localDataSource.insertClassroomFeeds(notificationEntities)
    }

    suspend fun getClassroomDetailsById(classroomId: Int) =
        localDataSource.getClassRoomById(classroomId)

    suspend fun deleteClassroomLessonMapping(classroomId: Int) =
        localDataSource.deleteClassroomLessonMapping(classroomId)

    suspend fun deleteClassroomResourceMapping(classroomId: Int) =
        localDataSource.deleteClassroomResourceMapping(classroomId)

    suspend fun deleteClassroomWebsiteMapping(classroomId: Int) =
        localDataSource.deleteClassroomWebsiteMapping(classroomId)

    suspend fun deleteClassroomLessonWebLinkMapping(classroomId: Int) =
        localDataSource.deleteClassroomLessonWebLinkMapping(classroomId)

    suspend fun deleteClassroomUnitLessonMapping(classroomId: Int) =
        localDataSource.deleteClassroomUnitLessonMapping(classroomId)

    suspend fun updateMyClassroomCount(classroomEntity: MyClassroomEntity) =
        localDataSource.updateMyClassroom(classroomEntity)

    suspend fun deleteClassroomFeed(resourceId: Int) =
        localDataSource.deleteClassroomFeed(resourceId)

    suspend fun deletePublicFeed(resourceId: Int) =
        localDataSource.deletePublicFeed(resourceId)

    suspend fun deleteClassroomLessonWebLinkMapping(classroomId: Int, lessonId: Int) =
        localDataSource.deleteClassroomLessonWebLinkMapping(classroomId, lessonId)

    suspend fun deleteClassroomLessonMapping(classroomId: Int, lessonId: Int) =
        localDataSource.deleteClassroomLessonMapping(classroomId, lessonId)

    suspend fun deleteClassroomUnitLessonMapping(classroomId: Int, lessonId: Int, unitId: Int) =
        localDataSource.deleteClassroomUnitLessonMapping(classroomId, lessonId, unitId)

    suspend fun insertPsm(psmEntity: PsmEntity) =
        localDataSource.insertPsm(psmEntity)

    suspend fun getGeneralPsm() =
        localDataSource.getGeneralPsm()

    suspend fun getPsmByClassroomId(classroomId: Int) =
        localDataSource.getPsmByClassroomId(classroomId)

    suspend fun getNotAttemptedMcqCountByClassroomAndUnit(classroomId: Int, unitInt: Int) =
        localDataSource.getNotAttemptedMcqCountByClassroomAndUnit(classroomId, unitInt)

    suspend fun deletePsmByClassroomId(classroomId: Int) =
        localDataSource.deletePsmByClassroomId(classroomId)

    suspend fun deleteGeneralPsm() =
        localDataSource.deleteGeneralPsm()

    suspend fun getClassroomTestsByType(classroomId: Int, type: Int) =
        localDataSource.getClassroomTestsByType(classroomId, type)

    suspend fun updateTestToExpired(testId: Int) =
        localDataSource.updateTestToExpired(testId)

    /*below methods are for device-analytics, keep other codes are above this line */


    /*---------------------------------------*/

    /*
    * below to perform CRUD on file table */
    suspend fun fileInsert(fileEntity: FileEntity) {
        fileEntity.createdTime = ViewUtil.getCurrentEpochTime()
        localDataSource.fileInsert(fileEntity)
    }

    suspend fun getAllFile() = localDataSource.getAllFile()
    suspend fun deleteAllFile() = localDataSource.deleteAllFile()
    fun postResourceRating(
        type: String,
        id: Int,
        rating: Float
    ): Flow<NetworkResult<RatingResponse>> {
        return flow {
            val request = RatingRequest(type = type, rating = rating, entityId = id)
            val response = remoteDataSource.postResourceRating(request)
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    return@flow emit(NetworkResult.Success(data))
                }
            }
            return@flow emit(
                NetworkResult.Error(
                    response.message(), response.code()
                )
            )
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }

    }

    suspend fun updateLessonRating(id: Int, rating: Float) {
        localDataSource.updateLessonRate(id,rating)
    }
    suspend fun getDocumentIdByClassroomId(classroomId: Int) =
        localDataSource.getDocumentIdByClassroomId(classroomId)
    suspend fun getVideoIdByClassroomId(classroomId: Int) =
        localDataSource.getVideoIdByClassroomId(classroomId)

    suspend fun getWebsiteIdByClasroomId(classRoomId: Int) =
        localDataSource.getWebsiteIdByClassroomId(classRoomId)

    suspend fun getUnitsWithLessonsIdsByClassroomId(classroomId: Int) =
        localDataSource.getUnitsWithLessonsByClassroomId(classroomId)

    suspend fun getMcqId(unitId: Int, classroomId: Int) =
        localDataSource.getMcqId(unitId, classroomId)

    suspend fun getClassroomMcqIds(classroomId: Int) =
        localDataSource.getClassroomMcqId(classroomId)
}