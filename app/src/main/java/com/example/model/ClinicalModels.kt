package com.example.model

data class OptimumCondition(
    val id: String,
    val parameter: String,
    val category: String, // "Vitals", "Hemodynamics", "Metabolic & Renal", "Acid-Base", "Perfusion"
    val optimumRange: String,
    val numericTarget: Double,
    val unit: String,
    val clinicalSignificance: String,
    val nursingInterventionIfAbnormal: String,
    val standardAuthority: String,
    val lastOnlineSync: String = ""
)

data class NursingTopic(
    val id: String,
    val title: String,
    val region: String, // "Zambia", "Global", "Both"
    val institutionOrGuideline: String,
    val category: String,
    val level: String,
    val summary: String,
    val keyPearls: List<String> = emptyList(),
    val priorityInterventions: List<String> = emptyList(),
    val examFocus: String = "",
    val recentUpdates: String? = null,
    val sourceAuthority: String? = null,
    val tags: List<String> = emptyList()
)

data class OsceVideo(
    val id: String,
    val title: String,
    val category: String,
    val categoryLabel: String,
    val channelName: String,
    val creatorTag: String,
    val channelSubscribers: String? = null,
    val institutionBadge: String,
    val youtubeId: String? = null,
    val videoUrl: String? = null,
    val driveFileId: String? = null,
    val driveFolderUrl: String = "https://drive.google.com/drive/folders/1OSCETUBE_Nursing_Skills_Zambia",
    val duration: String,
    val durationSeconds: Int = 900,
    val views: String? = null,
    val uploadDate: String? = null,
    val thumbnailUrl: String,
    val description: String,
    val keySteps: List<String> = emptyList(),
    val stepTimestamps: List<String> = emptyList(),
    val equipmentNeeded: List<String> = emptyList(),
    val examTips: String = "",
    val likesCount: String = "2.4K",
    val isDriveSource: Boolean = false
)

data class OsceComment(
    val id: String,
    val author: String,
    val authorAvatarBg: Long,
    val institution: String,
    val timeAgo: String,
    val text: String,
    val likes: Int,
    val isPinned: Boolean = false
)

data class Flashcard(
    val id: String,
    val question: String,
    val answer: String,
    val category: String, // "Priority Action", "NCLEX Case", "Clinical Rationale", "Drug & Pharmacology", "Diagnostic Sign", "Core Recall"
    val explanation: String,
    val keyPearl: String,
    val difficulty: String = "Standard" // "Standard", "Clinical Challenge", "NCLEX High-Yield"
)

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String,
    val clinicalPearl: String,
    val category: String = "Clinical Scenario",
    val difficulty: String = "NCLEX High-Yield"
)

data class QuizResult(
    val resourceId: String,
    val resourceTitle: String,
    val score: Int,
    val total: Int,
    val percentage: Int,
    val timeSpentSeconds: Int,
    val completedAt: String,
    val mode: String // "practice" or "exam"
)

data class LabValueItem(
    val testName: String,
    val normalRange: String,
    val unit: String,
    val panicLow: String? = null,
    val panicHigh: String? = null,
    val clinicalSignificance: String,
    val nursingAlert: String
)
