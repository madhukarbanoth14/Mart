package com.mart.distribution.demo.ui.dealer

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.mart.distribution.demo.data.api.dto.ReturnRequestDto
import com.mart.distribution.demo.feature.returns.ReturnsUiState
import com.mart.distribution.demo.feature.returns.ReturnsViewModel
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmMiniBarChart
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmStatCard
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlue
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

private val returnStatusFilters =
    listOf(null to "All", "REQUESTED" to "Pending", "APPROVED" to "Approved", "REJECTED" to "Rejected", "RETURN_COMPLETED" to "Completed")

@Composable
fun DealerRevenueScreen(
    returnsViewModel: ReturnsViewModel,
    onBack: () -> Unit,
    onOpenShopkeepers: () -> Unit,
) {
    val ui by returnsViewModel.uiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) { returnsViewModel.refreshDealerDashboard() }

    LaunchedEffect(ui.reportDownload) {
        val d = ui.reportDownload ?: return@LaunchedEffect
        try {
            val file = java.io.File(context.cacheDir, d.filename)
            file.writeBytes(d.bytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            context.startActivity(
                Intent(Intent.ACTION_SEND).apply {
                    type = d.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }.let { Intent.createChooser(it, "Share report") },
            )
        } finally {
            returnsViewModel.clearReportDownload()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FmSectionLabel(title = "Revenue dashboard")
        ui.error?.let { FmErrorBanner(it) }
        val s = ui.dealerDashboard?.summary
        if (s != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FmStatCard("Today", "₹${formatDecimal(s.todayRevenue)}", modifier = Modifier.weight(1f))
                FmStatCard("Week", "₹${formatDecimal(s.weeklyRevenue)}", modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FmStatCard("Month", "₹${formatDecimal(s.monthlyRevenue)}", modifier = Modifier.weight(1f))
                FmStatCard("Pending settlement", "₹${formatDecimal(s.pendingSettlementAmount)}", modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FmStatCard("Orders", "${s.totalOrdersReceived}", modifier = Modifier.weight(1f))
                FmStatCard("Delivered", "${s.totalOrdersDelivered}", modifier = Modifier.weight(1f))
                FmStatCard("Returns", "${s.returnedOrders}", modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FmStatCard("Qty sold", "${s.totalQuantitySold}", modifier = Modifier.weight(1f))
                FmStatCard("Received", "₹${formatDecimal(s.amountReceivedFromFlashMart)}", modifier = Modifier.weight(1f))
            }
        }
        val daily = ui.dealerDashboard?.charts?.dailyRevenue.orEmpty()
        if (daily.isNotEmpty()) {
            FmSectionLabel(title = "Daily revenue trend")
            FmMiniBarChart(data = daily.map { it.amount.toFloat() })
        }
        val top = ui.dealerDashboard?.charts?.topSellingProducts.orEmpty().take(5)
        if (top.isNotEmpty()) {
            FmSectionLabel(title = "Top products")
            top.forEach { p ->
                FmCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(p.name, fontWeight = FontWeight.SemiBold)
                        Text("₹${formatDecimal(p.revenue)}", color = WholesaleDealerBlue)
                    }
                }
            }
        }
        FmButton(text = "Shopkeeper revenue", onClick = onOpenShopkeepers)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FmButton(text = "Export revenue", onClick = { returnsViewModel.downloadDealerReport("revenue") }, modifier = Modifier.weight(1f))
            FmButton(text = "Export returns", onClick = { returnsViewModel.downloadDealerReport("returns") }, modifier = Modifier.weight(1f))
        }
        FmButton(text = "Back", onClick = onBack)
    }
}

@Composable
fun DealerShopkeeperRevenueScreen(
    returnsViewModel: ReturnsViewModel,
    onBack: () -> Unit,
) {
    val ui by returnsViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { returnsViewModel.refreshShopkeepers() }

    Column(Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp)) {
        FmSectionLabel(title = "Shopkeeper revenue")
        OutlinedTextField(
            value = ui.shopkeeperSearch,
            onValueChange = returnsViewModel::setShopkeeperSearch,
            label = { Text("Search shopkeeper") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = ui.areaFilter,
            onValueChange = returnsViewModel::setAreaFilter,
            label = { Text("Filter by area") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ui.shopkeeperRows) { row ->
                FmCard {
                    Text(row.name, fontWeight = FontWeight.Bold, color = WholesaleText)
                    Text(row.area ?: "—", fontSize = 12.sp, color = WholesaleMuted)
                    Text("Orders: ${row.totalOrders} · Revenue: ₹${formatDecimal(row.totalRevenue)}")
                    Text("Outstanding: ${row.outstandingOrders} · Returns: ${row.returnedOrders}")
                    row.lastOrderDate?.let { Text("Last order: $it", fontSize = 11.sp, color = WholesaleMuted) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        FmButton(text = "Back", onClick = onBack)
    }
}

@Composable
fun DealerReturnsScreen(
    returnsViewModel: ReturnsViewModel,
    onOpenReturn: (String) -> Unit,
    onBack: () -> Unit,
) {
    val ui by returnsViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { returnsViewModel.refreshReturns() }

    Column(Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp)) {
        FmSectionLabel(title = "Returns")
        ui.error?.let { FmErrorBanner(it) }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            returnStatusFilters.forEach { (value, label) ->
                FilterChip(
                    selected = ui.returnStatusFilter == value,
                    onClick = { returnsViewModel.setReturnStatusFilter(value) },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (ui.returns.isEmpty()) {
            FmEmptyState(
                icon = Icons.Outlined.Receipt,
                title = "No returns",
                message = "Return requests from shopkeepers will appear here.",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ui.returns, key = { it.id }) { row ->
                    ReturnListCard(row, onClick = { onOpenReturn(row.id) })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        FmButton(text = "Back", onClick = onBack)
    }
}

@Composable
fun DealerReturnDetailScreen(
    returnId: String,
    returnsViewModel: ReturnsViewModel,
    isDealer: Boolean,
    onBack: () -> Unit,
) {
    val ui by returnsViewModel.uiState.collectAsState()
    var remarks by remember { mutableStateOf("") }
    LaunchedEffect(returnId) { returnsViewModel.loadReturn(returnId) }
    val row = ui.selectedReturn

    Column(
        Modifier.fillMaxSize().background(WholesaleBg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FmSectionLabel(title = "Return details")
        ui.error?.let { FmErrorBanner(it) }
        ui.message?.let { Text(it, color = WholesaleDealerBlue) }
        if (row != null) {
            FmCard {
                Text(row.returnCode, fontWeight = FontWeight.Bold)
                Text("Status: ${row.status}")
                Text("Reason: ${row.reasonText ?: row.reason}")
                Text("Refund amount: ₹${formatDecimal(row.refundAmount)}")
                row.items.forEach { item ->
                    Text("• ${item.productName} × ${item.quantity}")
                }
            }
            if (isDealer) {
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (row.status == "REQUESTED" || row.status == "UNDER_REVIEW") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FmButton(text = "Approve", onClick = { returnsViewModel.approveReturn(returnId, remarks.ifBlank { null }) }, modifier = Modifier.weight(1f))
                        FmButton(text = "Reject", onClick = { returnsViewModel.rejectReturn(returnId, remarks.ifBlank { null }) }, modifier = Modifier.weight(1f))
                    }
                }
                if (row.status == "APPROVED" && row.refundRequest == null) {
                    FmButton(text = "Raise refund to admin", onClick = { returnsViewModel.raiseRefundRequest(returnId, remarks.ifBlank { null }) })
                }
            }
        }
        FmButton(text = "Back", onClick = onBack)
    }
}

@Composable
private fun ReturnListCard(row: ReturnRequestDto, onClick: () -> Unit) {
    FmCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(row.returnCode, fontWeight = FontWeight.Bold)
        Text("Order ${row.orderId.take(8)} · ${row.status}", fontSize = 12.sp, color = WholesaleMuted)
        Text("₹${formatDecimal(row.refundAmount)}", color = WholesaleDealerBlue)
    }
}

@Composable
fun AdminRefundsScreen(
    returnsViewModel: ReturnsViewModel,
    onOpenRefund: (String) -> Unit,
) {
    val ui by returnsViewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { returnsViewModel.refreshRefunds() }

    Column(Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp)) {
        FmSectionLabel(title = "Refund management")
        ui.error?.let { FmErrorBanner(it) }
        val filters = listOf(null to "All", "PENDING" to "Pending", "PROCESSING" to "Processing", "REFUNDED" to "Refunded", "REJECTED" to "Rejected")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEach { (value, label) ->
                FilterChip(
                    selected = ui.refundStatusFilter == value,
                    onClick = { returnsViewModel.setRefundStatusFilter(value) },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ui.refunds, key = { it.id }) { refund ->
                FmCard(Modifier.fillMaxWidth().clickable { onOpenRefund(refund.id) }) {
                    Text(refund.refundCode, fontWeight = FontWeight.Bold)
                    Text("Order ${refund.orderId.take(8)} · ${refund.status}")
                    Text("₹${formatDecimal(refund.amount)}")
                }
            }
        }
    }
}

@Composable
fun AdminRefundDetailScreen(
    refundId: String,
    returnsViewModel: ReturnsViewModel,
    onBack: () -> Unit,
) {
    val ui by returnsViewModel.uiState.collectAsState()
    var method by remember { mutableStateOf("UPI") }
    var reference by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    LaunchedEffect(refundId) { returnsViewModel.loadRefund(refundId) }
    val refund = ui.selectedRefund

    Column(
        Modifier.fillMaxSize().background(WholesaleBg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FmSectionLabel(title = "Refund request")
        ui.error?.let { FmErrorBanner(it) }
        refund?.let { r ->
            FmCard {
                Text(r.refundCode, fontWeight = FontWeight.Bold)
                Text("Status: ${r.status}")
                Text("Amount: ₹${formatDecimal(r.amount)}")
                Text("Dealer: ${r.dealer?.shopName ?: r.dealer?.name ?: "—"}")
                r.returnRequest?.let { ret ->
                    Text("Return: ${ret.returnCode}")
                    Text("Reason: ${ret.reasonText ?: ret.reason}")
                }
            }
            if (r.status == "PENDING") {
                FmButton(text = "Approve", onClick = { returnsViewModel.approveRefund(refundId, remarks.ifBlank { null }) })
                FmButton(text = "Reject", onClick = { returnsViewModel.rejectRefund(refundId, remarks.ifBlank { null }) })
            }
            if (r.status == "PENDING" || r.status == "PROCESSING") {
                OutlinedTextField(value = method, onValueChange = { method = it }, label = { Text("Method (RAZORPAY/UPI/BANK_TRANSFER)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Transaction reference") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") }, modifier = Modifier.fillMaxWidth())
                FmButton(
                    text = "Process refund",
                    onClick = {
                        if (reference.isNotBlank()) {
                            returnsViewModel.processRefund(refundId, method.uppercase(), reference, remarks.ifBlank { null })
                        }
                    },
                )
            }
        }
        FmButton(text = "Back", onClick = onBack)
    }
}
