package com.omang.app.data.database.techSupport.ticketsLogs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.omang.app.data.database.DBConstants


@Dao
interface TicketsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<TicketsEntity>)

    @Query("SELECT * FROM ${DBConstants.TICKETS_TABLE}")
    suspend fun getTickets(): List<TicketsEntity>

    @Query("SELECT * FROM ${DBConstants.TICKETS_TABLE} WHERE offline =:offline")
    suspend fun getAllLocalTickets(offline: Boolean): List<TicketsEntity>

    @Query("delete from ${DBConstants.TICKETS_TABLE} where ticket_id in (:idList)")
    suspend fun deleteTicketById(idList: List<String>)

    @Query("SELECT * FROM ${DBConstants.TICKETS_TABLE} WHERE ticket_id= :localId")
    suspend fun getTicketById(localId: String): TicketsEntity

    @Query("DELETE FROM tickets_table")
    suspend fun clear()
}