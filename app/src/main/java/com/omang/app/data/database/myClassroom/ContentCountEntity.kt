package com.omang.app.data.database.myClassroom

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
class ContentCountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "books")
    var books: Int,
    @ColumnInfo(name = "videos")
    var videos: Int,
    @ColumnInfo(name = "mcqs")
    var mcqs: Int,
    @ColumnInfo(name = "lessons")
    var lessons: Int,
    @ColumnInfo(name = "webPlatforms")
    var webPlatforms: Int
) : Parcelable

