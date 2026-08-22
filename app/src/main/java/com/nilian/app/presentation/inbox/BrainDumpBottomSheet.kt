package com.nilian.app.presentation.inbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nilian.app.core.ui.components.CalmCard
import com.nilian.app.core.ui.theme.BottomSheetShape
import com.nilian.app.core.ui.theme.CardShapeLarge
import com.nilian.app.core.ui.theme.CardShapeMedium
import com.nilian.app.core.ui.theme.NilianTheme
import com.nilian.app.core.ui.theme.PillShape
import com.nilian.app.core.ui.theme.SagePrimary
import com.nilian.app.core.ui.theme.extendedColors
import com.nilian.app.domain.model.InboxNote
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Instant friction-free Brain Dump Bottom Sheet.
 * Allows rapid continuous thought capture with immediate local persistence.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainDumpBottomSheet(
    onDismiss: () -> Unit,
    onSaveNote: (content: String, tag: String?) -> Unit,
    onConvertToTask: (InboxNote) -> Unit = {},
    onConvertToEvent: (InboxNote) -> Unit = {},
    onConvertToGoal: (InboxNote) -> Unit = {},
    onDeleteNote: (InboxNote) -> Unit = {},
    recentNotes: List<InboxNote> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    val defaultTags = listOf("#fikir", "#görev", "#acil", "#proje", "#okul", "#kişisel", "#kitap")

    LaunchedEffect(Unit) {
        // Auto-request focus for zero friction
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SagePrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🧠",
                            fontSize = 18.sp
                        )
                    }
                    Column {
                        Text(
                            text = "Hızlı Zihin Boşaltma",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Aklına gelenleri anında kaydet, sonra organize et",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // High-Focus Instant Capture Input Box
            CalmCard(
                shape = CardShapeMedium,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                borderColor = SagePrimary.copy(alpha = 0.5f),
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Aklındakini yaz... Fikir, görev, not, toplantı hatırlatması...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        minLines = 2,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                    )

                    // Quick Tags Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(defaultTags) { tag ->
                            val isSelected = selectedTag == tag
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(
                                        if (isSelected) SagePrimary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        selectedTag = if (isSelected) null else tag
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Save Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${inputText.length} karakter",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )

                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onSaveNote(inputText.trim(), selectedTag)
                                    inputText = ""
                                    selectedTag = null
                                }
                            },
                            enabled = inputText.isNotBlank(),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SagePrimary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gelen Kutusuna Ekle",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            // Recent Captured Items Preview (Context)
            if (recentNotes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Son Eklenen Fikirler (${recentNotes.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentNotes, key = { it.id }) { note ->
                            BrainDumpRecentNoteItem(
                                note = note,
                                onConvertToTask = { onConvertToTask(note) },
                                onConvertToEvent = { onConvertToEvent(note) },
                                onConvertToGoal = { onConvertToGoal(note) },
                                onDelete = { onDeleteNote(note) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Note card inside the bottom sheet with instant quick conversion shortcuts.
 */
@Composable
private fun BrainDumpRecentNoteItem(
    note: InboxNote,
    onConvertToTask: () -> Unit,
    onConvertToEvent: () -> Unit,
    onConvertToGoal: () -> Unit,
    onDelete: () -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale("tr")) }

    CalmCard(
        shape = CardShapeMedium,
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(
                        onClick = { isMenuOpen = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Seçenekler",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("⚡ Göreve Dönüştür") },
                            onClick = {
                                isMenuOpen = false
                                onConvertToTask()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.TaskAlt, contentDescription = null, tint = SagePrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📅 Etkinliğe Dönüştür") },
                            onClick = {
                                isMenuOpen = false
                                onConvertToEvent()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Event, contentDescription = null, tint = Color(0xFF3B82F6))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🎯 Hedefe Dönüştür") },
                            onClick = {
                                isMenuOpen = false
                                onConvertToGoal()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color(0xFFF97316))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🗑️ Sil", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                isMenuOpen = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer (Timestamp + Tags + Quick Action Pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = note.createdAt.format(timeFormatter),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    note.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(SagePrimary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = SagePrimary
                            )
                        }
                    }
                }

                // Quick Action Mini Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onConvertToTask() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "⚡ Görev Yap",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SagePrimary
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// Previews
// =========================================================================

@Preview(name = "Brain Dump Sheet Preview", showBackground = true)
@Composable
private fun BrainDumpPreview() {
    NilianTheme(darkTheme = true) {
        Surface(modifier = Modifier.padding(16.dp)) {
            val sampleNotes = listOf(
                InboxNote(
                    id = 1,
                    content = "Bahar dönemi tez konusu için danışman hocayla görüş",
                    tags = listOf("#okul", "#acil")
                ),
                InboxNote(
                    id = 2,
                    content = "Nilian için offline SQLite şifreleme modülü incele",
                    tags = listOf("#proje")
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sampleNotes.forEach { note ->
                    BrainDumpRecentNoteItem(
                        note = note,
                        onConvertToTask = {},
                        onConvertToEvent = {},
                        onConvertToGoal = {},
                        onDelete = {}
                    )
                }
            }
        }
    }
}
