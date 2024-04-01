package com.omang.app.data.database.test

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.omang.app.data.database.DBConstants

@Dao
interface TestDao {
    @Update
    suspend fun update(testEntity: TestEntity)

    /**
     * This function updates the tests to already sent to server
     */
    @Query(
        "UPDATE test_table SET type = 2, isSentToApi = 1 WHERE generalMcqId = :testId AND " +
                "classroomId = :classroomId AND unitId = :unitId"
    )
    suspend fun updateTestStatus(testId: Int, classroomId: Int, unitId: Int)

    @Query(
        "UPDATE test_table SET type = :type AND generalMcqId = :testId"
    )
    suspend fun updateTestToExpired(testId: Int, type: Int = DBConstants.TestType.EXPIRED.value)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(testEntity: List<TestEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(testEntity: TestEntity)

    @Transaction
    @Query("SELECT * FROM test_table WHERE id IN (SELECT test_id FROM relation_unit_test WHERE unit_id = :unitId)")
    suspend fun getTestsByUnitId(unitId: Int): List<TestEntity>

    @Query("SELECT * FROM test_table WHERE type = 0 and mcq_type = 1")
    fun getAllNewTests(): List<TestEntity>

    @Query("SELECT * FROM test_table WHERE type = 1 and mcq_type = 1")
    fun getAllExpiredTests(): List<TestEntity>

    @Query("SELECT * FROM test_table WHERE type = 2")
    fun getAllAttemptedTests(): List<TestEntity>

    @Query("SELECT * FROM test_table WHERE type = 2 AND isSentToApi = 0")
    fun getAllAttemptedOfflineTests(): List<TestEntity>

    @Query("SELECT COUNT(*) FROM test_table WHERE classroomId = :classroomId AND unitId = :unitId AND generalMcqId = :testId")
    fun isTestExist(classroomId: Int, unitId: Int, testId: Int): Int

    @Query("SELECT COUNT(*) FROM test_table WHERE generalMcqId = :testId AND mcq_type = :type")
    fun isClassroomTestExist(testId: Int, type: Int = DBConstants.TestContent.CLASSROOM.value): Int

    @Transaction
    @Query(
        "SELECT * FROM test_table WHERE generalMcqId IN (SELECT test_id FROM relation_unit_test WHERE " +
                "unit_id = :unitId AND classroom_id = :classroomId AND testStatus == 0) AND " +
                "classroomId = :classroomId AND unitId = :unitId AND mcq_type = :type LIMIT 1"
    )
    fun getUnitMainMcq(
        unitId: Int,
        classroomId: Int,
        type: Int = DBConstants.TestContent.UNIT.value
    ): TestEntity?

    @Transaction
    @Query(
        "SELECT COUNT(*) FROM test_table WHERE generalMcqId IN (SELECT test_id FROM relation_unit_test WHERE " +
                "unit_id = :unitId AND classroom_id = :classroomId) AND " +
                "classroomId = :classroomId AND unitId = :unitId"
    )
    fun mcqCount(unitId: Int, classroomId: Int): Int

    @Transaction
    @Query(
        "SELECT COUNT(*) FROM test_table WHERE generalMcqId IN (SELECT test_id FROM relation_unit_test WHERE " +
                "unit_id = :unitId AND classroom_id = :classroomId AND testStatus == 0) AND " +
                "classroomId = :classroomId AND unitId = :unitId"
    )
    fun getNotAttemptedMcqCountByClassroomAndUnit(unitId: Int, classroomId: Int): Int

    @Delete
    suspend fun deleteTests(testEntity: List<TestEntity>)

    @Query("SELECT * FROM test_table WHERE generalMcqId = :testId")
    suspend fun getTestById(testId: Int): TestEntity

    @Query("SELECT * FROM test_table WHERE type = :type and mcq_type = :testType AND classroomId =:classroomId")
    fun getClassroomTestsByType(
        classroomId: Int, type: Int,
        testType: Int = DBConstants.TestContent.CLASSROOM.value
    ): List<TestEntity>

    @Query("DELETE FROM test_table")
    suspend fun clear()
    @Transaction
    @Query("SELECT generalMcqId FROM test_table WHERE generalMcqId IN (SELECT test_id FROM relation_unit_test WHERE " +
            "unit_id = :unitId AND classroom_id = :classroomId) AND "+
            "classroomId = :classroomId AND unitId = :unitId")
    fun getMcqId(unitId: Int,classroomId: Int): List<Int>

    @Query("SELECT generalMcqId FROM test_table WHERE classroomId = :classroomId AND mcq_type = 1")
    fun getClassroomMcqId(classroomId: Int): List<Int>

}
