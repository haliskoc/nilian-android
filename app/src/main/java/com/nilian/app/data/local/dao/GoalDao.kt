package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nilian.app.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Goals.
 */
@Dao
interface GoalDao {

    @Query("SELECT * FROM goals ORDER BY is_archived ASC, id ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY is_archived ASC, id ASC")
    suspend fun getAllGoalsSync(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE is_archived = 0 ORDER BY id ASC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getGoalByIdFlow(id: Long): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<GoalEntity>): List<Long>

    @Update
    suspend fun update(goal: GoalEntity): Int

    @Query("UPDATE goals SET progress_percent = :progressPercent WHERE id = :goalId")
    suspend fun updateProgress(goalId: Long, progressPercent: Float): Int

    @Query("UPDATE goals SET is_archived = :isArchived WHERE id = :goalId")
    suspend fun setArchived(goalId: Long, isArchived: Boolean): Int

    @Delete
    suspend fun delete(goal: GoalEntity): Int

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals(): Int
}
