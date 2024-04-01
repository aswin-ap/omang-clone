package com.omang.app.data.model.techSupport

import com.google.gson.annotations.SerializedName
import com.omang.app.utils.DeviceUtil

data class IssueRequest(
    @SerializedName("tickets")
    var tickets: List<Ticket> = arrayListOf(),

)

data class Ticket(

    @field:SerializedName("issue")
    var issue: String? = null,

    @field:SerializedName("email")
    var email: String? = null,

    @field:SerializedName("phone")
    var phone: String? = null,

    @field:SerializedName("appTicketId")
    var appTicketId: String? = null,

    @field:SerializedName("appVersion")
    var appVersion: String? = DeviceUtil.getAppVersion(),

    @field:SerializedName("ticketPostedScreen")
    var ticketPostedScreen: String? = null,

    @field:SerializedName("ticketPostedAt")
    var ticketPostedAt: String? = null,

    @field:SerializedName("downloadSpeed")
    var downloadSpeed: String? = null,

    @field:SerializedName("resources")
    var resources: List<Int>? = null,

    @SerializedName("latestActivities")
    var latestActivities: List<NavigationModel> = arrayListOf()

)

data class NavigationModel(
    @SerializedName("appVersion") var appVersion: String? = null,
    @SerializedName("comment") var comment: String? = null,
    @SerializedName("page") var page: String? = null,
    @SerializedName("when") var whenTime: String? = null
)