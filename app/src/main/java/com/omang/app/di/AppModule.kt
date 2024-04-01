package com.omang.app.di

import android.content.Context
import com.omang.app.OneOmangApplication
import com.omang.app.utils.connectivity.NetworkConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext context: Context): OneOmangApplication {
        return context as OneOmangApplication
    }

    @Singleton
    @Provides
    fun provideNetworkConnection(@ApplicationContext context: Context): NetworkConnection {
        return NetworkConnection(context)
    }
}
