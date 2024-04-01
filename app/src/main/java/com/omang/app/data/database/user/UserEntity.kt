package com.omang.app.data.database.user

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.USER_TABLE

@Entity(tableName = USER_TABLE)
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    val firstName: String? = null,
    val lastName: String? = null,
    val phone: String? = null,
    val dropPoints: Int? = null,
    @Embedded val school: SchoolEntity? = null,
    val dob: String? = null,
    val registeredAt: String? = null,
    val avatar: String? = null,
    val accessionNumber: String? = null,
    val email: String? = null
)

fun UserEntity.asExternalModel(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        dropPoints = dropPoints,
        school = school?.asExternalModel(),
        dob = dob,
        registeredAt = registeredAt,
        avatar = avatar,
        accessionNumber = accessionNumber,
        email = email
    )
}

data class SchoolEntity(
    val school_address: String? = null,
    val school_phone: String? = null,
    val school_name: String? = null,
    val school_id: Int? = null,
    val school_email: String? = null
)

fun SchoolEntity.asExternalModel(): School {
    return School(
        school_address,
        school_phone,
        school_name,
        school_id,
        email = school_email
    )
}



