package com.omang.app.data.database.myClassroom

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.MY_CLASSROOM_TABLE


@Entity(tableName = MY_CLASSROOM_TABLE)
class MyClassroomEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "classroom_id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "logo")
    val logo: String,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "updatedAt")
    var updatedAt: String? = "",
    @ColumnInfo(name = "progress")
    val progress: Int,

    @Embedded(prefix = "classroom_content_count_")
    val contents: ContentCountEntity,

    )