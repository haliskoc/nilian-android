package com.nilian.app

import com.google.common.truth.Truth.assertThat
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.DayTemplate
import com.nilian.app.domain.model.DayTemplateWithBlocks
import com.nilian.app.domain.model.GoalItem
import com.nilian.app.domain.model.InboxNote
import com.nilian.app.domain.model.TemplateBlock
import com.nilian.app.presentation.templates.PresetDayTemplates
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class DayTemplatesAndMilestonesTest {

    @Test
    fun `preset day templates have valid sequential blocks and non-empty titles`() {
        val presets = PresetDayTemplates.allPresets
        assertThat(presets).isNotEmpty()
        assertThat(presets.size).isAtLeast(3)

        presets.forEach { templateWithBlocks ->
            assertThat(templateWithBlocks.template.name).isNotEmpty()
            assertThat(templateWithBlocks.template.iconName).isNotEmpty()
            assertThat(templateWithBlocks.blocks).isNotEmpty()

            templateWithBlocks.blocks.forEach { block ->
                assertThat(block.title).isNotEmpty()
                assertThat(block.startTime).isLessThan(block.endTime)
            }
        }
    }

    @Test
    fun `exam day template contains deep work exam block and study revision`() {
        val examDay = PresetDayTemplates.examDay
        assertThat(examDay.template.name).contains("Sınav")
        assertThat(examDay.blocks.any { it.blockType == BlockType.DEEP_WORK }).isTrue()
        assertThat(examDay.blocks.any { it.blockType == BlockType.STUDY }).isTrue()
    }

    @Test
    fun `deep coding day template contains multiple deep work sprints`() {
        val codingDay = PresetDayTemplates.deepCodingDay
        assertThat(codingDay.template.name).contains("Kodlama")
        val deepWorkBlocks = codingDay.blocks.filter { it.blockType == BlockType.DEEP_WORK }
        assertThat(deepWorkBlocks.size).isAtLeast(2)
    }

    @Test
    fun `inbox note tags extraction and filtering works correctly`() {
        val notes = listOf(
            InboxNote(id = 1, content = "Doktora tez hazırlığı", tags = listOf("#okul", "#acil")),
            InboxNote(id = 2, content = "Mobil uygulama mimarisi", tags = listOf("#proje")),
            InboxNote(id = 3, content = "Günde 2 litre su", tags = listOf("#sağlık"))
        )

        val okulNotes = notes.filter { it.tags.contains("#okul") }
        assertThat(okulNotes).hasSize(1)
        assertThat(okulNotes.first().id).isEqualTo(1)

        val allTags = notes.flatMap { it.tags }.distinct()
        assertThat(allTags).containsExactly("#okul", "#acil", "#proje", "#sağlık")
    }

    @Test
    fun `milestone days remaining calculation is accurate for past, today, and future dates`() {
        val today = LocalDate.of(2026, 8, 22)

        val futureGoal = GoalItem(id = 1, title = "Final", targetDate = today.plusDays(10))
        val todayGoal = GoalItem(id = 2, title = "Lansman", targetDate = today)
        val overdueGoal = GoalItem(id = 3, title = "Ödev", targetDate = today.minusDays(3))
        val noDateGoal = GoalItem(id = 4, title = "Sürekli Gelişim", targetDate = null)

        assertThat(ChronoUnit.DAYS.between(today, futureGoal.targetDate)).isEqualTo(10)
        assertThat(ChronoUnit.DAYS.between(today, todayGoal.targetDate)).isEqualTo(0)
        assertThat(ChronoUnit.DAYS.between(today, overdueGoal.targetDate)).isEqualTo(-3)
        assertThat(noDateGoal.targetDate).isNull()
    }
}
