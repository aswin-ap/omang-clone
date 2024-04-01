package com.omang.app.data.model.updates

import com.google.gson.annotations.SerializedName
import com.omang.app.data.model.feed.ClassRoom


data class AddedItem(

    @field:SerializedName("updateType")
    val updateType: Int? = null,

    @field:SerializedName("details")
    val details: List<DetailsItem?>? = null
)

data class DetailsItem(

    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("resource")
    val resource: Int,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("description")
    val description: String,

    @field:SerializedName("file")
    val file: String?,

    @field:SerializedName("createdBy")
    val createdBy: CreatedBy? =null,

    @field:SerializedName("classroom")
    val classroom: Int,

    @field:SerializedName("classroomDetails")
    val classroomDetails: ClassRoom? = null,

    @field:SerializedName("sentTo")
    val sentTo: String? = null,

    @field:SerializedName("unit")
    val unit: Int,

    @field:SerializedName("type")
    val type: String,

    @field:SerializedName("logo")
    val logo: String,

    @field:SerializedName("isPopUp")
    val isPopUp: Boolean? = null
)

data class CreatedBy(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("avatar")
    val avatar: String? = null,

    )
