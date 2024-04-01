package com.omang.app.ui.techSupport.rate


import com.google.gson.annotations.SerializedName

data class RateRequest(
    @SerializedName("RateRequest")
    val feedback: String,
    @SerializedName("rating")
    val rating: Int
)