package com.omang.app.data.model.updates


import com.google.gson.annotations.SerializedName

data class Priority(
    @SerializedName("id") val id: String,
    @SerializedName("updateType") val updateType: Int,
    @SerializedName("details") val details: Details
)

data class Details(
    @SerializedName("storageCleanUp") val storageCleanUp: StorageCleanUp,
)

data class StorageCleanUp(

    @SerializedName("deleteThresholdInDays") val deleteThresholdInDays: Int,

    @SerializedName("idealFreeStorageInPercentage") val idealFreeStorageInPercentage: Int,

    )