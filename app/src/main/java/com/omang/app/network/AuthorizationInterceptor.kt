package com.omang.app.network

import com.omang.app.BuildConfig
import com.omang.app.data.model.token.AccessToken
import com.omang.app.dataStore.SharedPref
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class AuthorizationInterceptor(
    private val sharedPref: SharedPref
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val signedRequest = chain.request().signedRequest()
        var response = chain.proceed(signedRequest)

        if (response.code == 418) {
            runBlocking {
                val newAccessToken = getNewToken(sharedPref.refreshToken)
                newAccessToken.body()?.let {
                    sharedPref.accessToken = it.data?.accessToken!!
                    response.close()
                    response = chain.proceed(chain.request().signedRequest())

                }
            }
        }

        return response
    }

    private suspend fun getNewToken(refreshToken: String): retrofit2.Response<AccessToken> {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

        val retrofit = Retrofit.Builder().baseUrl(BuildConfig.BASE_URL.replace("app/", ""))
            .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()

        val service = retrofit.create(RefreshApiService::class.java)

        return service.tokenRefresh(refreshToken)
    }

    private fun Request.signedRequest(): Request {
        val token = runBlocking {
            sharedPref.accessToken
        }

        val bearer = "Bearer $token"
        Timber.tag("Authorization").v(bearer)

        return newBuilder()
            .header("Authorization", bearer)
            .build()
    }
}
