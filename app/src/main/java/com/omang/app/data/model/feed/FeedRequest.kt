package com.omang.app.data.model.feed


import com.google.gson.annotations.SerializedName

data class FeedRequest(
    @SerializedName("classroom")
    val classroom: Int?,
    @SerializedName("description")
    val description: String,
    @SerializedName("image")
    val image: Int?,
    @SerializedName("sentTo")
    val sentTo: String
)