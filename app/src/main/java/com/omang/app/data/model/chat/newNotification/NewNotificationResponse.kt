package com.omang.app.data.model.chat.newNotification

import com.google.gson.annotations.SerializedName

data class NewNotificationResponse(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("message")
    val message: String? = null
)

data class Data(

    @field:SerializedName("context")
    val context: Context? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("message")
    val message: Message? = null
)

data class Message(

    @field:SerializedName("messageStatus")
    val messageStatus: List<MessageStatusItem?>? = null,

    @field:SerializedName("messageId")
    val messageId: Int? = null,

    @field:SerializedName("messageText")
    val messageText: String? = null,

    @field:SerializedName("createdOn")
    val createdOn: String? = null,

    @field:SerializedName("user")
    val user: User? = null
)

data class Context(

    @field:SerializedName("techSupportId")
    val techSupportId: Int? = null,

    @field:SerializedName("classroomName")
    val classroomName: Any? = null,

    @field:SerializedName("classroomId")
    val classroomId: Int? = null,

    @field:SerializedName("roomId")
    val roomId: Int? = null,

    @field:SerializedName("roomType")
    val roomType: String? = null,

    @field:SerializedName("techSupportIssue")
    val techSupportIssue: String? = null
)


data class MessageStatusItem(

    @field:SerializedName("createdOn")
    val createdOn: String? = null,

    @field:SerializedName("user")
    val user: User? = null,

    @field:SerializedName("status")
    val status: String? = null
)

data class User(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("userType")
    val userType: String? = null
)
