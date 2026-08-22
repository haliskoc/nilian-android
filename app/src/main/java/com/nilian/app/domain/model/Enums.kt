package com.nilian.app.domain.model

/**
 * Task priority level.
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Categories for calendar events.
 */
enum class EventCategory {
    LECTURE,
    MEETING,
    PERSONAL,
    WORK,
    STUDY,
    GENERAL,
    OTHER
}

/**
 * Types of time blocks for 24-hour daily timeline planning.
 */
enum class BlockType {
    SLEEP,
    WORKOUT,
    STUDY,
    DEEP_WORK,
    REST,
    BUFFER,
    GENERAL,
    OTHER
}

/**
 * App theme display preference.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
