package com.omang.app.network

import javax.inject.Inject

class RefreshApiSource @Inject constructor(
    private val refreshApiService: RefreshApiService,
) {
    suspend fun tokenRefresh(
        refreshToken: String,
    ) = refreshApiService.tokenRefresh(
        refreshToken
    )

}