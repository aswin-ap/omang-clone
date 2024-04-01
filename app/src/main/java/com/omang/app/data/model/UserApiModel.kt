package com.omang.app.data.model

import com.google.gson.annotations.SerializedName

data class UserApiModel(
/*
    @SerializedName("id") var id: Int ,
    @SerializedName("email") var email: String? = null,
    @SerializedName("first_name") var firstName: String? = null,
    @SerializedName("last_name") var lastName: String? = null,
    @SerializedName("avatar") var avatar: String? = null
*/

@field:SerializedName("data")
val data: Data
)

data class Data(

    @field:SerializedName("firstName")
    val firstName:  String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("dropPoints")
    val dropPoints: Int,

    @field:SerializedName("school")
    val school: School,

    @field:SerializedName("dob")
    val dob: String? = null,

    @field:SerializedName("registeredAt")
    val registeredAt: String? = null,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("avatar")
    val avatar: String? = null,

    @field:SerializedName("accessionNumber")
    val accessionNumber: String? = null,

    @field:SerializedName("email")
    val email: String? = null
)

data class School(

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("email")
    val email: String? = null
)