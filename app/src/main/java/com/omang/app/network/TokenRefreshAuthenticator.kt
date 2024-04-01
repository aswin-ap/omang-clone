package com.omang.app.network

import com.omang.app.BuildConfig
import com.omang.app.data.model.token.AccessToken
import com.omang.app.dataStore.SharedPref
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenRefreshAuthenticator(val sharedPref: SharedPref) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        runBlocking {
            try {
                val newToken = getNewToken(sharedPref.refreshToken)

                newToken.body()?.let {
                    sharedPref.accessToken = it.data?.accessToken!!

                    return@runBlocking response.request.newBuilder()
                        .header("Authorization", "Bearer ${sharedPref.accessToken}")
                        .build()

                }

            } catch (error: Throwable) {
                error.printStackTrace()

            }
        }

        return null
    }

    suspend fun getNewToken(refreshToken: String): retrofit2.Response<AccessToken> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL.replace("app/", ""))
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val service = retrofit.create(RefreshApiService::class.java)

        return service.tokenRefresh(refreshToken)
    }
}