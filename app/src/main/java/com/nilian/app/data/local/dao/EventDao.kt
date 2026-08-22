package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nilian.app.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Data Access Object for Events.
 */
@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY start_date_time ASC")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY start_date_time ASC")
    suspend fun getAllEventsSync(): List<EventEntity>

    @Query("SELECT * FROM events WHERE substr(start_date_time, 1, 10) = :date ORDER BY start_date_time ASC")
    fun getEventsForDate(date: LocalDate): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE substr(start_date_time, 1, 10) = :date ORDER BY start_date_time ASC")
    suspend fun getEventsForDateSync(date: LocalDate): List<EventEntity>

    @Query("SELECT * FROM events WHERE (start_date_time <= :end AND end_date_time >= :start) ORDER BY start_date_time ASC")
    fun getEventsBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE (start_date_time <= :end AND end_date_time >= :start) ORDER BY start_date_time ASC")
    suspend fun getEventsBetweenSync(start: LocalDateTime, end: LocalDateTime): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventByIdFlow(id: Long): Flow<EventEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>): List<Long>

    @Update
    suspend fun updateEvent(event: EventEntity): Int

    @Delete
    suspend fun deleteEvent(event: EventEntity): Int

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long): Int

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents(): Int
}
