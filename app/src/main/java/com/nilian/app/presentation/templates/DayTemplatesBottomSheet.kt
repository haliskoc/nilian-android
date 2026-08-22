package com.nilian.app.presentation.templates

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.components.CalmTextField
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.DialogShape
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.BlockType
import com.nilian.app.domain.model.DayTemplate
import com.nilian.app.domain.model.DayTemplateWithBlocks
import com.nilian.app.domain.model.TemplateBlock
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Built-in Preset Day Templates.
 */
object PresetDayTemplates {

    val examDay = DayTemplateWithBlocks(
        template = DayTemplate(
            id = 101L,
            name = "Sınav / Final Günü",
            description = "Kahvaltı, zihin ısınması, odaklı sınav oturumu ve dinlenme blokları",
            iconName = "🎓",
            isDefault = true
        ),
        blocks = listOf(
            TemplateBlock(1, 101L, "Besleyici Kahvaltı & Zihin Isınması", BlockType.REST, LocalTime.of(7, 30), LocalTime.of(8, 30)),
            TemplateBlock(2, 101L, "Hızlı Formül & Not Tekrarı", BlockType.STUDY, LocalTime.of(8, 30), LocalTime.of(10, 0)),
            TemplateBlock(3, 101L, "Sınav / Final Oturumu", BlockType.DEEP_WORK, LocalTime.of(10, 0), LocalTime.of(13, 0)),
            TemplateBlock(4, 101L, "Yemek & Zihinsel Rahatlama", BlockType.REST, LocalTime.of(13, 0), LocalTime.of(14, 30)),
            TemplateBlock(5, 101L, "Hafif Tekrar & Yarınki Hazırlık", BlockType.STUDY, LocalTime.of(14, 30), LocalTime.of(17, 30)),
            TemplateBlock(6, 101L, "Yürüyüş / Spor & Tampon", BlockType.WORKOUT, LocalTime.of(17, 30), LocalTime.of(19, 0)),
            TemplateBlock(7, 101L, "Serbest Zaman & Uyku Hazırlığı", BlockType.REST, LocalTime.of(19, 0), LocalTime.of(22, 30))
        )
    )

    val deepCodingDay = DayTemplateWithBlocks(
        template = DayTemplate(
            id = 102L,
            name = "Girişimcilik / Derin Kodlama",
            description = "Kesintisiz derin odak sprintleri, tampon zaman ve standup planı",
            iconName = "🚀",
            isDefault = true
        ),
        blocks = listOf(
            TemplateBlock(11, 102L, "Güne Başlangıç & Standup Planı", BlockType.BUFFER, LocalTime.of(8, 30), LocalTime.of(9, 15)),
            TemplateBlock(12, 102L, "Derin Kodlama Sprint #1", BlockType.DEEP_WORK, LocalTime.of(9, 15), LocalTime.of(12, 0)),
            TemplateBlock(13, 102L, "Öğle Molası & Açık Hava", BlockType.REST, LocalTime.of(12, 0), LocalTime.of(13, 0)),
            TemplateBlock(14, 102L, "Derin Kodlama & Mimari Sprint #2", BlockType.DEEP_WORK, LocalTime.of(13, 0), LocalTime.of(16, 0)),
            TemplateBlock(15, 102L, "E-postalar & PR İncelemeleri", BlockType.BUFFER, LocalTime.of(16, 0), LocalTime.of(17, 0)),
            TemplateBlock(16, 102L, "Spor & Egzersiz", BlockType.WORKOUT, LocalTime.of(17, 0), LocalTime.of(18, 30)),
            TemplateBlock(17, 102L, "Kişisel Gelişim & Okuma", BlockType.STUDY, LocalTime.of(18, 30), LocalTime.of(22, 0))
        )
    )

    val weekendRechargeDay = DayTemplateWithBlocks(
        template = DayTemplate(
            id = 103L,
            name = "Hafta Sonu Dinlenme & Yenilenme",
            description = "Doğa yürüyüşü, kitap okuma, sosyal vakit ve haftalık yansıma",
            iconName = "🌿",
            isDefault = true
        ),
        blocks = listOf(
            TemplateBlock(21, 103L, "Sakin Sabah & Sağlıklı Brunch", BlockType.REST, LocalTime.of(9, 0), LocalTime.of(10, 30)),
            TemplateBlock(22, 103L, "Doğa Yürüyüşü / Açık Hava Hareketi", BlockType.WORKOUT, LocalTime.of(10, 30), LocalTime.of(12, 30)),
            TemplateBlock(23, 103L, "Kitap Okuma & Kişisel Notlar", BlockType.STUDY, LocalTime.of(13, 0), LocalTime.of(15, 30)),
            TemplateBlock(24, 103L, "Sosyal Vakit & Hobi", BlockType.REST, LocalTime.of(15, 30), LocalTime.of(19, 0)),
            TemplateBlock(25, 103L, "Haftalık Yansıma & Dinlenme", BlockType.REST, LocalTime.of(19, 0), LocalTime.of(22, 0))
        )
    )

    val allPresets = listOf(examDay, deepCodingDay, weekendRechargeDay)
}

/**
 * Bottom Sheet for Day Templates (One-Tap 24h Timeline Population).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayTemplatesBottomSheet(
    onDismiss: () -> Unit,
    onApplyTemplate: (DayTemplateWithBlocks) -> Unit,
    customTemplates: List<DayTemplateWithBlocks> = emptyList(),
    onCreateCustomTemplate: (String, String, List<TemplateBlock>) -> Unit = { _, _, _ -> },
    onDeleteCustomTemplate: ((DayTemplateWithBlocks) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isCreateModalOpen by remember { mutableStateOf(false) }

    val allTemplates = remember(customTemplates) {
        PresetDayTemplates.allPresets + customTemplates
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(SagePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = SagePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Tek Dokunuşla Gün Şablonları",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "İdeal gün akışınızı 24 saatlik zaman çizelgenize anında uygulayın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action: Create Custom Template Button
            OutlinedButton(
                onClick = { isCreateModalOpen = true },
                shape = PillShape,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SagePrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SagePrimary.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.DashboardCustomize,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+ Yeni Özel Gün Şablonu Oluştur",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Template Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(allTemplates, key = { it.template.id }) { templateWithBlocks ->
                    DayTemplateCard(
                        templateWithBlocks = templateWithBlocks,
                        onApply = {
                            onApplyTemplate(templateWithBlocks)
                            onDismiss()
                        },
                        onDelete = if (!templateWithBlocks.template.isDefault && onDeleteCustomTemplate != null) {
                            { onDeleteCustomTemplate(templateWithBlocks) }
                        } else null
                    )
                }
            }
        }
    }

    if (isCreateModalOpen) {
        CreateCustomTemplateDialog(
            onDismiss = { isCreateModalOpen = false },
            onSave = { name, icon, blocks ->
                onCreateCustomTemplate(name, icon, blocks)
                isCreateModalOpen = false
            }
        )
    }
}

/**
 * Card representing a Day Template with timeline blocks preview & "Apply" action.
 */
@Composable
fun DayTemplateCard(
    templateWithBlocks: DayTemplateWithBlocks,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    val template = templateWithBlocks.template
    val blocks = templateWithBlocks.blocks

    val totalHours = remember(blocks) {
        val totalMinutes = blocks.sumOf {
            ChronoUnit.MINUTES.between(it.startTime, it.endTime).coerceAtLeast(0)
        }
        val hrs = totalMinutes / 60
        val mins = totalMinutes % 60
        if (mins > 0) "${hrs}s ${mins}dk" else "${hrs} saat"
    }

    val deepWorkCount = remember(blocks) {
        blocks.count { it.blockType == BlockType.DEEP_WORK }
    }

    CalmCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShapeLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            // Top Row: Icon + Title + Description + Optional Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SagePrimary.copy(alpha = 0.12f))
                            .border(1.dp, SagePrimary.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = template.iconName ?: "📅",
                            fontSize = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!template.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = template.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Şablonu Sil",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata Chips Row (Duration, Blocks count, Deep Work count)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⏱️ $totalHours",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📋 ${blocks.size} Blok",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (deepWorkCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(SagePrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "⚡ $deepWorkCount Odak Sprint",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SagePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Blocks Horizontal Preview (Scrollable or Collapsed)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(blocks) { block ->
                    TemplateBlockChip(block = block)
                }
            }

            // Expandable Detailed Breakdown
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShapeMedium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                    blocks.forEach { block ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(getBlockColor(block.blockType))
                                )
                                Text(
                                    text = block.title,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${block.startTime.format(timeFormatter)} - ${block.endTime.format(timeFormatter)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: "Ayrıntılar" toggle + "Bugüne Uygula" (Apply to Today)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Ayrıntıları Gizle" else "Blokları İncele",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Button(
                    onClick = onApply,
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SagePrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bugüne Uygula",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

/**
 * Compact visual chip representing a single template block.
 */
@Composable
private fun TemplateBlockChip(block: TemplateBlock) {
    val blockColor = getBlockColor(block.blockType)
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Row(
        modifier = Modifier
            .clip(PillShape)
            .background(blockColor.copy(alpha = 0.15f))
            .border(0.8.dp, blockColor.copy(alpha = 0.35f), PillShape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(blockColor)
        )
        Text(
            text = block.title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "• ${block.startTime.format(formatter)}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun getBlockColor(blockType: BlockType): Color {
    val extended = MaterialTheme.extendedColors
    return when (blockType) {
        BlockType.SLEEP -> extended.blockSleep
        BlockType.WORKOUT -> extended.blockWorkout
        BlockType.STUDY -> extended.blockStudy
        BlockType.DEEP_WORK -> extended.blockDeepWork
        BlockType.REST -> extended.blockRest
        BlockType.BUFFER, BlockType.GENERAL, BlockType.OTHER -> extended.blockBuffer
    }
}

/**
 * Dialog to create and save a new custom Day Template.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String, blocks: List<TemplateBlock>) -> Unit
) {
    var templateName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🎯") }
    val availableIcons = listOf("🎓", "🚀", "🌿", "💡", "⚡", "📚", "🎨", "🏋️", "💻", "🧘")

    // Block editor state
    val customBlocks = remember {
        mutableStateListOf(
            TemplateBlock(1, 0, "Sabah Odak Bloğu", BlockType.DEEP_WORK, LocalTime.of(9, 0), LocalTime.of(11, 0)),
            TemplateBlock(2, 0, "Öğle Molası", BlockType.REST, LocalTime.of(12, 0), LocalTime.of(13, 0)),
            TemplateBlock(3, 0, "Öğleden Sonra Sprinti", BlockType.DEEP_WORK, LocalTime.of(14, 0), LocalTime.of(17, 0))
        )
    }

    var newBlockTitle by remember { mutableStateOf("") }
    var newBlockStartHour by remember { mutableStateOf("09") }
    var newBlockStartMin by remember { mutableStateOf("00") }
    var newBlockEndHour by remember { mutableStateOf("10") }
    var newBlockEndMin by remember { mutableStateOf("30") }
    var newBlockType by remember { mutableStateOf(BlockType.DEEP_WORK) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = DialogShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Yeni Gün Şablonu",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                    }
                }

                // Template Name Field
                CalmTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = "Şablon Adı",
                    placeholder = "Örn: Doktora Tez Yazım Günü"
                )

                // Emoji Icon Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "İkon Seçin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableIcons) { emoji ->
                            val isSelected = selectedIcon == emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) SagePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) SagePrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedIcon = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                // Configured Blocks List
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Şablon Blokları (${customBlocks.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(customBlocks) { block ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = block.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${block.startTime} - ${block.endTime} • ${block.blockType.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { customBlocks.remove(block) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Sil",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Block Inline Editor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = newBlockTitle,
                        onValueChange = { newBlockTitle = it },
                        placeholder = { Text("Yeni blok adı...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            if (newBlockTitle.isNotBlank()) {
                                val sH = newBlockStartHour.toIntOrNull()?.coerceIn(0, 23) ?: 9
                                val sM = newBlockStartMin.toIntOrNull()?.coerceIn(0, 59) ?: 0
                                val eH = newBlockEndHour.toIntOrNull()?.coerceIn(0, 23) ?: 10
                                val eM = newBlockEndMin.toIntOrNull()?.coerceIn(0, 59) ?: 30
                                customBlocks.add(
                                    TemplateBlock(
                                        id = System.currentTimeMillis(),
                                        templateId = 0,
                                        title = newBlockTitle.trim(),
                                        blockType = newBlockType,
                                        startTime = LocalTime.of(sH, sM),
                                        endTime = LocalTime.of(eH, eM)
                                    )
                                )
                                newBlockTitle = ""
                            }
                        },
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Text("+ Ekle", fontSize = 11.sp)
                    }
                }

                // Action Buttons: Cancel / Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("İptal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (templateName.isNotBlank() && customBlocks.isNotEmpty()) {
                                onSave(templateName.trim(), selectedIcon, customBlocks.toList())
                            }
                        },
                        enabled = templateName.isNotBlank() && customBlocks.isNotEmpty(),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SagePrimary)
                    ) {
                        Text("Şablonu Kaydet")
                    }
                }
            }
        }
    }
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "Day Templates Bottom Sheet Preview", showBackground = true)
@Composable
private fun DayTemplatesPreview() {
    NilianTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DayTemplateCard(
                    templateWithBlocks = PresetDayTemplates.examDay,
                    onApply = {}
                )
                DayTemplateCard(
                    templateWithBlocks = PresetDayTemplates.deepCodingDay,
                    onApply = {}
                )
            }
        }
    }
}
