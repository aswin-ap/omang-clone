package com.omang.app.data.database.psm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants

@Dao
interface PsmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(psmEntity: PsmEntity)

    @Query("SELECT * FROM psm_table WHERE psmId = :psmId")
    suspend fun getGeneralPsm(psmId: Int = DBConstants.GENERAL_PSM_ID): PsmEntity?

    @Query("SELECT * FROM psm_table WHERE psmId = :classroomId")
    suspend fun getPsmByClassroomId(classroomId: Int): PsmEntity?

    @Query("DELETE FROM psm_table WHERE psmId = :classroomId")
    suspend fun deletePsmByClassroomId(classroomId: Int)

    @Query("DELETE FROM psm_table WHERE psmId = :psmId")
    suspend fun deleteGeneralPsm(psmId: Int = DBConstants.GENERAL_PSM_ID)

    @Query("DELETE FROM psm_table")
    suspend fun clear()

}