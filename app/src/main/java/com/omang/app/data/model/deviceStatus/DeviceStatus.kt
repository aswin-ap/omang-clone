package com.omang.app.data.model.deviceStatus

data class DeviceStatus(
    val csdkStatus: String,
    val deviceOwner: String,
    val pinnedStatus: String,
    val lockedStatus: String,
    val userStatus: String
)