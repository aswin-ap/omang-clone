package com.omang.app.data.model.techSupport


import com.google.gson.annotations.SerializedName

data class IssueResponse(
    @SerializedName("data")
    val data: List<TechSupportsItem>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int
)