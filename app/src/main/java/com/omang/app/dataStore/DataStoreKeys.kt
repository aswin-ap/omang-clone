package com.omang.app.dataStore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object DataStoreKeys {
    val NAME = stringPreferencesKey("NAME")
    val PHONE_NUMBER = stringPreferencesKey("PHONE_NUMBER")
    val ADDRESS = stringPreferencesKey("ADDRESS")
    val HOTSPOT_STATUS = booleanPreferencesKey("hotspot_status")
    val LOCKED_STATUS = booleanPreferencesKey("locked_status")
    val MOBILE_DATA_STATUS = booleanPreferencesKey("mobile_data_status")
    val SIM_NO = stringPreferencesKey("sim_no")
    val IMEI_NO = stringPreferencesKey("imei_no")
    val FCM_TOKEN = stringPreferencesKey("fcm")
//    val ACCESS_TOKEN = stringPreferencesKey("access_token")
//    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val IS_USER_ASSIGNED = booleanPreferencesKey("IS_USER_ASSIGNED")

    val FIRST_NAME = stringPreferencesKey("FIRST_NAME")
    val LAST_NAME = stringPreferencesKey("LAST_NAME")
    val SCHOOL = stringPreferencesKey("SCHOOL")
    val DROP_POINT = intPreferencesKey("DROP_POINT")

    val CSDK_LICENSE_STATUS = booleanPreferencesKey("isCSDKActivated")
    val DEVICE_OWNER_STATUS = booleanPreferencesKey("device_owner_status")
    val PINNED_STATUS = booleanPreferencesKey("pinned_status")
    val DEVICE_LOCK_STATUS = booleanPreferencesKey("device_lock_status")
    val USER_ACTIVE_STATUS = booleanPreferencesKey("user_active_status")
    val DEVICE_LOCK_MESSAGE = stringPreferencesKey("device_lock_message")
    val DEVICE_UN_ASSIGN_MESSAGE = stringPreferencesKey("device_un_assign_message")
    val TEST_STATUS = booleanPreferencesKey("test_status")
    val TEST_ATTENDED_COUNT = intPreferencesKey("attendedCount")
    val TEST_EXPIRED_COUNT = intPreferencesKey("expiredCount")
    val TEST_NEW_COUNT = intPreferencesKey("notAttendedCount")
    val PSM_STATUS = booleanPreferencesKey("psmStatus")

    val MOOD_ID = stringSetPreferencesKey("mood_id")
    val MOOD_DISPLAY_NAME = stringSetPreferencesKey("mood_display_name")
    val MOOD_EMOJI = stringSetPreferencesKey("mood_emoji")
    val USER_FIRST_NAME = stringPreferencesKey("mood_emoji")
//    val MOOD_DATE_STORE = intPreferencesKey("mood_date_store")

    val EXPLORE_LAST_UPDATED_DATE = stringPreferencesKey("explore_updated_date")
    val MY_LIBRARY_LAST_UPDATED_DATE = stringPreferencesKey("my_library_updated_date")
    val MY_WEB_PLATFORM_LAST_UPDATED_DATE = stringPreferencesKey("my_web_platform_updated_date")

    val IS_SOCKET_CONNECTED = booleanPreferencesKey("is_socket_connected")
    val WIFI_STATUS = booleanPreferencesKey("wifi")
    val DATA_STATUS = booleanPreferencesKey("data")

}