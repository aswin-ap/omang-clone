package com.omang.app.data.model.deviceUpdate

import com.google.gson.annotations.SerializedName

data class StatusRequest(

    @field:SerializedName("appVersion")
    var appVersion: String? = null,

    @field:SerializedName("totalDiskSpace")
    val totalDiskSpace: Int? = null,

    @field:SerializedName("lockedStatus")
    var lockedStatus: String? = null,

    @field:SerializedName("freeDiskSpacePercentage")
    val freeDiskSpacePercentage: Int? = null,

    @field:SerializedName("mobileDataStatus")
    val mobileDataStatus: String? = null,

    @field:SerializedName("wifiStatus")
    val wifiStatus: String? = null,

    @field:SerializedName("csdkStatus")
    var csdkStatus: String? = null,

    @field:SerializedName("diskSpaceUsed")
    val diskSpaceUsed: Int? = null,

    @field:SerializedName("hotspotStatus")
    val hotspotStatus: String? = null,

    @field:SerializedName("deviceSleepTime")
    val deviceSleepTime: Int? = null,

    @field:SerializedName("simNo")
    val simNo: String? = null,

    @field:SerializedName("pinnedStatus")
    var pinnedStatus: String? = null,

    @field:SerializedName("deviceOwnerStatus")
    var deviceOwnerStatus: String? = null,

    @field:SerializedName("userStatus")
    var userStatus: String? = null


)
