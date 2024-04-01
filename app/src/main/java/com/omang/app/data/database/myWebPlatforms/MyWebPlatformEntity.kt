package com.omang.app.data.database.myWebPlatforms

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.DBConstants.Companion.MY_WEB_PLATFORM_TABLE

@Entity(tableName = MY_WEB_PLATFORM_TABLE)
class MyWebPlatformEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "website_id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "logo")
    val logo: String,

    @ColumnInfo(name = "url")
    val url: String?,

    @ColumnInfo(name = "altUrl")
    val altUrl: String?,

    @ColumnInfo(name = "timeStamp")
    val timeStamp: String?,

    @ColumnInfo(name = "isFav")
    val isFav: Int = 0,

    @ColumnInfo(name = "type")
    val type: Int?,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Int? = 0
) {
    @Ignore
    var hasInternetConnection: Boolean = true
}