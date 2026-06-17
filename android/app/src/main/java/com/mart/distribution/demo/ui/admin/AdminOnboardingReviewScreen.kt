package com.mart.distribution.demo.ui.admin

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.api.dto.OnboardingDocumentDto
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainViewModel
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOnboardingReviewScreen(
    userId: String,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    onApprove: (String) -> Unit,
    onReject: (String, String?) -> Unit,
) {
    val ui by mainViewModel.uiState.collectAsState()
    val user =
        when (val u = ui.users) {
            is LoadState.Ok -> u.data.find { it.id == userId }
            else -> null
        }

    Scaffold(
        containerColor = WholesaleBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Review onboarding") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = WholesaleBg,
                    ),
            )
        },
    ) { padding ->
        if (user == null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
            ) {
                Text("User not found", color = MaterialTheme.colorScheme.error)
            }
            return@Scaffold
        }

        AdminOnboardingReviewContent(
            user = user,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            onApprove = { onApprove(user.id) },
            onReject = { onReject(user.id, null) },
        )
    }
}

@Composable
private fun AdminOnboardingReviewContent(
    user: UserRowDto,
    modifier: Modifier = Modifier,
    onApprove: () -> Unit,
    onReject: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Spacer(Modifier.height(4.dp))
        FmCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text(user.email, fontSize = 13.sp, color = WholesaleMuted)
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        FmBadge(user.status)
                        FmBadge(user.role, label = user.role.replace('_', ' '))
                    }
                }
                DetailLine("Phone", user.phone ?: "—")
                DetailLine("Area", user.area?.name ?: "—")
                DetailLine("Onboarded by", user.onboardedBy?.name ?: "—")
                user.onboardedBy?.email?.let { DetailLine("Employee email", it) }
                DetailLine("Submitted", user.createdAt?.take(19)?.replace('T', ' ') ?: "—")
                user.onboardingNotes?.takeIf { it.isNotBlank() }?.let {
                    DetailLine("Notes", it)
                }
                user.statusReason?.let { DetailLine("Status reason", it) }
            }
        }

        FmCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Uploaded documents", fontWeight = FontWeight.SemiBold, color = WholesaleText)
                if (user.onboardingDocuments.isEmpty()) {
                    Text("No documents attached.", color = WholesaleMuted, fontSize = 13.sp)
                } else {
                    user.onboardingDocuments.forEach { doc ->
                        DocumentRow(
                            doc = doc,
                            onOpen = {
                                openOnboardingDocument(context, user.id, doc)
                            },
                        )
                    }
                }
            }
        }

        if (user.status.equals("PENDING_APPROVAL", ignoreCase = true)) {
            Text(
                "Verify details and documents, then approve. A confirmation email is sent only after approval.",
                style = MaterialTheme.typography.bodySmall,
                color = WholesaleMuted,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FmButton("Approve", onClick = onApprove, modifier = Modifier.weight(1f))
                FmButton("Reject", onClick = onReject, modifier = Modifier.weight(1f), variant = FmButtonVariant.Outline)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String,
) {
    Column {
        Text(label, fontSize = 11.sp, color = WholesaleMuted)
        Text(value, fontSize = 14.sp, color = WholesaleText)
    }
}

@Composable
private fun DocumentRow(
    doc: OnboardingDocumentDto,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(doc.label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(doc.fileName, fontSize = 12.sp, color = WholesaleMuted)
        }
        FmButton("View", onClick = onOpen, variant = FmButtonVariant.Outline)
    }
}

private fun openOnboardingDocument(
    context: android.content.Context,
    userId: String,
    doc: OnboardingDocumentDto,
) {
    val localFile = OnboardingDocumentStorage.fileForDocument(doc)
    if (localFile != null) {
        val uri =
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile,
            )
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, doc.mimeType ?: "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        context.startActivity(Intent.createChooser(intent, "Open document"))
        return
    }
    if (!BuildConfig.USE_LOCAL_DEMO_AUTH) {
        val base = BuildConfig.API_BASE_URL.trimEnd('/')
        val url = "$base/users/$userId/onboarding-documents/${doc.id}/file"
        context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    }
}
