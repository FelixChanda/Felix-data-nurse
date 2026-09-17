package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    bookmarkedCount: Int,
    isShowingSavedOnly: Boolean,
    onOpenClinicalTools: () -> Unit,
    onToggleSavedOnly: () -> Unit,
    onOpenTopics: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenOsce: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color(0xFF071220),
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Tools
            BottomNavItem(
                icon = Icons.Outlined.Calculate,
                label = "Tools",
                badgeText = null,
                badgeColor = Color.Unspecified,
                isSelected = false,
                testTag = "nav_clinical_tools",
                onClick = onOpenClinicalTools
            )

            // 2. Saved
            BottomNavItem(
                icon = if (isShowingSavedOnly) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                label = "Saved",
                badgeText = if (bookmarkedCount > 0) bookmarkedCount.toString() else null,
                badgeColor = Color(0xFF0284C7),
                isSelected = isShowingSavedOnly,
                testTag = "nav_saved",
                onClick = onToggleSavedOnly
            )

            // 3. Topics (with AI Badge)
            BottomNavItem(
                icon = Icons.Outlined.MenuBook,
                label = "Topics",
                badgeText = "AI",
                badgeColor = Color(0xFFD97706),
                isSelected = false,
                testTag = "nav_topics_hub",
                onClick = onOpenTopics
            )

            // 4. Flashcards
            BottomNavItem(
                icon = Icons.Outlined.AutoAwesome,
                label = "Flashcards",
                badgeText = null,
                badgeColor = Color.Unspecified,
                isSelected = false,
                testTag = "nav_flashcards",
                onClick = onOpenFlashcards
            )

            // 5. OSCE (with LIVE Red Badge)
            BottomNavItem(
                icon = Icons.Outlined.Videocam,
                label = "OSCE",
                badgeText = "LIVE",
                badgeColor = Color(0xFFEF4444),
                isSelected = false,
                testTag = "nav_osce_hub",
                onClick = onOpenOsce
            )

            // 6. Settings
            BottomNavItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                badgeText = null,
                badgeColor = Color.Unspecified,
                isSelected = false,
                testTag = "nav_settings",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    badgeText: String?,
    badgeColor: Color,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = badgeColor,
                        modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8)
        )
    }
}
