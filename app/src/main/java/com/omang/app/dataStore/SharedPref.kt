package com.omang.app.dataStore

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPref @Inject constructor(context: Context) {

    private val preferences: SharedPreferences

    init {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    companion object {
        private const val NAME = "One-Omang"
        private const val MODE = Context.MODE_PRIVATE
        private lateinit var preferences: SharedPreferences

        /*
        * keys
        * first and second
        * */

        private val IS_LOGGED_IN = Pair("isLoggedIn", false)
        private val FCM_TOKEN = Pair("fcmToken", "")
        private val ACCESS_TOKEN = Pair("accessToken", "")
        private val REFRESH_TOKEN = Pair("refreshToken", "")
        private val WIFI = Pair("wifi", false)
        private val IS_SOCKET_CONNECTED = Pair("IS_SOCKET_CONNECTED", false)
        private val POST_TO_VALUE = Pair("POST_TO_VALUE", "")
        private val POST_CLASSROOM = Pair("POST_CLASSROOM", 0)
        private val POST_DESCRIPTION = Pair("POST_DESCRIPTION", "")
        private val EXPLORE_UPDATE = Pair("EXPLORE_UPDATE", false)
        private val MY_LIBRARY_UPDATE = Pair("MY_LIBRARY_UPDATE", false)
        private val MY_WEB_PLATFORM_UPDATE = Pair("MY_WEB_PLATFORM_UPDATE", false)
        private val MOOD_LAST_SHOWN_DATE = Pair("MOOD_LAST_SHOWN_DATE", 0)
        private val ANALYTICS_FLAG = Pair("ANALYTICS_FLAG", 0)
        private val DIAGNOSTIC_TIME = Pair("DIAGNOSTIC_TIME", "")
        private val DSL = Pair("DSL", 0) // deviceStorageLimit
        private val DRDD = Pair("DRDD", 0) // deviceResourceDeletionDays
        private val ACC = Pair("ACC", 0) // deviceUnpinningClickCount
        private val DAP = Pair("DAP", "") // deviceUnpinningPassword
        private val IS_DEBUG_USER = Pair("isDebugUser", false) // deviceResourceDeletionDays
        private val LOGS = Pair("LOGS", "") // entire log for debug
        private val TECH_PHONES = Pair("TECH_PHONES", "") // entire log for debug
        private val RATING_STATUS = Pair("RATING_STATUS", false)

    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var isLoggedIn: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(IS_LOGGED_IN.first, IS_LOGGED_IN.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(IS_LOGGED_IN.first, value)
        }

    var wifi: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(IS_SOCKET_CONNECTED.first, IS_SOCKET_CONNECTED.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(IS_SOCKET_CONNECTED.first, value)
        }

    var isSocketConnected: Boolean
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getBoolean(WIFI.first, WIFI.second)
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putBoolean(WIFI.first, value)
        }

    var fcmToken: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(FCM_TOKEN.first, FCM_TOKEN.second)!!
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(FCM_TOKEN.first, value)
        }

    var accessToken: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(ACCESS_TOKEN.first, ACCESS_TOKEN.second)!!
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(ACCESS_TOKEN.first, value)
        }

    var refreshToken: String
        // custom getter to get a preference of a desired type, with a predefined default value
        get() = preferences.getString(REFRESH_TOKEN.first, REFRESH_TOKEN.second)!!
        // custom setter to save a preference back to preferences file
        set(value) = preferences.edit {
            it.putString(REFRESH_TOKEN.first, value)
        }

    var selectedPostToItem: String
        get() = preferences.getString(POST_TO_VALUE.first, POST_TO_VALUE.second)!!
        set(value) = preferences.edit {
            it.putString(POST_TO_VALUE.first, value)
        }

    var selectedClassRoomItem: Int
        get() = preferences.getInt(POST_CLASSROOM.first, POST_CLASSROOM.second)
        set(value) = preferences.edit {
            it.putInt(POST_CLASSROOM.first, value)
        }

    var postDescription: String
        get() = preferences.getString(POST_DESCRIPTION.first, POST_DESCRIPTION.second)!!
        set(value) = preferences.edit {
            it.putString(POST_DESCRIPTION.first, value)
        }

    var exploreUpdate: Boolean
        get() = preferences.getBoolean(EXPLORE_UPDATE.first, EXPLORE_UPDATE.second)
        set(value) = preferences.edit {
            it.putBoolean(EXPLORE_UPDATE.first, value)
        }

    var myWebPlatformUpdate: Boolean
        get() = preferences.getBoolean(MY_WEB_PLATFORM_UPDATE.first, MY_WEB_PLATFORM_UPDATE.second)
        set(value) = preferences.edit {
            it.putBoolean(MY_WEB_PLATFORM_UPDATE.first, value)
        }

    var myLibraryUpdate: Boolean
        get() = preferences.getBoolean(MY_LIBRARY_UPDATE.first, MY_LIBRARY_UPDATE.second)
        set(value) = preferences.edit {
            it.putBoolean(MY_LIBRARY_UPDATE.first, value)
        }

    var moodLastShown: Int
        get() = preferences.getInt(MOOD_LAST_SHOWN_DATE.first, MOOD_LAST_SHOWN_DATE.second)
        set(value) = preferences.edit {
            it.putInt(MOOD_LAST_SHOWN_DATE.first, value)
        }

    var analyticsFlag: Int
        get() = preferences.getInt(ANALYTICS_FLAG.first, ANALYTICS_FLAG.second)
        set(value) = preferences.edit {
            it.putInt(ANALYTICS_FLAG.first, value)
        }

    var analyticsTime: String
        get() = preferences.getString(DIAGNOSTIC_TIME.first, DIAGNOSTIC_TIME.second)!!
        set(value) = preferences.edit {
            it.putString(DIAGNOSTIC_TIME.first, value)
        }

    var dsl: Int
        get() = preferences.getInt(DSL.first, DSL.second)
        set(value) = preferences.edit {
            it.putInt(DSL.first, value)
        }

    var drdd: Int
        get() = preferences.getInt(DRDD.first, DRDD.second)
        set(value) = preferences.edit {
            it.putInt(DRDD.first, value)
        }

    var acc: Int
        get() = preferences.getInt(ACC.first, ACC.second)
        set(value) = preferences.edit {
            it.putInt(ACC.first, value)
        }

    var dap: String
        get() = preferences.getString(DAP.first, DAP.second)!!
        set(value) = preferences.edit {
            it.putString(DAP.first, value)
        }

    var isDebugUser: Boolean
        get() = preferences.getBoolean(IS_DEBUG_USER.first, IS_DEBUG_USER.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_DEBUG_USER.first, value)
        }

    var logs: String
        get() = preferences.getString(LOGS.first, LOGS.second)!!
        set(value) = preferences.edit {
            it.putString(LOGS.first, value)
        }

    var techPhones: String
        get() = preferences.getString(TECH_PHONES.first, TECH_PHONES.second)!!
        set(value) = preferences.edit {
            it.putString(TECH_PHONES.first, value)
        }

}