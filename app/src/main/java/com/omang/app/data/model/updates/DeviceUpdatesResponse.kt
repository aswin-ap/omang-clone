package com.omang.app.data.model.updates


import com.google.gson.annotations.SerializedName


data class DeviceUpdatesResponse(
    @SerializedName("data")
    val deviceUpdatesData: DeviceUpdatesData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("statusCode")
    val statusCode: Int?
)