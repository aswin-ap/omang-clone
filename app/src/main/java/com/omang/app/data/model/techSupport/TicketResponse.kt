package com.omang.app.data.model.techSupport

import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity

data class TicketResponse(

	@field:SerializedName("data")
	val data: Data ,

	@field:SerializedName("statusCode")
	val statusCode: Int
)

data class Data(

	@field:SerializedName("pagination")
	val pagination: Pagination?,

	@field:SerializedName("techSupports")
	val techSupports: List<TechSupportsItem>
)

data class TechSupportsItem(

    @field:SerializedName("createdAt")
	val createdAt: String,

    @field:SerializedName("issue")
	val issue: String,

    @field:SerializedName("roomId")
	val roomId: Int,

    @field:SerializedName("downloadSpeed")
	val downloadSpeed: String,

    @field:SerializedName("isClosed")
	val isClosed: Boolean,

    @field:SerializedName("phone")
    val phone: String,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("appTicketId")
    val appTicketId: String,

    @field:SerializedName("closedAt")
    val closedAt: String,

    @field:SerializedName("reopenedAt")
    val reopenedAt: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("unreadMessages")
    val unreadMessages: Int,

    @field:SerializedName("rating")
    val rating: Int?,

    @field:SerializedName("feedback")
    val feedback: String?,

)

fun TechSupportsItem.asEntity(): TicketsEntity = TicketsEntity(
    id = this.id.toString(),
    roomId = this.roomId,
    issue = this.issue,
    downloadSpeed = this.downloadSpeed,
    isClosed = this.isClosed,
    phone = this.phone,
    email = this.email,
    createdAt = this.createdAt,
    reopenedAt = this.reopenedAt,
    message = this.message,
    closedAt = this.closedAt,
    unreadMessages = this.unreadMessages,
    rating = this.rating,
    feedback = this.feedback,
    navigation = ""

)

data class Pagination(

	@field:SerializedName("itemsInPage")
	val itemsInPage: Int ,

	@field:SerializedName("totalPages")
	val totalPages: Int ,

	@field:SerializedName("page")
	val page: Int ,

	@field:SerializedName("totalCount")
	val totalCount: Int
)
