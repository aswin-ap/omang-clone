package com.omang.app.data.model.appupdate


import com.google.gson.annotations.SerializedName


data class AppUpdateResponseData(
    @SerializedName("appUpdate")
    val appUpdate: AppUpdate?
)