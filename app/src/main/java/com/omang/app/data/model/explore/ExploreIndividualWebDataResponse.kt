package com.omang.app.data.model.explore

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ExploreIndividualWebDataResponse(
    @field:SerializedName("statusCode")
    val statusCode: Int,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: WebItem

)
@Parcelize
data class WebItem(
    @field:SerializedName("id")
    val id: Int?,

    @field:SerializedName("name")
    val name: String?,

    @field:SerializedName("description")
    val description: String?,

    @field:SerializedName("url")
    val url: String?,

    @field:SerializedName("alternateUrls")
    val alternateUrls: List<String>?,

    @field:SerializedName("userAgent")
    val userAgent: String?,

    @field:SerializedName("threshold")
    val threshold: Int?,

    @field:SerializedName("logo")
    val logo: String?
) : Parcelable

