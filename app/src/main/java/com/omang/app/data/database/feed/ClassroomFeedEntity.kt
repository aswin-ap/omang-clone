package com.omang.app.data.database.feed

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants

@Entity(tableName = DBConstants.CLASSROOM_FEED_TABLE)
class ClassroomFeedEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "feed_id")
    val id: String,
    @ColumnInfo(name = "resourceId")
    val resourceId: Int,
    @ColumnInfo(name = "classroom_id")
    val classroomId: Int,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "image")
    val imageUrl: String?,
    @ColumnInfo(name = "createdAt")
    val createdAt: String?,
    @Embedded(prefix = "createdBy")
    val createdBy: CreatedByEntity?,
    @ColumnInfo(name = "postedTo")
    val postedTo: String?,
    @Embedded(prefix = "classroomDetails")
    val classroomDetails: ClassroomDataEntity?,
)