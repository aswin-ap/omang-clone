package com.omang.app.ui.myClassroom.fragments.subjectContent.liveClassRoom.model

class LiveClassRoomEntity(

    val id: Int = 0,
    val name: String,
    val logo: String,
    val url: String?,
    val type: Int?,
    val status: String?

) {

    var hasInternetConnection: Boolean = true
}