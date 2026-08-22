package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nilian.app.data.local.entity.DailyRitualEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Daily Rituals (Morning/Evening reviews).
 */
@Dao
interface DailyRitualDao {

    @Query("SELECT * FROM daily_rituals WHERE date = :date LIMIT 1")
    fun getRitualForDate(date: LocalDate): Flow<DailyRitualEntity?>

    @Query("SELECT * FROM daily_rituals WHERE date = :date LIMIT 1")
    suspend fun getRitualForDateSync(date: LocalDate): DailyRitualEntity?

    @Query("SELECT * FROM daily_rituals ORDER BY date DESC")
    fun getAllRituals(): Flow<List<DailyRitualEntity>>

    @Query("SELECT * FROM daily_rituals ORDER BY date DESC")
    suspend fun getAllRitualsSync(): List<DailyRitualEntity>

    @Query("SELECT * FROM daily_rituals WHERE id = :id")
    suspend fun getRitualById(id: Long): DailyRitualEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(ritual: DailyRitualEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rituals: List<DailyRitualEntity>): List<Long>

    @Update
    suspend fun update(ritual: DailyRitualEntity): Int

    @Delete
    suspend fun delete(ritual: DailyRitualEntity): Int

    @Query("DELETE FROM daily_rituals WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate): Int

    @Query("DELETE FROM daily_rituals WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM daily_rituals")
    suspend fun deleteAllRituals(): Int
}
