package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NursingTopic
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsHubSheet(
    topics: List<NursingTopic>,
    onDismiss: () -> Unit
) {
    var selectedRegion by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var activeTopic by remember { mutableStateOf<NursingTopic?>(null) }
    var tutorQuestion by remember { mutableStateOf("") }
    var tutorAnswer by remember { mutableStateOf<String?>(null) }
    var isAskingTutor by remember { mutableStateOf(false) }

    val filteredTopics = remember(topics, selectedRegion, searchQuery) {
        topics.filter { t ->
            val matchesRegion = when (selectedRegion) {
                "Zambia" -> t.region.contains("Zambia", ignoreCase = true)
                "Global" -> t.region.contains("Global", ignoreCase = true)
                else -> true
            }
            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                    t.title.lowercase().contains(q) ||
                    t.summary.lowercase().contains(q) ||
                    t.category.lowercase().contains(q) ||
                    t.tags.any { it.lowercase().contains(q) }
            matchesRegion && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_topics_hub")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "Nursing Topics & Clinical Guidelines",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Zambia (GNCZ/MoH) & Global (WHO) Standards",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            if (activeTopic == null) {
                // Topic Catalog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All", "Zambia", "Global").forEach { r ->
                        FilterChip(
                            selected = selectedRegion == r,
                            onClick = { selectedRegion = r },
                            label = { Text(r, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search topics, malaria, cholera, sepsis, EmONC...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredTopics) { topic ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    activeTopic = topic
                                    tutorAnswer = null
                                    tutorQuestion = ""
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (topic.region == "Zambia") Color(0xFFFEF3C7) else CyanContainerLight
                                    ) {
                                        Text(
                                            text = topic.region.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (topic.region == "Zambia") Color(0xFFB45309) else CyanSecondary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(topic.level, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(topic.summary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Authority: ${topic.institutionOrGuideline}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            } else {
                // Topic Reader & AI Tutor Screen
                val topic = activeTopic!!
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        TextButton(
                            onClick = { activeTopic = null },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back to Topics Catalog", fontSize = 12.sp)
                        }
                    }

                    item {
                        Text(topic.title, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Guideline: ${topic.institutionOrGuideline}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(topic.summary, fontSize = 13.sp, lineHeight = 20.sp)
                    }

                    if (topic.keyPearls.isNotEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("💡 High-Yield Clinical Pearls", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF92400E))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    topic.keyPearls.forEach { pearl ->
                                        Text("• $pearl", fontSize = 12.sp, color = Color(0xFF78350F), modifier = Modifier.padding(vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (topic.priorityInterventions.isNotEmpty()) {
                        item {
                            Text("Standard Priority Interventions (ADPIE)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = TealContainerLight)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    topic.priorityInterventions.forEach { inter ->
                                        Text(inter, fontSize = 12.sp, color = TealPrimaryDark, modifier = Modifier.padding(vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🎯 Exam Focus (Licensure & OSCE):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text(topic.examFocus, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }

                    // Clinical AI Tutor Interactive Section
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, TealPrimary)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ask Clinical AI Tutor", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TealPrimary)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = tutorQuestion,
                                    onValueChange = { tutorQuestion = it },
                                    placeholder = { Text("Ask about dosage, diagnostic criteria, or emergency steps...", fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        isAskingTutor = true
                                        // Generates clinical tutor answer directly based on the topic
                                        tutorAnswer = generateClinicalAnswer(topic, tutorQuestion)
                                        isAskingTutor = false
                                    },
                                    enabled = tutorQuestion.isNotBlank() && !isAskingTutor,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                ) {
                                    Text("Ask Tutor")
                                }

                                if (tutorAnswer != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = TealContainerLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Clinical Tutor Response:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TealPrimary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(tutorAnswer!!, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateClinicalAnswer(topic: NursingTopic, query: String): String {
    val q = query.lowercase()
    return when {
        q.contains("dose") || q.contains("dosage") || q.contains("drug") || q.contains("medication") ->
            "Clinical Dosage Guideline for ${topic.title}:\nAdhere strictly to standard protocols (${topic.institutionOrGuideline}). Always verify patient weight (especially pediatric kg), renal status, and administer with an independent double-check."
        q.contains("priority") || q.contains("first") || q.contains("immediate") ->
            "Immediate Priority Action: ${topic.priorityInterventions.firstOrNull() ?: "Stabilize Airway, Breathing, and Circulation (ABC) and check blood glucose immediately."}"
        q.contains("exam") || q.contains("gncz") || q.contains("nmcz") || q.contains("nclex") ->
            "Key Exam Focus: ${topic.examFocus}. Ensure you can state both theoretical rationale and step-by-step bedside procedural execution."
        else ->
            "Clinical Overview for \"${topic.title}\":\n${topic.summary}\n\nKey Safety Pearl: ${topic.keyPearls.firstOrNull() ?: "Continuous monitoring of vital parameters is required."}"
    }
}
