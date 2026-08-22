package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nilian.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Data Access Object for Tasks.
 */
@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, due_date ASC, priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY is_completed ASC, due_date ASC, priority DESC")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE due_date = :date ORDER BY is_completed ASC, priority DESC, id ASC")
    fun getTasksForDate(date: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE due_date = :date ORDER BY is_completed ASC, priority DESC, id ASC")
    suspend fun getTasksForDateSync(date: LocalDate): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE due_date < :date AND is_completed = 0 ORDER BY due_date ASC")
    fun getUncompletedTasksBefore(date: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE due_date < :date AND is_completed = 0 ORDER BY due_date ASC")
    suspend fun getUncompletedTasksBeforeSync(date: LocalDate): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE goal_id = :goalId ORDER BY is_completed ASC, due_date ASC")
    fun getTasksByGoal(goalId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskByIdFlow(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE due_date < :beforeDate AND is_completed = 0 AND auto_rollover = 1 ORDER BY due_date ASC")
    fun getPendingRolloverTasks(beforeDate: LocalDate): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE due_date < :beforeDate AND is_completed = 0 AND auto_rollover = 1 ORDER BY due_date ASC")
    suspend fun getPendingRolloverTasksList(beforeDate: LocalDate): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

    @Update
    suspend fun updateTask(task: TaskEntity): Int

    @Update
    suspend fun updateTasks(tasks: List<TaskEntity>): Int

    @Delete
    suspend fun deleteTask(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long): Int

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks(): Int

    @Query("UPDATE tasks SET due_date = :newDueDate WHERE id = :id")
    suspend fun updateDueDate(id: Long, newDueDate: LocalDate): Int

    @Query("UPDATE tasks SET is_completed = :isCompleted, completed_at = :completedAt WHERE id = :id")
    suspend fun setTaskCompletion(id: Long, isCompleted: Boolean, completedAt: LocalDateTime?): Int

    @Query("UPDATE tasks SET due_date = :newDate WHERE due_date < :beforeDate AND is_completed = 0 AND auto_rollover = 1")
    suspend fun rolloverTasks(beforeDate: LocalDate, newDate: LocalDate): Int
}
