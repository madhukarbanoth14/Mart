import Foundation
import Observation

@Observable
final class ReturnsViewModel {
    var loading = false
    var error: String?
    var message: String?
    var period = "month"
    var returnStatusFilter: String?
    var refundStatusFilter: String?
    var returns: [ReturnRequest] = []
    var refunds: [RefundRequest] = []
    var selectedReturn: ReturnRequest?
    var selectedRefund: RefundRequest?
    var dealerDashboard: DealerRevenueDashboard?
    var shopkeeperRows: [DealerShopkeeperRow] = []
    var shopkeeperSearch = ""
    var areaFilter = ""
    var reportShareURL: URL?

    private let apiClient: MartAPIClient

    init(apiClient: MartAPIClient) {
        self.apiClient = apiClient
    }

    @MainActor
    func refreshReturns() async {
        loading = true
        error = nil
        do {
            returns = try await apiClient.listReturns(status: returnStatusFilter)
            loading = false
        } catch {
            loading = false
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func refreshRefunds() async {
        do {
            refunds = try await apiClient.listRefunds(status: refundStatusFilter)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func refreshDealerDashboard() async {
        loading = true
        error = nil
        do {
            dealerDashboard = try await apiClient.dealerRevenueDashboard(period: period)
            loading = false
        } catch {
            loading = false
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func refreshShopkeepers() async {
        do {
            let q = shopkeeperSearch.trimmingCharacters(in: .whitespaces)
            let a = areaFilter.trimmingCharacters(in: .whitespaces)
            shopkeeperRows = try await apiClient.dealerShopkeeperRevenue(
                shopkeeper: q.isEmpty ? nil : q,
                area: a.isEmpty ? nil : a
            )
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func loadReturn(_ id: String) async {
        do {
            selectedReturn = try await apiClient.getReturn(id)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func loadRefund(_ id: String) async {
        do {
            selectedRefund = try await apiClient.getRefund(id)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func approveReturn(_ id: String, remarks: String? = nil) async {
        do {
            _ = try await apiClient.approveReturnRequest(id, remarks: remarks)
            message = "Return approved"
            await refreshReturns()
            await loadReturn(id)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func rejectReturn(_ id: String, remarks: String?) async {
        do {
            _ = try await apiClient.rejectReturnRequest(id, remarks: remarks)
            message = "Return rejected"
            await refreshReturns()
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func raiseRefund(_ returnId: String, remarks: String? = nil) async {
        do {
            _ = try await apiClient.raiseRefundRequest(returnId, remarks: remarks)
            message = "Refund request sent to admin"
            await refreshReturns()
            await refreshRefunds()
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func approveRefund(_ id: String, remarks: String? = nil) async {
        do {
            _ = try await apiClient.approveRefundRequest(id, remarks: remarks)
            message = "Refund approved"
            await refreshRefunds()
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func rejectRefund(_ id: String, remarks: String?) async {
        do {
            _ = try await apiClient.rejectRefundRequest(id, remarks: remarks)
            message = "Refund rejected"
            await refreshRefunds()
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func processRefund(_ id: String, method: String, reference: String, remarks: String?) async {
        do {
            _ = try await apiClient.processRefundRequest(
                id,
                body: ProcessRefundRequest(refundMethod: method, transactionReference: reference, remarks: remarks)
            )
            message = "Refund processed"
            await refreshRefunds()
            await loadRefund(id)
        } catch {
            self.error = error.localizedDescription
        }
    }

    @MainActor
    func downloadDealerReport(_ type: String) async {
        do {
            reportShareURL = try await apiClient.downloadDealerReport(type: type, format: "xlsx", period: period)
        } catch {
            self.error = error.localizedDescription
        }
    }
}
