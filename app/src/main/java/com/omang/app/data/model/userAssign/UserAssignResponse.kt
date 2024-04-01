package com.omang.app.data.model.userAssign

import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.user.SchoolEntity
import com.omang.app.data.database.user.UserEntity
import com.omang.app.data.model.summary.MoodMeter
import com.omang.app.data.model.summary.Settings

data class UserAssignResponse(

    @field:SerializedName("data")
    val student: Student? = null,

    @field:SerializedName("mood")
    val moods: Moods? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("error")
    val error: String? = null,

    @field:SerializedName("statusCode")
    val statusCode: Int? = null
)


data class Student(

    @field:SerializedName("firstName")
    val firstName: String? = null,

    @field:SerializedName("lastName")
    val lastName: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("dropPoints")
    val dropPoints: Int? = null,

    @field:SerializedName("isDebugUser")
    val isDebugUser: Boolean = false,

    @field:SerializedName("school")
    val school: School? = null,

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
    val email: String? = null,

    @field:SerializedName("psm")
    val psm: List<Psm> = emptyList(),

    @field:SerializedName("settings")
    val settings: Settings? = null,

)

fun Student.asEntity(): UserEntity = UserEntity(
    id = this.id,
    firstName = this.firstName,
    lastName = this.lastName,
    phone = this.phone,
    dropPoints = this.dropPoints,
    school = this.school?.asEntity(),
    dob = this.dob,
    registeredAt = this.registeredAt,
    avatar = this.avatar,
    accessionNumber = this.accessionNumber,
    email = this.email,
)

data class Moods(

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("systemName")
    val systemName: Int? = null,

    @field:SerializedName("color")
    val color: String? = null
)

//fun Moods.asEntity(): ModeMeterEntity = ModeMeterEntity(
//    emoji = this.mood,
//    time = this.date,
//    rating =
//
//    )
data class Doe(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("title")
    val title: String,

    @field:SerializedName("logo")
    val logo: String
)

data class School(

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("phone")
    val phone: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("doe")
    val doe: Doe? = null
)

data class Psm(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("index")
    val index: Int? = null,


    @field:SerializedName("url")
    val url: String? = null,
)

fun School.asEntity() = SchoolEntity(
    school_id = this.id,
    school_name = name,
    school_address = this.address,
    school_phone = this.phone,
    school_email = this.email,
)