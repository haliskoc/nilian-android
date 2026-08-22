package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nilian.app.data.local.entity.HabitEntity
import com.nilian.app.data.local.entity.HabitLogEntity
import com.nilian.app.data.local.entity.HabitWithLogsRelation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Habits and HabitLogs.
 */
@Dao
interface HabitDao {

    // --- Habit Queries ---
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :id")
    fun getHabitByIdFlow(id: Long): Flow<HabitEntity?>

    @Transaction
    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitWithLogs(habitId: Long): Flow<HabitWithLogsRelation?>

    @Transaction
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogsRelation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>): List<Long>

    @Update
    suspend fun updateHabit(habit: HabitEntity): Int

    @Query("UPDATE habits SET current_streak = :currentStreak, best_streak = :bestStreak WHERE id = :habitId")
    suspend fun updateStreaks(habitId: Long, currentStreak: Int, bestStreak: Int): Int

    @Delete
    suspend fun deleteHabit(habit: HabitEntity): Int

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long): Int

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits(): Int

    // --- HabitLog Queries ---
    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY date DESC")
    suspend fun getLogsForHabitList(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId ORDER BY date DESC")
    suspend fun getLogsForHabitSync(habitId: Long): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    fun getLogsForDate(date: LocalDate): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date = :date")
    suspend fun getLog(habitId: Long, date: LocalDate): HabitLogEntity?

    @Query("SELECT * FROM habit_logs WHERE habit_id = :habitId AND date = :date")
    suspend fun getLogSync(habitId: Long, date: LocalDate): HabitLogEntity?

    @Query("SELECT * FROM habit_logs ORDER BY date DESC")
    suspend fun getAllHabitLogsSync(): List<HabitLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<HabitLogEntity>)

    @Query("DELETE FROM habit_logs WHERE habit_id = :habitId AND date = :date")
    suspend fun deleteLogForDate(habitId: Long, date: LocalDate): Int

    @Query("DELETE FROM habit_logs WHERE habit_id = :habitId")
    suspend fun deleteAllLogsForHabit(habitId: Long): Int

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllHabitLogs(): Int
}
