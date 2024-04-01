package com.omang.app.data.database.resource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.RESOURCES_TABLE
import com.omang.app.data.ui.DownloadStatus

/**
ResourcesEntity table is to store the book and video resources,
and now the classroom's book and video will be mapped with a ResourcesRelationEntity mapping table
 */
@Entity(tableName = RESOURCES_TABLE)
class ResourcesEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "resource_id")
    val id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "logo")
    val logo: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "file")
    val file: String?,
    @ColumnInfo(name = "type")
    val type: String?,// "book" or "video"
    @ColumnInfo(name = "isBonus")
    val isBonus: Int,
) {
    @Ignore
    var progress: Int = 0

    @Ignore
    var downloadStatus: DownloadStatus = DownloadStatus.NOT_START
}