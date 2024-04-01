package com.omang.app.data.database.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicFeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<FeedEntity>)

    @Query("SELECT * FROM ${DBConstants.PUBLIC_FEED_TABLE}")
    fun getAllNotifications(): Flow<List<FeedEntity>>

    @Query("SELECT EXISTS(SELECT * FROM public_feed_table WHERE feed_id IN (:feedList))")
    fun isContentsExist(feedList: List<String>): Boolean

    @Query("DELETE FROM ${DBConstants.PUBLIC_FEED_TABLE}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${DBConstants.PUBLIC_FEED_TABLE} WHERE resourceId = :resourceId")
    suspend fun delete(resourceId: Int)

}