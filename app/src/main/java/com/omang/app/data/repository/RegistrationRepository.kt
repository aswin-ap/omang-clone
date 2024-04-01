package com.omang.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.LocalDataSource
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.model.registerDevice.NetworkRegistrationResponse
import com.omang.app.data.model.registerDevice.RegisterResponseData
import com.omang.app.network.RemoteDataSource
import com.omang.app.utils.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject

class RegistrationRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val analyticsRepository: AnalyticsRepository
) {
    fun registerDevice(
        imeiNo: String,
        simNo: String,
        fcmToken: String,
        secret: String,
        brand: String,
        model: String,
        appVersion: String,
        deviceOS: Int
    ): Flow<NetworkResult<RegisterResponseData>> {
        return flow {
            emit(NetworkResult.Loading())
            val userResponse: Response<NetworkRegistrationResponse> =
                remoteDataSource.registerDevice(
                    imeiNo,
                    simNo,
                    fcmToken,
                    secret,
                    brand,
                    model,
                    appVersion,
                    deviceOS
                )
            if (userResponse.isSuccessful) {
                userResponse.body()?.let {
                    return@flow emit(NetworkResult.Success(it.registerResponseData!!))
                }
            }
            val errorMessage = userResponse.errorBody()?.string() ?: "Unknown error"
            val errorResponse =
                Gson().fromJson(errorMessage, NetworkRegistrationResponse::class.java)
            val error = errorResponse?.error ?: "Unknown error"

            analyticsRepository.insertErrorLog(
                AnalyticsEntity(
                    createdTime = ViewUtil.getCurrentEpochTime(),
                    startTime = ViewUtil.getUtcTimeWithMSec(),
                    logs = userResponse.raw().toString()
                )
            )
            return@flow emit(NetworkResult.Error(error, userResponse.code()))
        }.flowOn(Dispatchers.IO).catch { exceptions ->
            emit(NetworkResult.Failure(exceptions))
        }
    }


}