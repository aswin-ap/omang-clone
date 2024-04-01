package com.omang.app.data.model.techSupport

import com.google.gson.annotations.SerializedName

data class ImageUpload(

    @field:SerializedName("data")
    val data: ImageData,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("statusCode")
    val statusCode: Int
)

data class ImageData(

    @field:SerializedName("id")
    val id: Int,

    @field:SerializedName("fileName")
    val fileName: String,

    @field:SerializedName("originalFileName")
    val originalFileName: String,

    @field:SerializedName("fileSize")
    val fileSize: Int,

    @field:SerializedName("fileType")
    val fileType: String,

    @field:SerializedName("mimeType")
    val mimeType: String,

    @field:SerializedName("filePath")
    val filePath: String,

    @field:SerializedName("createdAt")
    val createdAt: String,

    )