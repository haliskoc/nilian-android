package com.nilian.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nilian.app.data.local.entity.DayTemplateEntity
import com.nilian.app.data.local.entity.DayTemplateWithBlocksRelation
import com.nilian.app.data.local.entity.TemplateBlockEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Day Templates and their associated TemplateBlocks.
 */
@Dao
interface DayTemplateDao {

    @Query("SELECT * FROM day_templates ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<DayTemplateEntity>>

    @Query("SELECT * FROM day_templates ORDER BY name ASC")
    suspend fun getAllTemplatesSync(): List<DayTemplateEntity>

    @Query("SELECT * FROM day_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): DayTemplateEntity?

    @Transaction
    @Query("SELECT * FROM day_templates WHERE id = :id")
    fun getTemplateWithBlocks(id: Long): Flow<DayTemplateWithBlocksRelation?>

    @Transaction
    @Query("SELECT * FROM day_templates WHERE id = :id")
    suspend fun getTemplateWithBlocksSync(id: Long): DayTemplateWithBlocksRelation?

    @Transaction
    @Query("SELECT * FROM day_templates ORDER BY name ASC")
    fun getTemplatesWithBlocks(): Flow<List<DayTemplateWithBlocksRelation>>

    @Transaction
    @Query("SELECT * FROM day_templates ORDER BY name ASC")
    suspend fun getTemplatesWithBlocksSync(): List<DayTemplateWithBlocksRelation>

    @Query("SELECT * FROM template_blocks WHERE template_id = :templateId ORDER BY start_time ASC")
    fun getBlocksForTemplate(templateId: Long): Flow<List<TemplateBlockEntity>>

    @Query("SELECT * FROM template_blocks WHERE template_id = :templateId ORDER BY start_time ASC")
    suspend fun getBlocksForTemplateSync(templateId: Long): List<TemplateBlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: DayTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateBlock(block: TemplateBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplateBlocks(blocks: List<TemplateBlockEntity>): List<Long>

    @Transaction
    suspend fun insertTemplateWithBlocks(
        template: DayTemplateEntity,
        blocks: List<TemplateBlockEntity>
    ): Long {
        val templateId = insertTemplate(template)
        if (blocks.isNotEmpty()) {
            val blocksWithId = blocks.map { it.copy(templateId = templateId) }
            insertTemplateBlocks(blocksWithId)
        }
        return templateId
    }

    @Update
    suspend fun updateTemplate(template: DayTemplateEntity): Int

    @Delete
    suspend fun deleteTemplate(template: DayTemplateEntity): Int

    @Query("DELETE FROM day_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long): Int

    @Delete
    suspend fun deleteTemplateBlock(block: TemplateBlockEntity): Int

    @Query("DELETE FROM template_blocks WHERE id = :id")
    suspend fun deleteTemplateBlockById(id: Long): Int

    @Query("DELETE FROM template_blocks WHERE template_id = :templateId")
    suspend fun deleteBlocksForTemplate(templateId: Long): Int

    @Query("DELETE FROM day_templates")
    suspend fun deleteAllTemplates(): Int
}
