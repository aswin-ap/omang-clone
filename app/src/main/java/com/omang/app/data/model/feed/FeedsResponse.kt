package com.omang.app.data.model.feed

import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.feed.ClassroomDataEntity
import com.omang.app.data.database.feed.ClassroomFeedEntity
import com.omang.app.data.database.feed.CreatedByEntity
import com.omang.app.data.database.feed.FeedEntity

data class FeedsResponse(

    @field:SerializedName("data")
    val data: Data,

    @field:SerializedName("statusCode")
    val statusCode: Int? = null,
)

data class Data(

    @field:SerializedName("feeds")
    val notificationLogs: List<NotificationItem>,

    @field:SerializedName("pagination")
    val pagination: Pagination? = null,
)

data class NotificationItem(

    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("resource")
    val resource: Int,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("file")
    val file: String? = null,

    @field:SerializedName("createdBy")
    val createdBy: CreatedBy? = null,

    @field:SerializedName("createdAt")
    val createdAt: String? = null,

    @field:SerializedName("sentTo")
    val sentTo: String? = null,

    @field:SerializedName("classroom")
    val classroom: ClassRoom? = null,

    @field:SerializedName("updateType")
    val updateType: Int? = null,
)

data class CreatedBy(
    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("avatar")
    val avatar: String? = null,
)

data class ClassRoom(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,
)

fun NotificationItem.asEntity(): FeedEntity =
    FeedEntity(
        id = this.id,
        resourceId = this.resource,
        title = this.name ?: "",
        description = this.description ?: "",
        imageUrl = this.file,
        createdAt = this.createdAt,
        createdBy = this.createdBy?.asEntity(),
        classroomDetails = this.classroom?.asEntity(),
        postedTo = this.sentTo,
        feedType = this.updateType
    )

private fun ClassRoom?.asEntity(): ClassroomDataEntity =
    ClassroomDataEntity(
        classRoomOrClub = this?.name,
        classRoomId = this?.id
    )

fun NotificationItem.asClassroomFeedEntity(classroomId: Int): ClassroomFeedEntity =
    ClassroomFeedEntity(
        id = this.id,
        resourceId = this.resource,
        classroomId = classroomId,
        title = this.name ?: "",
        description = this.description ?: "",
        imageUrl = this.file,
        createdAt = this.createdAt,
        createdBy = this.createdBy?.asEntity(),
        classroomDetails = this.classroom?.asEntity(),
        postedTo = this.sentTo
    )

fun CreatedBy.asEntity(): CreatedByEntity =
    CreatedByEntity(
        firstName = this.firstName,
        lastName = this.lastName,
        avatar = this.avatar
    )

data class Pagination(

    @field:SerializedName("itemsInPage")
    val itemsInPage: Int? = null,

    @field:SerializedName("totalPages")
    val totalPages: Int? = null,

    @field:SerializedName("page")
    val page: Int? = null,

    @field:SerializedName("totalCount")
    val totalCount: Int? = null,
)
