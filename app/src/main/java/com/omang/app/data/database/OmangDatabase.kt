package com.omang.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.impl.Migration_6_7
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.dataAnalytics.DeviceAnalyticsDao
import com.omang.app.data.database.feed.ClassroomFeedDao
import com.omang.app.data.database.feed.ClassroomFeedEntity
import com.omang.app.data.database.feed.FeedEntity
import com.omang.app.data.database.feed.PublicFeedDao
import com.omang.app.data.database.file.FileDao
import com.omang.app.data.database.file.FileEntity
import com.omang.app.data.database.location.LocationDao
import com.omang.app.data.database.location.LocationEntity
import com.omang.app.data.database.myClassroom.ContentContentDao
import com.omang.app.data.database.myClassroom.ContentCountEntity
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
import com.omang.app.utils.dbConvertors.Converters
import timber.log.Timber


@Database(
    // TODO: need to implement the logic for give the manual migration script
    version = 8,
    entities = [
        UserEntity::class,
        MyWebPlatformEntity::class,
        MyClassroomEntity::class,
        ContentCountEntity::class,
        ResourcesEntity::class,
        UnitEntity::class,
        LessonsEntity::class,
        ClassroomResourcesRelationEntity::class,
        UnitLessonRelationEntity::class,
        ClassroomLessonRelationEntity::class,
        ClassroomWebsiteRelationEntity::class,
        LessonWeblinksMappingEntity::class,
        TestEntity::class,
        UnitTestRelationEntity::class,
        TicketsEntity::class,
        FeedEntity::class,
        QuestionEntity::class,
        MobileNavigationEntity::class,
        LocationEntity::class,
        ModeMeterEntity::class,
        MoodDataEntity::class,
        PsmEntity::class,
        AnalyticsEntity::class,
        ClassroomFeedEntity::class,
        FileEntity::class
    ],
)

@TypeConverters(Converters::class)
abstract class OmangDatabase : RoomDatabase() {
    abstract fun userInfoDao(): UserDao
    abstract fun myWebPlatformDao(): MyWebPlatformDao
    abstract fun myClassroomDao(): MyClassroomDao
    abstract fun contentContentDao(): ContentContentDao
    abstract fun resourcesDao(): ResourcesDao
    abstract fun unitDao(): UnitDao
    abstract fun lessonDao(): LessonDao
    abstract fun classroomResourcesRelationDao(): ClassroomResourcesRelationDao
    abstract fun unitLessonRelationDao(): UnitLessonRelationDao
    abstract fun classroomLessonRelationDao(): ClassroomLessonRelationDao
    abstract fun classroomWebsiteRelationDao(): ClassroomWebsiteRelationDao
    abstract fun lessonWeblinksMappingDao(): LessonWeblinksMappingDao
    abstract fun testDao(): TestDao
    abstract fun unitTestDo(): UnitTestRelationDao
    abstract fun notificationDao(): PublicFeedDao
    abstract fun ticketsDao(): TicketsDao
    abstract fun questionDao(): QuestionDao
    abstract fun mobileNavDao(): MobileNavDao
    abstract fun locationDao(): LocationDao
    abstract fun psmDao(): PsmDao
    abstract fun modeMeterDao(): ModeMeterDao
    abstract fun modeDataDao(): MoodDataDao
    abstract fun deviceAnalyticsDao(): DeviceAnalyticsDao
    abstract fun classroomFeedDao(): ClassroomFeedDao
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var instance: OmangDatabase? = null

        fun getDatabase(context: Context): OmangDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

        private fun buildDatabase(appContext: Context) =
            Room.databaseBuilder(appContext, OmangDatabase::class.java, "omang")
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6,
                    Migration_6_7,
                    MIGRATION_7_8
                )
                .build()

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Timber.v("init migration -> ${database.version}")

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `temp_test_table` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`generalMcqId` INTEGER, " +
                            "`classroomId` INTEGER, " +
                            "`unitId` INTEGER, " +
                            "`startTime` TEXT NOT NULL, " +
                            "`endTime` TEXT NOT NULL, " +
                            "`createdOn` TEXT NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`instructions` TEXT NOT NULL, " +
                            "`mcq_type` INTEGER NOT NULL, " +
                            "`type` INTEGER, " +
                            "`question_id` TEXT NOT NULL, " +
                            "`score` REAL NOT NULL, " +
                            "`percentage` REAL NOT NULL, " +
                            "`attemptsCount` INTEGER NOT NULL, " +
                            "`correctAttempts` INTEGER NOT NULL, " +
                            "`wrongAttempts` INTEGER NOT NULL, " +
                            "`isSentToApi` INTEGER NOT NULL, " +
                            "`questions` TEXT NOT NULL)"
                )

                database.execSQL("DROP TABLE IF EXISTS `test_table`")

                database.execSQL("ALTER TABLE `temp_test_table` RENAME TO `test_table`")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `temp_relation_unit_test` (" +
                            "`classroom_id` INTEGER NOT NULL, " +
                            "`unit_id` INTEGER NOT NULL, " +
                            "`test_id` INTEGER NOT NULL, " +
                            "`isAttempted` INTEGER NOT NULL, " +
                            "`testStatus` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`unit_id`, `test_id`, `classroom_id`))"
                )

                database.execSQL("DROP TABLE IF EXISTS `relation_unit_test`")

                database.execSQL("ALTER TABLE `temp_relation_unit_test` RENAME TO `relation_unit_test`")
            }
        }

        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `public_feed_table_temp` " +
                            "(`feed_id` TEXT NOT NULL, " +
                            "`resourceId` INTEGER NOT NULL, " +
                            "`title` TEXT NOT NULL, " +
                            "`description` TEXT NOT NULL, " +
                            "`image` TEXT, " +
                            "`createdAt` TEXT, " +
                            "`postedTo` TEXT, " +
                            "`feedType` INTEGER, " +
                            "`createdByfirstName` TEXT, " +
                            "`createdBylastName` TEXT, " +
                            "`createdByavatar` TEXT, " +
                            "`classroomDetailsclassRoomOrClub` TEXT, " +
                            "`classroomDetailsclassRoomId` INTEGER, " +
                            "PRIMARY KEY(`feed_id`))"
                )

                database.execSQL("DROP TABLE IF EXISTS `public_feed_table`")

                database.execSQL("ALTER TABLE `public_feed_table_temp` RENAME TO `public_feed_table`")

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `classroom_feed_table_temp` " +
                            "(`feed_id` TEXT NOT NULL, " +
                            "`resourceId` INTEGER NOT NULL, " +
                            "`classroom_id` INTEGER NOT NULL, " +
                            "`title` TEXT NOT NULL, " +
                            "`description` TEXT NOT NULL, " +
                            "`image` TEXT, " +
                            "`createdAt` TEXT, " +
                            "`postedTo` TEXT, " +
                            "`createdByfirstName` TEXT, " +
                            "`createdBylastName` TEXT, " +
                            "`createdByavatar` TEXT, " +
                            "`classroomDetailsclassRoomOrClub` TEXT, " +
                            "`classroomDetailsclassRoomId` INTEGER, " +
                            "PRIMARY KEY(`feed_id`))"
                )

                database.execSQL("DROP TABLE IF EXISTS `classroom_feed_table`")

                database.execSQL("ALTER TABLE `classroom_feed_table_temp` RENAME TO `classroom_feed_table`")

            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `analytics_table_temp` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`type` INTEGER, `created_time` INTEGER, `start_time` TEXT, `end_time` TEXT, " +
                            "`resource_id` INTEGER, `webPlatform_id` INTEGER, `web_url` TEXT, `classroom_id` INTEGER, " +
                            "`lesson_id` INTEGER, `unit_id` INTEGER, `menu` INTEGER, `latitude` REAL, `longitude` REAL, " +
                            "`logs` TEXT)"
                )

                database.execSQL(
                    "INSERT INTO `analytics_table_temp` (`type`, `created_time`, `start_time`, `end_time`, " +
                            "`resource_id`, `webPlatform_id`, `web_url`, `classroom_id`, `lesson_id`, `unit_id`, `menu`, " +
                            "`latitude`, `longitude`) SELECT `type`, `created_time`, `start_time`, `end_time`, `resource_id`, " +
                            "`webPlatform_id`, `web_url`, `classroom_id`, `lesson_id`, `unit_id`, `menu`, `latitude`, `longitude` FROM `analytics_table`"
                )

                database.execSQL("DROP TABLE `analytics_table`")

                database.execSQL("ALTER TABLE `analytics_table_temp` RENAME TO `analytics_table`")
            }
        }

        private val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `file_table` ( " +
                            "    `file_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "    `file_path` TEXT NOT NULL, " +
                            "    `created_time` INTEGER " +
                            ")"
                )

                database.execSQL("CREATE TABLE IF NOT EXISTS `temp_my_web_platform_table` (" +
                        "`website_id` INTEGER PRIMARY KEY NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`logo` TEXT NOT NULL, " +
                        "`url` TEXT, " +
                        "`altUrl` TEXT, " +
                        "`timeStamp` TEXT, " +
                        "`isFav` INTEGER NOT NULL, " +
                        "`type` INTEGER, " +
                        "`is_deleted` INTEGER" +
                        ")")

                database.execSQL("INSERT INTO `temp_my_web_platform_table` (" +
                        "`website_id`, `name`, `logo`, `url`, `altUrl`, `timeStamp`, `isFav`, `type`, `is_deleted` " +
                        ") " +
                        "SELECT `website_id`, `name`, `logo`, `url`, `altUrl`, `timeStamp`, `isFav`, `type`, 0 " +
                        "FROM `my_web_platform_table`")

                database.execSQL("DROP TABLE `my_web_platform_table`")

                database.execSQL("ALTER TABLE `temp_my_web_platform_table` RENAME TO `my_web_platform_table`")
            }
        }

        private val Migration_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
//                migration for psm_id field added in analytics_table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `analytics_table_temp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`type` INTEGER, `created_time` INTEGER, `start_time` TEXT, `end_time` TEXT, " +
                            "`resource_id` INTEGER, `webPlatform_id` INTEGER, `web_url` TEXT, `classroom_id` INTEGER, " +
                            "`lesson_id` INTEGER, `unit_id` INTEGER, `psm_id` INTEGER, `menu` INTEGER, `latitude` REAL, `longitude` REAL, " +
                            "`logs` TEXT)"
                )

                database.execSQL(
                    "INSERT INTO `analytics_table_temp` (`type`, `created_time`, `start_time`, `end_time`, " +
                            "`resource_id`, `webPlatform_id`, `web_url`, `classroom_id`, `lesson_id`, `unit_id`, `menu`, " +
                            "`latitude`, `longitude`, `logs`) SELECT `type`, `created_time`, `start_time`, `end_time`, `resource_id`, " +
                            "`webPlatform_id`, `web_url`, `classroom_id`, `lesson_id`, `unit_id`, `menu`, `latitude`, `longitude`, `logs` FROM `analytics_table`"
                )

                database.execSQL("DROP TABLE `analytics_table`")

                database.execSQL("ALTER TABLE `analytics_table_temp` RENAME TO `analytics_table`")
            }
        }

        private val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `temp_lesson_table` (" +
                            "`lesson_id` INTEGER NOT NULL, " +
                            "`name` TEXT NOT NULL, " +
                            "`description` TEXT NOT NULL, " +
                            "`logo` TEXT NOT NULL, " +
                            "`file` TEXT NOT NULL, " +
                            "`mimeType` TEXT NOT NULL, " +
                            "`lesson_rate` REAL, " +
                            "`createdByname` TEXT NOT NULL, " +
                            "`createdByavatar` TEXT NOT NULL, " +
                            "PRIMARY KEY(`lesson_id`))"
                )

                database.execSQL(
                    "INSERT INTO `temp_lesson_table` (`lesson_id`, `name`, `description`, `logo`, `file`, `mimeType`, `createdByname`, `createdByavatar`) " +
                            "SELECT `lesson_id`, `name`, `description`, `logo`, `file`, `mimeType`, `createdByname`, `createdByavatar` FROM `lesson_table`"
                )

                database.execSQL("DROP TABLE IF EXISTS `lesson_table`")

                database.execSQL("ALTER TABLE `temp_lesson_table` RENAME TO `lesson_table`")
            }
        }

    }
}
