package com.omang.app.data.model.updates


import com.google.gson.annotations.SerializedName

data class DeviceUpdatesData(
    @SerializedName("normal")
    val normal: List<Any?>?,
    @SerializedName("priority")
    val priority: List<Priority?>?,
    @SerializedName("deleted")
    val deleted: List<DeletedItems>,
    @field:SerializedName("added")
    val added: List<AddedItem?>?,
    @field:SerializedName("updated")
    val updated: List<UpdatedItem?>?
)