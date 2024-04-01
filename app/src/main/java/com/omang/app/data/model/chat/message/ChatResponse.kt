package com.omang.app.data.model.chat.message

import com.google.gson.annotations.SerializedName

data class ChatResponse(

    @SerializedName("data")
    val data: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int

)

data class Data(

    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int

)