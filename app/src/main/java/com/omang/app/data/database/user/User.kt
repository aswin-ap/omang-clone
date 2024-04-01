package com.omang.app.data.database.user


data class User(
    var id: Int,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val dropPoints: Int? = null,
    val isDebugUser: Boolean = false,
    val school: School? = null,
    val dob: String? = null,
    val registeredAt: String? = null,
    val avatar: String? = null,
    val accessionNumber: String? = null,
    val email: String? = null
) {

}

data class School(
    val address: String? = null,
    val phone: String? = null,
    val name: String? = null,
    val id: Int? = null,
    val email: String? = null
)

