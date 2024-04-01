package com.omang.app.data.database

class DBConstants {
    companion object {

        const val DATABASE_NAME = "one_omang_db"
        const val USER_TABLE = "user_table"
        const val MY_LIBRARY_TABLE = "my_library_table"
        const val MY_WEB_PLATFORM_TABLE = "my_web_platform_table"

        const val MY_CLASSROOM_TABLE = "my_classroom_table"
        const val LOCATION = "app_location"
        const val UNIT_TABLE = "unit_table"
        const val LESSON_TABLE = "lesson_table"
        const val CREATED_BY_TABLE = "created_by_table"
        const val LESSON_TEST_TABLE = "lesson_test_table"
        const val LESSON_WEBSITES_TABLE = "lesson_websites_table"

        const val UNIT_LESSON_RELATION_TABLE = "relation_unit_lesson"
        const val CLASSROOM_LESSON_RELATION_TABLE = "relation_classroom_lesson"
        const val LESSON_WEBLINKS_RELATION_TABLE = "relation_lesson_weblinks"
        const val CLASSROOM_RESOURCES_RELATION_TABLE = "relation_classroom_resource"
        const val CLASSROOM_WEBSITE_RELATION_TABLE = "relation_classroom_website"

        const val RESOURCES_TABLE = "resources_table"
        const val TEST_TABLE = "test_table"
        const val UNIT_TEST_RELATION_TABLE = "relation_unit_test"
        const val QUESTIONS_TABLE = "questions_table"

        const val CLASSROOM_RESOURCE = "classroom_resource"
        const val WEBSITE_RESOURCE = "website_resource"
        const val TICKETS_TABLE = "tickets_table"
        const val MOBILE_NAV_TABLE = "mobile_navigation_table"
        const val PSM_TABLE = "psm_table"
        const val FILE_TABLE = "file_table"

        const val MODE_METER_TABLE = "mode_meter_table"
        const val MODE_DATA_TABLE = "mode_data_table"
        const val PUBLIC_FEED_TABLE = "public_feed_table"
        const val CLASSROOM_FEED_TABLE = "classroom_feed_table"
        const val CLASSROOM_FEED_RELATION_TABLE = "relation_classroom_feed"

        const val DATA_ANALYTICS = "analytics_table"
        const val GENERAL_PSM_ID = -1

    }

    enum class BonusContent(val value: Int) {
        FALSE(0),
        TRUE(1)// my library content
    }

    enum class WebsiteContent(val value: Int) {
        CLASSROOM(0),
        WEBLINKS(1),
        MY_WEB_PLATFORM(2),
        EXPLORE(3),
        DOE(4)
    }

    enum class AnalyticsType(val value: Int) {
        WATCH(101),
        LOCATION(102),
        SLEEP_AWAKE(103),
        UNUSUAL_ACTIVITIES(104),
        NETWORK(105),
        ERROR_LOG(106),
        PSM(107),
        MANUAL_CLEANUP(108)
    }

    enum class AnalyticsMenu(val value: Int) {
        CLASSROOM(101),
        EXPLORE(102),
        MY_WEB_PLATFORM(103),
        LESSON(104),
        MY_LIBRARY(106),
        DOE(107),
        GALLERY(108)
    }

    enum class TestContent(val value: Int) {
        UNIT(0),
        CLASSROOM(1),
    }

    enum class IsTestAttempted(val value: Int) {
        TRUE(1),
        FALSE(0),
    }

    enum class TestStatus(val value: Int) {
        NOT_ATTEMPTED(0),
        PASSED(1),
        FAILED(2)
    }

    enum class TestType(val value: Int) {
        NEW(0),
        EXPIRED(1),
        ATTEMPTED(2)
    }

    enum class IsTestSentToApi(val value: Int) {
        FALSE(0),
        TRUE(1),
    }

    enum class Event(val value: Int) {
        VISIT(0),
        CLICK(1),
        TRIGGER(2),
        ACTION(3),
        DOWNLOAD(4)
    }
}
