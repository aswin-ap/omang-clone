package com.omang.app.data.model.registerDevice

import com.google.gson.annotations.SerializedName

data class NetworkRegisterRequest(
    @SerializedName("imeiNo") var imeiNo: String,
    @SerializedName("simNo") var simNo: String,
    @SerializedName("fcmToken") var fcmToken: String,
    @SerializedName("secret") var secret: String,
    @SerializedName("brand") var brand: String,
    @SerializedName("model") var model: String
)