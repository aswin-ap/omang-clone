package com.omang.app.data.repository

import com.google.gson.Gson
import com.omang.app.data.NetworkResult
import com.omang.app.data.database.LocalDataSource
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.data.database.user.asExternalModel
import com.omang.app.data.model.userAssign.Student
import com.omang.app.data.model.userAssign.UserAssignResponse
import com.omang.app.data.model.userAssign.asEntity
import com.omang.app.network.RemoteDataSource
import com.omang.app.utils.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val analyticsRepository: AnalyticsRepository

) {

    fun assignUser(
        userId: Int,
        simNo: String,
        secret: String,
        ): Flow<NetworkResult<UserAssignResponse>> {
        return flow {
            emit(NetworkResult.Loading())
            val userResponse: Response<UserAssignResponse> =
                remoteDataSource.assignUser(userId, simNo, secret)
            if (userResponse.isSuccessful) {
                userResponse.body()?.let {
                    return@flow emit(NetworkResult.Success(it))
                }
            }
            val errorMessage = userResponse.errorBody()?.string() ?: "Unknown error"
            val errorResponse = Gson().fromJson(errorMessage, UserAssignResponse::class.java)
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

    suspend fun saveUser(userEntityList: Student) = localDataSource.insertUser(userEntityList.asEntity())

    suspend fun getUsers() = localDataSource.getUser()?.asExternalModel()
    suspend fun getUserFirstName() = localDataSource.getUserFirstName()
    suspend fun clearUser() = localDataSource.clearUser()
}