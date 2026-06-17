package com.mart.distribution.demo.ui.onboarding

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentSlot
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.data.onboarding.PendingOnboardingDocument

@Composable
fun OnboardingDocumentsSection(
    slots: List<OnboardingDocumentSlot>,
    modifier: Modifier = Modifier,
    onDocumentsChanged: (List<PendingOnboardingDocument>) -> Unit,
) {
    val context = LocalContext.current
    val attached = remember { mutableStateMapOf<String, PendingOnboardingDocument>() }
    var pendingLabel by remember { mutableStateOf<String?>(null) }

    fun publish() {
        onDocumentsChanged(attached.values.toList())
    }

    val pickDocument =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val label = pendingLabel ?: return@rememberLauncherForActivityResult
            pendingLabel = null
            if (uri == null) return@rememberLauncherForActivityResult
            val staged = OnboardingDocumentStorage.stageDocument(context, uri, label) ?: return@rememberLauncherForActivityResult
            attached[label] = staged
            publish()
        }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (BuildConfig.REQUIRE_ONBOARDING_DOCUMENTS) {
                "Required documents"
            } else {
                "Documents (optional for testing)"
            },
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            if (BuildConfig.REQUIRE_ONBOARDING_DOCUMENTS) {
                "Upload KYC and business documents. Admin will review these before approval."
            } else {
                "Attach documents if available. Uploads are mandatory in production builds."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        slots.forEach { slot ->
            val doc = attached[slot.label]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        slot.label + if (slot.required) " *" else "",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    doc?.let {
                        Text(
                            it.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        pendingLabel = slot.label
                        pickDocument.launch("*/*")
                    },
                ) {
                    Text(if (doc == null) "Upload" else "Replace")
                }
            }
        }
    }
}

fun validateRequiredDocuments(
    slots: List<OnboardingDocumentSlot>,
    documents: List<PendingOnboardingDocument>,
): String? {
    if (!BuildConfig.REQUIRE_ONBOARDING_DOCUMENTS) return null
    val labels = documents.map { it.label }.toSet()
    val missing =
        slots.filter { it.required && it.label !in labels }.map { it.label }
    return if (missing.isEmpty()) {
        null
    } else {
        "Please upload: ${missing.joinToString(", ")}"
    }
}
