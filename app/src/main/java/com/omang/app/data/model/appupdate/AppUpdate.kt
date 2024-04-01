package com.omang.app.data.model.appupdate


import com.google.gson.annotations.SerializedName


data class AppUpdate(
    @SerializedName("android10_path")
    val android10Path: String?,
    @SerializedName("android13_path")
    val android13Path: String?,
    @SerializedName("appLatestVersion")
    val appLatestVersion: String?,
    @SerializedName("isForceUpdate")
    val isForceUpdate: Boolean,
    @SerializedName("releaseNote")
    val releaseNote: String?,
    @SerializedName("forceUpdateDate")
    val forceUpdateDate: String?
)