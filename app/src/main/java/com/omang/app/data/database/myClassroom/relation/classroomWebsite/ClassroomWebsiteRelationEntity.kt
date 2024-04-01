package com.omang.app.data.database.myClassroom.relation.classroomWebsite

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.omang.app.data.database.DBConstants.Companion.CLASSROOM_WEBSITE_RELATION_TABLE
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity

@Entity(
    tableName = CLASSROOM_WEBSITE_RELATION_TABLE,
    primaryKeys = ["classroom_id", "website_id"],
    foreignKeys = [
        ForeignKey(
            entity = MyClassroomEntity::class,
            parentColumns = ["classroom_id"],
            childColumns = ["classroom_id"],
            onDelete = ForeignKey.NO_ACTION // Or simply omit onDelete
        ),
        ForeignKey(
            entity = MyWebPlatformEntity::class,
            parentColumns = ["website_id"],
            childColumns = ["website_id"],
            onDelete = ForeignKey.NO_ACTION // Or simply omit onDelete
        )
    ],
    indices = [Index("website_id")] // Create an index on the resource_id column

)

class ClassroomWebsiteRelationEntity(
    @ColumnInfo(name = "classroom_id")
    val classroomId: Int,

    @ColumnInfo(name = "website_id")
    val websiteId: Int,

    )
