package com.nilian.app.domain.usecase

import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.DayTemplate
import com.nilian.app.domain.model.DayTemplateType
import com.nilian.app.domain.model.DayTemplateWithBlocks
import com.nilian.app.domain.model.PredefinedDayTemplate
import com.nilian.app.domain.model.TemplateBlock
import com.nilian.app.domain.model.TimeBlock
import com.nilian.app.domain.repository.TemplateRepository
import com.nilian.app.domain.repository.TimeBlockRepository
import java.time.LocalDate
import java.time.LocalTime

/**
 * Deterministic engine for managing and applying structured daily timeline templates.
 *
 * Provides ready-to-use archetypes:
 * 1. Exam Day (Targeted active recall, test simulation, review intervals, sleep protection).
 * 2. Deep Coding / Entrepreneur Day (Deep focus coding sprints, async inbox buffer, physical reset).
 * 3. Weekend Rest (Slow morning, nature movement, creative flow, weekly review preview).
 *
 * Instantiates template blocks into concrete [TimeBlock] models mapped to any target date
 * on the 24-hour timeline and optionally persists them to [TimeBlockRepository].
 */
class DayTemplateUseCase(
    private val timeBlockRepository: TimeBlockRepository? = null,
    private val templateRepository: TemplateRepository? = null
) {

    /**
     * Pure function to transform template blocks into concrete [TimeBlock] items for [targetDate].
     */
    fun instantiateTemplateBlocks(
        blocks: List<TemplateBlock>,
        targetDate: LocalDate
    ): List<TimeBlock> {
        return blocks.map { block ->
            TimeBlock(
                id = 0L,
                title = block.title,
                blockType = block.blockType,
                startTime = block.startTime,
                endTime = block.endTime,
                date = targetDate
            )
        }
    }

    /**
     * Applies template blocks directly to the 24-hour timeline on [targetDate] and persists to [TimeBlockRepository].
     *
     * @param templateWithBlocks The template and its time blocks.
     * @param targetDate The destination date on the calendar.
     * @param replaceExisting If true, deletes existing time blocks on targetDate before inserting.
     * @return The list of created [TimeBlock] items with their generated IDs.
     */
    suspend fun applyTemplateToDate(
        templateWithBlocks: DayTemplateWithBlocks,
        targetDate: LocalDate,
        replaceExisting: Boolean = false
    ): List<TimeBlock> {
        val blocksToInsert = instantiateTemplateBlocks(templateWithBlocks.blocks, targetDate)
        val repo = timeBlockRepository ?: return blocksToInsert

        if (replaceExisting) {
            val existing = repo.getAllTimeBlocksSync().filter { it.date == targetDate }
            for (oldBlock in existing) {
                repo.deleteTimeBlockById(oldBlock.id)
            }
        }

        val generatedIds = repo.insertTimeBlocks(blocksToInsert)
        return blocksToInsert.mapIndexed { index, block ->
            val assignedId = generatedIds.getOrElse(index) { 0L }
            block.copy(id = assignedId)
        }
    }

    /**
     * Applies a predefined archetype template (Exam Day, Deep Coding, Weekend Rest) to a date.
     */
    suspend fun applyPredefinedTemplate(
        type: DayTemplateType,
        targetDate: LocalDate,
        replaceExisting: Boolean = false
    ): List<TimeBlock> {
        val predefined = getPredefinedTemplates().firstOrNull { it.type == type }
            ?: getPredefinedTemplates().first()

        val templateWithBlocks = DayTemplateWithBlocks(
            template = predefined.template,
            blocks = predefined.blocks
        )

        return applyTemplateToDate(templateWithBlocks, targetDate, replaceExisting)
    }

    /**
     * Built-in archetype definitions designed for calm productivity and optimal cognitive pacing.
     */
    fun getPredefinedTemplates(): List<PredefinedDayTemplate> {
        return listOf(
            // 1. Exam Day Template
            PredefinedDayTemplate(
                template = DayTemplate(
                    id = 1L,
                    name = "Sınav Günü (Exam Day)",
                    description = "Aktif hatırlama, sınav oturumu, zihinsel dinlenme ve uyku hijyeni odaklı plan.",
                    iconName = "school",
                    isDefault = true
                ),
                type = DayTemplateType.EXAM_DAY,
                blocks = listOf(
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Güne Sakin Başlangıç & Kahvaltı",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(7, 0),
                        endTime = LocalTime.of(8, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Aktif Hatırlama & Sınav Öncesi Hızlı Tekrar",
                        blockType = BlockType.STUDY,
                        startTime = LocalTime.of(8, 0),
                        endTime = LocalTime.of(9, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Ulaşım & Zihinsel Hazırlık Tamponu",
                        blockType = BlockType.BUFFER,
                        startTime = LocalTime.of(9, 30),
                        endTime = LocalTime.of(10, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Sınav / Değerlendirme Oturumu",
                        blockType = BlockType.STUDY,
                        startTime = LocalTime.of(10, 0),
                        endTime = LocalTime.of(12, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Beslenme & Zihinsel Reset",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(12, 30),
                        endTime = LocalTime.of(13, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Soru Çözümü & İkinci Ders Odak",
                        blockType = BlockType.STUDY,
                        startTime = LocalTime.of(13, 30),
                        endTime = LocalTime.of(15, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Hafif Yürüyüş / Esneme & Hava Alma",
                        blockType = BlockType.WORKOUT,
                        startTime = LocalTime.of(16, 0),
                        endTime = LocalTime.of(17, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Özet Çıkarma & Akşam Tekrarı",
                        blockType = BlockType.STUDY,
                        startTime = LocalTime.of(18, 0),
                        endTime = LocalTime.of(20, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 1L,
                        title = "Mavi Işık Molası & Uyku Hijyeni",
                        blockType = BlockType.SLEEP,
                        startTime = LocalTime.of(22, 0),
                        endTime = LocalTime.of(23, 0)
                    )
                )
            ),

            // 2. Deep Coding & Entrepreneur Day Template
            PredefinedDayTemplate(
                template = DayTemplate(
                    id = 2L,
                    name = "Derin Kodlama / Girişimci Günü",
                    description = "Bölünmeyen derin odak blokları, asenkron iletişim aralıkları ve fiziksel sıfırlama.",
                    iconName = "code",
                    isDefault = true
                ),
                type = DayTemplateType.DEEP_CODING,
                blocks = listOf(
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Sabah Rutini, Kahve & Günlük Niyet",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(6, 30),
                        endTime = LocalTime.of(7, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Derin Kodlama Sprinti 1 — Çekirdek Mimari",
                        blockType = BlockType.DEEP_WORK,
                        startTime = LocalTime.of(7, 30),
                        endTime = LocalTime.of(10, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Zihin Molası & Hidrasyon",
                        blockType = BlockType.BUFFER,
                        startTime = LocalTime.of(10, 30),
                        endTime = LocalTime.of(11, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Derin Kodlama Sprinti 2 — Entegrasyon & Test",
                        blockType = BlockType.DEEP_WORK,
                        startTime = LocalTime.of(11, 0),
                        endTime = LocalTime.of(13, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Dengeli Öğle Yemeği & Yürüyüş",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(13, 0),
                        endTime = LocalTime.of(14, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Asenkron İletişim, E-postalar & PR İnceleme",
                        blockType = BlockType.BUFFER,
                        startTime = LocalTime.of(14, 0),
                        endTime = LocalTime.of(15, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Antrenman / Fitness / Kardiyo",
                        blockType = BlockType.WORKOUT,
                        startTime = LocalTime.of(16, 0),
                        endTime = LocalTime.of(17, 15)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Stratejik Planlama & Ürün Vizyonu",
                        blockType = BlockType.DEEP_WORK,
                        startTime = LocalTime.of(18, 30),
                        endTime = LocalTime.of(20, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 2L,
                        title = "Ekran Dışı Dinlenme & Gün Kapanışı",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(21, 30),
                        endTime = LocalTime.of(22, 30)
                    )
                )
            ),

            // 3. Weekend Rest Template
            PredefinedDayTemplate(
                template = DayTemplate(
                    id = 3L,
                    name = "Hafta Sonu Dinlenme & Yenilenme",
                    description = "Doğal uyanış, açık hava hareketi, kitap okuma ve haftalık değerlendirme.",
                    iconName = "spa",
                    isDefault = true
                ),
                type = DayTemplateType.WEEKEND_REST,
                blocks = listOf(
                    TemplateBlock(
                        id = 0L,
                        templateId = 3L,
                        title = "Doğal Uyanış & Yavaş Kahvaltı",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(8, 30),
                        endTime = LocalTime.of(9, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 3L,
                        title = "Doğa Yürüyüşü / Açık Hava Hareketi",
                        blockType = BlockType.WORKOUT,
                        startTime = LocalTime.of(9, 30),
                        endTime = LocalTime.of(11, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 3L,
                        title = "Hobi, Sanat veya Kitap Okuma",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(12, 0),
                        endTime = LocalTime.of(14, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 3L,
                        title = "Sosyal Vakit & Sevdiklerle Zaman",
                        blockType = BlockType.REST,
                        startTime = LocalTime.of(15, 0),
                        endTime = LocalTime.of(17, 0)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 3L,
                        title = "Haftalık Değerlendirme & Gelecek Hafta Önizlemesi",
                        blockType = BlockType.BUFFER,
                        startTime = LocalTime.of(18, 0),
                        endTime = LocalTime.of(19, 30)
                    ),
                    TemplateBlock(
                        id = 0L,
                        templateId = 3L,
                        title = "Sakinleşme, Meditasyon & Uyku",
                        blockType = BlockType.SLEEP,
                        startTime = LocalTime.of(21, 30),
                        endTime = LocalTime.of(22, 30)
                    )
                )
            )
        )
    }
}
