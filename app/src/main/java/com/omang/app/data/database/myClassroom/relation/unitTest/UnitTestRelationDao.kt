package com.omang.app.data.database.myClassroom.relation.unitTest

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UnitTestRelationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnitTestRelation(relationEntities: List<UnitTestRelationEntity>)

    @Update
    suspend fun updateUniTestRelation(unitTestRelationEntity: UnitTestRelationEntity)

    @Query("SELECT COUNT(*) FROM relation_unit_test WHERE testStatus = 1 AND unit_id = :unitId AND classroom_id = :classroomId")
    suspend fun checkIfTestIsPassed(unitId: Int, classroomId: Int): Int

    @Query("SELECT COUNT(*) FROM relation_unit_test WHERE testStatus == 0 AND unit_id = :unitId AND classroom_id = :classroomId")
    suspend fun checkIfTestsAvailableToAttend(unitId: Int, classroomId: Int): Int

    @Query("SELECT * FROM relation_unit_test WHERE unit_id = :unitId AND test_id = :testId AND classroom_id = :classroomId")
    suspend fun getUniTestEntity(unitId: Int, testId: Int, classroomId: Int): UnitTestRelationEntity


    @Query("SELECT * FROM relation_unit_test WHERE isAttempted == 1")
    suspend fun getAllAttemptedTests(): List<UnitTestRelationEntity>

    @Update
    suspend fun updateUnitMcqTests(unitTestRelationEntity: List<UnitTestRelationEntity>)

    @Query("DELETE FROM relation_unit_test")
    suspend fun clear()
}