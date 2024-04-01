package com.omang.app.data.database.myClassroom.unit.lessons

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.omang.app.data.database.DBConstants.Companion.LESSON_TABLE
import com.omang.app.data.database.myClassroom.unit.lessons.createdBy.CreatedByEntity
import com.omang.app.data.ui.DownloadStatus

@Entity(tableName = LESSON_TABLE)
class LessonsEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "lesson_id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "logo")
    val logo: String,
    @field:SerializedName("file")
    val file: String,
    @ColumnInfo(name = "mimeType")
    val mimeType: String,
    @ColumnInfo(name = "lesson_rate")
    var lessonRate: Float? = null,

    @Embedded(prefix = "createdBy")
    val createdBy: CreatedByEntity,

    ) {
    @Ignore
    var progress: Int = 0

    @Ignore
    var downloadStatus: DownloadStatus = DownloadStatus.NOT_START

}
