package com.omang.app.data.database.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassroomFeedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications : List<ClassroomFeedEntity>)

    @Query("SELECT EXISTS(SELECT * FROM public_feed_table WHERE feed_id IN (:feedList))")
    fun isFeedsExist(feedList: List<String>) : Boolean

    @Query("SELECT * FROM classroom_feed_table WHERE classroom_id = :classroomId")
    fun getFeedsByClassroomId(classroomId: Int) : Flow<List<ClassroomFeedEntity>>

    @Query("DELETE FROM ${DBConstants.CLASSROOM_FEED_TABLE}")
    suspend fun deleteAll()

    @Query("DELETE FROM ${DBConstants.CLASSROOM_FEED_TABLE} WHERE resourceId = :resourceId")
    suspend fun delete(resourceId : Int)

}