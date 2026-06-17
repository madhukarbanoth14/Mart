package com.mart.distribution.demo.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.data.api.dto.InvoiceDocumentDto
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.pdf.InvoicePdfExporter
import com.mart.distribution.demo.ui.util.formatDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
) {
    val context = LocalContext.current
    var doc by remember { mutableStateOf<InvoiceDocumentDto?>(null) }
    var err by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        err = null
        doc = null
        try {
            doc =
                if (container.sessionRepository.isLocalDemoMode()) {
                    container.localDemoMartStore.invoiceByOrder(orderId)
                } else {
                    container.martApi.invoiceByOrder(orderId)
                }
        } catch (e: Exception) {
            err = e.message ?: "Could not load invoice"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Invoice") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                err != null -> {
                    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(err!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Invoices appear after a dealer confirms the order.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                doc != null -> {
                    val d = doc!!
                    MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Text(d.invoiceNumber, style = MaterialTheme.typography.headlineMedium)
                        d.generatedAt?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        val o = d.order
                        if (o != null) {
                            Text("Payable: ${formatDecimal(o.finalAmount)}", style = MaterialTheme.typography.titleLarge)
                            Text("GST: ${formatDecimal(o.gstAmount)} · Discount: ${formatDecimal(o.discountAmount)}")
                        }
                    }
                    for (line in d.order?.items.orEmpty()) {
                        MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(line.product?.name ?: line.productId, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${line.quantity} × line total ${formatDecimal(line.finalAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    GradientGoldButton(
                        text = if (busy) "Preparing PDF…" else "Download / share PDF",
                        onClick = {
                            if (busy) return@GradientGoldButton
                            busy = true
                            try {
                                val file = InvoicePdfExporter.write(context, d)
                                val uri =
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file,
                                    )
                                val send =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                context.startActivity(Intent.createChooser(send, "Share invoice"))
                            } catch (e: Exception) {
                                err = e.message ?: "PDF failed"
                            } finally {
                                busy = false
                            }
                        },
                        enabled = !busy,
                    )
                }
                else -> {
                    Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
