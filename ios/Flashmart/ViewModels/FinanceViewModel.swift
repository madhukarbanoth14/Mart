import Foundation
import Observation

@Observable
final class FinanceViewModel {
    var loading = false
    var error: String?
    var message: String?
    var period = "month"
    var settlementStatusFilter: String?
    var overview: FinanceOverview?
    var investor: InvestorDashboard?
    var settlements: [DealerSettlement] = []
    var selectedSettlement: DealerSettlement?
    var commissionRules: [CommissionRule] = []
    var auditLogs: [FinanceAuditLog] = []
    var dealerPerformance: DealerPerformance?
    var reportShareURL: URL?

    private let apiClient: MartAPIClient
    private let isoDay: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withFullDate]
        return f
    }()

    init(apiClient: MartAPIClient) {
        self.apiClient = apiClient
    }

    @MainActor
    func setPeriod(_ value: String) {
        period = value
        Task { await refresh() }
    }

    @MainActor
    func setSettlementStatusFilter(_ value: String?) {
        settlementStatusFilter = value
        Task { await refreshSettlements() }
    }

    @MainActor
    func refresh() async {
        loading = true
        error = nil
        do {
            async let overviewTask = apiClient.financeOverview(period: period)
            async let investorTask = apiClient.investorDashboard()
            async let settlementsTask = apiClient.settlements(period: period, status: settlementStatusFilter)
            async let rulesTask = apiClient.commissionRules()
            async let auditTask = apiClient.financeAudit()
            overview = try await overviewTask
            investor = try await investorTask
            settlements = try await settlementsTask
            commissionRules = try await rulesTask
            auditLogs = try await auditTask
            loading = false
        } catch {
            loading = false
            self.error = error.localizedDescription
        }
    }

    @MainActor
    private func refreshSettlements() async {
        do {
            settlements = try await apiClient.settlements(period: period, status: settlementStatusFilter)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func generateSettlement(dealerId: String) async {
        loading = true
        error = nil
        let end = Date()
        let start = Calendar.current.date(byAdding: .day, value: -30, to: end) ?? end
        do {
            _ = try await apiClient.generateSettlement(
                GenerateSettlementRequest(
                    dealerId: dealerId,
                    startDate: isoDay.string(from: start),
                    endDate: isoDay.string(from: end)
                )
            )
            message = "Settlement generated"
            await refresh()
        } catch {
            loading = false
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func loadSettlement(_ id: String) async {
        do {
            selectedSettlement = try await apiClient.settlementDetail(id)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func loadDealerPerformance(_ dealerId: String) async {
        loading = true
        error = nil
        dealerPerformance = nil
        do {
            dealerPerformance = try await apiClient.dealerPerformance(dealerId, period: period)
            loading = false
        } catch {
            loading = false
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func recordPayment(
        settlementId: String,
        amount: Double,
        method: String,
        utr: String,
        reference: String,
        remarks: String
    ) async -> Bool {
        loading = true
        error = nil
        do {
            let updated = try await apiClient.recordSettlementPayment(
                settlementId,
                body: RecordSettlementPaymentRequest(
                    amount: amount,
                    paymentMethod: method,
                    utrNumber: utr.isEmpty ? nil : utr,
                    transactionReference: reference.isEmpty ? nil : reference,
                    paymentDate: isoDay.string(from: Date()),
                    remarks: remarks.isEmpty ? nil : remarks
                )
            )
            selectedSettlement = updated
            message = "Payment recorded"
            await refresh()
            return true
        } catch {
            loading = false
            self.error = error.localizedDescription
            return false
        }
    }

    @MainActor
    func updateGlobalCommission(rate: Double) async {
        await upsertRule(
            UpsertCommissionRuleRequest(ruleType: "GLOBAL", rate: rate, dealerId: nil, productId: nil),
            message: "Global commission updated to \(rate)%"
        )
    }

    @MainActor
    func updateDealerCommission(dealerId: String, rate: Double) async {
        await upsertRule(
            UpsertCommissionRuleRequest(ruleType: "DEALER", rate: rate, dealerId: dealerId, productId: nil),
            message: "Dealer commission updated to \(rate)%"
        )
    }

    @MainActor
    func updateProductCommission(productId: String, rate: Double) async {
        await upsertRule(
            UpsertCommissionRuleRequest(ruleType: "PRODUCT", rate: rate, dealerId: nil, productId: productId),
            message: "Product commission updated to \(rate)%"
        )
    }

    @MainActor
    private func upsertRule(_ body: UpsertCommissionRuleRequest, message: String) async {
        do {
            _ = try await apiClient.upsertCommissionRule(body)
            self.message = message
            await refresh()
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func backfillRevenues() async {
        do {
            let res = try await apiClient.backfillRevenues()
            message = "Backfilled \(res.created ?? 0) revenue records"
            await refresh()
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func downloadReport(type: String, format: String) async {
        loading = true
        error = nil
        do {
            reportShareURL = try await apiClient.downloadFinanceReport(type: type, format: format, period: period)
            message = "Report ready to share"
            loading = false
        } catch {
            loading = false
            self.error = error.localizedDescription
        }
    }

    func clearReportShareURL() {
        reportShareURL = nil
    }

    func clearMessage() {
        message = nil
    }
}
