package com.omang.app.di

import android.content.Context
import com.omang.app.csdk.CSDKHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CSDKModule {

    @Provides
    @Singleton
    fun provideCSDKRepository(@ApplicationContext appContext: Context): CSDKHandler {
        return CSDKHandler(appContext)
    }

}
