package com.omang.app.data.model.resources


import com.google.gson.annotations.SerializedName

data class LibraryResourceResponse(
    @SerializedName("data")
    val data: LibraryData?,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int
)

data class LibraryData(
    @SerializedName("added")
    val added: List<LibraryAdded>,
    @SerializedName("deleted")
    val deleted: List<Int>
)

data class LibraryAdded(
    @SerializedName("description")
    val description: String,
    @SerializedName("file")
    val file: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("logo")
    val logo: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String
)