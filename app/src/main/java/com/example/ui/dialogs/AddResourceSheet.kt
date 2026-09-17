package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddResourceSheet(
    onAddResource: (title: String, category: String, domain: String, yearLevel: String, description: String, content: String, tags: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("notes") }
    var domain by remember { mutableStateOf("Adult Health & Med-Surg") }
    var yearLevel by remember { mutableStateOf("Year 2 (Adult Health & Patho)") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("Clinical, Nursing, Ward") }

    val categories = listOf("notes", "modules", "documents", "past_papers", "textbooks")
    val domains = listOf(
        "Adult Health & Med-Surg",
        "Fundamentals & Assessment",
        "Pharmacology",
        "Maternal & Neonatal",
        "Pediatric Nursing",
        "Critical Care & Emergency",
        "Community & Public Health"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_add_resource")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Add Custom Clinical Resource",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            HorizontalDivider()

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Resource Title *") },
                        placeholder = { Text("e.g. Diabetic Ketoacidosis Emergency Nursing Protocol") },
                        modifier = Modifier.fillMaxWidth().testTag("input_add_title"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.replace("_", " ").uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Short Description / Summary *") },
                        placeholder = { Text("Clinical summary of this topic or guideline...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Detailed Clinical Content / Protocol *") },
                        placeholder = { Text("Enter the full notes, step-by-step nursing interventions, dosages, rationale...") },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        maxLines = 10,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Comma-separated)") },
                        placeholder = { Text("Emergency, DKA, Insulin, Ward") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onAddResource(title, category, domain, yearLevel, description, content, tags)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(bottom = 8.dp)
                    .testTag("btn_save_new_resource"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Save to Clinical Database", fontWeight = FontWeight.Bold)
            }
        }
    }
}
