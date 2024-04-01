package com.omang.app.data.database.myClassroom.relation.classroomResource

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.omang.app.data.database.DBConstants.Companion.CLASSROOM_RESOURCES_RELATION_TABLE
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.database.resource.ResourcesEntity

@Entity(
    tableName = CLASSROOM_RESOURCES_RELATION_TABLE,
    primaryKeys = ["classroom_id", "resource_id"],
    foreignKeys = [
        ForeignKey(
            entity = MyClassroomEntity::class,
            parentColumns = ["classroom_id"],
            childColumns = ["classroom_id"],
//            onDelete = ForeignKey.CASCADE // Or simply omit onDelete
        ),
        ForeignKey(
            entity = ResourcesEntity::class,
            parentColumns = ["resource_id"],
            childColumns = ["resource_id"],
//            onDelete = ForeignKey.CASCADE // Or simply omit onDelete
        )
    ],
    indices = [Index("resource_id")] // Create an index on the resource_id column

)
class ClassroomResourcesRelationEntity(

    @ColumnInfo(name = "classroom_id")
    val classroomId: Int,

    @ColumnInfo(name = "resource_id")
    val resourceId: Int
)


