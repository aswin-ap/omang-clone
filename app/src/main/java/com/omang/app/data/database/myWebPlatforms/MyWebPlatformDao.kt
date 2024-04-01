package com.omang.app.data.database.myWebPlatforms

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.utils.NotificationUtil
import kotlinx.coroutines.flow.Flow

@Dao
interface MyWebPlatformDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(myWebPlatformEntity: List<MyWebPlatformEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(myWebPlatformEntity: MyWebPlatformEntity)

    @Query("SELECT * FROM my_web_platform_table WHERE type = :type AND is_deleted == 0")
    suspend fun getAllMyWebPlatformItems(type: Int = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value): List<MyWebPlatformEntity>

    @Query("SELECT * FROM my_web_platform_table WHERE type = :type AND is_deleted == 0")
    fun getExploreItems(type: Int = DBConstants.WebsiteContent.EXPLORE.value): Flow<List<MyWebPlatformEntity>>

    @Query("SELECT * FROM my_web_platform_table WHERE type = :type AND is_deleted == 0")
    fun getMyWebPlatformItems(type: Int = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value): Flow<List<MyWebPlatformEntity>>

    @Query("SELECT * FROM my_web_platform_table WHERE website_id IN (SELECT website_id FROM relation_classroom_website WHERE classroom_id = :classroomId)")
    suspend fun getWebsitesByClassroomId(classroomId: Int): List<MyWebPlatformEntity>

    @Query("SELECT * FROM my_web_platform_table WHERE website_id IN (:websiteIds)")
    suspend fun getWebsitesByIds(websiteIds: List<Int>): List<MyWebPlatformEntity>

    @Query("SELECT * FROM my_web_platform_table WHERE isFav = 1")
    fun fetchFavItems(): Flow<List<MyWebPlatformEntity>>

    @Query("UPDATE my_web_platform_table SET isFav = 1 WHERE website_id = :id ")
    suspend fun setFavorite(id: Int)

    @Query("UPDATE my_web_platform_table SET isFav = 0 WHERE website_id = :id ")
    suspend fun removeFavorite(id: Int)

    @Query("UPDATE my_web_platform_table SET timeStamp = :timeData WHERE website_id = :id ")
    suspend fun setTimeStamp(id: Int, timeData: String)

    @Query("UPDATE my_web_platform_table SET is_deleted = 1 WHERE website_id = :websiteId AND type = :type")
    suspend fun deleteWebPlatform(
        websiteId: Int,
        type: Int = DBConstants.WebsiteContent.EXPLORE.value
    )

    @Query("UPDATE my_web_platform_table SET is_deleted = 1 WHERE website_id = :websiteId AND type = :type")
    suspend fun deleteMyWebPlatform(
        websiteId: Int,
        type: Int = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value
    )

    @Query("DELETE FROM my_web_platform_table")
    suspend fun clear()

    @Query("DELETE FROM my_web_platform_table WHERE type = :type")
    suspend fun clearDoe(type: Int = DBConstants.WebsiteContent.DOE.value)

    @Query("SELECT website_id FROM my_web_platform_table WHERE type = :type")
    fun getAllExploreIds(type: Int = DBConstants.WebsiteContent.EXPLORE.value): List<Int>

    @Query("SELECT website_id FROM my_web_platform_table WHERE type = :type")
    fun getAllMyWebPlatformIds(type: Int = DBConstants.WebsiteContent.MY_WEB_PLATFORM.value): List<Int>

    @Query("SELECT * FROM my_web_platform_table WHERE type = :doeType")
    fun getDoe(doeType: Int = DBConstants.WebsiteContent.DOE.value): MyWebPlatformEntity?

    @Query("SELECT website_id FROM my_web_platform_table WHERE website_id IN (SELECT website_id FROM relation_classroom_website WHERE classroom_id = :classroomId)")
    suspend fun getWebsiteIdByClassroomId(classroomId: Int): List<Int>
}