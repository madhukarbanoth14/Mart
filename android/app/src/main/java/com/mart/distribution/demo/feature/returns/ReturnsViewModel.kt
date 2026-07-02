package com.mart.distribution.demo.feature.returns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.CreateReturnItemRequest
import com.mart.distribution.demo.data.api.dto.CreateReturnRequest
import com.mart.distribution.demo.data.api.dto.DealerRevenueDashboardDto
import com.mart.distribution.demo.data.api.dto.DealerShopkeeperRowDto
import com.mart.distribution.demo.data.api.dto.ProcessRefundRequest
import com.mart.distribution.demo.data.api.dto.RaiseRefundRequest
import com.mart.distribution.demo.data.api.dto.RefundRequestDto
import com.mart.distribution.demo.data.api.dto.ReturnActionRequest
import com.mart.distribution.demo.data.api.dto.ReturnRequestDto
import com.mart.distribution.demo.util.ApiErrorMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReturnsReportDownload(
    val bytes: ByteArray,
    val filename: String,
    val mimeType: String,
)

data class ReturnsUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val period: String = "month",
    val returnStatusFilter: String? = null,
    val refundStatusFilter: String? = null,
    val returns: List<ReturnRequestDto> = emptyList(),
    val refunds: List<RefundRequestDto> = emptyList(),
    val selectedReturn: ReturnRequestDto? = null,
    val selectedRefund: RefundRequestDto? = null,
    val dealerDashboard: DealerRevenueDashboardDto? = null,
    val shopkeeperRows: List<DealerShopkeeperRowDto> = emptyList(),
    val shopkeeperSearch: String = "",
    val areaFilter: String = "",
    val reportDownload: ReturnsReportDownload? = null,
)

class ReturnsViewModel(
    private val martApi: MartApi,
) : ViewModel() {
    private val _ui = MutableStateFlow(ReturnsUiState())
    val uiState: StateFlow<ReturnsUiState> = _ui.asStateFlow()

    fun setPeriod(period: String) {
        _ui.update { it.copy(period = period) }
        refreshDealerDashboard()
    }

    fun setReturnStatusFilter(status: String?) {
        _ui.update { it.copy(returnStatusFilter = status) }
        refreshReturns()
    }

    fun setRefundStatusFilter(status: String?) {
        _ui.update { it.copy(refundStatusFilter = status) }
        refreshRefunds()
    }

    fun setShopkeeperSearch(q: String) {
        _ui.update { it.copy(shopkeeperSearch = q) }
        refreshShopkeepers()
    }

    fun setAreaFilter(area: String) {
        _ui.update { it.copy(areaFilter = area) }
        refreshShopkeepers()
    }

    fun refreshAll() {
        refreshReturns()
        refreshRefunds()
        refreshDealerDashboard()
        refreshShopkeepers()
    }

    fun refreshReturns() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val list = martApi.listReturns(status = _ui.value.returnStatusFilter)
                _ui.update { it.copy(loading = false, returns = list) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun refreshRefunds() {
        viewModelScope.launch {
            try {
                val list = martApi.listRefunds(status = _ui.value.refundStatusFilter)
                _ui.update { it.copy(refunds = list) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun refreshDealerDashboard() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val dash = martApi.dealerRevenueDashboard(period = _ui.value.period)
                _ui.update { it.copy(loading = false, dealerDashboard = dash) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun refreshShopkeepers() {
        viewModelScope.launch {
            try {
                val q = _ui.value.shopkeeperSearch.trim().ifBlank { null }
                val area = _ui.value.areaFilter.trim().ifBlank { null }
                val rows = martApi.dealerShopkeeperRevenue(shopkeeper = q, area = area)
                _ui.update { it.copy(shopkeeperRows = rows) }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun loadReturn(id: String) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val row = martApi.getReturn(id)
                _ui.update { it.copy(loading = false, selectedReturn = row) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun loadRefund(id: String) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            try {
                val row = martApi.getRefund(id)
                _ui.update { it.copy(loading = false, selectedRefund = row) }
            } catch (e: Exception) {
                _ui.update { it.copy(loading = false, error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun createReturn(
        orderId: String,
        reason: String,
        reasonText: String?,
        comments: String?,
        items: List<CreateReturnItemRequest>,
        onDone: () -> Unit = {},
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(error = null, message = null) }
            try {
                martApi.createReturn(
                    orderId,
                    CreateReturnRequest(
                        reason = reason,
                        reasonText = reasonText,
                        comments = comments,
                        items = items,
                    ),
                )
                _ui.update { it.copy(message = "Return request submitted") }
                refreshReturns()
                onDone()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun approveReturn(id: String, remarks: String? = null) {
        viewModelScope.launch {
            try {
                martApi.approveReturn(id, ReturnActionRequest(remarks))
                _ui.update { it.copy(message = "Return approved") }
                refreshReturns()
                loadReturn(id)
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun rejectReturn(id: String, remarks: String?) {
        viewModelScope.launch {
            try {
                martApi.rejectReturn(id, ReturnActionRequest(remarks))
                _ui.update { it.copy(message = "Return rejected") }
                refreshReturns()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun raiseRefundRequest(returnId: String, remarks: String? = null) {
        viewModelScope.launch {
            try {
                martApi.raiseRefundRequest(returnId, RaiseRefundRequest(remarks))
                _ui.update { it.copy(message = "Refund request sent to admin") }
                refreshReturns()
                refreshRefunds()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun approveRefund(id: String, remarks: String? = null) {
        viewModelScope.launch {
            try {
                martApi.approveRefund(id, ReturnActionRequest(remarks))
                _ui.update { it.copy(message = "Refund approved") }
                refreshRefunds()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun rejectRefund(id: String, remarks: String?) {
        viewModelScope.launch {
            try {
                martApi.rejectRefund(id, ReturnActionRequest(remarks))
                _ui.update { it.copy(message = "Refund rejected") }
                refreshRefunds()
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun processRefund(
        id: String,
        method: String,
        transactionReference: String,
        remarks: String? = null,
    ) {
        viewModelScope.launch {
            try {
                martApi.processRefund(
                    id,
                    ProcessRefundRequest(
                        refundMethod = method,
                        transactionReference = transactionReference,
                        remarks = remarks,
                    ),
                )
                _ui.update { it.copy(message = "Refund processed") }
                refreshRefunds()
                loadRefund(id)
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun downloadDealerReport(type: String) {
        viewModelScope.launch {
            try {
                val body = martApi.downloadDealerReport(type, "xlsx", period = _ui.value.period)
                val bytes = body.bytes()
                _ui.update {
                    it.copy(
                        reportDownload = ReturnsReportDownload(
                            bytes = bytes,
                            filename = "$type-report.xlsx",
                            mimeType = "application/vnd.ms-excel",
                        ),
                    )
                }
            } catch (e: Exception) {
                _ui.update { it.copy(error = ApiErrorMessages.fromThrowable(e)) }
            }
        }
    }

    fun downloadAdminReport(type: String, financeDownload: (String) -> Unit) {
        financeDownload(type)
    }

    fun clearReportDownload() {
        _ui.update { it.copy(reportDownload = null) }
    }

    fun clearMessage() {
        _ui.update { it.copy(message = null, error = null) }
    }
}
