package com.mart.distribution.demo.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.data.api.dto.InvoiceDocumentDto
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDataRow
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmInfoBanner
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.navigation.safePopBack
import com.mart.distribution.demo.ui.pdf.InvoicePdfExporter
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import com.mart.distribution.demo.util.ApiErrorMessages

@Composable
fun InvoiceScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
) {
    val goBack = { navController.safePopBack() }
    NavBackHandler(goBack)
    val context = LocalContext.current
    var doc by remember { mutableStateOf<InvoiceDocumentDto?>(null) }
    var err by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(orderId) {
        err = null
        doc = null
        loading = true
        try {
            doc =
                if (container.sessionRepository.isLocalDemoMode()) {
                    container.localDemoMartStore.invoiceByOrder(orderId)
                } else {
                    container.martApi.invoiceByOrder(orderId)
                }
        } catch (e: Exception) {
            err =
                ApiErrorMessages.fromThrowable(
                    e,
                    fallback = "Could not load invoice",
                    notFoundFallback = "No invoice available for this order yet.",
                )
        } finally {
            loading = false
        }
    }

    Scaffold(containerColor = WholesaleBg, topBar = {}) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            FmAppHeader(
                title = "Tax invoice",
                subtitle = doc?.invoiceNumber,
                onBack = goBack,
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = FmSpacing.screenH)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
            ) {
                when {
                    loading -> FmLoadingState(message = "Loading invoice…")
                    err != null -> {
                        FmErrorBanner(message = err!!)
                        FmInfoBanner(
                            message = "Invoices are generated after the dealer confirms the order and payment is recorded.",
                        )
                    }
                    doc != null -> {
                        val d = doc!!
                        FmCard(modifier = Modifier.fillMaxWidth(), padding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(WholesaleInkSurface)
                                        .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            ) {
                                Text("Tax Invoice", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(d.invoiceNumber, fontSize = 11.sp, color = Color.White.copy(0.6f))
                            }
                            Column(Modifier.padding(14.dp)) {
                            d.generatedAt?.let {
                                Text(it, fontSize = 12.sp, color = WholesaleMuted, modifier = Modifier.padding(bottom = 8.dp))
                            }
                            val o = d.order
                            if (o != null) {
                                FmMoneyRow("Subtotal", formatDecimal(o.totalAmount))
                                FmMoneyRow("Discount", "− ${formatDecimal(o.discountAmount)}", accent = WholesaleGreen)
                                FmMoneyRow("GST", "+ ${formatDecimal(o.gstAmount)}")
                                androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().height(1.dp).background(com.mart.distribution.demo.ui.theme.WholesaleBorder))
                                FmMoneyRow("Total", formatDecimal(o.finalAmount), strong = true, accent = WholesaleGreen)
                            }
                            }
                        }
                        FmSectionLabel(title = "Line items")
                        for (line in d.order?.items.orEmpty()) {
                            FmCard(modifier = Modifier.fillMaxWidth()) {
                                FmDataRow(
                                    title = line.product?.name ?: line.productId,
                                    subtitle = "${line.quantity} × line total ${formatDecimal(line.finalAmount)}",
                                )
                            }
                        }
                        FmButton(
                            text = if (busy) "Preparing PDF…" else "Download / share PDF",
                            onClick = {
                                if (busy) return@FmButton
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
                                    err =
                                        ApiErrorMessages.fromThrowable(
                                            e,
                                            fallback = "Could not create PDF",
                                        )
                                } finally {
                                    busy = false
                                }
                            },
                            enabled = !busy,
                        )
                    }
                    else -> {
                        FmEmptyState(
                            icon = Icons.Outlined.Description,
                            title = "No invoice yet",
                            message = "An invoice will appear here once the order is confirmed and paid.",
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
