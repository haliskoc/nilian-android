package com.nilian.app.core.database

import androidx.room.TypeConverter
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.EventCategory
import com.nilian.app.domain.model.Priority
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Room type converters for non-primitive types:
 * - Date/Time objects (LocalDate, LocalDateTime, LocalTime)
 * - Domain Enums (Priority, EventCategory, BlockType)
 * - Collections (Set<DayOfWeek>)
 */
class Converters {

    // --- LocalDate ---
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }
    }

    // --- LocalDateTime ---
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        }
    }

    // --- LocalTime ---
    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return value?.let {
            runCatching { LocalTime.parse(it) }.getOrNull()
        }
    }

    // --- Priority ---
    @TypeConverter
    fun fromPriority(priority: Priority?): String? {
        return priority?.name
    }

    @TypeConverter
    fun toPriority(value: String?): Priority? {
        return value?.let {
            runCatching { Priority.valueOf(it) }.getOrDefault(Priority.MEDIUM)
        }
    }

    // --- EventCategory ---
    @TypeConverter
    fun fromEventCategory(category: EventCategory?): String? {
        return category?.name
    }

    @TypeConverter
    fun toEventCategory(value: String?): EventCategory? {
        return value?.let {
            runCatching { EventCategory.valueOf(it) }.getOrDefault(EventCategory.GENERAL)
        }
    }

    // --- BlockType ---
    @TypeConverter
    fun fromBlockType(blockType: BlockType?): String? {
        return blockType?.name
    }

    @TypeConverter
    fun toBlockType(value: String?): BlockType? {
        return value?.let {
            runCatching { BlockType.valueOf(it) }.getOrDefault(BlockType.OTHER)
        }
    }

    // --- Set<DayOfWeek> ---
    @TypeConverter
    fun fromDayOfWeekSet(days: Set<DayOfWeek>?): String? {
        return days?.joinToString(separator = ",") { it.name }
    }

    @TypeConverter
    fun toDayOfWeekSet(value: String?): Set<DayOfWeek>? {
        if (value.isNullOrBlank()) return emptySet()
        return value.split(",")
            .mapNotNull { token ->
                runCatching { DayOfWeek.valueOf(token.trim()) }.getOrNull()
            }
            .toSet()
    }
}
