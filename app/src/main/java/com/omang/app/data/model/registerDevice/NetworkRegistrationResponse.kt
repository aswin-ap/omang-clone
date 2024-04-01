package com.omang.app.data.model.registerDevice

import com.google.gson.annotations.SerializedName

data class NetworkRegistrationResponse(
    @SerializedName("statusCode") var statusCode: Int,
    @SerializedName("message") var message: String,
    @SerializedName("error") var error: String,
    @SerializedName("data") var registerResponseData: RegisterResponseData? = RegisterResponseData()

)