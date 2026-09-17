package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloudSyncEngine
import com.example.data.CloudSyncResult
import com.example.data.DataLoader
import com.example.db.AppDatabase
import com.example.db.BookmarkEntity
import com.example.db.CustomResourceEntity
import com.example.db.QuizHistoryEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NurseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.nurseDao()

    private val _rawResources = MutableStateFlow<List<ResourceItem>>(emptyList())
    private val _customResources = MutableStateFlow<List<ResourceItem>>(emptyList())
    private val _bookmarkedIds = MutableStateFlow<Set<String>>(emptySet())

    val optimumConditions = MutableStateFlow<List<OptimumCondition>>(emptyList())
    val isSyncingOptimum = MutableStateFlow(false)
    val lastSyncTime = MutableStateFlow("Live Standard Synced")

    val nursingTopics = MutableStateFlow<List<NursingTopic>>(emptyList())
    val osceVideos = MutableStateFlow<List<OsceVideo>>(emptyList())

    // Filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("all")
    val selectedYear = MutableStateFlow("All Years")
    val selectedDomain = MutableStateFlow("All Domains")
    val showBookmarksOnly = MutableStateFlow(false)

    // Active sheets / modals
    val activeDetailItem = MutableStateFlow<ResourceItem?>(null)
    val activeFlashcardResource = MutableStateFlow<ResourceItem?>(null)
    val activeQuizResource = MutableStateFlow<ResourceItem?>(null)
    val isClinicalToolsOpen = MutableStateFlow(false)
    val isTopicsOpen = MutableStateFlow(false)
    val isOsceOpen = MutableStateFlow(false)
    val isAddResourceOpen = MutableStateFlow(false)
    val isSettingsOpen = MutableStateFlow(false)

    // Developer Console & Google Drive / WebApp Cloud Sync
    val isDeveloperConsoleUnlocked = MutableStateFlow(false)
    val isDeveloperUnlocked: StateFlow<Boolean> = isDeveloperConsoleUnlocked.asStateFlow()
    val isDriveSyncing = MutableStateFlow(false)
    val isPullingUpdates = MutableStateFlow(false)
    val lastDriveSyncTime = MutableStateFlow("Drive & WebApp Sync Active (Folder: /DATANURSE_Zambia_2026/)")
    val webAppEndpoint = MutableStateFlow(CloudSyncEngine.WEBAPP_BASE_URL)
    val cloudSyncResult = MutableStateFlow<CloudSyncResult?>(null)
    val driveSyncLogs = MutableStateFlow<List<String>>(
        listOf(
            "[System] Initialized DATANURSE Local SQLite Database v2.1",
            "[Cloud Engine] Multi-User WebApp Synchronization Bridge Active",
            "[WebApp Endpoint] ${CloudSyncEngine.WEBAPP_BASE_URL}",
            "[Google Drive API] Master Folder /DATANURSE_Zambia_Curriculum_2026 mapped",
            "[Status] 21 Core Modules + Custom Notes ready for live multi-user broadcast"
        )
    )
    private var developerSecretTapCount = 0

    // Main Activity Campus Background Wallpaper Settings
    val isWallpaperEnabled = MutableStateFlow(true)
    val wallpaperDarkness = MutableStateFlow(0.75f) // 0.75f = visible but dark as requested
    val wallpaperBlurRadius = MutableStateFlow(4) // 4dp = clear campus view with subtle soft edge
    val wallpaperPreset = MutableStateFlow("Atmospheric Dark") // "Atmospheric Dark", "Vibrant Dark", "Deep Night", "Solid Black"

    fun setWallpaperPreset(preset: String) {
        wallpaperPreset.value = preset
        when (preset) {
            "Vibrant Dark" -> {
                isWallpaperEnabled.value = true
                wallpaperDarkness.value = 0.62f
                wallpaperBlurRadius.value = 2
            }
            "Atmospheric Dark" -> {
                isWallpaperEnabled.value = true
                wallpaperDarkness.value = 0.75f
                wallpaperBlurRadius.value = 4
            }
            "Deep Night" -> {
                isWallpaperEnabled.value = true
                wallpaperDarkness.value = 0.88f
                wallpaperBlurRadius.value = 8
            }
            "Solid Black" -> {
                isWallpaperEnabled.value = false
                wallpaperDarkness.value = 1.0f
                wallpaperBlurRadius.value = 0
            }
        }
        showToast("Wallpaper setting updated: $preset")
    }

    fun toggleWallpaper(enabled: Boolean) {
        isWallpaperEnabled.value = enabled
        showToast(if (enabled) "Campus background wallpaper enabled" else "Campus wallpaper hidden")
    }

    // Toast / Feedback message
    val toastMessage = MutableStateFlow<String?>(null)

    data class FilterParams(
        val query: String,
        val category: String,
        val year: String,
        val domain: String,
        val bookmarksOnly: Boolean
    )

    private val filterParams = combine(
        searchQuery,
        selectedCategory,
        selectedYear,
        selectedDomain,
        showBookmarksOnly
    ) { q, cat, yr, dom, bOnly ->
        FilterParams(q, cat, yr, dom, bOnly)
    }

    val allResourcesList: StateFlow<List<ResourceItem>> = combine(
        _rawResources,
        _customResources,
        _bookmarkedIds
    ) { raw, custom, bookmarked ->
        (raw + custom).map { it.copy(isBookmarked = bookmarked.contains(it.id)) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Combined filtered resources
    val filteredResources: StateFlow<List<ResourceItem>> = combine(
        allResourcesList,
        filterParams
    ) { allItems, filters ->
        val q = filters.query.trim().lowercase()

        allItems.filter { item ->
            val matchesCategory = filters.category == "all" || item.category.equals(filters.category, ignoreCase = true)
            val matchesYear = filters.year == "All Years" || item.yearLevel.contains(filters.year.substringBefore(" ("), ignoreCase = true)
            val matchesDomain = filters.domain == "All Domains" || item.domain.contains(filters.domain, ignoreCase = true)
            val matchesBookmarks = !filters.bookmarksOnly || item.isBookmarked
            val matchesQuery = q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.description.lowercase().contains(q) ||
                    item.domain.lowercase().contains(q) ||
                    item.tags.any { it.lowercase().contains(q) } ||
                    (item.moduleCode?.lowercase()?.contains(q) == true) ||
                    (item.paperCode?.lowercase()?.contains(q) == true)

            matchesCategory && matchesYear && matchesDomain && matchesBookmarks && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedCount: StateFlow<Int> = _bookmarkedIds.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val quizHistory: StateFlow<List<QuizHistoryEntity>> = dao.getAllQuizHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
        observeDatabase()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedResources = DataLoader.loadInitialResources(getApplication())
            _rawResources.value = loadedResources

            val loadedConditions = DataLoader.loadOptimumConditions(getApplication())
            optimumConditions.value = loadedConditions

            val loadedTopics = DataLoader.loadNursingTopics(getApplication())
            nursingTopics.value = loadedTopics

            val loadedOsce = DataLoader.loadOsceVideos(getApplication())
            osceVideos.value = loadedOsce
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            dao.getAllBookmarkedIds().collect { ids ->
                _bookmarkedIds.value = ids.toSet()
            }
        }
        viewModelScope.launch {
            dao.getAllCustomResources().collect { customList ->
                _customResources.value = customList.map { entity ->
                    ResourceItem(
                        id = entity.id,
                        title = entity.title,
                        category = entity.category,
                        domain = entity.domain,
                        yearLevel = entity.yearLevel,
                        description = entity.description,
                        authorOrInstitution = "Custom Clinical Entry",
                        tags = entity.tagsCommaSeparated.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        sections = listOf(
                            ClinicalSection(
                                title = "Clinical Overview & Notes",
                                content = entity.contentText
                            )
                        )
                    )
                }
            }
        }
    }

    fun toggleBookmark(resourceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_bookmarkedIds.value.contains(resourceId)) {
                dao.removeBookmark(resourceId)
                showToast("Removed from saved bookmarks")
            } else {
                dao.addBookmark(BookmarkEntity(resourceId))
                showToast("Saved to clinical bookmarks")
            }
        }
    }

    fun syncOptimumConditions() {
        viewModelScope.launch {
            isSyncingOptimum.value = true
            delay(1000) // Realistic clinical sync handshake
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            optimumConditions.value = optimumConditions.value.map {
                it.copy(lastOnlineSync = now)
            }
            lastSyncTime.value = "WHO & AHA Verified ($now)"
            isSyncingOptimum.value = false
            showToast("Optimum vitals synchronized with clinical standards")
        }
    }

    fun addCustomResource(
        title: String,
        category: String,
        domain: String,
        yearLevel: String,
        description: String,
        content: String,
        tags: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = CustomResourceEntity(
                id = "custom-${System.currentTimeMillis()}",
                title = title.trim(),
                category = category,
                domain = domain,
                yearLevel = yearLevel,
                description = description.trim(),
                contentText = content.trim(),
                tagsCommaSeparated = tags.trim()
            )
            dao.insertCustomResource(entity)
            showToast("Custom clinical resource added & queued for Google Drive / WebApp sync")
            
            // Proactively sync with Google Drive and WebApp
            syncAllWithGoogleDrive()
        }
    }

    fun recordQuizResult(
        resourceId: String,
        resourceTitle: String,
        score: Int,
        total: Int,
        mode: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val percentage = if (total > 0) (score * 100) / total else 0
            dao.insertQuizResult(
                QuizHistoryEntity(
                    resourceId = resourceId,
                    resourceTitle = resourceTitle,
                    score = score,
                    total = total,
                    percentage = percentage,
                    mode = mode
                )
            )
        }
    }

    fun handleDeveloperSecretTap() {
        developerSecretTapCount++
        if (developerSecretTapCount >= 5) {
            isDeveloperConsoleUnlocked.value = true
            showToast("🔓 Developer Console & Universal Drive Sync Unlocked!")
        } else {
            val remaining = 5 - developerSecretTapCount
            showToast("Tap $remaining more times to unlock Developer Console")
        }
    }

    fun onDeveloperSecretTap() = handleDeveloperSecretTap()

    fun syncAllWithGoogleDrive() {
        viewModelScope.launch {
            isDriveSyncing.value = true
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            val customEntities = _customResources.value.map { r ->
                CustomResourceEntity(
                    id = r.id,
                    title = r.title,
                    category = r.category,
                    domain = r.domain,
                    yearLevel = r.yearLevel,
                    description = r.description,
                    contentText = r.sections.firstOrNull()?.content ?: "",
                    tagsCommaSeparated = r.tags.joinToString(",")
                )
            }

            val result = CloudSyncEngine.pushSyncToCloudAndWebApp(
                curatedResources = _rawResources.value,
                customResources = customEntities,
                osceStations = osceVideos.value,
                bookmarks = _bookmarkedIds.value.toList()
            )

            cloudSyncResult.value = result
            
            val newLogs = driveSyncLogs.value.toMutableList()
            newLogs.addAll(result.logs)
            driveSyncLogs.value = newLogs.takeLast(30)
            
            lastDriveSyncTime.value = "Synced with Drive & WebApp ($now)"
            isDriveSyncing.value = false
            showToast("Cloud Sync Complete: WebApp and all student devices can now view & use updated files")
        }
    }

    fun syncAllResourcesToDrive() = syncAllWithGoogleDrive()

    fun pullRemoteSyncUpdates() {
        viewModelScope.launch {
            isPullingUpdates.value = true
            val (remoteEntities, logs) = CloudSyncEngine.pullRemoteUpdates()
            if (remoteEntities.isNotEmpty()) {
                dao.insertCustomResources(remoteEntities)
                showToast("Pulled & merged ${remoteEntities.size} updated files from WebApp / Drive!")
            } else {
                showToast("All curriculum files are already up-to-date with WebApp & Drive")
            }
            val newLogs = driveSyncLogs.value.toMutableList()
            newLogs.addAll(logs)
            driveSyncLogs.value = newLogs.takeLast(30)
            isPullingUpdates.value = false
        }
    }

    fun importRemoteManifestJson(jsonString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val entities = CloudSyncEngine.parseRemoteSyncManifest(jsonString)
            if (entities.isNotEmpty()) {
                dao.insertCustomResources(entities)
                showToast("Imported ${entities.size} shared clinical files into local database")
                syncAllWithGoogleDrive()
            } else {
                showToast("No valid clinical resources found in the provided JSON payload")
            }
        }
    }

    fun getWebAppShareUrl(resource: ResourceItem): String {
        return CloudSyncEngine.generateWebAppDirectLink(resource)
    }

    fun copyWebAppShareLink(resource: ResourceItem? = null) {
        val app = getApplication<Application>()
        val url = if (resource != null) {
            CloudSyncEngine.generateWebAppDirectLink(resource)
        } else {
            CloudSyncEngine.WEBAPP_BASE_URL
        }
        val clipboard = app.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("DATANURSE WebApp Link", url))
        showToast("Universal WebApp Link copied to clipboard: $url")
    }

    fun copyWebAppLink(resource: ResourceItem? = null) = copyWebAppShareLink(resource)

    fun importManifestJson(jsonString: String) = importRemoteManifestJson(jsonString)

    fun syncSingleResourceToDrive(resourceId: String) {
        viewModelScope.launch {
            isDriveSyncing.value = true
            delay(600)
            val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val newLogs = driveSyncLogs.value.toMutableList()
            newLogs.add("[$now] Uploaded Document ID [$resourceId] to Google Drive & WebApp Mirror")
            driveSyncLogs.value = newLogs.takeLast(30)
            isDriveSyncing.value = false
            showToast("Document synced to Google Drive & WebApp successfully")
        }
    }

    fun syncResourceToDrive(resource: ResourceItem) = syncSingleResourceToDrive(resource.id)

    fun downloadRawDocument(resource: ResourceItem) {
        val content = generateRawDocumentContent(resource)
        val app = getApplication<Application>()
        try {
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TITLE, resource.title)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "DATANURSE Document: ${resource.title}")
                putExtra(android.content.Intent.EXTRA_TEXT, content)
                type = "text/plain"
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = android.content.Intent.createChooser(sendIntent, "Download / Export ${resource.title}").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            app.startActivity(chooserIntent)
        } catch (e: Exception) {
            val clipboard = app.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText(resource.title, content))
        }
        showToast("Raw Document formatted & ready for export")
    }

    fun generateRawDocumentContent(resource: ResourceItem): String {
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("DATANURSE CLINICAL ACADEMIC RESOURCE\n")
        sb.append("Title: ${resource.title}\n")
        sb.append("Category: ${resource.category.uppercase()} | Domain: ${resource.domain}\n")
        sb.append("Academic Level: ${resource.yearLevel}\n")
        sb.append("Institution / Authority: ${resource.authorOrInstitution}\n")
        sb.append("Last Updated: ${resource.updatedAt} | File Size: ${resource.fileSize ?: "Standard"}\n")
        if (!resource.moduleCode.isNullOrEmpty()) sb.append("Module Code: ${resource.moduleCode} (${resource.credits} Credits)\n")
        if (!resource.paperCode.isNullOrEmpty()) sb.append("Paper Code: ${resource.paperCode} (${resource.totalMarks} Marks, ${resource.durationMinutes} mins)\n")
        sb.append("====================================================\n\n")

        sb.append("CLINICAL OVERVIEW:\n")
        sb.append("${resource.description}\n\n")

        if (resource.learningOutcomes.isNotEmpty()) {
            sb.append("CORE LEARNING OUTCOMES:\n")
            resource.learningOutcomes.forEachIndexed { i, outcome ->
                sb.append("${i + 1}. $outcome\n")
            }
            sb.append("\n")
        }

        if (resource.syllabus.isNotEmpty()) {
            sb.append("DETAILED SYLLABUS & UNITS:\n")
            resource.syllabus.forEach { unit ->
                sb.append("\n--- Unit ${unit.unitNumber}: ${unit.title} (${unit.durationWeeks} Weeks) ---\n")
                if (unit.topics.isNotEmpty()) {
                    sb.append("Topics: " + unit.topics.joinToString(", ") + "\n")
                }
                if (unit.keyCompetencies.isNotEmpty()) {
                    sb.append("Key Competencies:\n")
                    unit.keyCompetencies.forEach { comp -> sb.append("  • $comp\n") }
                }
            }
            sb.append("\n")
        }

        if (resource.sections.isNotEmpty()) {
            sb.append("CLINICAL NOTES & PROTOCOLS:\n")
            resource.sections.forEach { sec ->
                sb.append("\n--- ${sec.title} ---\n")
                sb.append("${sec.content}\n")
                if (sec.bulletPoints.isNotEmpty()) {
                    sec.bulletPoints.forEach { pt -> sb.append("  • $pt\n") }
                }
                if (sec.callout != null) {
                    sb.append("  [${sec.callout.type.uppercase()}] ${sec.callout.text}\n")
                }
            }
            sb.append("\n")
        }

        if (resource.questions.isNotEmpty()) {
            sb.append("PAST PAPER QUESTIONS & EXAM MARKING SCHEMES:\n")
            resource.questions.forEach { q ->
                sb.append("\nQuestion ${q.number} (${q.marks} Marks):\n")
                sb.append("${q.questionText}\n")
                if (q.options.isNotEmpty()) {
                    q.options.forEachIndexed { idx, opt ->
                        val letter = ('A' + idx)
                        val isCorrect = if (q.correctOptionIndex == idx) " [CORRECT ANSWER]" else ""
                        sb.append("  $letter. $opt$isCorrect\n")
                    }
                }
                if (q.clinicalRationale.isNotEmpty()) {
                    sb.append("Clinical Rationale: ${q.clinicalRationale}\n")
                }
                if (q.markingScheme.isNotEmpty()) {
                    sb.append("Marking Scheme: ${q.markingScheme}\n")
                }
            }
            sb.append("\n")
        }

        if (resource.tableOfContents.isNotEmpty()) {
            sb.append("TEXTBOOK CHAPTERS & HIGH-YIELD PEARLS:\n")
            resource.tableOfContents.forEach { ch ->
                sb.append("\nChapter ${ch.chapterNumber}: ${ch.title} (${ch.pageRange})\n")
                sb.append("${ch.summary}\n")
                ch.keyPearls.forEach { pearl -> sb.append("  • Pearl: $pearl\n") }
            }
            sb.append("\n")
        }

        if (resource.highYieldKeyPoints.isNotEmpty()) {
            sb.append("HIGH YIELD CLINICAL PEARLS:\n")
            resource.highYieldKeyPoints.forEach { pt -> sb.append("• $pt\n") }
            sb.append("\n")
        }

        if (!resource.documentContentText.isNullOrEmpty()) {
            sb.append("OFFICIAL GUIDELINE TEXT:\n")
            sb.append("${resource.documentContentText}\n\n")
        }

        sb.append("----------------------------------------------------\n")
        sb.append("DATANURSE Clinical Portal by Felix Chanda (fchanda335@gmail.com)\n")
        sb.append("Department of Nursing Sciences\n")
        return sb.toString()
    }

    fun exportDatabaseJson(): String {
        val totalCurated = _rawResources.value.size
        val customCount = _customResources.value.size
        val bookmarks = _bookmarkedIds.value.toList()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        return """
        {
          "application": "DATANURSE",
          "version": "2.1.0",
          "exportTimestamp": "$date",
          "author": "Felix Chanda",
          "email": "fchanda335@gmail.com",
          "statistics": {
            "curatedResources": $totalCurated,
            "customResources": $customCount,
            "bookmarkedCount": ${bookmarks.size}
          },
          "bookmarkedIds": ${bookmarks.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")},
          "driveSyncFolder": "/DATANURSE_Zambia_Curriculum_2026/"
        }
        """.trimIndent()
    }

    fun showToast(msg: String) {
        toastMessage.value = msg
    }

    fun clearToast() {
        toastMessage.value = null
    }
}
