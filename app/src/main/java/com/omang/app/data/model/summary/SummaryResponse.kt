package com.omang.app.data.model.summary

import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myClassroom.ContentCountEntity
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.model.userAssign.Student

data class SummaryResponse(

    @field:SerializedName("data")
    val data: Data,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("statusCode")
    val statusCode: Int,
)

data class Data(
    @field:SerializedName("student")
    val student: Student,

    @field:SerializedName("myWebplatform")
    val myWebPlatform: List<MyWebPlatformItem>,

    @field:SerializedName("myLibrary")
    val myLibrary: List<MyLibraryItem>,

    @field:SerializedName("myClassroom")
    val myClassroom: List<MyClassroomItem>,

    @field:SerializedName("mcq")
    val mcq: McqSummary,
)

data class McqSummary(
    @field:SerializedName("attendedCount")
    val attendedCount: Int,
    @field:SerializedName("expiredCount")
    val expiredCount: Int,
    @field:SerializedName("notAttendedCount")
    val notAttendedCount: Int,
)

data class Settings(
    @field:SerializedName("deviceStorageLimit")
    val deviceStorageLimit: Int,

    @field:SerializedName("deviceResourceDeletionDays")
    val deviceResourceDeletionDays: Int,

    @field:SerializedName("deviceUnpinningClickCount")
    val deviceUnpinningClickCount: Int,

    @field:SerializedName("deviceUnpinningPassword")
    val deviceUnpinningPassword: String,

    @field:SerializedName("allowedIncomingPhNos")
    val allowedIncomingPhNos: List<String>,

    )

data class MyLibraryItem(
    @field:SerializedName("id")
    val id: Int,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("logo")
    val logo: String,
    @field:SerializedName("file")
    val file: String,
    @field:SerializedName("type")
    val type: String,
    @field:SerializedName("description")
    val description: String,
)

fun MyLibraryItem.asEntity(): ResourcesEntity = ResourcesEntity(
    id = this.id,
    name = this.name,
    description = this.description,
    type = this.type,
    logo = this.logo,
    file = this.file,
    isBonus = DBConstants.BonusContent.TRUE.value

)

data class MyWebPlatformItem(
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("logo")
    val logo: String,
    @field:SerializedName("id")
    val id: Int,
)

fun MyWebPlatformItem.asEntity(): MyWebPlatformEntity = MyWebPlatformEntity(
    id = this.id,
    name = this.name,
    logo = this.logo,
    url = null,
    altUrl = null,
    timeStamp = null,
    type = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value
)

fun MyWebPlatformItem.asExploreEntity(): MyWebPlatformEntity = MyWebPlatformEntity(
    id = this.id,
    name = this.name,
    logo = this.logo,
    url = null,
    altUrl = null,
    timeStamp = null,
    type = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value
)

data class MyClassroomItem(

    @field:SerializedName("contents")
    val contents: Contents,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("logo")
    val logo: String?,

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("type")
    val type: String,

    @field:SerializedName("updatedAt")
    val updatedAt: String,

    @field:SerializedName("unitProgress")
    val unitProgress: UnitProgress?,
)

data class UnitProgress(
    @field:SerializedName("total")
    var total: Int? = null,

    @field:SerializedName("completed")
    var completed: Int? = null

)

fun MyClassroomItem.asEntity(): MyClassroomEntity {
    var progress = 0
    this.unitProgress?.let {
        val completed = it.completed ?: 0
        val total = it.total ?: 0

        progress = if (total != 0) {
            (completed.toDouble() / total.toDouble() * 100).toInt()
        } else 0
    }
    return MyClassroomEntity(
        id = this.id,
        name = this.name,
        logo = this.logo ?: "",
        type = this.type,
        contents = this.contents.asEntity(),
        progress = progress

    )
}

data class Contents(

    @field:SerializedName("books")
    val books: Int,

    @field:SerializedName("videos")
    val videos: Int,

    @field:SerializedName("mcqs")
    val mcqs: Int,

    @field:SerializedName("lessons")
    val lessons: Int,

    @field:SerializedName("webPlatforms")
    val webPlatforms: Int,
)


fun Contents.asEntity(): ContentCountEntity = ContentCountEntity(
    books = this.books,
    videos = this.books,
    lessons = this.lessons,
    mcqs = this.mcqs,
    webPlatforms = this.webPlatforms
)

data class MoodMeter(
    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("displayName")
    val displayName: String,

    @field:SerializedName("systemName")
    val systemName: String,

    @field:SerializedName("color")
    val color: String,

    @field:SerializedName("emoji")
    val emoji: String

)