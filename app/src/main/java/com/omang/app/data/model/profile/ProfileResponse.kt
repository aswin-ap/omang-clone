package com.omang.app.data.model.profile


import com.google.gson.annotations.SerializedName
import com.omang.app.data.model.userAssign.Student

data class ProfileResponse(
    @SerializedName("data")
    val `data`: Student?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("statusCode")
    val statusCode: Int?
)