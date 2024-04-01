package com.omang.app.data.model.registerDevice

import com.google.gson.annotations.SerializedName

data class RegisterResponseData(

    @SerializedName("accessToken") var accessToken: String? = null,
    @SerializedName("refreshToken") var refreshToken: String? = null

)