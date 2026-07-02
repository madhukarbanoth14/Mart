package com.mart.distribution.demo.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.onboarding.BusinessDocumentTypes
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDocumentCenterLoading
import com.mart.distribution.demo.ui.flashmart.FmDocumentCenterRow
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmVerificationProgressCard
import com.mart.distribution.demo.ui.flashmart.documentIcon
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleGold
import com.mart.distribution.demo.ui.theme.WholesaleGoldInk
import com.mart.distribution.demo.ui.theme.WholesaleGoldTint
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import java.io.File

@Composable
fun ProfileDocumentsScreen(
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
) {
    NavBackHandler(onBack)
    val ui by mainViewModel.uiState.collectAsState()
    val docsState = ui.myDocuments
    val context = LocalContext.current
    var uploadError by remember { mutableStateOf<String?>(null) }
    var uploadingType by remember { mutableStateOf<String?>(null) }
    var pendingType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        mainViewModel.loadMyDocuments()
        mainViewModel.refreshAuthProfile()
    }

    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            val type = pendingType ?: return@rememberLauncherForActivityResult
            pendingType = null
            if (uri == null) return@rememberLauncherForActivityResult
            val label = BusinessDocumentTypes.labelFor(type)
            val staged = OnboardingDocumentStorage.stageDocument(context, uri, label)
            if (staged == null) {
                uploadError = "Could not read selected file"
                return@rememberLauncherForActivityResult
            }
            uploadingType = type
            uploadError = null
            mainViewModel.uploadMyDocument(
                documentType = type,
                file = File(staged.localPath),
                mimeType = staged.mimeType,
                displayName = staged.displayName,
                onFinished = { err ->
                    uploadingType = null
                    uploadError = err
                },
            )
        }

    val docs = when (docsState) { is LoadState.Ok -> docsState.data; else -> emptyList() }
    val verifiedCount = docs.count { it.verificationStatus.equals("VERIFIED", true) }
    val pendingCount = docs.count { !it.verificationStatus.equals("VERIFIED", true) }
    val totalSlots = BusinessDocumentTypes.all.size
    val progressSubtitle =
        when {
            verifiedCount >= totalSlots -> "All required documents verified."
            pendingCount > 0 -> "$pendingCount document${if (pendingCount == 1) "" else "s"} awaiting verification."
            docs.isEmpty() -> "Upload at least one document to unlock full verification."
            else -> "${totalSlots - verifiedCount} more document${if (totalSlots - verifiedCount == 1) "" else "s"} recommended."
        }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        FmAppHeader(
            title = "Document center",
            subtitle = "$verifiedCount verified · $pendingCount pending",
            onBack = onBack,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = FmSpacing.screenH, vertical = FmSpacing.itemGap),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FmVerificationProgressCard(
                verifiedCount = verifiedCount,
                totalSlots = totalSlots,
                subtitle = progressSubtitle,
            )
            uploadError?.let { FmErrorBanner(it) }
            when (docsState) {
                is LoadState.Loading -> FmDocumentCenterLoading()
                is LoadState.Err -> FmErrorBanner(docsState.message)
                is LoadState.Ok -> {
                    if (docs.isEmpty()) {
                        FmEmptyState(
                            icon = Icons.Outlined.Description,
                            title = "No documents yet",
                            message = "Upload a business document to enable checkout.",
                        )
                    } else {
                        docs.forEach { doc ->
                            FmDocumentCenterRow(doc)
                        }
                    }
                }
                else -> Unit
            }

            val missingSlots =
                remember(docs) {
                    val uploadedTypes = docs.mapNotNull { it.documentType?.uppercase() }.toSet()
                    BusinessDocumentTypes.all.filter { it.type !in uploadedTypes }
                }
            if (missingSlots.isNotEmpty()) {
                Text(
                    "Recommended uploads",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleMuted,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                )
                missingSlots.forEach { slot ->
                    FmCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(WholesaleGoldTint),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    documentIcon(slot.type),
                                    contentDescription = null,
                                    tint = WholesaleGoldInk,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(slot.label, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                                Text("Not uploaded yet", fontSize = 12.sp, color = WholesaleMuted)
                            }
                        }
                    }
                }
            }

            FmButton(
                text = if (uploadingType != null) "Uploading…" else "Upload document",
                onClick = {
                    val next = missingSlots.firstOrNull()?.type ?: BusinessDocumentTypes.all.first().type
                    pendingType = next
                    picker.launch(arrayOf("application/pdf", "image/*"))
                },
                enabled = uploadingType == null,
                variant = FmButtonVariant.Primary,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
