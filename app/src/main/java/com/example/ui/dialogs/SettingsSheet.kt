package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DriveCategoryFolder
import com.example.db.QuizHistoryEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    quizHistory: List<QuizHistoryEntity>,
    isDeveloperUnlocked: Boolean = false,
    isDriveSyncing: Boolean = false,
    isPullingUpdates: Boolean = false,
    linkedDriveAccount: String = "f94976173@gmail.com",
    driveCategoryFolders: List<DriveCategoryFolder> = emptyList(),
    webAppEndpoint: String = "https://ais-pre-cfr26ct6xov6ir5jqbkj3l-327265371817.europe-west2.run.app",
    lastDriveSyncTime: String = "Active",
    driveSyncLogs: List<String> = emptyList(),
    wallpaperPreset: String = "Atmospheric Dark",
    isWallpaperEnabled: Boolean = true,
    downloadedCount: Int = 0,
    onSetWallpaperPreset: (String) -> Unit = {},
    onToggleWallpaper: (Boolean) -> Unit = {},
    onSecretTap: () -> Unit = {},
    onSyncAllDrive: () -> Unit = {},
    onPullRemoteUpdates: () -> Unit = {},
    onSyncDriveFolder: (DriveCategoryFolder) -> Unit = {},
    onOpenDriveFolder: (String) -> Unit = {},
    onOpenDownloadsManager: () -> Unit = {},
    onImportManifestJson: (String) -> Unit = {},
    onCopyWebAppLink: () -> Unit = {},
    onExportJson: () -> String = { "" },
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isDarkMode by remember { mutableStateOf(true) }
    var showAppInstaller by remember { mutableStateOf(false) }
    var showImportManifestDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }

    var feedbackSubject by remember { mutableStateOf("") }
    var feedbackText by remember { mutableStateOf("") }
    var feedbackSentMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0B132B),
        contentColor = Color.White,
        modifier = Modifier.testTag("sheet_settings")
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0D9488)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text(
                            text = "DATANURSE Settings & Preferences",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Drive Workspace sync, appearance & developer controls",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Visual Appearance & Theme Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.LightMode, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Text("Visual Appearance & Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = if (isDarkMode) "Active: Dark Mode" else "Active: Light Mode",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Light Mode Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (!isDarkMode) Color(0xFF1E293B) else Color(0xFF111827).copy(alpha = 0.6f)
                                ),
                                border = if (!isDarkMode) BorderStroke(1.5.dp, Color(0xFF00E5FF)) else BorderStroke(1.dp, Color(0xFF1F2937)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isDarkMode = false }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Light Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("High contrast daylight theme for academic reading.", fontSize = 11.sp, color = Color(0xFF94A3B8), lineHeight = 15.sp)
                                }
                            }

                            // Dark Mode Card
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkMode) Color(0xFF132A3E) else Color(0xFF111827).copy(alpha = 0.6f)
                                ),
                                border = if (isDarkMode) BorderStroke(1.5.dp, Color(0xFF00E5A3)) else BorderStroke(1.dp, Color(0xFF1F2937)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { isDarkMode = true }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF312E81)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.NightlightRound, contentDescription = null, tint = Color(0xFFA5B4FC), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Dark Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Eye-safe nighttime clinical shift theme.", fontSize = 11.sp, color = Color(0xFF94A3B8), lineHeight = 15.sp)
                                }
                            }
                        }
                    }
                }

                // Main Activity Campus Background Wallpaper Settings Section
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132038)),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF0369A1)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text("Campus Wallpaper Background", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text("Subtle, dark academic campus backdrop", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                Switch(
                                    checked = isWallpaperEnabled,
                                    onCheckedChange = onToggleWallpaper,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF0284C7),
                                        uncheckedThumbColor = Color(0xFF94A3B8),
                                        uncheckedTrackColor = Color(0xFF1E293B)
                                    )
                                )
                            }

                            if (isWallpaperEnabled) {
                                Text("Select Backdrop Atmosphere & Contrast:", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                val presets = listOf("Atmospheric Dark", "Vibrant Dark", "Deep Night", "Solid Black")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    presets.forEach { preset ->
                                        val isSelected = wallpaperPreset == preset
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                            border = if (isSelected) BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { onSetWallpaperPreset(preset) }
                                        ) {
                                            Text(
                                                text = preset,
                                                fontSize = 9.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Google Drive & WebApp Universal Cloud Synchronization Hub
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1F33)),
                        border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0D9488)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Column {
                                        Text("Google Drive & WebApp Sync Hub", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF064E3B)) {
                                                Text("MULTI-USER ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF075985)) {
                                                Text("WEBAPP READY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "When you sync files to Google Drive, the updated syllabus modules, clinical notes, and OSCE stations are broadcast in real-time so all students and the companion WebApp can see and use them immediately.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 16.sp
                            )

                            // WebApp Live Link Banner
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF081220),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Public, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                            Text("Live Universal WebApp Portal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Text("v2.2 Universal", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    Text(
                                        text = webAppEndpoint,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF38BDF8),
                                        maxLines = 1
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("DATANURSE WebApp URL", webAppEndpoint))
                                                onCopyWebAppLink()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f).height(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy Web Link", fontSize = 10.sp, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webAppEndpoint))
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("DATANURSE WebApp URL", webAppEndpoint))
                                                    onCopyWebAppLink()
                                                }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.weight(1f).height(32.dp)
                                        ) {
                                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Open WebApp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Primary Action Buttons: Push & Pull
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSyncAllDrive() },
                                    enabled = !isDriveSyncing,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    if (isDriveSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Syncing...", fontSize = 11.sp)
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync & Broadcast", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { onPullRemoteUpdates() },
                                    enabled = !isPullingUpdates,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    if (isPullingUpdates) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Checking...", fontSize = 11.sp, color = Color.White)
                                    } else {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF38BDF8))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pull Updates", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                            }

                            // Import / Merge Remote Manifest Button
                            OutlinedButton(
                                onClick = { showImportManifestDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Input, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFA78BFA))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import Shared Drive / WebApp Manifest Payload", fontSize = 11.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Dedicated Datanurse Downloads Manager Card (/Download/Datanurse/)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2338)),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0284C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Column {
                                        Text("Datanurse Downloads Manager", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text("Location: /Download/Datanurse/", fontSize = 10.sp, color = Color(0xFF38BDF8), fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0369A1)
                                ) {
                                    Text(
                                        text = "$downloadedCount Files Saved",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Whenever you download documents, past papers, or clinical checklists, they are automatically saved to your device's '/Download/Datanurse/' folder and indexed for offline reading and sharing.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )

                            Button(
                                onClick = onOpenDownloadsManager,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.fillMaxWidth().height(38.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Downloads Manager ($downloadedCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Linked Google Drive Account & Category Folders (f94976173@gmail.com)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2E)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF059669)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Column {
                                        Text("Linked Google Drive Account", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text(linkedDriveAccount, fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF064E3B)) {
                                    Text("CONNECTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }

                            Text(
                                text = "Category folders mapped to your account. Files uploaded to these folders on Google Drive can be synced directly into the app and shared with the webapp:",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 15.sp
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                driveCategoryFolders.forEach { folder ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0B1624),
                                        border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF1E293B)
                                                    ) {
                                                        Text(
                                                            text = folder.icon,
                                                            fontSize = 14.sp,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                    Column {
                                                        Text(
                                                            text = folder.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = Color.White
                                                        )
                                                        Text(
                                                            text = folder.path,
                                                            fontSize = 10.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = Color(0xFF38BDF8)
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF0F2E3A)
                                                ) {
                                                    Text(
                                                        text = "${folder.fileCount} items",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF38BDF8),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = folder.description,
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8),
                                                lineHeight = 14.sp
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { onOpenDriveFolder(folder.driveWebUrl) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(30.dp)
                                                ) {
                                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Open Folder", fontSize = 10.sp, color = Color.White)
                                                }

                                                Button(
                                                    onClick = { onSyncDriveFolder(folder) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.weight(1f).height(30.dp)
                                                ) {
                                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Sync & Import", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Android & VPhone (Virtual Phone) APK Section
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B3A)),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF6366F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text("Android & VPhone (Virtual Phone) APK", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Download and install DATANURSE natively on Android physical devices and VPhone virtual environments.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showAppInstaller = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Get APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // About DATANURSE (Secret Tap Trigger)
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSecretTap() }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }
                                Column {
                                    Text("About DATANURSE", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text("Zambian Nursing & Midwifery Academic Database & Clinical OSCE Station.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                                    Text("App Version: v2.4.0 (Native Android Build 104)", fontSize = 11.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Developer Console for Drive Files Sync (Hidden by default, unlocked via 5 taps)
                if (isDeveloperUnlocked) {
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                        Text("Developer Console & Drive Sync", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White)
                                    }
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF0369A1)) {
                                        Text("DRIVE v3 ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Cloud Storage Target: Google Drive (/DATANURSE_Zambia_Curriculum_2026/)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("Last Global Drive Sync: $lastDriveSyncTime", fontSize = 11.sp, color = Color(0xFF38BDF8))

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onSyncAllDrive() },
                                        enabled = !isDriveSyncing,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                    ) {
                                        if (isDriveSyncing) {
                                             CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Syncing...", fontSize = 11.sp)
                                        } else {
                                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Sync All Files", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val json = onExportJson()
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("DATANURSE JSON Backup", json))

                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TITLE, "DATANURSE Database Backup JSON")
                                                putExtra(Intent.EXTRA_TEXT, json)
                                                type = "application/json"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Export Database JSON Backup"))
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Export JSON", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Drive Daemon Console Logs:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF020617),
                                    modifier = Modifier.fillMaxWidth().height(90.dp)
                                ) {
                                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                                        items(driveSyncLogs) { logLine ->
                                            Text(
                                                text = logLine,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (logLine.contains("SUCCESS")) Color(0xFF4ADE80) else if (logLine.contains("Google Drive")) Color(0xFF38BDF8) else Color(0xFFCBD5E1),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Contact Compiler (CHANDA FELIX) Section
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF0D9488).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0D9488), modifier = Modifier.size(18.dp))
                                Text("Contact Compiler (CHANDA FELIX)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }

                            Text("Have past papers, updated modules, or feedback? Reach out directly.", fontSize = 11.sp, color = Color(0xFF94A3B8))

                            // Email display box with Copy button
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF020617),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("fchanda335@gmail.com", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    OutlinedButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Author Email", "fchanda335@gmail.com"))
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFCBD5E1))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Email", fontSize = 10.sp, color = Color(0xFFCBD5E1))
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = feedbackSubject,
                                onValueChange = { feedbackSubject = it },
                                placeholder = { Text("Inquiry subject (e.g. Adding Year 3 Past Papers)", fontSize = 11.sp, color = Color(0xFF64748B)) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF020617),
                                    unfocusedContainerColor = Color(0xFF020617),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = feedbackText,
                                onValueChange = { feedbackText = it },
                                placeholder = { Text("Write your note or question to Chanda Felix...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF0D9488),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF020617),
                                    unfocusedContainerColor = Color(0xFF020617),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                            )

                            Button(
                                onClick = {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:fchanda335@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "[DATANURSE] ${feedbackSubject.ifEmpty { "Inquiry" }}")
                                        putExtra(Intent.EXTRA_TEXT, feedbackText)
                                    }
                                    try {
                                        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
                                    } catch (e: Exception) {}
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Email with Message", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("DATANURSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("fchanda335@gmail.com", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("Close Settings", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }

    if (showAppInstaller) {
        AppInstallerDialog(
            onExportJson = onExportJson,
            onDismiss = { showAppInstaller = false }
        )
    }

    if (showImportManifestDialog) {
        AlertDialog(
            onDismissRequest = { showImportManifestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF38BDF8))
                    Text("Import Shared Curriculum & Notes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste a shared DATANURSE JSON manifest or Google Drive sync payload from a classmate, instructor, or the WebApp.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        placeholder = { Text("Paste JSON payload here...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isNotBlank()) {
                            onImportManifestJson(importJsonInput)
                            showImportManifestDialog = false
                            importJsonInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                ) {
                    Text("Import & Sync", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportManifestDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}
