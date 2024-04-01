package com.omang.app.data.model.appupdate


import com.google.gson.annotations.SerializedName


data class AppUpdateResponse(
    @SerializedName("data")
    val appUpdateResponseData: AppUpdateResponseData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("statusCode")
    val statusCode: Int?
)