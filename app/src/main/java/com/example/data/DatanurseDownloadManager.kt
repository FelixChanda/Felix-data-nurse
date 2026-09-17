package com.example.data

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.model.ResourceItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

data class DownloadedFileItem(
    val id: String,
    val title: String,
    val fileName: String,
    val filePath: String,
    val fileSizeFormatted: String,
    val fileSizeBytes: Long,
    val category: String,
    val downloadedAt: String,
    val downloadedAtTimestamp: Long,
    val isAvailable: Boolean = true
)

object DatanurseDownloadManager {
    private const val TAG = "DatanurseDownload"
    const val DOWNLOAD_SUBFOLDER = "Datanurse"

    private val _downloadedFiles = MutableStateFlow<List<DownloadedFileItem>>(emptyList())
    val downloadedFiles: StateFlow<List<DownloadedFileItem>> = _downloadedFiles.asStateFlow()

    private val _lastDownloadedFile = MutableStateFlow<DownloadedFileItem?>(null)
    val lastDownloadedFile: StateFlow<DownloadedFileItem?> = _lastDownloadedFile.asStateFlow()

    fun initialize(context: Context) {
        refreshDownloads(context)
    }

    /**
     * Resolves and creates the public /Download/Datanurse directory on the device
     */
    fun getDatanurseDownloadDir(context: Context): File {
        val publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val datanurseDir = File(publicDownloads, DOWNLOAD_SUBFOLDER)
        if (!datanurseDir.exists()) {
            val created = datanurseDir.mkdirs()
            Log.d(TAG, "Created public Download/Datanurse directory: $created (${datanurseDir.absolutePath})")
        }
        if (!datanurseDir.canWrite()) {
            // Fallback to app-specific external files if public download is restricted
            val appExtDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), DOWNLOAD_SUBFOLDER)
            if (!appExtDir.exists()) appExtDir.mkdirs()
            return appExtDir
        }
        return datanurseDir
    }

    /**
     * Automatically intercepts download actions and writes files into Download/Datanurse/
     */
    suspend fun downloadResource(
        context: Context,
        resource: ResourceItem,
        customContent: String? = null
    ): Result<DownloadedFileItem> = withContext(Dispatchers.IO) {
        try {
            val targetDir = getDatanurseDownloadDir(context)
            val sanitizedTitle = resource.title
                .replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "")
                .trim()
                .replace("\\s+".toRegex(), "_")
                .take(40)
            
            val fileName = "DATANURSE_${resource.category.uppercase()}_$sanitizedTitle.txt"
            val targetFile = File(targetDir, fileName)

            val content = customContent ?: generateFormattedDocumentContent(resource)

            FileOutputStream(targetFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            // Trigger MediaScanner so the file shows up immediately in device Files & Downloads app
            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf("text/plain")
            ) { path, uri ->
                Log.d(TAG, "Media scan complete for: $path -> $uri")
            }

            val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            val fileItem = DownloadedFileItem(
                id = resource.id,
                title = resource.title,
                fileName = fileName,
                filePath = targetFile.absolutePath,
                fileSizeFormatted = formatFileSize(targetFile.length()),
                fileSizeBytes = targetFile.length(),
                category = resource.category,
                downloadedAt = now,
                downloadedAtTimestamp = System.currentTimeMillis(),
                isAvailable = true
            )

            _lastDownloadedFile.value = fileItem
            refreshDownloads(context)

            Log.i(TAG, "Successfully intercepted and saved file to: ${targetFile.absolutePath}")
            Result.success(fileItem)
        } catch (e: Exception) {
            Log.e(TAG, "Download interception error", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads custom OSCE Station checklist or study guide to Download/Datanurse/
     */
    suspend fun downloadOsceGuide(
        context: Context,
        title: String,
        content: String
    ): Result<DownloadedFileItem> = withContext(Dispatchers.IO) {
        try {
            val targetDir = getDatanurseDownloadDir(context)
            val sanitizedTitle = title
                .replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "")
                .trim()
                .replace("\\s+".toRegex(), "_")
                .take(40)
            
            val fileName = "DATANURSE_OSCE_$sanitizedTitle.txt"
            val targetFile = File(targetDir, fileName)

            FileOutputStream(targetFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(targetFile.absolutePath),
                arrayOf("text/plain"),
                null
            )

            val now = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            val fileItem = DownloadedFileItem(
                id = "osce_${System.currentTimeMillis()}",
                title = title,
                fileName = fileName,
                filePath = targetFile.absolutePath,
                fileSizeFormatted = formatFileSize(targetFile.length()),
                fileSizeBytes = targetFile.length(),
                category = "osce",
                downloadedAt = now,
                downloadedAtTimestamp = System.currentTimeMillis(),
                isAvailable = true
            )

            _lastDownloadedFile.value = fileItem
            refreshDownloads(context)
            Result.success(fileItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun refreshDownloads(context: Context) {
        try {
            val targetDir = getDatanurseDownloadDir(context)
            val files = targetDir.listFiles { file -> file.isFile } ?: emptyArray()
            val list = files.map { file ->
                val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
                val name = file.name
                val cat = when {
                    name.contains("CLINICAL", ignoreCase = true) -> "clinical"
                    name.contains("OSCE", ignoreCase = true) -> "osce"
                    name.contains("COMMUNITY", ignoreCase = true) -> "community"
                    name.contains("MIDWIFERY", ignoreCase = true) -> "midwifery"
                    name.contains("PHARMACOLOGY", ignoreCase = true) -> "pharmacology"
                    name.contains("EXAM", ignoreCase = true) || name.contains("PAPER", ignoreCase = true) -> "past_paper"
                    else -> "curriculum"
                }
                DownloadedFileItem(
                    id = file.name,
                    title = file.nameWithoutExtension.replace("DATANURSE_", "").replace("_", " "),
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileSizeFormatted = formatFileSize(file.length()),
                    fileSizeBytes = file.length(),
                    category = cat,
                    downloadedAt = dateStr,
                    downloadedAtTimestamp = file.lastModified(),
                    isAvailable = true
                )
            }.sortedByDescending { it.downloadedAtTimestamp }

            _downloadedFiles.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing downloads", e)
        }
    }

    fun deleteDownloadedFile(context: Context, item: DownloadedFileItem): Boolean {
        return try {
            val file = File(item.filePath)
            val deleted = if (file.exists()) file.delete() else false
            refreshDownloads(context)
            deleted
        } catch (e: Exception) {
            false
        }
    }

    fun openDownloadedFile(context: Context, item: DownloadedFileItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) return

            val uri = try {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e: Exception) {
                Uri.fromFile(file)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: share
            shareDownloadedFile(context, item)
        }
    }

    fun shareDownloadedFile(context: Context, item: DownloadedFileItem) {
        try {
            val file = File(item.filePath)
            if (!file.exists()) return

            val content = file.readText(Charsets.UTF_8)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TITLE, item.title)
                putExtra(Intent.EXTRA_SUBJECT, "DATANURSE: ${item.title}")
                putExtra(Intent.EXTRA_TEXT, content)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share ${item.title}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
        }
    }

    private fun generateFormattedDocumentContent(resource: ResourceItem): String {
        val sb = StringBuilder()
        sb.append("======================================================================\n")
        sb.append("              DATANURSE ACADEMIC & CLINICAL RESOURCE                 \n")
        sb.append("       General Nursing Council of Zambia (GNC) Competency Manual      \n")
        sb.append("======================================================================\n\n")
        sb.append("TITLE:            ${resource.title}\n")
        sb.append("CATEGORY:         ${resource.category.uppercase()}\n")
        sb.append("DOMAIN:           ${resource.domain}\n")
        sb.append("ACADEMIC LEVEL:   ${resource.yearLevel}\n")
        sb.append("INSTITUTION:      ${resource.authorOrInstitution}\n")
        sb.append("LAST UPDATED:     ${resource.updatedAt}\n")
        if (!resource.moduleCode.isNullOrEmpty()) sb.append("MODULE CODE:      ${resource.moduleCode} (${resource.credits} Credits)\n")
        if (!resource.paperCode.isNullOrEmpty()) sb.append("EXAM PAPER:       ${resource.paperCode} (${resource.totalMarks} Marks, ${resource.durationMinutes} Minutes)\n")
        sb.append("LOCAL PATH:       Device Downloads/Datanurse/\n")
        sb.append("ONLINE REPOSITORY:https://ais-pre-cfr26ct6xov6ir5jqbkj3l-327265371817.europe-west2.run.app\n")
        sb.append("GOOGLE DRIVE:     f94976173@gmail.com /DATANURSE_Zambia_Curriculum_2026/\n")
        sb.append("----------------------------------------------------------------------\n\n")

        sb.append("OVERVIEW & CLINICAL OBJECTIVES:\n")
        sb.append(resource.description)
        sb.append("\n\n")

        if (resource.sections.isNotEmpty()) {
            sb.append("======================================================================\n")
            sb.append("                         CURRICULUM CONTENT                           \n")
            sb.append("======================================================================\n\n")
            resource.sections.forEachIndexed { idx, sec ->
                sb.append("--- SECTION ${idx + 1}: ${sec.title.uppercase()} ---\n")
                sb.append(sec.content)
                sb.append("\n\n")
            }
        }

        if (resource.highYieldKeyPoints.isNotEmpty() || resource.learningOutcomes.isNotEmpty()) {
            sb.append("======================================================================\n")
            sb.append("                      KEY CLINICAL TAKEAWAYS                         \n")
            sb.append("======================================================================\n")
            val points = if (resource.highYieldKeyPoints.isNotEmpty()) resource.highYieldKeyPoints else resource.learningOutcomes
            points.forEachIndexed { i, pt ->
                sb.append("[${i + 1}] $pt\n")
            }
            sb.append("\n")
        }

        if (resource.questions.isNotEmpty()) {
            sb.append("======================================================================\n")
            sb.append("                 GNC PRACTICE QUIZ & BOARD QUESTIONS                  \n")
            sb.append("======================================================================\n\n")
            resource.questions.forEachIndexed { i, q ->
                sb.append("QUESTION ${i + 1}: ${q.questionText}\n")
                q.options.forEachIndexed { optIdx, opt ->
                    val letter = ('A' + optIdx).toChar()
                    val isCorrect = if (optIdx == q.correctOptionIndex) " [CORRECT ANSWER]" else ""
                    sb.append("  $letter. $opt$isCorrect\n")
                }
                if (q.clinicalRationale.isNotEmpty()) {
                    sb.append("RATIONALE: ${q.clinicalRationale}\n\n")
                } else if (q.markingScheme.isNotEmpty()) {
                    sb.append("MARKING SCHEME: ${q.markingScheme}\n\n")
                } else {
                    sb.append("\n")
                }
            }
        }

        if (!resource.documentContentText.isNullOrEmpty()) {
            sb.append("======================================================================\n")
            sb.append("                         DOCUMENT CLINICAL TEXT                       \n")
            sb.append("======================================================================\n\n")
            sb.append(resource.documentContentText)
            sb.append("\n\n")
        }

        sb.append("======================================================================\n")
        sb.append("DATANURSE ZAMBIA - Powered by GNC Standards & National Health Strategic Plan\n")
        sb.append("Saved to /Download/Datanurse/ on ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
        sb.append("======================================================================\n")
        return sb.toString()
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        }
    }
}
