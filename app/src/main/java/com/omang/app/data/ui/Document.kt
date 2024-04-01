package com.omang.app.data.ui

enum class DownloadStatus { NOT_START, INTIALISED, STARTED, ERROR, FINISHED, PROGRESS }

data class Document(
    val id: String,
    val cover: String? = null,
    val role: String? = null,
    val file: String? = null,
    val isDeleted: Boolean? = false,
    val description: String,
    val language: String? = null,
    val title: String,
    val type: String? = null,
    val createdOn: String? = null,
    val downloadIdFromServer: String? = null,
    val download_Id: Int = 0,
    val message: String? = null,
    var progress: Int = 0,
    var downloadStatus: DownloadStatus = DownloadStatus.NOT_START
)