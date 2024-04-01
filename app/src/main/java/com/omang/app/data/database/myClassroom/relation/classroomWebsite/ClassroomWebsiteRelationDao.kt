package com.omang.app.data.database.myClassroom.relation.classroomWebsite

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClassroomWebsiteRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<ClassroomWebsiteRelationEntity>)

    @Query("DELETE FROM relation_classroom_website WHERE classroom_id = :classroomId AND website_id = :websiteId")
    suspend fun deleteClassroomResourceRelation(classroomId: Int, websiteId: Int) : Int
    @Query("DELETE FROM relation_classroom_website WHERE classroom_id = :classroomId")
    suspend fun deleteClassroomWebsiteMapping(classroomId: Int)
    @Query("DELETE FROM relation_classroom_website")
    suspend fun clear()
}