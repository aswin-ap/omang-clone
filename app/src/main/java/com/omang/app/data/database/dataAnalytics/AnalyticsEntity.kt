package com.omang.app.data.database.dataAnalytics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants.Companion.DATA_ANALYTICS

@Entity(tableName = DATA_ANALYTICS)
    class AnalyticsEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "type")
    var type: Int? = null,

    @ColumnInfo(name = "created_time")
    var createdTime: Long? = null,

    @ColumnInfo(name = "start_time")
    var startTime: String? = null,

    @ColumnInfo(name = "end_time")
    var endTime: String? = null,

    // Watch history
    @ColumnInfo(name = "resource_id")
    var resourceId: Int? = null,

    @ColumnInfo(name = "webPlatform_id")
    var webPlatformId: Int? = null,

    @ColumnInfo(name = "web_url")
    var webUrl: String? = null,

    @ColumnInfo(name = "classroom_id")
    var classroomId: Int? = null,

    @ColumnInfo(name = "lesson_id")
    var lessonId: Int? = null,

    @ColumnInfo(name = "unit_id")
    var unitId: Int? = null,

    @ColumnInfo(name = "psm_id")
    var psmId: Int? = null,

    @ColumnInfo(name = "menu")
    var menu: Int? = null,

    @ColumnInfo(name = "latitude")
    var latitude: Double? = null,

    @ColumnInfo(name = "longitude")
    var longitude: Double? = null,

    @ColumnInfo(name = "logs")
    var logs: String? = null

)

// for book (booo)