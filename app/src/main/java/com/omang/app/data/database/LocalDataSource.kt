package com.omang.app.data.database

import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.dataAnalytics.DeviceAnalyticsDao
import com.omang.app.data.database.feed.ClassroomFeedDao
import com.omang.app.data.database.feed.ClassroomFeedEntity
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.data.database.feed.PublicFeedDao
import com.omang.app.data.database.file.FileDao
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.location.LocationDao
import com.omang.app.data.database.myClassroom.ContentContentDao
import com.omang.app.data.database.myClassroom.MyClassroomDao
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.myClassroom.relation.classroomLesson.ClassroomLessonRelationDao
import com.omang.app.data.database.myClassroom.relation.classroomLesson.ClassroomLessonRelationEntity
import com.omang.app.data.database.myClassroom.relation.classroomResource.ClassroomResourcesRelationDao
import com.omang.app.data.database.myClassroom.relation.classroomResource.ClassroomResourcesRelationEntity
import com.omang.app.data.database.myClassroom.relation.classroomWebsite.ClassroomWebsiteRelationDao
import com.omang.app.data.database.myClassroom.relation.classroomWebsite.ClassroomWebsiteRelationEntity
import com.omang.app.data.database.myClassroom.relation.lessonWeblinks.LessonWeblinksMappingDao
import com.omang.app.data.database.myClassroom.relation.lessonWeblinks.LessonWeblinksMappingEntity
import com.omang.app.data.database.myClassroom.relation.unitLesson.UnitLessonRelationDao
import com.omang.app.data.database.myClassroom.relation.unitLesson.UnitLessonRelationEntity
import com.omang.app.data.database.myClassroom.relation.unitTest.UnitTestRelationDao
import com.omang.app.data.database.myClassroom.relation.unitTest.UnitTestRelationEntity
import com.omang.app.data.database.myClassroom.unit.UnitDao
import com.omang.app.data.database.myClassroom.unit.UnitEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonDao
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformDao
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.database.navigation.MobileNavDao
import com.omang.app.data.database.navigation.MobileNavigationEntity
import com.omang.app.data.database.psm.PsmDao
import com.omang.app.data.database.psm.PsmEntity
import com.omang.app.data.database.resource.ResourcesDao
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsDao
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity
import com.omang.app.data.database.test.QuestionDao
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.database.test.TestDao
import com.omang.app.data.database.test.TestEntity
import com.omang.app.data.database.user.UserDao
import com.omang.app.data.database.user.UserEntity
import com.omang.app.data.model.modeMeter.ModeMeterDao
import com.omang.app.data.model.modeMeter.ModeMeterEntity
import com.omang.app.data.model.modeMeter.MoodDataDao
import com.omang.app.data.model.modeMeter.MoodDataEntity
import com.omang.app.data.model.test.TestSubmitResponse
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.utils.extensions.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val userDao: UserDao,

    private val myWebPlatformDao: MyWebPlatformDao,
    private val modeMeterDao: ModeMeterDao,
    private val moodDataDao: MoodDataDao,

    private val myClassroomDao: MyClassroomDao,
    private val contentContentDao: ContentContentDao,
    private val resourcesDao: ResourcesDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val classroomResourcesRelationDao: ClassroomResourcesRelationDao,
    private val unitLessonRelationDao: UnitLessonRelationDao,
    private val classroomLessonRelationDao: ClassroomLessonRelationDao,
    private val classroomWebsiteRelationDao: ClassroomWebsiteRelationDao,
    private val lessonWeblinksMappingDao: LessonWeblinksMappingDao,
    private val testDao: TestDao,
    private val unitTestRelationDao: UnitTestRelationDao,
    private val publicFeedDao: PublicFeedDao,
    private val ticketsDao: TicketsDao,
    private val questionDao: QuestionDao,
    private val mobileNavDao: MobileNavDao,
    private val locationDao: LocationDao,
    private val psmDao: PsmDao,
    private val classroomFeedDao: ClassroomFeedDao,
    private val deviceAnalyticsDao: DeviceAnalyticsDao,
    private val fileDao: FileDao,

    ) {

    /* below for user */
    suspend fun insertUser(user: UserEntity) = userDao.insert(user)
    suspend fun getUser(): UserEntity? = userDao.getUser()
    suspend fun getUserFirstName(): String? = userDao.getUserFirstName()
    suspend fun clearUser() = userDao.clear()

    /* below for my library */
    suspend fun insertMyLibrary(resourcesEntities: List<ResourcesEntity>) =
        resourcesDao.insertAll(resourcesEntities)

    suspend fun fetchMyLibraryDocs() = resourcesDao.getAllMyLibraryDocs()
    suspend fun fetchMyLibraryVideos() = resourcesDao.getAllMyLibraryVideos()
    suspend fun updateIsBonusToFalse(resourceId: Int) =
        resourcesDao.updateIsBonusToFalse(resourceId)

    suspend fun deleteResource(resourceId: Int) = resourcesDao.deleteResource(resourceId)

    /* my web platforms */
    suspend fun fetchAllMyWebPlatforms() = myWebPlatformDao.getAllMyWebPlatformItems()

    fun getExploreItems() = myWebPlatformDao.getExploreItems()

    fun getMyWebPlatformItems() = myWebPlatformDao.getMyWebPlatformItems()

    suspend fun getWebsitesByIds(websiteIds: List<Int>) =
        myWebPlatformDao.getWebsitesByIds(websiteIds)

    suspend fun insertMyWebPlatform(myWebPlatformEntity: List<MyWebPlatformEntity>) =
        myWebPlatformDao.insertAll(myWebPlatformEntity)

    suspend fun getWebsiteByClassroomID(classroomId: Int) =
        myWebPlatformDao.getWebsitesByClassroomId(classroomId)

    suspend fun setFavorite(id: Int) =
        myWebPlatformDao.setFavorite(id)

    suspend fun removeFavorite(id: Int) =
        myWebPlatformDao.removeFavorite(id)

    fun fetchFavItems() =
        myWebPlatformDao.fetchFavItems()

    suspend fun setTimeStamp(id: Int, timeData: String) =
        myWebPlatformDao.setTimeStamp(id, timeData)

    suspend fun deleteWebPlatform(id: Int) =
        myWebPlatformDao.deleteWebPlatform(id)

    suspend fun deleteMyWebPlatform(id: Int) =
        myWebPlatformDao.deleteMyWebPlatform(id)


    /*my classroom*/
    suspend fun insertMyClassroom(myClassroomEntity: MyClassroomEntity) =
        myClassroomDao.insert(myClassroomEntity)

    fun fetchAllClassrooms() = myClassroomDao.fetchAllClassrooms()

    fun fetchAllClubs() = myClassroomDao.fetchAllClubs()
    suspend fun fetchAllClubsList() = myClassroomDao.fetchMyClubsList()
    suspend fun getClassRoomById(id: Int) = myClassroomDao.getClassRoomById(id)
    suspend fun fetchAllClassroomsList() = myClassroomDao.fetchMyClassroomList()
    suspend fun fetchMyClassroomsAndClubsList() = myClassroomDao.fetchMyClassroomsAndClubsList()

    /*
    * relation unit lesson*/
    suspend fun getUnitsWithLessonsByClassroomId(classroomId: Int): List<UnitWithLessons> {
        val uniqueCombinations = unitLessonRelationDao.getUniqueCombinations(classroomId)

        Timber.tag("ERROR_06")
            .e("uniqueCombinations for classroom id $classroomId is $uniqueCombinations")

        val result = mutableMapOf<Int, MutableList<LessonsEntity>>()

        for (combination in uniqueCombinations) {
            val unitId = combination.unitId
            val lessonId = combination.lessonId

            val unit = unitDao.getUnitById(unitId)
            val lesson = lessonDao.getLessonById(lessonId)

            if (unit != null && lesson != null) {
                if (result.containsKey(unitId)) {
                    result[unitId]?.add(lesson)
                } else {
                    result[unitId] = mutableListOf(lesson)
                }
            }
        }

        return result.mapNotNull { (unitId, lessons) ->
            val unit = unitDao.getUnitById(unitId)
            if (unit != null) {
                withContext(Dispatchers.IO) {
                    val mcq = testDao.mcqCount(unitId, classroomId)
                    if (mcq > 0) {
                        val isTestPassed =
                            unitTestRelationDao.checkIfTestIsPassed(unitId, classroomId)
                        if (isTestPassed > 0) {
                            unit.isTestPassed = DBConstants.TestStatus.PASSED
                        } else {
                            val isTestsAvailable =
                                unitTestRelationDao.checkIfTestsAvailableToAttend(
                                    unitId,
                                    classroomId
                                )
                            if (isTestsAvailable > 0)
                                unit.isTestPassed = DBConstants.TestStatus.NOT_ATTEMPTED
                            else
                                unit.isTestPassed = DBConstants.TestStatus.FAILED
                        }
                    } else {
                        unit.isTestPassed = DBConstants.TestStatus.PASSED
                    }
                }
                UnitWithLessons(unit = unit, lessons = lessons)
            } else {
                null
            }
        }
    }

    /*
    resource for classroom (book and video)*/
    suspend fun insertAllResources(resourcesEntities: List<ResourcesEntity>) =
        resourcesDao.insertAll(resourcesEntities)

    suspend fun getResourcesByClassroomId(classroomId: Int) =
        resourcesDao.getResourcesByClassroomId(classroomId)


    suspend fun fetchMyLibrary() = resourcesDao.getAllMyLibraryItems()

    /*
    * mapping table operation for resource (book/video)*/
    suspend fun insertClassroomResourcesRelations(classroomResourcesRelationEntities: MutableList<ClassroomResourcesRelationEntity>) =
        classroomResourcesRelationDao.insertClassroomResourcesRelations(
            classroomResourcesRelationEntities
        )

    suspend fun deleteClassroomResourceRelation(classroomId: Int, resourceId: Int) =
        classroomResourcesRelationDao.deleteClassroomResourceRelation(classroomId, resourceId)

    /*
     unit*/
    suspend fun insertAllUnits(lessonsEntity: List<UnitEntity>) = unitDao.insertAll(lessonsEntity)

    /*
    * lesson data */
    suspend fun insertAllLessons(lessonsEntity: List<LessonsEntity>) =
        lessonDao.insertAll(lessonsEntity)


    /*insert unit and it's lesson mapping */
    suspend fun insertUnitLessonRelation(unitLessonRelationEntities: MutableList<UnitLessonRelationEntity>) =
        unitLessonRelationDao.insertUnitLessonRelation(unitLessonRelationEntities)


    /*
    *insert classroom and it's lesson mapping */
    suspend fun insertClassroomLesson(classroomLessonRelationEntity: MutableList<ClassroomLessonRelationEntity>) =
        classroomLessonRelationDao.insertClassroomLessonMapping(classroomLessonRelationEntity)


    /*
    * insert classroom and it's website mapping*/
    suspend fun insertClassroomWebsiteRelation(classroomWebsiteRelationEntities: MutableList<ClassroomWebsiteRelationEntity>) =
        classroomWebsiteRelationDao.insertAll(classroomWebsiteRelationEntities)

    suspend fun deleteClassroomWebsiteRelation(classroomId: Int, websiteId: Int) =
        classroomWebsiteRelationDao.deleteClassroomResourceRelation(classroomId, websiteId)

    /*
    * weblink and lesson operation with classroom */
    suspend fun insertLessonWeblinksMapping(lessonWeblinksMappingEntities: MutableList<LessonWeblinksMappingEntity>) =
        lessonWeblinksMappingDao.insertLessonWeblinksMapping(lessonWeblinksMappingEntities)

    suspend fun fetchLessonIds(classroomId: Int, lessonId: Int) =
        lessonWeblinksMappingDao.getLessonWeblinksMapping(classroomId, lessonId)

    /*
    * push notification history*/
    suspend fun insertNotification(notifications: List<FeedEntity>) =
        publicFeedDao.insertAll(notifications)

    fun getAllNotifications() = publicFeedDao.getAllNotifications()
    suspend fun getLastTenNavigation() = mobileNavDao.getLastTenNavigation()

    /*
    * clear table */
    //TODO : add remaining tables
    suspend fun clearAllTables() {
        classroomLessonRelationDao.clear()
        classroomResourcesRelationDao.clear()
        classroomWebsiteRelationDao.clear()
        lessonWeblinksMappingDao.clear()
        unitLessonRelationDao.clear()
        lessonDao.clear()
        unitDao.clear()
        myClassroomDao.clear()
        myWebPlatformDao.clear()
        resourcesDao.clear()
        userDao.clear()
        locationDao.clear()
        unitTestRelationDao.clear()
        contentContentDao.clear()
        mobileNavDao.clear()
        publicFeedDao.deleteAll()
        psmDao.clear()
        ticketsDao.clear()
        questionDao.clear()
        testDao.clear()
        deviceAnalyticsDao.clear()
        classroomFeedDao.deleteAll()
    }

    suspend fun insertTestList(testList: List<TestEntity>) {
        withContext(Dispatchers.IO) {
            val testEntities = mutableListOf<TestEntity>()
            testList.forEach {
                val isTestExist = testDao.isTestExist(
                    it.classroomId!!, it.unitId!!, it.generalMcqId!!
                )
                if (isTestExist == 0)
                    testEntities.add(it)
            }
            testDao.insertAll(testEntities)
        }
    }

    suspend fun insertClassroomTest(testList: List<TestEntity>) {
        withContext(Dispatchers.IO) {
            val testEntities = mutableListOf<TestEntity>()
            testList.forEach {
                val isTestExist = testDao.isClassroomTestExist(
                    it.generalMcqId!!
                )
                if (isTestExist == 0)
                    testEntities.add(it)
            }
            testDao.insertAll(testEntities)
        }
    }

    /*
    * Unit and Test mapping
    * */
    suspend fun insertUnitTestMapping(lessonUnitEntities: MutableList<UnitTestRelationEntity>) =
        unitTestRelationDao.insertUnitTestRelation(lessonUnitEntities)

    suspend fun getTestsByUnitId(unitId: Int) =
        testDao.getTestsByUnitId(unitId)

    suspend fun getTestById(testId: Int) =
        testDao.getTestById(testId)

    suspend fun insertQuestions(questionList: List<QuestionEntity>) =
        questionDao.insertAll(questionList)

    suspend fun getTestQuestionsById(questionIdList: List<Int>) =
        questionDao.getQuestionsById(questionIdList)

    /*
    * tech-support*/

    suspend fun insertTickets(tickets: List<TicketsEntity>) =
        ticketsDao.insertAll(tickets)

    suspend fun getTickets() = ticketsDao.getTickets()

    suspend fun getUniTestEntityByUnitAndTestId(unitId: Int, testId: Int, classroomId: Int) =
        unitTestRelationDao.getUniTestEntity(unitId, testId, classroomId)

    suspend fun updateUniTestRelationEntity(unitTestRelationEntity: UnitTestRelationEntity) =
        unitTestRelationDao.updateUniTestRelation(unitTestRelationEntity)

    suspend fun insertMobileNavigation(mobileNavigationEntity: MobileNavigationEntity) =
        mobileNavDao.insert(mobileNavigationEntity)

    suspend fun getMobileNavigationList() = mobileNavDao.getAllNavigation()
    fun getUnitMainMcq(unitId: Int, classroomId: Int) =
        testDao.getUnitMainMcq(unitId, classroomId)

    suspend fun checkIfTestPassed(unitId: Int, classroomId: Int) =
        unitTestRelationDao.checkIfTestIsPassed(unitId, classroomId)

    fun getAllAttemptedOfflineTests() =
        testDao.getAllAttemptedOfflineTests()

    suspend fun deleteAllAttemptedTests(offlineTestEntity: List<TestEntity>) =
        testDao.deleteTests(offlineTestEntity)

    fun getAllNewTests() =
        testDao.getAllNewTests()

    fun getAllExpiredTests() =
        testDao.getAllExpiredTests()

    fun getAllAttemptedTests() =
        testDao.getAllAttemptedTests()

    suspend fun updateTest(testEntity: TestEntity) =
        testDao.update(testEntity)

    suspend fun updateListOfTestToAttemptedAndSent(testEntities: List<TestSubmitResponse.McqResultData>) {
        withContext(Dispatchers.IO) {
            testEntities.forEach {
                testDao.updateTestStatus(
                    testId = it.mcq,
                    classroomId = it.classroom,
                    unitId = it.unit
                )
            }
        }
    }

    suspend fun getTicketById(localId: String) = ticketsDao.getTicketById(localId)

    suspend fun deleteTicketById(localId: String) = ticketsDao.deleteTicketById(listOf(localId))
    suspend fun deleteMyClassRoomOrClub(resourceId: Int) = myClassroomDao.delete(resourceId)
    suspend fun checkIfResourceExistInClassroom(resourceId: Int) =
        classroomResourcesRelationDao.checkIfResourceExists(resourceId)

    suspend fun getResourceById(resourceId: Int) =
        resourcesDao.getResourceById(resourceId)

    suspend fun insertModeMeterData(moodMeterEntity: List<ModeMeterEntity>) =
        modeMeterDao.insertModeMeterData(moodMeterEntity)

    suspend fun insertClickedData(moodDataEntity: MoodDataEntity) =
        moodDataDao.insertClickedData(moodDataEntity)

    //for getting data from RoomDB
    fun getMoodMeterItems() =
        modeMeterDao.getMoodMeterItems()

    suspend fun getClickedData() =
        moodDataDao.getClickedData()


    suspend fun setMoodMeterTime(formattedDateTime: String, modeId: Int) =
        moodDataDao.setMoodMeterTime(formattedDateTime, modeId)

    suspend fun clearMoodData() =
        moodDataDao.clearMoodData()

    suspend fun getRowCount(): Int =
        modeMeterDao.getRowCount()

    suspend fun setLastUpdatedDate(classroomId: Int) =
        myClassroomDao.updateLastUpdatedDateById(classroomId)

    suspend fun getLastUpdatedDateById(classroomId: Int) =
        myClassroomDao.getLastUpdatedDateById(classroomId)

    fun getExploreItemIds() =
        myWebPlatformDao.getAllExploreIds()

    fun getMyWebPlatformItemIds() =
        myWebPlatformDao.getAllMyWebPlatformIds()

    fun getAllMyLibraryContentIds() =
        resourcesDao.getAllMyLibraryIds()

    suspend fun insertDoe(myWebPlatformEntity: MyWebPlatformEntity) =
        myWebPlatformDao.insert(myWebPlatformEntity)

    suspend fun clearDoe() = myWebPlatformDao.clearDoe()

    fun getDoe() = myWebPlatformDao.getDoe()
    suspend fun clearLocalNotifications() = publicFeedDao.deleteAll()
    suspend fun deletePublicFeed(resourceId: Int) = publicFeedDao.delete(resourceId)
    suspend fun deleteClassroomFeed(resourceId: Int) = classroomFeedDao.delete(resourceId)

    fun getFeedsByClassroomId(classroomId: Int) =
        classroomFeedDao.getFeedsByClassroomId(classroomId)

    suspend fun insertClassroomFeeds(feeds: List<ClassroomFeedEntity>) =
        classroomFeedDao.insertAll(feeds)

    suspend fun insertPsm(psm: PsmEntity) =
        psmDao.insert(psm)

    suspend fun getGeneralPsm() =
        psmDao.getGeneralPsm()

    suspend fun getPsmByClassroomId(classroomId: Int) =
        psmDao.getPsmByClassroomId(classroomId)

    suspend fun deletePsmByClassroomId(classroomId: Int) =
        psmDao.deletePsmByClassroomId(classroomId)

    suspend fun deleteGeneralPsm() =
        psmDao.deleteGeneralPsm()

    suspend fun getClassroomTestsByType(classroomId: Int,type: Int) =
        testDao.getClassroomTestsByType(classroomId,type)

    /*below methods are for device-analytics, keep other codes are above this line */

    suspend fun insertAnalytics(analyticsEntity: AnalyticsEntity) {
        log(analyticsEntity)
        return deviceAnalyticsDao.insert(analyticsEntity)
    }

    suspend fun getAllAnalytics() = deviceAnalyticsDao.getAllAnalytics()
    suspend fun clearAnalytics() = deviceAnalyticsDao.clear()
    suspend fun deleteClassroomLessonMapping(classroomId: Int) =
        classroomLessonRelationDao.deleteClassroomLessonMapping(classroomId)

    suspend fun deleteClassroomResourceMapping(classroomId: Int) =
        classroomResourcesRelationDao.deleteClassroomResourceMapping(classroomId)

    suspend fun deleteClassroomWebsiteMapping(classroomId: Int) =
        classroomWebsiteRelationDao.deleteClassroomWebsiteMapping(classroomId)

    suspend fun deleteClassroomLessonWebLinkMapping(classroomId: Int) =
        lessonWeblinksMappingDao.deleteClassroomLessonWebLinkMapping(classroomId)

    suspend fun deleteClassroomUnitLessonMapping(classroomId: Int) =
        unitLessonRelationDao.deleteClassroomUnitLessonMapping(classroomId)

    suspend fun updateMyClassroom(classroomEntity: MyClassroomEntity) =
        myClassroomDao.updateClassroom(classroomEntity)

    suspend fun deleteClassroomLessonWebLinkMapping(classroomId: Int, lessonId: Int) =
        lessonWeblinksMappingDao.deleteClassroomLessonWebLinkMapping(classroomId, lessonId)

    suspend fun deleteClassroomLessonMapping(classroomId: Int, lessonId: Int) =
        classroomLessonRelationDao.deleteClassroomLessonMapping(classroomId, lessonId)

    suspend fun deleteClassroomUnitLessonMapping(classroomId: Int, lessonId: Int, unitId: Int) =
        unitLessonRelationDao.deleteClassroomUnitLessonMapping(classroomId, lessonId, unitId)

    suspend fun getNotAttemptedMcqCountByClassroomAndUnit(classroomId: Int, unitId: Int) =
        testDao.getNotAttemptedMcqCountByClassroomAndUnit(unitId, classroomId)

    suspend fun updateTestToExpired(testId: Int) =
        testDao.updateTestToExpired(testId)

    /*--------------------------------------------------------------------------------*/

    /*
    * below to perform CRUD on file table */
    suspend fun fileInsert(fileEntity: FileEntity) = fileDao.insert(fileEntity)
    suspend fun getAllFile() = fileDao.getAllFiles()
    suspend fun deleteAllFile() = fileDao.clear()
    suspend fun updateLessonRate(id: Int, rating: Float) =
        lessonDao.updateLessonRating(id,rating)

    suspend fun getDocumentIdByClassroomId(classroomId: Int) =
        resourcesDao.getDocumentIdByClassroomId(classroomId)

    suspend fun getVideoIdByClassroomId(classroomId: Int) =
        resourcesDao.getVideoIdByClassroomId(classroomId)

    suspend fun getWebsiteIdByClassroomId(classRoomId: Int) =
        myWebPlatformDao.getWebsiteIdByClassroomId(classRoomId)

    suspend fun getMcqId(unitId: Int, classroomId: Int) =
        testDao.getMcqId(unitId, classroomId)

    suspend fun getClassroomMcqId(classroomId: Int) =
        testDao.getClassroomMcqId(classroomId)

}