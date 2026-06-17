package com.mart.distribution.demo.data.onboarding

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.api.dto.OnboardingDocumentDto
import java.io.File
import java.util.UUID

data class PendingOnboardingDocument(
    val label: String,
    val displayName: String,
    val localPath: String,
    val mimeType: String?,
    val fileSize: Long,
)

object OnboardingDocumentStorage {
    fun dealerDocumentSlots(): List<OnboardingDocumentSlot> =
        withDocumentPolicy(
            listOf(
                OnboardingDocumentSlot("ID proof (Aadhaar / PAN)", required = true),
                OnboardingDocumentSlot("GST certificate", required = true),
                OnboardingDocumentSlot("Business registration / trade license", required = true),
                OnboardingDocumentSlot("Bank proof / cancelled cheque", required = false),
            ),
        )

    fun shopkeeperDocumentSlots(): List<OnboardingDocumentSlot> =
        withDocumentPolicy(
            listOf(
                OnboardingDocumentSlot("ID proof (Aadhaar / PAN)", required = true),
                OnboardingDocumentSlot("Shop license / rent agreement", required = true),
                OnboardingDocumentSlot("GST certificate", required = false),
            ),
        )

    private fun withDocumentPolicy(slots: List<OnboardingDocumentSlot>): List<OnboardingDocumentSlot> =
        if (BuildConfig.REQUIRE_ONBOARDING_DOCUMENTS) {
            slots
        } else {
            slots.map { slot -> slot.copy(required = false) }
        }

    fun stageDocument(
        context: Context,
        uri: Uri,
        label: String,
    ): PendingOnboardingDocument? {
        val resolver = context.contentResolver
        val displayName =
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx >= 0) cursor.getString(nameIdx) else null
                    } else {
                        null
                    }
                }
                ?: uri.lastPathSegment
                ?: "document"
        val mimeType = resolver.getType(uri)
        val stagingDir = File(context.filesDir, "onboarding/staging").apply { mkdirs() }
        val ext =
            MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType ?: "")
                ?.takeIf { it.isNotBlank() }
                ?: displayName.substringAfterLast('.', "bin")
        val outFile = File(stagingDir, "${UUID.randomUUID()}.$ext")
        resolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        return PendingOnboardingDocument(
            label = label,
            displayName = displayName,
            localPath = outFile.absolutePath,
            mimeType = mimeType,
            fileSize = outFile.length(),
        )
    }

    fun persistForUser(
        context: Context,
        userId: String,
        pending: List<PendingOnboardingDocument>,
    ): List<OnboardingDocumentDto> {
        val userDir = File(context.filesDir, "onboarding/$userId").apply { mkdirs() }
        return pending.map { doc ->
            val target = File(userDir, doc.displayName.replace(Regex("[^\\w.\\-]+"), "_"))
            File(doc.localPath).copyTo(target, overwrite = true)
            File(doc.localPath).delete()
            OnboardingDocumentDto(
                id = "doc-${UUID.randomUUID()}",
                label = doc.label,
                fileName = doc.displayName,
                mimeType = doc.mimeType,
                fileSize = doc.fileSize,
                uploadedAt = java.time.Instant.now().toString(),
                localPath = target.absolutePath,
            )
        }
    }

    fun fileForDocument(document: OnboardingDocumentDto): File? =
        document.localPath?.let { path ->
            val file = File(path)
            if (file.exists()) file else null
        }
}

data class OnboardingDocumentSlot(
    val label: String,
    val required: Boolean,
)
