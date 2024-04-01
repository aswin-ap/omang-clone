package com.omang.app.data.model.analytics

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnalyticItem(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("type")
    val type: Int?,

    @field:SerializedName("createdTime")
    val createdTime: Long?,

    @field:SerializedName("startTime")
    val startTime: String?,

    @field:SerializedName("endTime")
    val endTime: String?,

    @field:SerializedName("resource")
    val resourceId: Int?,

    @field:SerializedName("webPlatform")
    val webPlatformId: Int?,

    @field:SerializedName("webUrl")
    val webUrl: String?,

    @field:SerializedName("classroom")
    val classroomId: Int?,

    @field:SerializedName("lesson")
    val lessonId: Int?,

    @field:SerializedName("unit")
    val unitId: Int?,

    @field:SerializedName("psmId")
    val psmId: Int?,

    @field:SerializedName("menu")
    val menu: Int?,

    @field:SerializedName("latitude")
    val latitude: Double?,

    @field:SerializedName("longitude")
    val longitude: Double?,

    @field:SerializedName("logs")
    val logs: String?

) : Serializable