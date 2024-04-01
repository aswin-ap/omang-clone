package com.omang.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.omang.app.dataStore.SharedPref
import com.omang.app.utils.socket.SocketManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    @Provides
    @Singleton
    fun provideSocketConnection(
        sharedPref: SharedPref
    ): SocketManager {
        return SocketManager(sharedPref)
    }

}