package com.omang.app.ui.techSupport.rate


import com.google.gson.annotations.SerializedName

data class RateResponse(
    @SerializedName("data")
    val data: Any?,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int
)