package com.example.data

import android.content.Context
import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object DataLoader {

    fun loadInitialResources(context: Context): List<ResourceItem> {
        val list = mutableListOf<ResourceItem>()
        try {
            val jsonString = readAsset(context, "initial_resources.json")
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(parseResourceItem(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun loadOptimumConditions(context: Context): List<OptimumCondition> {
        val list = mutableListOf<OptimumCondition>()
        try {
            val jsonString = readAsset(context, "optimum_conditions.json")
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OptimumCondition(
                        id = obj.optString("id"),
                        parameter = obj.optString("parameter"),
                        category = obj.optString("category"),
                        optimumRange = obj.optString("optimumRange"),
                        numericTarget = obj.optDouble("numericTarget", 0.0),
                        unit = obj.optString("unit"),
                        clinicalSignificance = obj.optString("clinicalSignificance"),
                        nursingInterventionIfAbnormal = obj.optString("nursingInterventionIfAbnormal"),
                        standardAuthority = obj.optString("standardAuthority"),
                        lastOnlineSync = obj.optString("lastOnlineSync")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun loadNursingTopics(context: Context): List<NursingTopic> {
        val list = mutableListOf<NursingTopic>()
        try {
            val jsonString = readAsset(context, "nursing_topics.json")
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NursingTopic(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        region = obj.optString("region", "Zambia"),
                        institutionOrGuideline = obj.optString("institutionOrGuideline"),
                        category = obj.optString("category"),
                        level = obj.optString("level"),
                        summary = obj.optString("summary"),
                        keyPearls = jsonArrayToStringList(obj.optJSONArray("keyPearls")),
                        priorityInterventions = jsonArrayToStringList(obj.optJSONArray("priorityInterventions")),
                        examFocus = obj.optString("examFocus"),
                        recentUpdates = if (obj.has("recentUpdates")) obj.optString("recentUpdates") else null,
                        sourceAuthority = if (obj.has("sourceAuthority")) obj.optString("sourceAuthority") else null,
                        tags = jsonArrayToStringList(obj.optJSONArray("tags"))
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun loadOsceVideos(context: Context): List<OsceVideo> {
        val list = mutableListOf<OsceVideo>()
        try {
            val jsonString = readAsset(context, "osce_videos.json")
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val durationStr = obj.optString("duration", "15:00")
                val durSec = parseDurationToSeconds(durationStr)
                val keySteps = jsonArrayToStringList(obj.optJSONArray("keySteps"))
                val stepTimestamps = if (obj.has("stepTimestamps")) {
                    jsonArrayToStringList(obj.optJSONArray("stepTimestamps"))
                } else {
                    generateTimestampsForSteps(keySteps.size, durSec)
                }

                list.add(
                    OsceVideo(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        category = obj.optString("category"),
                        categoryLabel = obj.optString("categoryLabel"),
                        channelName = obj.optString("channelName"),
                        creatorTag = obj.optString("creatorTag"),
                        channelSubscribers = obj.optString("channelSubscribers", "52.4K students"),
                        institutionBadge = obj.optString("institutionBadge"),
                        youtubeId = if (obj.has("youtubeId")) obj.optString("youtubeId") else null,
                        videoUrl = if (obj.has("videoUrl")) obj.optString("videoUrl") else if (obj.has("youtubeId")) "https://www.youtube.com/watch?v=" + obj.optString("youtubeId") else null,
                        driveFileId = if (obj.has("driveFileId")) obj.optString("driveFileId") else "1OSCETUBE_Drive_${obj.optString("id")}",
                        driveFolderUrl = obj.optString("driveFolderUrl", "https://drive.google.com/drive/folders/1OSCETUBE_Nursing_Skills_Zambia"),
                        duration = durationStr,
                        durationSeconds = durSec,
                        views = obj.optString("views", "75.4K views"),
                        uploadDate = obj.optString("uploadDate", "Recently uploaded"),
                        thumbnailUrl = obj.optString("thumbnailUrl"),
                        description = obj.optString("description"),
                        keySteps = keySteps,
                        stepTimestamps = stepTimestamps,
                        equipmentNeeded = jsonArrayToStringList(obj.optJSONArray("equipmentNeeded")),
                        examTips = obj.optString("examTips"),
                        likesCount = obj.optString("likesCount", "3.2K"),
                        isDriveSource = obj.optBoolean("isDriveSource", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun parseDurationToSeconds(durationStr: String): Int {
        return try {
            val parts = durationStr.split(":")
            if (parts.size == 2) {
                parts[0].trim().toInt() * 60 + parts[1].trim().toInt()
            } else if (parts.size == 3) {
                parts[0].trim().toInt() * 3600 + parts[1].trim().toInt() * 60 + parts[2].trim().toInt()
            } else 900
        } catch (e: Exception) {
            900
        }
    }

    private fun generateTimestampsForSteps(stepCount: Int, totalDurationSec: Int): List<String> {
        if (stepCount == 0) return emptyList()
        val interval = totalDurationSec / stepCount
        return (0 until stepCount).map { i ->
            val sec = i * interval
            val m = sec / 60
            val s = sec % 60
            String.format("%02d:%02d", m, s)
        }
    }

    private fun parseResourceItem(obj: JSONObject): ResourceItem {
        val syllabusList = mutableListOf<ModuleSyllabusUnit>()
        val syllabusArr = obj.optJSONArray("syllabus")
        if (syllabusArr != null) {
            for (s in 0 until syllabusArr.length()) {
                val unitObj = syllabusArr.getJSONObject(s)
                syllabusList.add(
                    ModuleSyllabusUnit(
                        unitNumber = unitObj.optInt("unitNumber", s + 1),
                        title = unitObj.optString("title"),
                        durationWeeks = unitObj.optInt("durationWeeks", 2),
                        topics = jsonArrayToStringList(unitObj.optJSONArray("topics")),
                        keyCompetencies = jsonArrayToStringList(unitObj.optJSONArray("keyCompetencies")),
                        clinicalHours = if (unitObj.has("clinicalHours")) unitObj.optInt("clinicalHours") else null
                    )
                )
            }
        }

        val questionsList = mutableListOf<PastPaperQuestion>()
        val qArr = obj.optJSONArray("questions")
        if (qArr != null) {
            for (q in 0 until qArr.length()) {
                val qObj = qArr.getJSONObject(q)
                questionsList.add(
                    PastPaperQuestion(
                        id = qObj.optString("id", "q-$q"),
                        number = qObj.optInt("number", q + 1),
                        type = qObj.optString("type", "multiple_choice"),
                        questionText = qObj.optString("questionText"),
                        options = jsonArrayToStringList(qObj.optJSONArray("options")),
                        correctOptionIndex = if (qObj.has("correctOptionIndex")) qObj.optInt("correctOptionIndex") else null,
                        marks = qObj.optInt("marks", 1),
                        markingScheme = qObj.optString("markingScheme"),
                        clinicalRationale = qObj.optString("clinicalRationale"),
                        highYieldTip = if (qObj.has("highYieldTip")) qObj.optString("highYieldTip") else null
                    )
                )
            }
        }

        val chaptersList = mutableListOf<TextbookChapter>()
        val chArr = obj.optJSONArray("tableOfContents")
        if (chArr != null) {
            for (c in 0 until chArr.length()) {
                val chObj = chArr.getJSONObject(c)
                chaptersList.add(
                    TextbookChapter(
                        chapterNumber = chObj.optInt("chapterNumber", c + 1),
                        title = chObj.optString("title"),
                        pageRange = chObj.optString("pageRange"),
                        keyPearls = jsonArrayToStringList(chObj.optJSONArray("keyPearls")),
                        summary = chObj.optString("summary")
                    )
                )
            }
        }

        val sectionsList = mutableListOf<ClinicalSection>()
        val secArr = obj.optJSONArray("sections")
        if (secArr != null) {
            for (sc in 0 until secArr.length()) {
                val secObj = secArr.getJSONObject(sc)
                var callout: CalloutData? = null
                val calloutObj = secObj.optJSONObject("callout")
                if (calloutObj != null) {
                    callout = CalloutData(
                        type = calloutObj.optString("type", "pearl"),
                        text = calloutObj.optString("text")
                    )
                }
                sectionsList.add(
                    ClinicalSection(
                        title = secObj.optString("title"),
                        content = secObj.optString("content"),
                        bulletPoints = jsonArrayToStringList(secObj.optJSONArray("bulletPoints")),
                        callout = callout
                    )
                )
            }
        }

        return ResourceItem(
            id = obj.optString("id"),
            title = obj.optString("title"),
            category = obj.optString("category"),
            domain = obj.optString("domain"),
            yearLevel = obj.optString("yearLevel"),
            description = obj.optString("description"),
            authorOrInstitution = obj.optString("authorOrInstitution", "Department of Nursing Sciences"),
            updatedAt = obj.optString("updatedAt", "2024-09"),
            fileSize = if (obj.has("fileSize")) obj.optString("fileSize") else null,
            tags = jsonArrayToStringList(obj.optJSONArray("tags")),
            isBookmarked = obj.optBoolean("isBookmarked", false),
            isFeatured = obj.optBoolean("isFeatured", false),
            downloadCount = obj.optInt("downloadCount", 120),
            moduleCode = if (obj.has("moduleCode")) obj.optString("moduleCode") else null,
            credits = if (obj.has("credits")) obj.optInt("credits") else null,
            semester = if (obj.has("semester")) obj.optString("semester") else null,
            syllabus = syllabusList,
            learningOutcomes = jsonArrayToStringList(obj.optJSONArray("learningOutcomes")),
            clinicalPlacementHours = if (obj.has("clinicalPlacementHours")) obj.optInt("clinicalPlacementHours") else null,
            examYear = if (obj.has("examYear")) obj.optInt("examYear") else null,
            examPeriod = if (obj.has("examPeriod")) obj.optString("examPeriod") else null,
            paperCode = if (obj.has("paperCode")) obj.optString("paperCode") else null,
            totalMarks = if (obj.has("totalMarks")) obj.optInt("totalMarks") else null,
            durationMinutes = if (obj.has("durationMinutes")) obj.optInt("durationMinutes") else null,
            questions = questionsList,
            authors = if (obj.has("authors")) obj.optString("authors") else null,
            edition = if (obj.has("edition")) obj.optString("edition") else null,
            publisher = if (obj.has("publisher")) obj.optString("publisher") else null,
            publicationYear = if (obj.has("publicationYear")) obj.optInt("publicationYear") else null,
            isbn = if (obj.has("isbn")) obj.optString("isbn") else null,
            coverAccent = if (obj.has("coverAccent")) obj.optString("coverAccent") else null,
            tableOfContents = chaptersList,
            noteType = if (obj.has("noteType")) obj.optString("noteType") else null,
            readTimeMinutes = if (obj.has("readTimeMinutes")) obj.optInt("readTimeMinutes") else null,
            sections = sectionsList,
            highYieldKeyPoints = jsonArrayToStringList(obj.optJSONArray("highYieldKeyPoints")),
            documentFormat = if (obj.has("documentFormat")) obj.optString("documentFormat") else null,
            documentGuidelineType = if (obj.has("documentGuidelineType")) obj.optString("documentGuidelineType") else null,
            pageCount = if (obj.has("pageCount")) obj.optInt("pageCount") else null,
            documentContentText = if (obj.has("documentContentText")) obj.optString("documentContentText") else null,
            externalUrl = if (obj.has("externalUrl")) obj.optString("externalUrl") else "https://drive.google.com/drive/folders/1A_Nursing_Curriculum_Zambia?file=${obj.optString("id")}",
            driveFileId = if (obj.has("driveFileId")) obj.optString("driveFileId") else "gdrive-curriculum-${obj.optString("id")}",
            isDriveSynced = obj.optBoolean("isDriveSynced", true),
            lastDriveSyncDate = obj.optString("lastDriveSyncDate", "2024-09-15 08:30 GMT")
        )
    }

    private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }

    private fun readAsset(context: Context, fileName: String): String {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line).append("\n")
            line = reader.readLine()
        }
        reader.close()
        inputStream.close()
        return sb.toString()
    }
}
