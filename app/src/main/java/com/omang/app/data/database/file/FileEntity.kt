package com.omang.app.data.database.file

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.FILE_TABLE
import com.omang.app.data.database.DBConstants.Companion.PSM_TABLE

@Entity(tableName = FILE_TABLE)
class FileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("file_id")
    val fileId: Int = 0,

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "created_time")
    var createdTime: Long? = null,

    )