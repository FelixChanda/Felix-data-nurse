package com.example.model

data class CalloutData(
    val type: String = "pearl", // "warning", "pearl", "danger", "rule"
    val text: String = ""
)

data class ClinicalSection(
    val title: String,
    val content: String,
    val bulletPoints: List<String> = emptyList(),
    val callout: CalloutData? = null
)

data class ModuleSyllabusUnit(
    val unitNumber: Int,
    val title: String,
    val durationWeeks: Int,
    val topics: List<String> = emptyList(),
    val keyCompetencies: List<String> = emptyList(),
    val clinicalHours: Int? = null
)

data class PastPaperQuestion(
    val id: String,
    val number: Int,
    val type: String = "multiple_choice", // multiple_choice, scenario_case, short_answer, care_plan
    val questionText: String,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int? = null,
    val marks: Int = 1,
    val markingScheme: String = "",
    val clinicalRationale: String = "",
    val highYieldTip: String? = null
)

data class TextbookChapter(
    val chapterNumber: Int,
    val title: String,
    val pageRange: String = "",
    val keyPearls: List<String> = emptyList(),
    val summary: String = ""
)

data class ResourceItem(
    val id: String,
    val title: String,
    val category: String, // "modules", "past_papers", "textbooks", "notes", "documents"
    val domain: String,
    val yearLevel: String,
    val description: String,
    val authorOrInstitution: String = "Nursing Faculty",
    val updatedAt: String = "",
    val fileSize: String? = null,
    val tags: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    val isFeatured: Boolean = false,
    val downloadCount: Int = 0,
    
    // Module specific
    val moduleCode: String? = null,
    val credits: Int? = null,
    val semester: String? = null,
    val syllabus: List<ModuleSyllabusUnit> = emptyList(),
    val learningOutcomes: List<String> = emptyList(),
    val clinicalPlacementHours: Int? = null,
    
    // Past paper specific
    val examYear: Int? = null,
    val examPeriod: String? = null,
    val paperCode: String? = null,
    val totalMarks: Int? = null,
    val durationMinutes: Int? = null,
    val questions: List<PastPaperQuestion> = emptyList(),
    
    // Textbook specific
    val authors: String? = null,
    val edition: String? = null,
    val publisher: String? = null,
    val publicationYear: Int? = null,
    val isbn: String? = null,
    val coverAccent: String? = null,
    val tableOfContents: List<TextbookChapter> = emptyList(),
    
    // Notes specific
    val noteType: String? = null,
    val readTimeMinutes: Int? = null,
    val sections: List<ClinicalSection> = emptyList(),
    val highYieldKeyPoints: List<String> = emptyList(),
    
    // Document specific
    val documentFormat: String? = null,
    val documentGuidelineType: String? = null,
    val pageCount: Int? = null,
    val documentContentText: String? = null,
    
    // External Document & Google Drive Cloud Sync
    val externalUrl: String? = null,
    val driveFileId: String? = null,
    val isDriveSynced: Boolean = false,
    val lastDriveSyncDate: String? = null
)
