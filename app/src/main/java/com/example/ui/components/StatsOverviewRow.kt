package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ResourceItem

@Composable
fun StatsOverviewRow(
    resources: List<ResourceItem>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = resources.size
    val modulesCount = resources.count { it.category.equals("modules", ignoreCase = true) }
    val papersCount = resources.count { it.category.equals("past_papers", ignoreCase = true) }
    val booksCount = resources.count { it.category.equals("textbooks", ignoreCase = true) }
    val notesCount = resources.count { it.category.equals("notes", ignoreCase = true) }
    val docsCount = resources.count { it.category.equals("documents", ignoreCase = true) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatPill(
            icon = Icons.Default.Layers,
            label = "Total Items",
            count = totalCount,
            isSelected = selectedCategory == "all",
            badgeColor = MaterialTheme.colorScheme.primary,
            onClick = { onSelectCategory("all") }
        )
        StatPill(
            icon = Icons.Default.School,
            label = "Modules",
            count = modulesCount,
            isSelected = selectedCategory == "modules",
            badgeColor = Color(0xFF0F766E),
            onClick = { onSelectCategory("modules") }
        )
        StatPill(
            icon = Icons.Default.Quiz,
            label = "Past Exams",
            count = papersCount,
            isSelected = selectedCategory == "past_papers",
            badgeColor = Color(0xFFD97706),
            onClick = { onSelectCategory("past_papers") }
        )
        StatPill(
            icon = Icons.Default.MenuBook,
            label = "Textbooks",
            count = booksCount,
            isSelected = selectedCategory == "textbooks",
            badgeColor = Color(0xFF7C3AED),
            onClick = { onSelectCategory("textbooks") }
        )
        StatPill(
            icon = Icons.Default.Description,
            label = "Clinical Notes",
            count = notesCount,
            isSelected = selectedCategory == "notes",
            badgeColor = Color(0xFF059669),
            onClick = { onSelectCategory("notes") }
        )
        StatPill(
            icon = Icons.Default.VerifiedUser,
            label = "Guidelines",
            count = docsCount,
            isSelected = selectedCategory == "documents",
            badgeColor = Color(0xFF0284C7),
            onClick = { onSelectCategory("documents") }
        )
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    label: String,
    count: Int,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) badgeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, badgeColor) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) badgeColor else MaterialTheme.colorScheme.onSurface
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) badgeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}
