package com.omang.app.network

import com.omang.app.data.model.token.AccessToken
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RefreshApiService {
    @FormUrlEncoded
    @POST("auth/device/refresh")
    suspend fun tokenRefresh(
        @Field("refreshToken") refreshToken: String,
    ): Response<AccessToken>

}