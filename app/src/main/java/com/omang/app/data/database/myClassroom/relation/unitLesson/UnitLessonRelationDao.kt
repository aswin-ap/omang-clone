package com.omang.app.data.database.myClassroom.relation.unitLesson

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.omang.app.data.database.myClassroom.unit.UnitEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity
import com.omang.app.data.model.unitLessonMapping.UnitLessonCombination
import com.omang.app.data.model.unitWithLessons.UnitWithLessons

@Dao
interface UnitLessonRelationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUnitLessonRelation(relationEntities: List<UnitLessonRelationEntity>)

    @Query("DELETE FROM relation_unit_lesson WHERE classroom_id = :classroomId")
    suspend fun deleteClassroomUnitLessonMapping(classroomId: Int)

    @Query("SELECT DISTINCT unit_id, lesson_id FROM relation_unit_lesson WHERE classroom_id = :classroomId")
    suspend fun getUniqueCombinations(classroomId: Int): List<UnitLessonCombination>
    @Query("DELETE FROM relation_unit_lesson WHERE classroom_id = :classroomId AND lesson_id = :lessonId AND unit_id = :unitId")
    suspend fun deleteClassroomUnitLessonMapping(classroomId: Int,lessonId: Int,unitId: Int)
    @Query("DELETE FROM relation_unit_lesson")
    suspend fun clear()
}