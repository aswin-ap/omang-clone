package com.omang.app.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
//import com.omang.app.repository.UserSettings
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class PreferenceDataSource @Inject constructor(private val dataStore: DataStore<Preferences>) {

/*    suspend fun saveToDataStore(user: UserSettings) {
        dataStore.edit { pref ->
            user.phoneNumber?.let {
                pref[PHONE_NUMBER] = it
            }

            user.name?.let {
                pref[NAME] = it

            }

            user.phoneNumber?.let {
                pref[PHONE_NUMBER] = it
            }

            user.address?.let {
                pref[ADDRESS] = it

            }

        }
    }

    suspend fun getFromDataStore() = dataStore.data.map {
        UserSettings(
            name = it[NAME] ?: "",
            phoneNumber = it[PHONE_NUMBER] ?: "",
            address = it[ADDRESS] ?: ""
        )
    }*/


    /**
     * Function to retrieve the value from preference datastore.
     * @param key Key of the value to be retrieved
     * */
    fun <T> getFromDataStore(key: Preferences.Key<T>) =
        dataStore.data.map {
            it[key]
        }

    /**
     * Function to store any type of value to the preference datastore.
     * @param key The preference key such as stringPreferencesKey, boolPreferencesKey
     * @param value The value to be stored in the corresponding key
     * */
    suspend fun <T> saveToDataStore(key: Preferences.Key<T>, value: T?) {
        value?.let {
            dataStore.edit { pref ->
                pref[key] = value
            }
        }
    }

    suspend fun saveMoodDataToDataStore(
        moodIdKey: Preferences.Key<Set<String>>,
        moodIdValue: Set<String>,
        moodDisplayNameKey: Preferences.Key<Set<String>>,
        moodDisplayNameValue: Set<String>,
        moodEmojiKey: Preferences.Key<Set<String>>,
        moodEmojiValue: Set<String>
    ) {
        moodIdValue?.let {
            dataStore.edit { moodV ->
                moodV[moodIdKey] = moodIdValue
            }
        }

        moodDisplayNameValue?.let {
            dataStore.edit { nameV ->
                nameV[moodDisplayNameKey] = moodDisplayNameValue
            }
        }

        moodEmojiValue?.let {
            dataStore.edit { emojiV ->
                emojiV[moodEmojiKey] = moodEmojiValue
            }
        }

    }

 /*   fun isCSDKLicense(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CSDK_LICENSE] ?: false
    }

    suspend fun setCDSKLicense(isCSDKActivated: Boolean) {
        dataStore.edit { pref ->
            pref[CSDK_LICENSE] = isCSDKActivated
        }
    }*/
}
