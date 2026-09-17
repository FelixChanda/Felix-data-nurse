package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalloutData
import com.example.model.ResourceItem
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceDetailSheet(
    resource: ResourceItem,
    onDismiss: () -> Unit,
    onOpenFlashcards: (ResourceItem) -> Unit,
    onOpenQuiz: (ResourceItem) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onDownloadRawDocument: ((ResourceItem) -> Unit)? = null,
    onSyncToDrive: ((ResourceItem) -> Unit)? = null,
    onShareWebAppLink: ((ResourceItem) -> Unit)? = null
) {
    val context = LocalContext.current
    var isDriveSyncSuccess by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_resource_detail")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = resource.category.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (resource.isDriveSynced || isDriveSyncSuccess) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE0F2FE)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Drive Synced", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleBookmark(resource.id) }) {
                        Icon(
                            imageVector = if (resource.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (resource.isBookmarked) AmberTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            HorizontalDivider()

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = resource.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${resource.domain} • ${resource.yearLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Published by: ${resource.authorOrInstitution} • Updated: ${resource.updatedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Bar: Download Raw Document, Drive Sync & External Link
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (onDownloadRawDocument != null) {
                                        onDownloadRawDocument(resource)
                                    } else {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TITLE, resource.title)
                                            putExtra(Intent.EXTRA_SUBJECT, "DATANURSE Document: ${resource.title}")
                                            putExtra(Intent.EXTRA_TEXT, "DATANURSE RESOURCE:\n\n${resource.title}\n${resource.description}")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Download / Export ${resource.title}")
                                        context.startActivity(shareIntent)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Raw Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    isDriveSyncSuccess = true
                                    onSyncToDrive?.invoke(resource)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF0284C7))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Drive & Web Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    onShareWebAppLink?.invoke(resource)
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF1E293B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFF38BDF8))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WebApp Link", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }

                            if (!resource.externalUrl.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.externalUrl))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("External File", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = resource.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }

                // Module: Learning outcomes & Syllabus Units
                if (resource.learningOutcomes.isNotEmpty()) {
                    item {
                        SectionHeader("Core Learning Outcomes")
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                resource.learningOutcomes.forEach { outcome ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ClinicalAlertGreen,
                                            modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(outcome, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }

                if (resource.syllabus.isNotEmpty()) {
                    item { SectionHeader("Curriculum Syllabus & Units (${resource.syllabus.size} Units)") }
                    items(resource.syllabus) { unit ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Unit ${unit.unitNumber}: ${unit.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(shape = RoundedCornerShape(4.dp), color = TealContainerLight) {
                                        Text(
                                            "${unit.durationWeeks} Weeks",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (unit.topics.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Topics:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    unit.topics.forEach { t ->
                                        Text("• $t", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                                    }
                                }
                                if (unit.keyCompetencies.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Clinical Competencies:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ClinicalAlertGreen)
                                    unit.keyCompetencies.forEach { c ->
                                        Text("✓ $c", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Past Paper: Questions with Rationale & Marking Scheme
                if (resource.questions.isNotEmpty()) {
                    item {
                        SectionHeader("Examination Questions & Model Answers (${resource.questions.size} Questions)")
                    }
                    items(resource.questions) { q ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Question ${q.number} (${q.marks} Marks)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberTertiary)
                                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surface) {
                                        Text(q.type.replace("_", " ").uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(q.questionText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)

                                if (q.options.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    q.options.forEachIndexed { idx, opt ->
                                        val isCorrect = q.correctOptionIndex == idx
                                        Surface(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isCorrect) ClinicalAlertGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Text("${('A' + idx)}. ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text(opt, fontSize = 12.sp, color = if (isCorrect) ClinicalAlertGreen else MaterialTheme.colorScheme.onSurface)
                                                if (isCorrect) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Icon(Icons.Default.Check, contentDescription = "Correct", tint = ClinicalAlertGreen, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                if (q.clinicalRationale.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = TealContainerLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text("Clinical Rationale & Marking Scheme:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                                            Text(q.clinicalRationale, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Textbook: Table of Contents & Pearls
                if (resource.tableOfContents.isNotEmpty()) {
                    item { SectionHeader("Table of Contents & High-Yield Pearls") }
                    items(resource.tableOfContents) { ch ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Chapter ${ch.chapterNumber}: ${ch.title}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                if (ch.summary.isNotBlank()) {
                                    Text(ch.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                                }
                                ch.keyPearls.forEach { pearl ->
                                    Text("💡 $pearl", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                }

                // Clinical Notes: Sections & Callout Boxes
                if (resource.sections.isNotEmpty()) {
                    item { SectionHeader("Clinical Protocol & Management Sections") }
                    items(resource.sections) { sec ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(sec.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(sec.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)

                                if (sec.bulletPoints.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    sec.bulletPoints.forEach { pt ->
                                        Text("• $pt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
                                    }
                                }

                                if (sec.callout != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    CalloutBox(sec.callout)
                                }
                            }
                        }
                    }
                }

                // Document Content Text
                if (!resource.documentContentText.isNullOrBlank()) {
                    item {
                        SectionHeader("Full Guideline Document")
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resource.documentContentText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Action Bar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenFlashcards(resource)
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Style, contentDescription = null, tint = AmberTertiary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Study Cards", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onOpenQuiz(resource)
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Launch Quiz", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CalloutBox(callout: CalloutData) {
    val (bgColor, icon, title, tint) = when (callout.type.lowercase()) {
        "warning" -> Quadruple(Color(0xFFFEF3C7), Icons.Default.Warning, "CLINICAL WARNING", Color(0xFFD97706))
        "danger" -> Quadruple(Color(0xFFFEE2E2), Icons.Default.Error, "EMERGENCY SAFETY RULE", Color(0xFFDC2626))
        "rule" -> Quadruple(Color(0xFFE0F2FE), Icons.Default.Gavel, "LICENSURE RULE", Color(0xFF0284C7))
        else -> Quadruple(Color(0xFFCCFBF1), Icons.Default.Lightbulb, "CLINICAL PEARL", Color(0xFF0F766E))
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp).padding(top = 2.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = tint, letterSpacing = 0.5.sp)
                Text(callout.text, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
