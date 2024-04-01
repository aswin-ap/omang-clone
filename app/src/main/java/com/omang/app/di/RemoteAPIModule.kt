package com.omang.app.di

import com.omang.app.BuildConfig
import com.omang.app.dataStore.SharedPref
import com.omang.app.network.AuthorizationInterceptor
import com.omang.app.network.OmangApiService
import com.omang.app.network.RefreshApiService
import com.omang.app.network.TokenRefreshAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)

object RemoteAPIModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AuthClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Auth

    @Provides
    fun provideBaseUrl() = BuildConfig.BASE_URL

    @Singleton
    @Provides
    fun provideLogInterceptor() = run {
        val loggingInterceptor = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)

        }
        return@run loggingInterceptor
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor) = run {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @AuthClient
    @Provides
    fun provideOkHttpAuthClient(
        loggingInterceptor: HttpLoggingInterceptor,
        sharedPref: SharedPref
    ) = run {
        OkHttpClient.Builder()
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthorizationInterceptor(sharedPref))
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    @Singleton
    @Auth
    @Provides
    fun provideAuthRetrofit(@AuthClient okHttpClient: OkHttpClient, baseUrl: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideApiService(@Auth retrofit: Retrofit): OmangApiService =
        retrofit.create(OmangApiService::class.java)

    @Provides
    @Singleton
    fun provideRefreshApiService(@Auth retrofit: Retrofit): RefreshApiService =
        retrofit.create(RefreshApiService::class.java)

}