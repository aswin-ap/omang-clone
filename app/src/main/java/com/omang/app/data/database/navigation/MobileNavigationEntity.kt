package com.omang.app.data.database.navigation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.MOBILE_NAV_TABLE
import com.omang.app.utils.DeviceUtil
import java.sql.Date

@Entity(tableName = MOBILE_NAV_TABLE)
class MobileNavigationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "page")
    val page: String,

    @ColumnInfo(name = "subject_id")
    val subjectId: Int? = null,

    @ColumnInfo(name = "content_id")
    val contentId: Int? = null,

    @ColumnInfo(name = "event")
    val event: Int,

    @ColumnInfo(name = "comment")
    val comment: String? = null,

    @ColumnInfo(name = "createdTime")
    val createdTime: java.util.Date = Date(System.currentTimeMillis()),

    @ColumnInfo(name = "app_version")
    val appVersion: String = DeviceUtil.getAppVersion(),
)