package com.omang.app.data.database.myClassroom.relation.unitTest

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import com.omang.app.data.database.DBConstants.Companion.UNIT_TEST_RELATION_TABLE

@Entity(
    tableName = UNIT_TEST_RELATION_TABLE,
    primaryKeys = ["unit_id", "test_id","classroom_id"]
)
class UnitTestRelationEntity(
    @ColumnInfo(name = "classroom_id")
    val classroomId: Int,
    @ColumnInfo(name = "unit_id")
    val unitId: Int,
    @ColumnInfo(name = "test_id")
    val testId: Int,
    @ColumnInfo(name = "isAttempted")
    var isAttempted: Int,
    @ColumnInfo(name = "testStatus")
    var testStatus: Int,
)