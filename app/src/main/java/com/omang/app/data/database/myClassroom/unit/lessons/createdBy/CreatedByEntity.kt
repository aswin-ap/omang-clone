package com.omang.app.data.database.myClassroom.unit.lessons.createdBy

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.omang.app.data.database.DBConstants

@Entity(tableName = DBConstants.CREATED_BY_TABLE)
class CreatedByEntity(
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "avatar")
    val avatar: String,

    )