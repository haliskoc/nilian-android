package com.nilian.app.domain.model

/**
 * Task priority level.
 */
enum class Priority(val label: String) {
    LOW("Düşük"),
    MEDIUM("Orta"),
    HIGH("Yüksek")
}

/**
 * Categories for calendar events.
 */
enum class EventCategory(val label: String) {
    LECTURE("Ders"),
    MEETING("Toplantı"),
    PERSONAL("Kişisel"),
    WORK("İş"),
    STUDY("Çalışma"),
    GENERAL("Genel"),
    OTHER("Diğer")
}

/**
 * Types of time blocks for 24-hour daily timeline planning.
 */
enum class BlockType(val label: String) {
    SLEEP("Uyku"),
    WORKOUT("Spor"),
    STUDY("Ders / Çalışma"),
    DEEP_WORK("Derin Odak"),
    REST("Dinlenme"),
    BUFFER("Tampon"),
    GENERAL("Genel"),
    OTHER("Diğer")
}

/**
 * App theme display preference.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
