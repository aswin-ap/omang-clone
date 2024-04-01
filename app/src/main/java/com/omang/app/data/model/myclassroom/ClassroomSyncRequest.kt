package com.omang.app.data.model.myclassroom

import com.google.gson.annotations.SerializedName

data class ClassroomSyncRequest(
    @field:SerializedName("books")
    var books: List<Int>? = null,

    @field:SerializedName("category")
    var category: String? = null,

    @field:SerializedName("resourceId")
    var resourceId: Int? = null,

    @field:SerializedName("units")
    var units: List<Unit>? = null,

    @field:SerializedName("mcqs")
    var mcqs: List<Int>? = null,

    @field:SerializedName("videos")
    var videos: List<Int>? = null,

    @field:SerializedName("webPlatforms")
    var webPlatforms: List<Int>? = null
)