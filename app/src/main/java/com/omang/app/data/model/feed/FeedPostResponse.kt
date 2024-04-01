package com.omang.app.data.model.feed


import com.google.gson.annotations.SerializedName

data class FeedPostResponse(
    @SerializedName("data")
    val `data`: PostData?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("statusCode")
    val statusCode: Int?
)

data class PostData(
    @SerializedName("classroom")
    val classroom: ClassRoom?,
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("createdBy")
    val createdBy: FeedCreatedBy?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("file")
    val `file`: String?,
    @SerializedName("id")
    val id: String,
    @SerializedName("resource")
    val resource: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("sentTo")
    val sentTo: String?,
    @SerializedName("updateType")
    val updateType: Int?
)

data class FeedCreatedBy(
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?
)