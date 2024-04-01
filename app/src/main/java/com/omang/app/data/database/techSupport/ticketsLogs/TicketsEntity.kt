package com.omang.app.data.database.techSupport.ticketsLogs

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.omang.app.data.database.DBConstants
import kotlinx.android.parcel.Parcelize

@Entity(tableName = DBConstants.TICKETS_TABLE)
@Parcelize
class TicketsEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "ticket_id")
    val id: String,
    @ColumnInfo(name = "roomId")
    val roomId: Int,
    @ColumnInfo(name = "issue")
    val issue: String,
    @ColumnInfo(name = "downloadSpeed")
    val downloadSpeed: String?,
    @ColumnInfo(name = "isClosed")
    val isClosed: Boolean,
    @ColumnInfo(name = "phone")
    val phone: String,
    @ColumnInfo(name = "createdAt")
    val createdAt: String,
    @ColumnInfo(name = "reopenedAt")
    val reopenedAt: String?,
    @ColumnInfo(name = "message")
    val message: String?,
    @ColumnInfo(name = "closedAt")
    val closedAt: String?,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "offline")
    val isOffline: Boolean = false,
    @ColumnInfo(name = "ticketImage")
    val ticketImage: String = "",
    @ColumnInfo(name = "unreadMessages")
    val unreadMessages: Int = 0,
    @ColumnInfo(name = "rating")
    val rating: Int?,
    @ColumnInfo(name = "feedback")
    val feedback: String?,
    @ColumnInfo(name = "navigation")
    val navigation: String?,
) : Parcelable