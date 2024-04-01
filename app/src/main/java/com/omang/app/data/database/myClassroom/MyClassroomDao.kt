package com.omang.app.data.database.myClassroom

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.utils.ViewUtil
import kotlinx.coroutines.flow.Flow

@Dao
interface MyClassroomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(myClassroomEntity: MyClassroomEntity)

    @Query("SELECT * FROM my_classroom_table WHERE type = 'Classroom'")
    fun fetchAllClassrooms(): Flow<List<MyClassroomEntity>>

    @Query("SELECT * FROM my_classroom_table WHERE type == 'Club'")
    fun fetchAllClubs(): Flow<List<MyClassroomEntity>>

    @Query("SELECT name FROM my_classroom_table WHERE type == 'Classroom'")
    suspend fun fetchMyClassroomList(): List<String>

    @Query("SELECT name FROM my_classroom_table WHERE type == 'Club'")
    suspend fun fetchMyClubsList(): List<String>

    @Query("SELECT * FROM my_classroom_table WHERE classroom_id = :classroomId")
    fun getClassRoomById(classroomId: Int): MyClassroomEntity?

    @Query("SELECT * FROM my_classroom_table")
    suspend fun fetchMyClassroomsAndClubsList(): List<MyClassroomEntity>

    @Query("DELETE FROM my_classroom_table WHERE classroom_id = :classroomId")
    suspend fun delete(classroomId: Int): Int

    @Query("UPDATE my_classroom_table SET updatedAt = :date WHERE classroom_id = :classroomId")
    suspend fun updateLastUpdatedDateById(classroomId: Int, date: String = ViewUtil.getUtcTime())

    @Query("SELECT updatedAt FROM  my_classroom_table WHERE classroom_id = :classroomId")
    suspend fun getLastUpdatedDateById(classroomId: Int) : String
    @Update
    suspend fun updateClassroom(classroomEntity: MyClassroomEntity)

    @Query("DELETE FROM my_classroom_table")
    suspend fun clear()

}