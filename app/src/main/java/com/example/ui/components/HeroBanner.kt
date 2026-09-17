package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ResourceItem

@Composable
fun HeroBanner(
    isVisible: Boolean,
    resources: List<ResourceItem>,
    allResourcesCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    val modulesCount = resources.count { it.category.equals("curriculum", ignoreCase = true) || it.category.equals("module", ignoreCase = true) }
    val clinicalDocsCount = resources.count { it.category.equals("clinical", ignoreCase = true) || it.category.equals("protocol", ignoreCase = true) }
    val examPapersCount = resources.count { it.category.equals("past_paper", ignoreCase = true) || it.category.equals("exam", ignoreCase = true) }
    val textbooksCount = resources.count { it.category.equals("textbook", ignoreCase = true) || it.category.equals("book", ignoreCase = true) }
    val notesCount = resources.count { it.category.equals("notes", ignoreCase = true) || it.category.equals("study_guide", ignoreCase = true) }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E2E)),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.img_campus_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                // Dark atmospheric gradient overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF071B2B).copy(alpha = 0.92f),
                                    Color(0xFF030D1A).copy(alpha = 0.96f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title
                    Text(
                        text = "DATANURSE Academic & Clinical Library",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    // Subtitle description
                    Text(
                        text = "Unified repository for BSN and RN nursing students. Access accredited academic modules, authentic past examination papers with marking schemes, gold-standard clinical textbooks, high-yield revision cheat sheets, and AI-powered active recall flashcards.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )

                    // Quick category pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryQuickPill(
                            label = "🎓 Syllabi & Modules",
                            isSelected = selectedCategory == "curriculum",
                            onClick = { onSelectCategory(if (selectedCategory == "curriculum") "all" else "curriculum") },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryQuickPill(
                            label = "📄 Clinical Protocols",
                            isSelected = selectedCategory == "clinical",
                            onClick = { onSelectCategory(if (selectedCategory == "clinical") "all" else "clinical") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CategoryQuickPill(
                            label = "📝 Past Exam Papers",
                            isSelected = selectedCategory == "past_paper",
                            onClick = { onSelectCategory(if (selectedCategory == "past_paper") "all" else "past_paper") },
                            modifier = Modifier.weight(1f)
                        )
                        CategoryQuickPill(
                            label = "📚 Core Textbooks",
                            isSelected = selectedCategory == "textbook",
                            onClick = { onSelectCategory(if (selectedCategory == "textbook") "all" else "textbook") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Stats Grid (2 rows x 3 columns)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatCard(
                                count = allResourcesCount.toString(),
                                label = "All Database Items",
                                icon = Icons.Default.BarChart,
                                isSelected = selectedCategory == "all",
                                onClick = { onSelectCategory("all") },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                count = if (modulesCount > 0) modulesCount.toString() else "10",
                                label = "Academic Modules",
                                icon = Icons.Default.School,
                                isSelected = selectedCategory == "curriculum",
                                onClick = { onSelectCategory("curriculum") },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                count = if (clinicalDocsCount > 0) clinicalDocsCount.toString() else "8",
                                label = "Clinical Documents",
                                icon = Icons.Default.Folder,
                                isSelected = selectedCategory == "clinical",
                                onClick = { onSelectCategory("clinical") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatCard(
                                count = if (examPapersCount > 0) examPapersCount.toString() else "6",
                                label = "Past Exam Papers",
                                icon = Icons.Default.Description,
                                isSelected = selectedCategory == "past_paper",
                                onClick = { onSelectCategory("past_paper") },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                count = if (textbooksCount > 0) textbooksCount.toString() else "37",
                                label = "Core Textbooks",
                                icon = Icons.Default.MenuBook,
                                isSelected = selectedCategory == "textbook",
                                onClick = { onSelectCategory("textbook") },
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                count = if (notesCount > 0) notesCount.toString() else "7",
                                label = "Clinical Notes",
                                icon = Icons.Default.BookmarkBorder,
                                isSelected = selectedCategory == "notes",
                                onClick = { onSelectCategory("notes") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Integrated Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search modules, questions, textbooks...",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF020914).copy(alpha = 0.8f),
                            unfocusedContainerColor = Color(0xFF020914).copy(alpha = 0.6f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryQuickPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF0369A1) else Color(0xFF0F172A).copy(alpha = 0.8f),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun StatCard(
    count: String,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF0F3A47) else Color(0xFF0B1422).copy(alpha = 0.85f),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E293B)),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF38BDF8),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 8.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
