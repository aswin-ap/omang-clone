package com.omang.app.data.model.test


import com.google.gson.annotations.SerializedName

data class TestSubmitResponse(
    @SerializedName("data")
    val mcqResultData: List<McqResultData>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int
) {
    data class McqResultData(
        @SerializedName("mcq")
        val mcq: Int,
        @SerializedName("classroom")
        val classroom: Int,
        @SerializedName("unit")
        val unit: Int
    )
}