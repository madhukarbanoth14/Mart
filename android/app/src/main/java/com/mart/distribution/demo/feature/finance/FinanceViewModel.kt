package com.mart.distribution.demo.feature.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.CommissionRuleDto
import com.mart.distribution.demo.data.api.dto.DealerPerformanceDto
import com.mart.distribution.demo.data.api.dto.DealerSettlementDto
import com.mart.distribution.demo.data.api.dto.FinanceAuditLogDto
import com.mart.distribution.demo.data.api.dto.FinanceOverviewDto
import com.mart.distribution.demo.data.api.dto.GenerateSettlementRequest
import com.mart.distribution.demo.data.api.dto.InvestorDashboardDto
import com.mart.distribution.demo.data.api.dto.RecordSettlementPaymentRequest
import com.mart.distribution.demo.data.api.dto.UpsertCommissionRuleRequest
import com.mart.distribution.demo.util.ApiErrorMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FinanceReportDownload(
    val bytes: ByteArray,
    val filename: String,
    val mimeType: String,
)

data class FinanceUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val period: String = "month",
    val settlementStatusFilter: String? = null,
    val overview: FinanceOverviewDto? = null,
    val investor: InvestorDashboardDto? = null,
    val settlements: List<DealerSettlementDto> = emptyList(),
    val selectedSettlement: DealerSettlementDto? = null,
    val commissionRules: List<CommissionRuleDto> = emptyList(),
    val auditLogs: List<FinanceAuditLogDto> = emptyList(),
    val dealerPerformance: DealerPerformanceDto? = null,
    val message: String? = null,
    val reportDownload: FinanceReportDownload? = null,
)

class FinanceViewModel(
    private val martApi: MartApi,
) : ViewModel() {
    private val _ui = MutableStateFlow(FinanceUiState())
    val uiState: StateFlow<FinanceUiState> = _ui.asStateFlow()

    fun setPeriod(period: String) {
        _ui.update { it.copy(period = period) }
        refresh()
    }

    fun setSettlementStatusFilter(status: String?) {
        _ui.update { it.copy(settlementStatusFilter = status) }
        refreshSettlements()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val period = _ui.value.period
                val status = _ui.value.settlementStatusFilter
                val overview = martApi.financeOverview(period = period)
                val investor = martApi.investorDashboard()
                val settlements = martApi.settlements(period = period, status = status)
                val rules = martApi.commissionRules()
                val audit = martApi.financeAudit()
                _ui.update {
                    it.copy(
                        loading = false,
                        overview = overview,
                        investor = investor,
                        settlements = settlements,
                        commissionRules = rules,
                        auditLogs = audit,
                    )
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Could not load finance data"))
                }
            }
        }
    }

    private fun refreshSettlements() {
        viewModelScope.launch {
            try {
                val period = _ui.value.period
                val status = _ui.value.settlementStatusFilter
                val settlements = martApi.settlements(period = period, status = status)
                _ui.update { it.copy(settlements = settlements) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e, "Could not load settlements")) }
            }
        }
    }

    fun generateSettlement(dealerId: String, onDone: () -> Unit = {}) {
        val end = LocalDate.now()
        val start = end.minusDays(30)
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                martApi.generateSettlement(
                    GenerateSettlementRequest(
                        dealerId = dealerId,
                        startDate = start.toString(),
                        endDate = end.toString(),
                    ),
                )
                refresh()
                _ui.update { it.copy(message = "Settlement generated") }
                onDone()
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Could not generate settlement"))
                }
            }
        }
    }

    fun loadSettlement(id: String) {
        viewModelScope.launch {
            try {
                val detail = martApi.settlementDetail(id)
                _ui.update { it.copy(selectedSettlement = detail) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e, "Could not load settlement")) }
            }
        }
    }

    fun loadDealerPerformance(dealerId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, dealerPerformance = null) }
            try {
                val perf = martApi.dealerPerformance(dealerId, period = _ui.value.period)
                _ui.update { it.copy(loading = false, dealerPerformance = perf) }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Could not load dealer performance"))
                }
            }
        }
    }

    fun recordPayment(
        settlementId: String,
        amount: Double,
        method: String,
        utr: String,
        reference: String,
        remarks: String,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val updated =
                    martApi.recordSettlementPayment(
                        settlementId,
                        RecordSettlementPaymentRequest(
                            amount = amount,
                            paymentMethod = method,
                            utrNumber = utr.ifBlank { null },
                            transactionReference = reference.ifBlank { null },
                            paymentDate = LocalDate.now().toString(),
                            remarks = remarks.ifBlank { null },
                        ),
                    )
                _ui.update { it.copy(selectedSettlement = updated, message = "Payment recorded", loading = false) }
                refresh()
                onDone()
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Could not record payment"))
                }
            }
        }
    }

    fun updateGlobalCommission(rate: Double) {
        upsertRule(UpsertCommissionRuleRequest(ruleType = "GLOBAL", rate = rate), "Global commission updated to $rate%")
    }

    fun updateDealerCommission(dealerId: String, rate: Double) {
        upsertRule(
            UpsertCommissionRuleRequest(ruleType = "DEALER", rate = rate, dealerId = dealerId),
            "Dealer commission updated to $rate%",
        )
    }

    fun updateProductCommission(productId: String, rate: Double) {
        upsertRule(
            UpsertCommissionRuleRequest(ruleType = "PRODUCT", rate = rate, productId = productId),
            "Product commission updated to $rate%",
        )
    }

    private fun upsertRule(body: UpsertCommissionRuleRequest, successMessage: String) {
        viewModelScope.launch {
            try {
                martApi.upsertCommissionRule(body)
                refresh()
                _ui.update { it.copy(message = successMessage) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e, "Could not update commission")) }
            }
        }
    }

    fun backfillRevenues() {
        viewModelScope.launch {
            try {
                val res = martApi.backfillRevenues()
                _ui.update { it.copy(message = "Backfilled ${res["created"] ?: 0} revenue records") }
                refresh()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e, "Backfill failed")) }
            }
        }
    }

    fun downloadReport(type: String, format: String) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val body = martApi.downloadReport(type = type, format = format, period = _ui.value.period)
                val bytes = body.bytes()
                val ext = if (format == "xlsx") "xlsx" else "csv"
                val mime = if (format == "xlsx") "application/vnd.ms-excel" else "text/csv"
                _ui.update {
                    it.copy(
                        loading = false,
                        reportDownload = FinanceReportDownload(bytes, "$type-report.$ext", mime),
                        message = "Report ready to share",
                    )
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e, "Could not download report"))
                }
            }
        }
    }

    fun clearReportDownload() {
        _ui.update { it.copy(reportDownload = null) }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null) }
    }
}
