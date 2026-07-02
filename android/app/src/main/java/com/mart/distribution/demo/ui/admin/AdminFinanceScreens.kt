package com.mart.distribution.demo.ui.admin

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Store
import androidx.compose.foundation.clickable
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
import com.mart.distribution.demo.data.api.dto.DealerSettlementDto
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.feature.finance.FinanceUiState
import com.mart.distribution.demo.feature.finance.FinanceViewModel
import com.mart.distribution.demo.feature.returns.ReturnsViewModel
import com.mart.distribution.demo.ui.dealer.AdminRefundsScreen
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmInfoBanner
import com.mart.distribution.demo.ui.flashmart.FmMiniBarChart
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmStatCard
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

private enum class FinanceSection {
    OVERVIEW, SETTLEMENTS, DEALERS, INVESTOR, COMMISSION, REPORTS, AUDIT, REFUNDS,
}

@Composable
fun AdminFinanceTab(
    financeViewModel: FinanceViewModel,
    returnsViewModel: ReturnsViewModel? = null,
    mainUi: MainUiState,
    onOpenSettlement: (String) -> Unit,
    onOpenDealer: (String) -> Unit,
    onOpenRefund: (String) -> Unit = {},
) {
    val ui by financeViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var section by remember { mutableStateOf(FinanceSection.OVERVIEW) }

    LaunchedEffect(Unit) { financeViewModel.refresh() }

    LaunchedEffect(ui.reportDownload) {
        val download = ui.reportDownload ?: return@LaunchedEffect
        try {
            val file = java.io.File(context.cacheDir, download.filename)
            file.writeBytes(download.bytes)
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            val send =
                Intent(Intent.ACTION_SEND).apply {
                    type = download.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            context.startActivity(Intent.createChooser(send, "Share report"))
        } catch (_: Exception) {
            // error surfaced via ViewModel if needed
        } finally {
            financeViewModel.clearReportDownload()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Finance & settlements", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            Text("Collections, commission, dealer payouts", fontSize = 13.sp, color = WholesaleMuted)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FinanceSection.entries.forEach { s ->
                    FilterChip(
                        selected = section == s,
                        onClick = { section = s },
                        label = { Text(s.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            if (section != FinanceSection.COMMISSION && section != FinanceSection.AUDIT && section != FinanceSection.REFUNDS) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("today", "week", "month", "year").forEach { p ->
                        FilterChip(
                            selected = ui.period == p,
                            onClick = { financeViewModel.setPeriod(p) },
                            label = { Text(p.replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }
        }

        ui.error?.let { FmErrorBanner(it, modifier = Modifier.padding(horizontal = 16.dp)) }
        ui.message?.let { FmInfoBanner(it, modifier = Modifier.padding(horizontal = 16.dp)) }

        when (section) {
            FinanceSection.OVERVIEW -> FinanceOverviewSection(ui)
            FinanceSection.SETTLEMENTS -> FinanceSettlementsSection(ui, mainUi, financeViewModel, onOpenSettlement)
            FinanceSection.DEALERS -> FinanceDealersSection(mainUi, onOpenDealer)
            FinanceSection.INVESTOR -> FinanceInvestorSection(ui)
            FinanceSection.COMMISSION -> FinanceCommissionSection(ui, mainUi, financeViewModel)
            FinanceSection.REPORTS -> FinanceReportsSection(ui, financeViewModel)
            FinanceSection.AUDIT -> FinanceAuditSection(ui)
            FinanceSection.REFUNDS ->
                returnsViewModel?.let {
                    AdminRefundsScreen(returnsViewModel = it, onOpenRefund = onOpenRefund)
                } ?: FmEmptyState(
                    icon = Icons.Outlined.AccountBalance,
                    title = "Refunds unavailable",
                    message = "Sign in as admin to manage refund requests.",
                )
        }
    }
}

@Composable
private fun FinanceOverviewSection(ui: FinanceUiState) {
    val rev = ui.overview?.revenue
    val col = ui.overview?.collections
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("GMV", "₹${formatDecimal(rev?.gmv ?: 0.0)}", modifier = Modifier.weight(1f), accent = WholesaleBlue)
            FmStatCard("Commission", "₹${formatDecimal(rev?.platformCommission ?: 0.0)}", modifier = Modifier.weight(1f), accent = WholesaleGreen)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Dealer payables", "₹${formatDecimal(rev?.dealerPayables ?: 0.0)}", modifier = Modifier.weight(1f), accent = WholesaleOrange)
            FmStatCard("Pending settlement", "₹${formatDecimal(rev?.pendingSettlement ?: 0.0)}", modifier = Modifier.weight(1f), accent = WholesaleOrange)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Net earnings", "₹${formatDecimal(rev?.netPlatformEarnings ?: 0.0)}", modifier = Modifier.weight(1f), accent = WholesaleGreen)
            FmStatCard("Settled", "₹${formatDecimal(rev?.settledAmount ?: 0.0)}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Refunds", "₹${formatDecimal(rev?.refunds ?: 0.0)}", modifier = Modifier.weight(1f))
            FmStatCard("Pending pay", "${col?.pending ?: 0}", modifier = Modifier.weight(1f))
        }
        FmSectionLabel("Payment collections")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Collected", "₹${formatDecimal(col?.total ?: 0.0)}", modifier = Modifier.weight(1f))
            FmStatCard("Successful", "${col?.successful ?: 0}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Failed", "${col?.failed ?: 0}", modifier = Modifier.weight(1f))
            FmStatCard("Refunded", "${col?.refunded ?: 0}", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FinanceInvestorSection(ui: FinanceUiState) {
    val biz = ui.investor?.business
    val trend = ui.investor?.charts?.dailyRevenueTrend.orEmpty().map { it.gmv.toFloat() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FmSectionLabel("Business overview")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Total orders", "${biz?.totalOrders ?: 0}", modifier = Modifier.weight(1f))
            FmStatCard("Delivered", "${biz?.deliveredOrders ?: 0}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Dealers", "${biz?.activeDealers ?: 0}", modifier = Modifier.weight(1f))
            FmStatCard("Shopkeepers", "${biz?.activeShopkeepers ?: 0}", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Employees", "${biz?.activeEmployees ?: 0}", modifier = Modifier.weight(1f))
            FmStatCard("GMV", "₹${formatDecimal(ui.investor?.revenue?.gmv ?: 0.0)}", modifier = Modifier.weight(1f))
        }
        val rev = ui.investor?.revenue
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            FmStatCard("Commission", "₹${formatDecimal(rev?.platformCommission ?: 0.0)}", modifier = Modifier.weight(1f))
            FmStatCard("Net revenue", "₹${formatDecimal(rev?.netPlatformEarnings ?: 0.0)}", modifier = Modifier.weight(1f))
        }
        FmSectionLabel("Revenue trend (30 days)")
        FmCard {
            if (trend.isEmpty()) {
                Text("No revenue data yet", color = WholesaleMuted, fontSize = 13.sp)
            } else {
                FmMiniBarChart(trend, modifier = Modifier.fillMaxWidth().height(80.dp))
            }
        }
    }
}

@Composable
private fun FinanceSettlementsSection(
    ui: FinanceUiState,
    mainUi: MainUiState,
    financeViewModel: FinanceViewModel,
    onOpenSettlement: (String) -> Unit,
) {
    val dealers =
        when (val u = mainUi.users) {
            is LoadState.Ok -> u.data.filter { it.role.equals("DEALER", true) }
            else -> emptyList()
        }
    var selectedDealer by remember { mutableStateOf(dealers.firstOrNull()?.id.orEmpty()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        FmSectionLabel("Filter by status")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(null to "All", "PENDING" to "Pending", "PARTIALLY_SETTLED" to "Partial", "SETTLED" to "Settled").forEach { (status, label) ->
                FilterChip(
                    selected = ui.settlementStatusFilter == status,
                    onClick = { financeViewModel.setSettlementStatusFilter(status) },
                    label = { Text(label) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (dealers.isNotEmpty()) {
            FmSectionLabel("Generate settlement")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                dealers.forEach { d ->
                    FilterChip(
                        selected = selectedDealer == d.id,
                        onClick = { selectedDealer = d.id },
                        label = { Text(d.name) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            FmButton(
                text = if (ui.loading) "Generating…" else "Generate 30-day settlement",
                enabled = selectedDealer.isNotBlank() && !ui.loading,
                onClick = { financeViewModel.generateSettlement(selectedDealer) {} },
            )
            Spacer(Modifier.height(16.dp))
        }
        FmSectionLabel("Settlements")
        if (ui.settlements.isEmpty()) {
            FmEmptyState(
                icon = Icons.Outlined.AccountBalance,
                title = "No settlements yet",
                message = "Generate a settlement after orders are delivered and paid",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ui.settlements, key = { it.id }) { s ->
                    SettlementRow(s, onClick = { onOpenSettlement(s.id) })
                }
            }
        }
    }
}

@Composable
private fun SettlementRow(s: DealerSettlementDto, onClick: () -> Unit) {
    FmCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(s.settlementCode, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(s.dealer?.shopName ?: s.dealer?.name ?: "Dealer", fontSize = 13.sp, color = WholesaleMuted)
            Text(
                "${s.settlementStartDate.take(10)} → ${s.settlementEndDate.take(10)} · ${s.totalOrders} orders",
                fontSize = 12.sp,
                color = WholesaleMuted,
            )
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Payable ₹${formatDecimal(s.dealerPayable.toDouble())}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(s.settlementStatus, fontSize = 12.sp, color = WholesaleGreen)
            }
        }
    }
}

@Composable
private fun FinanceCommissionSection(
    ui: FinanceUiState,
    mainUi: MainUiState,
    financeViewModel: FinanceViewModel,
) {
    val dealers =
        when (val u = mainUi.users) {
            is LoadState.Ok -> u.data.filter { it.role.equals("DEALER", true) }
            else -> emptyList()
        }
    val products =
        when (val p = mainUi.products) {
            is LoadState.Ok -> p.data
            else -> emptyList()
        }
    var globalRate by remember(ui.commissionRules) {
        mutableStateOf(
            ui.commissionRules.firstOrNull { it.ruleType == "GLOBAL" }?.rate?.toDouble()?.toString() ?: "8",
        )
    }
    var dealerRate by remember { mutableStateOf("") }
    var productRate by remember { mutableStateOf("") }
    var selectedDealerId by remember { mutableStateOf(dealers.firstOrNull()?.id.orEmpty()) }
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id.orEmpty()) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FmSectionLabel("Global commission %")
        OutlinedTextField(
            value = globalRate,
            onValueChange = { globalRate = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Rate %") },
            modifier = Modifier.fillMaxWidth(),
        )
        FmButton(text = "Save global commission", onClick = {
            globalRate.toDoubleOrNull()?.let { financeViewModel.updateGlobalCommission(it) }
        })

        if (dealers.isNotEmpty()) {
            FmSectionLabel("Dealer-specific commission")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                dealers.forEach { d ->
                    FilterChip(
                        selected = selectedDealerId == d.id,
                        onClick = { selectedDealerId = d.id },
                        label = { Text(d.name) },
                    )
                }
            }
            OutlinedTextField(
                value = dealerRate,
                onValueChange = { dealerRate = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Dealer rate %") },
                modifier = Modifier.fillMaxWidth(),
            )
            FmButton(
                text = "Save dealer commission",
                enabled = selectedDealerId.isNotBlank() && dealerRate.toDoubleOrNull() != null,
                onClick = {
                    dealerRate.toDoubleOrNull()?.let { financeViewModel.updateDealerCommission(selectedDealerId, it) }
                },
            )
        }

        if (products.isNotEmpty()) {
            FmSectionLabel("Product-specific commission")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                products.take(20).forEach { p ->
                    FilterChip(
                        selected = selectedProductId == p.id,
                        onClick = { selectedProductId = p.id },
                        label = { Text(p.name, maxLines = 1) },
                    )
                }
            }
            OutlinedTextField(
                value = productRate,
                onValueChange = { productRate = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Product rate %") },
                modifier = Modifier.fillMaxWidth(),
            )
            FmButton(
                text = "Save product commission",
                enabled = selectedProductId.isNotBlank() && productRate.toDoubleOrNull() != null,
                onClick = {
                    productRate.toDoubleOrNull()?.let { financeViewModel.updateProductCommission(selectedProductId, it) }
                },
            )
        }

        FmButton(text = "Backfill revenue from delivered orders", onClick = { financeViewModel.backfillRevenues() })
        FmSectionLabel("Active rules")
        ui.commissionRules.forEach { rule ->
            FmCard {
                Text("${rule.ruleType} · ${rule.rate}%", fontWeight = FontWeight.SemiBold)
                rule.dealer?.name?.let { Text("Dealer: $it", fontSize = 12.sp, color = WholesaleMuted) }
                rule.product?.name?.let { Text("Product: $it", fontSize = 12.sp, color = WholesaleMuted) }
            }
        }
    }
}

@Composable
private fun FinanceDealersSection(mainUi: MainUiState, onOpenDealer: (String) -> Unit) {
    val dealers =
        when (val u = mainUi.users) {
            is LoadState.Ok -> u.data.filter { it.role.equals("DEALER", true) }
            else -> emptyList()
        }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        FmSectionLabel("Dealer performance")
        if (dealers.isEmpty()) {
            FmEmptyState(
                icon = Icons.Outlined.Store,
                title = "No dealers",
                message = "Onboard dealers to track sales and settlements",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(dealers, key = { it.id }) { d ->
                    FmCard(modifier = Modifier.fillMaxWidth().clickable { onOpenDealer(d.id) }) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(d.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(d.email, fontSize = 12.sp, color = WholesaleMuted)
                            Text("Tap for daily sales & settlement KPIs", fontSize = 12.sp, color = WholesaleGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinanceReportsSection(ui: FinanceUiState, financeViewModel: FinanceViewModel) {
    var reportType by remember { mutableStateOf("settlements") }
    var format by remember { mutableStateOf("csv") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FmSectionLabel("Report type")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf(
                "settlements" to "Settlements",
                "revenue" to "Revenue",
                "collections" to "Collections",
                "return-requests" to "Return requests",
                "refund-history" to "Refund history",
                "pending-refunds" to "Pending refunds",
                "completed-refunds" to "Completed refunds",
            ).forEach { (id, label) ->
                FilterChip(selected = reportType == id, onClick = { reportType = id }, label = { Text(label) })
            }
        }
        FmSectionLabel("Format")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = format == "csv", onClick = { format = "csv" }, label = { Text("CSV") })
            FilterChip(selected = format == "xlsx", onClick = { format = "xlsx" }, label = { Text("Excel") })
        }
        Text("Period: ${ui.period.replaceFirstChar { it.uppercase() }}", fontSize = 12.sp, color = WholesaleMuted)
        FmButton(
            text = if (ui.loading) "Downloading…" else "Download & share report",
            enabled = !ui.loading,
            onClick = { financeViewModel.downloadReport(reportType, format) },
        )
    }
}

@Composable
private fun FinanceAuditSection(ui: FinanceUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        FmSectionLabel("Audit trail")
        if (ui.auditLogs.isEmpty()) {
            FmEmptyState(
                icon = Icons.Outlined.History,
                title = "No audit logs yet",
                message = "Financial actions will appear here automatically",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ui.auditLogs.take(50), key = { it.id }) { log ->
                    FmCard {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(log.action.replace('_', ' '), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${log.entityType} · ${log.entityId.take(8)}…", fontSize = 12.sp, color = WholesaleMuted)
                            Text(log.createdAt.take(19).replace('T', ' '), fontSize = 11.sp, color = WholesaleMuted)
                            log.actor?.name?.let { Text("By $it", fontSize = 11.sp, color = WholesaleMuted) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDealerPerformanceScreen(
    financeViewModel: FinanceViewModel,
    dealerId: String,
    dealerName: String,
    onBack: () -> Unit,
) {
    val ui by financeViewModel.uiState.collectAsState()
    LaunchedEffect(dealerId) { financeViewModel.loadDealerPerformance(dealerId) }
    val perf = ui.dealerPerformance
    val summary = perf?.summary
    val trend = perf?.dailySales.orEmpty().map { it.gmv.toFloat() }

    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(dealerName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Dealer performance", fontSize = 13.sp, color = WholesaleMuted)
        ui.error?.let { FmErrorBanner(it) }
        if (summary == null && ui.loading) {
            Text("Loading…", color = WholesaleMuted)
        } else if (summary != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FmStatCard("Delivered", "${summary.ordersDelivered}", modifier = Modifier.weight(1f))
                FmStatCard("Gross sales", "₹${formatDecimal(summary.grossSales)}", modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FmStatCard("Commission", "₹${formatDecimal(summary.commissionDeducted)}", modifier = Modifier.weight(1f))
                FmStatCard("Dealer earnings", "₹${formatDecimal(summary.dealerEarnings)}", modifier = Modifier.weight(1f))
            }
            FmStatCard("Pending settlement", "₹${formatDecimal(summary.pendingSettlement)}", modifier = Modifier.fillMaxWidth())
            FmSectionLabel("Daily sales")
            FmCard {
                if (trend.isEmpty()) {
                    Text("No sales data", color = WholesaleMuted, fontSize = 13.sp)
                } else {
                    FmMiniBarChart(trend, modifier = Modifier.fillMaxWidth().height(80.dp))
                }
            }
            if (perf?.topProducts.orEmpty().isNotEmpty()) {
                FmSectionLabel("Top products")
                perf?.topProducts?.forEach { p ->
                    FmCard {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(p.name, fontWeight = FontWeight.SemiBold)
                            Text("₹${formatDecimal(p.revenue)} · ${p.qty} qty", fontSize = 12.sp, color = WholesaleMuted)
                        }
                    }
                }
            }
        }
        FmButton(text = "Back", onClick = onBack)
    }
}

@Composable
fun AdminSettlementDetailScreen(
    financeViewModel: FinanceViewModel,
    settlementId: String,
    onBack: () -> Unit,
) {
    val ui by financeViewModel.uiState.collectAsState()
    var amount by remember { mutableStateOf("") }
    var utr by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("NEFT") }

    LaunchedEffect(settlementId) { financeViewModel.loadSettlement(settlementId) }
    val s = ui.selectedSettlement

    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settlement detail", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        ui.error?.let { FmErrorBanner(it) }
        if (s == null) {
            Text("Loading…", color = WholesaleMuted)
            return@Column
        }
        FmCard {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(s.settlementCode, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(s.dealer?.shopName ?: s.dealer?.name.orEmpty(), color = WholesaleMuted)
                Text("Period: ${s.settlementStartDate.take(10)} → ${s.settlementEndDate.take(10)}")
                Text("Orders: ${s.totalOrders} · Qty: ${s.totalQuantity}")
                Text("Gross ₹${formatDecimal(s.grossSales.toDouble())} · GST ₹${formatDecimal(s.gstAmount.toDouble())}")
                Text("Commission ₹${formatDecimal(s.commissionAmount.toDouble())}")
                Text("Dealer payable ₹${formatDecimal(s.dealerPayable.toDouble())}")
                Text("Settled ₹${formatDecimal(s.settledAmount.toDouble())} · Balance ₹${formatDecimal(s.balanceAmount.toDouble())}")
                Text("Status: ${s.settlementStatus}", color = WholesaleGreen)
            }
        }
        if (s.payments.isNotEmpty()) {
            FmSectionLabel("Payment history")
            s.payments.forEach { p ->
                FmCard {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("₹${formatDecimal(p.amount.toDouble())} · ${p.paymentMethod}", fontWeight = FontWeight.SemiBold)
                        p.utrNumber?.let { Text("UTR: $it", fontSize = 12.sp, color = WholesaleMuted) }
                        p.transactionReference?.let { Text("Ref: $it", fontSize = 12.sp, color = WholesaleMuted) }
                        Text(p.paymentDate.take(10), fontSize = 11.sp, color = WholesaleMuted)
                    }
                }
            }
        }
        if (s.balanceAmount.toDouble() > 0.01) {
            FmSectionLabel("Record payment")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("NEFT", "UPI", "RTGS", "BANK_TRANSFER").forEach { m ->
                    FilterChip(
                        selected = paymentMethod == m,
                        onClick = { paymentMethod = m },
                        label = { Text(m.replace('_', ' ')) },
                    )
                }
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = utr, onValueChange = { utr = it }, label = { Text("UTR number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text("Transaction reference") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (optional)") }, modifier = Modifier.fillMaxWidth())
            FmButton(
                text = "Mark payment",
                enabled = amount.toDoubleOrNull() != null && (utr.isNotBlank() || reference.isNotBlank()),
                onClick = {
                    financeViewModel.recordPayment(
                        settlementId,
                        amount.toDouble(),
                        paymentMethod,
                        utr,
                        reference,
                        remarks,
                    ) { onBack() }
                },
            )
        }
        FmButton(text = "Back", onClick = onBack)
    }
}
