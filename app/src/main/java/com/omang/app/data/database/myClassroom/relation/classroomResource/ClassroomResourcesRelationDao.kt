package com.omang.app.data.database.myClassroom.relation.classroomResource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClassroomResourcesRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassroomResourcesRelations(relations: List<ClassroomResourcesRelationEntity>)

    @Query("DELETE FROM relation_classroom_resource WHERE classroom_id = :classroomId AND resource_id = :resourceId")
    suspend fun deleteClassroomResourceRelation(classroomId: Int, resourceId: Int) : Int

    @Query("SELECT COUNT(*) FROM relation_classroom_resource WHERE resource_id = :resourceId")
    suspend fun checkIfResourceExists(resourceId: Int): Int
    @Query("DELETE FROM relation_classroom_resource WHERE classroom_id = :classroomId")
    suspend fun deleteClassroomResourceMapping(classroomId: Int)

    @Query("DELETE FROM relation_classroom_resource")
    suspend fun clear()
}