package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.model.OsceComment
import com.example.model.OsceVideo
import com.example.ui.components.AdMobBanner
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OsceHubSheet(
    stations: List<OsceVideo>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var activeStation by remember { mutableStateOf<OsceVideo?>(stations.firstOrNull()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedChannelFilter by remember { mutableStateOf("All Channels") }
    var activeTab by remember { mutableStateOf(0) } // 0: Video Player, 1: Station Directory, 2: OSCETUBE Drive Folder

    // Player State
    var isPlaying by remember { mutableStateOf(false) }
    var currentSeconds by remember { mutableStateOf(145) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isMuted by remember { mutableStateOf(false) }
    var showCc by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var playerMode by remember { mutableStateOf("Native Player") } // "Native Player" or "Drive/Web Embed"
    var isSubscribed by remember { mutableStateOf(false) }
    var isLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(2420) }
    var isSaved by remember { mutableStateOf(false) }
    var showDriveInputDialog by remember { mutableStateOf(false) }
    var customDriveUrlInput by remember { mutableStateOf("") }

    // Sub-tab under Video Player
    var videoSubTab by remember { mutableStateOf(0) } // 0: Checklist & Steps, 1: Comments, 2: Equipment, 3: Examiner Pearls, 4: Up Next

    // User Comments List
    var commentsList by remember {
        mutableStateOf(
            listOf(
                OsceComment(
                    id = "c-1",
                    author = "Sr. Mwansa Banda, RN",
                    authorAvatarBg = 0xFF7C3AED,
                    institution = "UTH Intensive Care Unit",
                    timeAgo = "2 days ago",
                    text = "Remember during Step 6: 2 minutes of Chlorhexidine drying time is strictly timed with a stopwatch by examiners at UTH. Do not blow or touch the skin!",
                    likes = 48,
                    isPinned = true
                ),
                OsceComment(
                    id = "c-2",
                    author = "Dr. C. Tembo",
                    authorAvatarBg = 0xFF0D9488,
                    institution = "UNZA School of Nursing Sciences",
                    timeAgo = "5 days ago",
                    text = "Excellent procedure breakdown. For final year OSCEs, candidates must verbalize the 5 Rights and confirm patient identity before opening the sterile kit.",
                    likes = 31
                ),
                OsceComment(
                    id = "c-3",
                    author = "Nurse Chileshe K.",
                    authorAvatarBg = 0xFFEA580C,
                    institution = "LMMU Skills Lab",
                    timeAgo = "1 week ago",
                    text = "Watching this from the OSCETUBE Google Drive folder in HD. The checklist step synchronization is so helpful for clinical revision.",
                    likes = 19
                )
            )
        )
    }
    var newCommentText by remember { mutableStateOf("") }

    val channelFilters = listOf("All Channels", "UNZA", "UTH", "LMMU", "Apex", "WHO", "Google Drive OSCETUBE")

    val filteredStations = remember(stations, searchQuery, selectedChannelFilter) {
        val q = searchQuery.trim().lowercase()
        stations.filter {
            val matchesChannel = when (selectedChannelFilter) {
                "UNZA" -> it.creatorTag.equals("unza", ignoreCase = true) || it.institutionBadge.contains("UNZA", ignoreCase = true)
                "UTH" -> it.creatorTag.equals("uth", ignoreCase = true) || it.institutionBadge.contains("UTH", ignoreCase = true)
                "LMMU" -> it.creatorTag.equals("lmmu", ignoreCase = true) || it.institutionBadge.contains("LMMU", ignoreCase = true)
                "Apex" -> it.creatorTag.equals("apex", ignoreCase = true) || it.institutionBadge.contains("Apex", ignoreCase = true)
                "WHO" -> it.creatorTag.equals("who", ignoreCase = true) || it.institutionBadge.contains("WHO", ignoreCase = true)
                "Google Drive OSCETUBE" -> it.isDriveSource || it.driveFileId != null
                else -> true
            }
            val matchesQuery = q.isEmpty() ||
                    it.title.lowercase().contains(q) ||
                    it.institutionBadge.lowercase().contains(q) ||
                    it.categoryLabel.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)

            matchesChannel && matchesQuery
        }
    }

    // Auto-advance simulated playback timer
    LaunchedEffect(isPlaying, playbackSpeed) {
        while (isPlaying) {
            val delayMs = (1000 / playbackSpeed).toLong()
            delay(delayMs)
            activeStation?.let { st ->
                if (currentSeconds < st.durationSeconds) {
                    currentSeconds += 1
                    if (st.keySteps.isNotEmpty()) {
                        val progress = currentSeconds.toFloat() / st.durationSeconds.toFloat()
                        currentStepIndex = ((progress * st.keySteps.size).toInt()).coerceIn(0, st.keySteps.size - 1)
                    }
                } else {
                    isPlaying = false
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF090D16),
        modifier = Modifier.testTag("sheet_osce_hub")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.96f)
        ) {
            // YouTube / OSCETUBE Top App Bar
            Surface(
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "OSCETUBE",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF22C55E)
                                ) {
                                    Text(
                                        text = "LIVE HD",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Google Drive Video Player & Faculty Masterclasses",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1OSCETUBE_Nursing_Skills_Zambia"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "OSCETUBE Drive Folder", tint = Color(0xFF38BDF8), modifier = Modifier.size(22.dp))
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }

            // Top Navigation Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PlayCircleFilled, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("OSCE Video Player", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                            Text("All Stations (${filteredStations.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF38BDF8))
                            Text("📁 OSCETUBE Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            when (activeTab) {
                0 -> {
                    // MAIN YOUTUBE-STYLE VIDEO PLAYER VIEW
                    val currentVideo = activeStation ?: stations.first()
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF090D16)),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // 1. VIDEO PLAYER CANVAS (16:9 Aspect Ratio)
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp)
                                    .background(Color.Black)
                            ) {
                                if (playerMode == "Drive/Web Embed" && (!currentVideo.videoUrl.isNullOrEmpty() || !currentVideo.youtubeId.isNullOrEmpty())) {
                                    // Embedded Web / Drive Player
                                    val embedUrl = if (!currentVideo.driveFileId.isNullOrEmpty() && currentVideo.isDriveSource) {
                                        "https://drive.google.com/file/d/${currentVideo.driveFileId}/preview"
                                    } else if (!currentVideo.youtubeId.isNullOrEmpty()) {
                                        "https://www.youtube.com/embed/${currentVideo.youtubeId}?autoplay=1&rel=0"
                                    } else {
                                        currentVideo.videoUrl ?: "https://www.youtube.com"
                                    }

                                    AndroidView(
                                        factory = { ctx ->
                                            WebView(ctx).apply {
                                                layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                                )
                                                settings.javaScriptEnabled = true
                                                settings.domStorageEnabled = true
                                                settings.mediaPlaybackRequiresUserGesture = false
                                                settings.loadWithOverviewMode = true
                                                settings.useWideViewPort = true
                                                settings.cacheMode = WebSettings.LOAD_DEFAULT
                                                webChromeClient = WebChromeClient()
                                                webViewClient = WebViewClient()
                                                loadUrl(embedUrl)
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Native Interactive YouTube-Style Player Canvas
                                    AsyncImage(
                                        model = currentVideo.thumbnailUrl,
                                        contentDescription = currentVideo.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Dark gradient overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color.Black.copy(alpha = 0.65f),
                                                        Color.Black.copy(alpha = 0.35f),
                                                        Color.Black.copy(alpha = 0.85f)
                                                    )
                                                )
                                            )
                                    )

                                    // Top Player Controls Overlay (YouTube Style)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.TopCenter)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "1080p60 HD",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                color = if (playerMode == "Drive/Web Embed") Color(0xFF0284C7) else Color(0xFFEF4444),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = if (playerMode == "Drive/Web Embed") "DRIVE STREAM" else "OSCE TUBE",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // Closed Captions Toggle
                                            IconButton(
                                                onClick = { showCc = !showCc },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    if (showCc) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionDisabled,
                                                    contentDescription = "Captions",
                                                    tint = if (showCc) Color.White else Color(0xFF94A3B8),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            // Playback Speed Selector
                                            TextButton(
                                                onClick = {
                                                    playbackSpeed = when (playbackSpeed) {
                                                        1.0f -> 1.25f
                                                        1.25f -> 1.5f
                                                        1.5f -> 2.0f
                                                        2.0f -> 0.75f
                                                        else -> 1.0f
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("${playbackSpeed}x", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // Switch Player Engine (Native vs Web Embed)
                                            IconButton(
                                                onClick = {
                                                    playerMode = if (playerMode == "Native Player") "Drive/Web Embed" else "Native Player"
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    if (playerMode == "Native Player") Icons.Default.Language else Icons.Default.TouchApp,
                                                    contentDescription = "Switch Engine",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Center Playback Controls (10s Rewind, Play/Pause, 10s Fast Forward)
                                    Row(
                                        modifier = Modifier.align(Alignment.Center),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                                    ) {
                                        // -10s Skip
                                        IconButton(
                                            onClick = {
                                                currentSeconds = (currentSeconds - 10).coerceAtLeast(0)
                                            },
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(24.dp))
                                        }

                                        // Main Play/Pause Button
                                        IconButton(
                                            onClick = { isPlaying = !isPlaying },
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(Color(0xFFEF4444).copy(alpha = 0.9f), CircleShape)
                                        ) {
                                            Icon(
                                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = if (isPlaying) "Pause" else "Play",
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        // +10s Skip
                                        IconButton(
                                            onClick = {
                                                currentSeconds = (currentSeconds + 10).coerceAtMost(currentVideo.durationSeconds)
                                            },
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        ) {
                                            Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }

                                    // Subtitle / Closed Caption Strip (Dynamic Step Overlay)
                                    if (showCc && currentVideo.keySteps.isNotEmpty()) {
                                        val activeStepText = currentVideo.keySteps.getOrElse(currentStepIndex) { currentVideo.keySteps.first() }
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.85f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 36.dp, start = 16.dp, end = 16.dp)
                                        ) {
                                            Text(
                                                text = "Step ${currentStepIndex + 1}: $activeStepText",
                                                color = Color(0xFFFDE047),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Bottom Red YouTube Scrubber & Time Display
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        val progress = (currentSeconds.toFloat() / currentVideo.durationSeconds.toFloat()).coerceIn(0f, 1f)
                                        Slider(
                                            value = progress,
                                            onValueChange = { newProg ->
                                                currentSeconds = (newProg * currentVideo.durationSeconds).toInt()
                                                if (currentVideo.keySteps.isNotEmpty()) {
                                                    currentStepIndex = ((newProg * currentVideo.keySteps.size).toInt()).coerceIn(0, currentVideo.keySteps.size - 1)
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(14.dp),
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFFEF4444),
                                                activeTrackColor = Color(0xFFEF4444),
                                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                            )
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val curMin = currentSeconds / 60
                                            val curSec = currentSeconds % 60
                                            Text(
                                                text = String.format("%02d:%02d / %s", curMin, curSec, currentVideo.duration),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Step ${currentStepIndex + 1} of ${currentVideo.keySteps.size}",
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. VIDEO TITLE & METADATA SECTION
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Text(
                                    text = currentVideo.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 21.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${currentVideo.views ?: "88.4K views"} • ${currentVideo.uploadDate ?: "2 weeks ago"}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    Text("•", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        text = "#OSCETube",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "#NursingSkills",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // 3. PROMINENT ACTION BAR (YouTube Style with "Watch on YouTube" & "Open in Drive")
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "Watch on YouTube" Button (Requested prominent button!)
                                item {
                                    Button(
                                        onClick = {
                                            val url = currentVideo.videoUrl
                                                ?: if (!currentVideo.youtubeId.isNullOrEmpty()) "https://www.youtube.com/watch?v=${currentVideo.youtubeId}" else "https://www.youtube.com/results?search_query=OSCE+nursing+${Uri.encode(currentVideo.title)}"
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFEF4444),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Watch now on YouTube", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // "Open in Drive / OSCETUBE Folder" Button
                                item {
                                    FilledTonalButton(
                                        onClick = {
                                            val driveUrl = currentVideo.driveFolderUrl
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(driveUrl))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFF0369A1),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Google Drive OSCETUBE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Thumbs Up / Like Pill
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    isLiked = !isLiked
                                                    likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                                                }
                                            ) {
                                                Icon(
                                                    if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                                    contentDescription = "Like",
                                                    tint = if (isLiked) Color(0xFF38BDF8) else Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (likeCount >= 1000) String.format("%.1fK", likeCount / 1000.0) else "$likeCount",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(modifier = Modifier.width(1.dp).height(14.dp).background(Color(0xFF475569)))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Outlined.ThumbDown,
                                                contentDescription = "Dislike",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp).clickable {
                                                    Toast.makeText(context, "Feedback noted", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }

                                // Share Button
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.clickable {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(Intent.EXTRA_SUBJECT, currentVideo.title)
                                                putExtra(Intent.EXTRA_TEXT, "Study OSCE Clinical Station on DATANURSE: ${currentVideo.title}\nWatch on YouTube: https://www.youtube.com/watch?v=${currentVideo.youtubeId ?: ""}\nGoogle Drive Folder: ${currentVideo.driveFolderUrl}")
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share OSCE Video"))
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Share", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                // Bookmark / Save Button
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.clickable {
                                            isSaved = !isSaved
                                            Toast.makeText(context, if (isSaved) "Saved to OSCE playlist" else "Removed from playlist", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                if (isSaved) Icons.Default.BookmarkAdded else Icons.Outlined.BookmarkBorder,
                                                contentDescription = null,
                                                tint = if (isSaved) Color(0xFF34D399) else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isSaved) "Saved" else "Save", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // 4. FACULTY CHANNEL BAR (Subscribe & Institution Badge)
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF111827),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when (currentVideo.creatorTag.lowercase()) {
                                                "unza" -> Color(0xFF7C3AED)
                                                "uth" -> Color(0xFF0D9488)
                                                "lmmu" -> Color(0xFF2563EB)
                                                "apex" -> Color(0xFFEA580C)
                                                else -> Color(0xFF059669)
                                            },
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.School, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = currentVideo.channelName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Verified Faculty", tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
                                            }
                                            Text(
                                                text = "${currentVideo.channelSubscribers ?: "52.4K students"} • ${currentVideo.institutionBadge}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    // Subscribe Button (Interactive)
                                    Button(
                                        onClick = {
                                            isSubscribed = !isSubscribed
                                            Toast.makeText(context, if (isSubscribed) "Subscribed to ${currentVideo.channelName}" else "Unsubscribed", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSubscribed) Color(0xFF334155) else Color.White,
                                            contentColor = if (isSubscribed) Color.White else Color.Black
                                        ),
                                        shape = RoundedCornerShape(18.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isSubscribed) "Subscribed" else "Subscribe",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 5. EXPANDABLE DESCRIPTION & OVERVIEW CARD
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF1E293B).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Description & Clinical Overview",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentVideo.description,
                                        fontSize = 11.sp,
                                        color = Color(0xFFCBD5E1),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Exam Marking Tip: ${currentVideo.examTips}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE047),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // 6. REAL ADMOB BANNER SPONSORED UNIT
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                AdMobBanner()
                            }
                        }

                        // 7. SUB-TABS (Checklist, Comments, Equipment, Pearls, Up Next)
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            ScrollableTabRow(
                                selectedTabIndex = videoSubTab,
                                containerColor = Color(0xFF090D16),
                                edgePadding = 16.dp,
                                divider = {}
                            ) {
                                Tab(
                                    selected = videoSubTab == 0,
                                    onClick = { videoSubTab = 0 },
                                    text = { Text("Procedural Steps (${currentVideo.keySteps.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = videoSubTab == 1,
                                    onClick = { videoSubTab = 1 },
                                    text = { Text("Comments (${commentsList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = videoSubTab == 2,
                                    onClick = { videoSubTab = 2 },
                                    text = { Text("Equipment Tray", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                Tab(
                                    selected = videoSubTab == 3,
                                    onClick = { videoSubTab = 3 },
                                    text = { Text("Up Next / Related", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }

                        // 8. SUB-TAB CONTENT
                        when (videoSubTab) {
                            0 -> {
                                // Procedural Steps with Timecodes (Interactive Sync)
                                itemsIndexed(currentVideo.keySteps) { idx, step ->
                                    val isCurrent = idx == currentStepIndex
                                    val timestampStr = currentVideo.stepTimestamps.getOrElse(idx) {
                                        val sec = idx * (currentVideo.durationSeconds / (currentVideo.keySteps.size.coerceAtLeast(1)))
                                        String.format("%02d:%02d", sec / 60, sec % 60)
                                    }

                                    Surface(
                                        color = if (isCurrent) Color(0xFF1E1B4B) else Color(0xFF111827),
                                        shape = RoundedCornerShape(8.dp),
                                        border = if (isCurrent) BorderStroke(1.dp, Color(0xFF818CF8)) else null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clickable {
                                                currentStepIndex = idx
                                                currentSeconds = idx * (currentVideo.durationSeconds / (currentVideo.keySteps.size.coerceAtLeast(1)))
                                                isPlaying = true
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Surface(
                                                color = if (isCurrent) Color(0xFF4F46E5) else Color(0xFF374151),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = timestampStr,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Step ${idx + 1}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCurrent) Color(0xFFA5B4FC) else Color(0xFF94A3B8)
                                                )
                                                Text(
                                                    text = step,
                                                    fontSize = 12.sp,
                                                    color = Color.White,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                            if (isCurrent) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = "Active Step", tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // Comments & Faculty Discussion Section
                                item {
                                    // Add Comment Input Box
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = newCommentText,
                                            onValueChange = { newCommentText = it },
                                            placeholder = { Text("Add clinical question or revision pearl...", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedContainerColor = Color(0xFF1E293B),
                                                unfocusedContainerColor = Color(0xFF1E293B)
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = {
                                                if (newCommentText.isNotBlank()) {
                                                    val newComment = OsceComment(
                                                        id = "c-${System.currentTimeMillis()}",
                                                        author = "Nurse Student (You)",
                                                        authorAvatarBg = 0xFF10B981,
                                                        institution = "DATANURSE Candidate",
                                                        timeAgo = "Just now",
                                                        text = newCommentText.trim(),
                                                        likes = 0
                                                    )
                                                    commentsList = listOf(newComment) + commentsList
                                                    newCommentText = ""
                                                    Toast.makeText(context, "Clinical note posted!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier
                                                .background(Color(0xFF38BDF8), CircleShape)
                                                .size(40.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                items(commentsList) { c ->
                                    Surface(
                                        color = Color(0xFF111827),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(c.authorAvatarBg),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(c.author.first().toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(c.author, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text(c.timeAgo, fontSize = 9.sp, color = Color(0xFF94A3B8))
                                                    if (c.isPinned) {
                                                        Surface(color = Color(0xFF7C3AED), shape = RoundedCornerShape(2.dp)) {
                                                            Text("PINNED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                        }
                                                    }
                                                }
                                                Text(c.institution, fontSize = 9.sp, color = Color(0xFF38BDF8))
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(c.text, fontSize = 11.sp, color = Color(0xFFE2E8F0), lineHeight = 16.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Icon(Icons.Outlined.ThumbUp, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                                                    Text("${c.likes}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // Equipment Tray
                                items(currentVideo.equipmentNeeded) { eq ->
                                    Surface(
                                        color = Color(0xFF111827),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(eq, fontSize = 12.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                            3 -> {
                                // Up Next / Related Videos (YouTube Style)
                                items(stations.filter { it.id != currentVideo.id }) { nextSt ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clickable {
                                                activeStation = nextSt
                                                currentSeconds = 0
                                                currentStepIndex = 0
                                                isPlaying = true
                                            },
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 110.dp, height = 66.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black)
                                            ) {
                                                AsyncImage(
                                                    model = nextSt.thumbnailUrl,
                                                    contentDescription = nextSt.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.8f),
                                                    shape = RoundedCornerShape(2.dp),
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(3.dp)
                                                ) {
                                                    Text(
                                                        text = nextSt.duration,
                                                        fontSize = 9.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = nextSt.title,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = nextSt.channelName,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF94A3B8),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${nextSt.institutionBadge} • ${nextSt.views ?: "60K views"}",
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ALL OSCE STATIONS DIRECTORY
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF090D16))
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search OSCE stations: CVC, CPR, Chest Tube, MgSO4, LP...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF1E293B),
                                unfocusedContainerColor = Color(0xFF1E293B)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(channelFilters) { ch ->
                                FilterChip(
                                    selected = selectedChannelFilter == ch,
                                    onClick = { selectedChannelFilter = ch },
                                    label = { Text(ch, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredStations) { station ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeStation = station
                                            currentSeconds = 0
                                            currentStepIndex = 0
                                            isPlaying = true
                                            activeTab = 0
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 100.dp, height = 62.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black)
                                        ) {
                                            AsyncImage(
                                                model = station.thumbnailUrl,
                                                contentDescription = station.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Surface(
                                                color = Color(0xFFEF4444),
                                                shape = RoundedCornerShape(2.dp),
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(2.dp)
                                            ) {
                                                Text(
                                                    text = "HD",
                                                    fontSize = 8.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                                                )
                                            }
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(2.dp),
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .padding(2.dp)
                                            ) {
                                                Text(
                                                    text = station.duration,
                                                    fontSize = 8.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                                )
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Surface(
                                                color = Color(0xFF1E1B4B),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = station.institutionBadge,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFA5B4FC),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = station.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${station.channelName} • ${station.keySteps.size} Key Steps",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // GOOGLE DRIVE OSCETUBE FOLDER INTEGRATION
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF090D16))
                            .padding(16.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C4A6E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                                    Text("OSCETUBE Google Drive Cloud Vault", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All OSCE demonstration videos are uploaded to the shared OSCETUBE Google Drive folder. You can stream them inside DATANURSE with HD step syncing or open the cloud repository directly.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE0F2FE),
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/folders/1OSCETUBE_Nursing_Skills_Zambia"))
                                            context.startActivity(intent)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open OSCETUBE Drive", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { showDriveInputDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Stream Custom Link", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Uploaded OSCETUBE Videos on Cloud Drive", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(stations) { st ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeStation = st.copy(isDriveSource = true)
                                            playerMode = "Drive/Web Embed"
                                            activeTab = 0
                                            isPlaying = true
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF0369A1),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.VideoFile, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Column {
                                                Text(st.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text("Drive ID: ${st.driveFileId ?: "OSCETUBE-${st.id}"} • ${st.duration}", fontSize = 10.sp, color = Color(0xFF38BDF8))
                                            }
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                activeStation = st.copy(isDriveSource = true)
                                                playerMode = "Drive/Web Embed"
                                                activeTab = 0
                                                isPlaying = true
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Play in Drive", fontSize = 10.sp)
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

    // Custom Google Drive Link Input Dialog
    if (showDriveInputDialog) {
        AlertDialog(
            onDismissRequest = { showDriveInputDialog = false },
            title = { Text("Stream Google Drive Video", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Paste any Google Drive shareable video URL or File ID to stream it directly inside DATANURSE with interactive checklist controls.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDriveUrlInput,
                        onValueChange = { customDriveUrlInput = it },
                        placeholder = { Text("https://drive.google.com/file/d/...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customDriveUrlInput.isNotBlank()) {
                            val customStation = OsceVideo(
                                id = "custom-drive-${System.currentTimeMillis()}",
                                title = "Custom OSCETUBE Google Drive Stream",
                                category = "custom",
                                categoryLabel = "Google Drive Stream",
                                channelName = "OSCETUBE Cloud Vault",
                                creatorTag = "drive",
                                channelSubscribers = "Direct Stream",
                                institutionBadge = "Google Drive",
                                videoUrl = customDriveUrlInput,
                                driveFileId = customDriveUrlInput,
                                duration = "20:00",
                                durationSeconds = 1200,
                                thumbnailUrl = "https://images.unsplash.com/photo-1579684385127-1ef15d508118?w=800&auto=format&fit=crop&q=60",
                                description = "Streamed from Google Drive video file link: $customDriveUrlInput",
                                keySteps = listOf(
                                    "Confirm patient identity and explain clinical procedure",
                                    "Perform surgical hand hygiene and prepare sterile field",
                                    "Execute clinical technique adhering to national guidelines",
                                    "Monitor patient response and document findings"
                                ),
                                isDriveSource = true
                            )
                            activeStation = customStation
                            playerMode = "Drive/Web Embed"
                            activeTab = 0
                            isPlaying = true
                            showDriveInputDialog = false
                            Toast.makeText(context, "Loaded Google Drive video!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Stream Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDriveInputDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
