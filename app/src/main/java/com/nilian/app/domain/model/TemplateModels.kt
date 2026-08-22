package com.nilian.app.domain.model

/**
 * Standard archetypes for structured daily timeline templates.
 */
enum class DayTemplateType {
    EXAM_DAY,
    DEEP_CODING,
    WEEKEND_REST,
    CUSTOM
}

/**
 * Predefined day template archetype container with template definition and blocks.
 */
data class PredefinedDayTemplate(
    val template: DayTemplate,
    val type: DayTemplateType,
    val blocks: List<TemplateBlock> = emptyList()
)
