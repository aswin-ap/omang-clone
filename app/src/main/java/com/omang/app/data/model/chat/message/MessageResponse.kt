package com.omang.app.data.model.chat.message

import com.google.gson.annotations.SerializedName

data class MessageResponse(

    @field:SerializedName("data")
    val data: Message,

    @field:SerializedName("message")
    val message: String
)

data class Message(

    @field:SerializedName("messageText")
    val messageText: String,

    @field:SerializedName("messageId")
    val messageId: Int,

    @field:SerializedName("createdOn")
    val createdOn: String,

    @field:SerializedName("user")
    val user: User,

    @field:SerializedName("context")
    val context: Context? = null
)

data class User(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("firstName")
    val firstName: String,

    @field:SerializedName("lastName")
    val lastName: String,

    @field:SerializedName("avatar")
    val avatar: String,

    @field:SerializedName("userType")
    val userType: String
)

data class Context(
    @field:SerializedName("roomId")
    val roomId: Int,

    @field:SerializedName("roomType")
    val roomType: String,

    @field:SerializedName("techSupportId")
    val techSupportId: Int?,

    @field:SerializedName("techSupportIssue")
    val techSupportIssue: String?,

    @field:SerializedName("classroomId")
    val classroomId: Int?,

    @field:SerializedName("classroomName")
    val classroomName: String?,

    )
