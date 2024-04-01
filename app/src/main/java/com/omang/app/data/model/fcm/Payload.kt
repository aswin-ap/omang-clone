package com.omang.app.data.model.fcm

import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("update_type")
    val updateType: Int,
    @SerializedName("status")
    val status: String,
    var body: String?,
    var title: String?,
    var classroom: Int?,
)