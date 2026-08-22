package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nilian.app.data.local.entity.TimeBlockEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Time Blocks in the 24h timeline.
 */
@Dao
interface TimeBlockDao {

    @Query("SELECT * FROM time_blocks WHERE date = :date ORDER BY start_time ASC")
    fun getTimeBlocksForDate(date: LocalDate): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_blocks WHERE date = :date ORDER BY start_time ASC")
    suspend fun getTimeBlocksForDateSync(date: LocalDate): List<TimeBlockEntity>

    @Query("SELECT * FROM time_blocks ORDER BY date ASC, start_time ASC")
    fun getAllTimeBlocks(): Flow<List<TimeBlockEntity>>

    @Query("SELECT * FROM time_blocks ORDER BY date ASC, start_time ASC")
    suspend fun getAllTimeBlocksSync(): List<TimeBlockEntity>

    @Query("SELECT * FROM time_blocks WHERE id = :id")
    suspend fun getTimeBlockById(id: Long): TimeBlockEntity?

    @Query("SELECT * FROM time_blocks WHERE id = :id")
    fun getTimeBlockByIdFlow(id: Long): Flow<TimeBlockEntity?>

    @Query("SELECT * FROM time_blocks WHERE linked_task_id = :taskId")
    suspend fun getTimeBlockByTaskId(taskId: Long): TimeBlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeBlock: TimeBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timeBlocks: List<TimeBlockEntity>): List<Long>

    @Update
    suspend fun update(timeBlock: TimeBlockEntity): Int

    @Delete
    suspend fun delete(timeBlock: TimeBlockEntity): Int

    @Query("DELETE FROM time_blocks WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM time_blocks WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate): Int

    @Query("DELETE FROM time_blocks")
    suspend fun deleteAllTimeBlocks(): Int
}
