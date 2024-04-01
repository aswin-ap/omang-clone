package com.omang.app.data.database.resource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.DBConstants.Companion.CLASSROOM_RESOURCES_RELATION_TABLE
import com.omang.app.data.database.DBConstants.Companion.RESOURCES_TABLE

@Dao
interface ResourcesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resourcesEntity: ResourcesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(resources: List<ResourcesEntity>)

    @Query("SELECT * FROM $RESOURCES_TABLE WHERE resource_id IN (SELECT resource_id FROM $CLASSROOM_RESOURCES_RELATION_TABLE WHERE classroom_id = :classroomId)")
    suspend fun getResourcesByClassroomId(classroomId: Int): List<ResourcesEntity>

    @Query("SELECT * FROM resources_table where isBonus = :bonusStatus")
    suspend fun getAllMyLibraryItems(bonusStatus: Int = DBConstants.BonusContent.TRUE.value): List<ResourcesEntity>

    @Query("SELECT * FROM RESOURCES_TABLE WHERE type = 'Book' AND isBonus = :bonusStatus")
    suspend fun getAllMyLibraryDocs(bonusStatus: Int = DBConstants.BonusContent.TRUE.value): List<ResourcesEntity>

    @Query("SELECT * FROM RESOURCES_TABLE WHERE type = 'Video' AND isBonus = :bonusStatus")
    suspend fun getAllMyLibraryVideos(bonusStatus: Int = DBConstants.BonusContent.TRUE.value): List<ResourcesEntity>

    @Query("UPDATE RESOURCES_TABLE SET isBonus = :isBonusValue WHERE resource_id = :resourceId")
    suspend fun updateIsBonusToFalse(
        resourceId: Int,
        isBonusValue: Int = DBConstants.BonusContent.FALSE.value
    )

    @Query("DELETE FROM RESOURCES_TABLE WHERE resource_id = :resourceId")
    suspend fun deleteResource(resourceId: Int)

    @Query("SELECT * FROM RESOURCES_TABLE WHERE resource_id = :resourceId")
    suspend fun getResourceById(resourceId: Int): ResourcesEntity?

    @Query("SELECT resource_id FROM resources_table WHERE isBonus = :type")
    fun getAllMyLibraryIds(type: Int = DBConstants.BonusContent.TRUE.value): List<Int>

    @Query("DELETE FROM resources_table")
    suspend fun clear()

    @Query("SELECT resource_id FROM $RESOURCES_TABLE WHERE resource_id IN (SELECT resource_id FROM $CLASSROOM_RESOURCES_RELATION_TABLE WHERE classroom_id = :classroomId) AND type = 'Book'")
    suspend fun getDocumentIdByClassroomId(classroomId: Int): List<Int>

    @Query("SELECT resource_id FROM $RESOURCES_TABLE WHERE resource_id IN (SELECT resource_id FROM $CLASSROOM_RESOURCES_RELATION_TABLE WHERE classroom_id = :classroomId) AND type = 'Video'")
    suspend fun getVideoIdByClassroomId(classroomId: Int): List<Int>

}
