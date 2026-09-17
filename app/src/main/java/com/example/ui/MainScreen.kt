package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.*
import com.example.ui.dialogs.*
import com.example.ui.theme.TealPrimary
import com.example.viewmodel.NurseViewModel

@Composable
fun MainScreen(
    viewModel: NurseViewModel,
    modifier: Modifier = Modifier
) {
    var isSplashVisible by remember { mutableStateOf(true) }
    var isHeroBannerVisible by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }

    val allResources by viewModel.allResourcesList.collectAsStateWithLifecycle()
    val resources by viewModel.filteredResources.collectAsStateWithLifecycle()
    val bookmarkedCount by viewModel.bookmarkedCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedDomain by viewModel.selectedDomain.collectAsStateWithLifecycle()
    val showBookmarksOnly by viewModel.showBookmarksOnly.collectAsStateWithLifecycle()

    val optimumConditions by viewModel.optimumConditions.collectAsStateWithLifecycle()
    val isSyncingOptimum by viewModel.isSyncingOptimum.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()

    val nursingTopics by viewModel.nursingTopics.collectAsStateWithLifecycle()
    val osceVideos by viewModel.osceVideos.collectAsStateWithLifecycle()
    val quizHistory by viewModel.quizHistory.collectAsStateWithLifecycle()

    val isDeveloperUnlocked by viewModel.isDeveloperUnlocked.collectAsStateWithLifecycle()
    val isDriveSyncing by viewModel.isDriveSyncing.collectAsStateWithLifecycle()
    val isPullingUpdates by viewModel.isPullingUpdates.collectAsStateWithLifecycle()
    val lastDriveSyncTime by viewModel.lastDriveSyncTime.collectAsStateWithLifecycle()
    val webAppEndpoint by viewModel.webAppEndpoint.collectAsStateWithLifecycle()
    val driveSyncLogs by viewModel.driveSyncLogs.collectAsStateWithLifecycle()

    // Wallpaper Background Settings
    val isWallpaperEnabled by viewModel.isWallpaperEnabled.collectAsStateWithLifecycle()
    val wallpaperDarkness by viewModel.wallpaperDarkness.collectAsStateWithLifecycle()
    val wallpaperBlurRadius by viewModel.wallpaperBlurRadius.collectAsStateWithLifecycle()
    val wallpaperPreset by viewModel.wallpaperPreset.collectAsStateWithLifecycle()

    // Active sheets
    val activeDetailItem by viewModel.activeDetailItem.collectAsStateWithLifecycle()
    val activeFlashcards by viewModel.activeFlashcardResource.collectAsStateWithLifecycle()
    val activeQuiz by viewModel.activeQuizResource.collectAsStateWithLifecycle()
    val isClinicalToolsOpen by viewModel.isClinicalToolsOpen.collectAsStateWithLifecycle()
    val isTopicsOpen by viewModel.isTopicsOpen.collectAsStateWithLifecycle()
    val isOsceOpen by viewModel.isOsceOpen.collectAsStateWithLifecycle()
    val isAddResourceOpen by viewModel.isAddResourceOpen.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isDownloadsManagerOpen by viewModel.isDownloadsManagerOpen.collectAsStateWithLifecycle()
    val downloadedFiles by viewModel.downloadedFiles.collectAsStateWithLifecycle()
    val linkedDriveAccount by viewModel.linkedDriveAccount.collectAsStateWithLifecycle()
    val driveCategoryFolders = viewModel.driveCategoryFolders

    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    Crossfade(targetState = isSplashVisible, label = "SplashScreenTransition") { showSplash ->
        if (showSplash) {
            LoadingSplashScreen(
                onFinished = { isSplashVisible = false }
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopNavBar(
                        isHeroBannerVisible = isHeroBannerVisible,
                        onToggleHeroBanner = { isHeroBannerVisible = !isHeroBannerVisible },
                        isFullscreen = isFullscreen,
                        onToggleFullscreen = { isFullscreen = !isFullscreen }
                    )
                },
                bottomBar = {
                    BottomNavBar(
                        bookmarkedCount = bookmarkedCount,
                        isShowingSavedOnly = showBookmarksOnly,
                        onOpenClinicalTools = { viewModel.isClinicalToolsOpen.value = true },
                        onToggleSavedOnly = { viewModel.showBookmarksOnly.value = !viewModel.showBookmarksOnly.value },
                        onOpenTopics = { viewModel.isTopicsOpen.value = true },
                        onOpenFlashcards = {
                            val firstResource = resources.firstOrNull() ?: allResources.firstOrNull()
                            if (firstResource != null) {
                                viewModel.activeFlashcardResource.value = firstResource
                            }
                        },
                        onOpenOsce = { viewModel.isOsceOpen.value = true },
                        onOpenSettings = { viewModel.isSettingsOpen.value = true }
                    )
                },
                containerColor = Color(0xFF060D17)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Campus Wallpaper Background - Visible but Dark & High-Contrast
                    if (isWallpaperEnabled) {
                        Image(
                            painter = painterResource(id = R.drawable.img_campus_bg),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (wallpaperBlurRadius > 0) Modifier.blur(radius = wallpaperBlurRadius.dp) else Modifier),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF060E1A).copy(alpha = wallpaperDarkness),
                                            Color(0xFF030810).copy(alpha = (wallpaperDarkness + 0.08f).coerceAtMost(0.96f))
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF060D17))
                        )
                    }

                    // Main Scrollable Content: Stationary Message Window (HeroBanner) and FilterBar now scroll smoothly together with resources!
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("feed_resources"),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Hero Header Banner (Scrolls with library list)
                        item(key = "header_hero_banner") {
                            HeroBanner(
                                isVisible = isHeroBannerVisible,
                                resources = allResources,
                                allResourcesCount = allResources.size,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.searchQuery.value = it },
                                selectedCategory = selectedCategory,
                                onSelectCategory = { viewModel.selectedCategory.value = it }
                            )
                        }

                        // 2. Category & Domain Filter Chips (Scrolls with library list)
                        item(key = "header_category_filter_bar") {
                            CategoryFilterBar(
                                selectedCategory = selectedCategory,
                                onCategorySelected = { viewModel.selectedCategory.value = it },
                                selectedYear = selectedYear,
                                onYearSelected = { viewModel.selectedYear.value = it },
                                selectedDomain = selectedDomain,
                                onDomainSelected = { viewModel.selectedDomain.value = it }
                            )
                        }

                        // 3. Saved Filter Pill indicator
                        if (showBookmarksOnly) {
                            item(key = "header_saved_pill") {
                                Surface(
                                    color = Color(0xFF0C4A6E),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Showing $bookmarkedCount Bookmarked Resources",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        TextButton(
                                            onClick = { viewModel.showBookmarksOnly.value = false },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Show All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Main Resources Feed List or Empty State
                        if (resources.isEmpty()) {
                            item(key = "empty_state") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp, horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No matching nursing resources found",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Try adjusting your search query, domain filters, or year level.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Button(
                                            onClick = {
                                                viewModel.searchQuery.value = ""
                                                viewModel.selectedCategory.value = "all"
                                                viewModel.selectedYear.value = "All Years"
                                                viewModel.selectedDomain.value = "All Domains"
                                                viewModel.showBookmarksOnly.value = false
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                        ) {
                                            Text("Reset All Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        } else {
                            items(
                                items = resources,
                                key = { it.id }
                            ) { item ->
                                ResourceCard(
                                    resource = item,
                                    onOpenDetail = { viewModel.activeDetailItem.value = item },
                                    onOpenFlashcards = { viewModel.activeFlashcardResource.value = item },
                                    onOpenQuiz = { viewModel.activeQuizResource.value = item },
                                    onToggleBookmark = { viewModel.toggleBookmark(item.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Modal Bottom Sheets
            activeDetailItem?.let { item ->
                ResourceDetailSheet(
                    resource = item,
                    onDismiss = { viewModel.activeDetailItem.value = null },
                    onOpenFlashcards = { viewModel.activeFlashcardResource.value = it },
                    onOpenQuiz = { viewModel.activeQuizResource.value = it },
                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                    onDownloadRawDocument = { viewModel.downloadRawDocument(it) },
                    onSyncToDrive = { viewModel.syncResourceToDrive(it) },
                    onShareWebAppLink = { viewModel.copyWebAppLink(it) }
                )
            }

            activeFlashcards?.let { item ->
                FlashcardStudySheet(
                    resource = item,
                    onDismiss = { viewModel.activeFlashcardResource.value = null }
                )
            }

            activeQuiz?.let { item ->
                QuizPracticeSheet(
                    resource = item,
                    onSaveResult = { resId, resTitle, score, total, mode ->
                        viewModel.recordQuizResult(resId, resTitle, score, total, mode)
                    },
                    onDismiss = { viewModel.activeQuizResource.value = null }
                )
            }

            if (isClinicalToolsOpen) {
                ClinicalToolsSheet(
                    optimumConditions = optimumConditions,
                    isSyncingOptimum = isSyncingOptimum,
                    lastSyncTime = lastSyncTime,
                    onSyncOptimum = { viewModel.syncOptimumConditions() },
                    onDismiss = { viewModel.isClinicalToolsOpen.value = false }
                )
            }

            if (isTopicsOpen) {
                TopicsHubSheet(
                    topics = nursingTopics,
                    onDismiss = { viewModel.isTopicsOpen.value = false }
                )
            }

            if (isOsceOpen) {
                OsceHubSheet(
                    stations = osceVideos,
                    onDismiss = { viewModel.isOsceOpen.value = false }
                )
            }

            if (isAddResourceOpen) {
                AddResourceSheet(
                    onAddResource = { title, cat, dom, yr, desc, cont, tags ->
                        viewModel.addCustomResource(title, cat, dom, yr, desc, cont, tags)
                    },
                    onDismiss = { viewModel.isAddResourceOpen.value = false }
                )
            }

            if (isDownloadsManagerOpen) {
                DownloadsManagerSheet(
                    downloadedFiles = downloadedFiles,
                    onOpenFile = { viewModel.openDownloadedFile(it) },
                    onShareFile = { viewModel.shareDownloadedFile(it) },
                    onDeleteFile = { viewModel.deleteDownloadedFile(it) },
                    onRefreshDownloads = { viewModel.refreshDownloads() },
                    onDismiss = { viewModel.isDownloadsManagerOpen.value = false }
                )
            }

            if (isSettingsOpen) {
                SettingsSheet(
                    quizHistory = quizHistory,
                    isDeveloperUnlocked = isDeveloperUnlocked,
                    isDriveSyncing = isDriveSyncing,
                    isPullingUpdates = isPullingUpdates,
                    linkedDriveAccount = linkedDriveAccount,
                    driveCategoryFolders = driveCategoryFolders,
                    webAppEndpoint = webAppEndpoint,
                    lastDriveSyncTime = lastDriveSyncTime,
                    driveSyncLogs = driveSyncLogs,
                    wallpaperPreset = wallpaperPreset,
                    isWallpaperEnabled = isWallpaperEnabled,
                    downloadedCount = downloadedFiles.size,
                    onSetWallpaperPreset = { viewModel.setWallpaperPreset(it) },
                    onToggleWallpaper = { viewModel.toggleWallpaper(it) },
                    onSecretTap = { viewModel.onDeveloperSecretTap() },
                    onSyncAllDrive = { viewModel.syncAllResourcesToDrive() },
                    onPullRemoteUpdates = { viewModel.pullRemoteSyncUpdates() },
                    onSyncDriveFolder = { viewModel.syncDriveCategoryFolder(it) },
                    onOpenDriveFolder = { viewModel.openGoogleDriveFolder(it) },
                    onOpenDownloadsManager = {
                        viewModel.isSettingsOpen.value = false
                        viewModel.isDownloadsManagerOpen.value = true
                    },
                    onImportManifestJson = { viewModel.importManifestJson(it) },
                    onCopyWebAppLink = { viewModel.copyWebAppLink() },
                    onExportJson = { viewModel.exportDatabaseJson() },
                    onDismiss = { viewModel.isSettingsOpen.value = false }
                )
            }
        }
    }
}
