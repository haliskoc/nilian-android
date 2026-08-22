package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nilian.app.data.local.entity.InboxNoteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Quick Brain Dump Inbox Notes.
 */
@Dao
interface InboxNoteDao {

    @Query("SELECT * FROM inbox_notes WHERE is_archived = 0 ORDER BY created_at DESC")
    fun getActiveNotes(): Flow<List<InboxNoteEntity>>

    @Query("SELECT * FROM inbox_notes WHERE is_archived = 0 ORDER BY created_at DESC")
    suspend fun getActiveNotesSync(): List<InboxNoteEntity>

    @Query("SELECT * FROM inbox_notes WHERE is_archived = 1 ORDER BY created_at DESC")
    fun getArchivedNotes(): Flow<List<InboxNoteEntity>>

    @Query("SELECT * FROM inbox_notes ORDER BY created_at DESC")
    fun getAllNotes(): Flow<List<InboxNoteEntity>>

    @Query("SELECT * FROM inbox_notes ORDER BY created_at DESC")
    suspend fun getAllNotesSync(): List<InboxNoteEntity>

    @Query("SELECT * FROM inbox_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): InboxNoteEntity?

    @Query("SELECT * FROM inbox_notes WHERE id = :id")
    fun getNoteByIdFlow(id: Long): Flow<InboxNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: InboxNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<InboxNoteEntity>): List<Long>

    @Update
    suspend fun update(note: InboxNoteEntity): Int

    @Query("UPDATE inbox_notes SET is_archived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean): Int

    @Delete
    suspend fun delete(note: InboxNoteEntity): Int

    @Query("DELETE FROM inbox_notes WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM inbox_notes")
    suspend fun deleteAllNotes(): Int
}
