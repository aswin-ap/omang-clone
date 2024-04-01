package com.omang.app.data.repository

import androidx.datastore.preferences.core.Preferences
import com.omang.app.dataStore.PreferenceDataSource
import javax.inject.Inject

//data class UserSettings(val name: String?, val phoneNumber: String?, val address: String?)
class DataManagerRepository @Inject constructor(private val dataStore: PreferenceDataSource) {

    suspend fun <T> saveToDataStore(key: Preferences.Key<T>, value: T) =
        dataStore.saveToDataStore(key, value)

    fun <T> getFromDataStore(key: Preferences.Key<T>) =
        dataStore.getFromDataStore(key)

    suspend fun  saveMoodDataToDataStore(
        moodIdKey: Preferences.Key<Set<String>>,
        moodIdValue: Set<String>,
        moodDisplayNameKey: Preferences.Key<Set<String>>,
        moodDisplayNameValue: Set<String>,
        moodEmojiKey: Preferences.Key<Set<String>>,
        moodEmojiValue: Set<String>
    ) =
        dataStore.saveMoodDataToDataStore(moodIdKey,moodIdValue, moodDisplayNameKey,moodDisplayNameValue,moodEmojiKey,moodEmojiValue)

}