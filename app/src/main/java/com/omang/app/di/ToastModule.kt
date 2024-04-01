package com.omang.app.di

import android.content.Context
import com.omang.app.utils.ToastMessage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object ToastModule {
    @Provides
    fun provideToast(@ActivityContext context: Context): ToastMessage {
        return ToastMessage(context)
    }

}
