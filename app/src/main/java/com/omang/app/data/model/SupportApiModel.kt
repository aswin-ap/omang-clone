package com.omang.app.data.model

import com.google.gson.annotations.SerializedName


data class SupportApiModel(
    @SerializedName("url") var url: String? = null,
    @SerializedName("text") var text: String? = null
)