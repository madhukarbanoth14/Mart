import SwiftUI

struct DealerRevenueView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        let vm = env.returnsViewModel
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                FMSectionLabel(title: "Revenue dashboard")
                if let err = vm.error { FMErrorBanner(text: err) }
                if let s = vm.dealerDashboard?.summary {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 8) {
                        FMStatTile(label: "Today", value: fmt(s.todayRevenue))
                        FMStatTile(label: "Week", value: fmt(s.weeklyRevenue))
                        FMStatTile(label: "Month", value: fmt(s.monthlyRevenue))
                        FMStatTile(label: "Pending settlement", value: fmt(s.pendingSettlementAmount))
                        FMStatTile(label: "Orders", value: "\(s.totalOrdersReceived ?? 0)")
                        FMStatTile(label: "Delivered", value: "\(s.totalOrdersDelivered ?? 0)")
                    }
                }
                Button { path.append(AppRoute.dealerShopkeepers) } label: {
                    FMCard {
                        HStack {
                            Text("Shopkeeper revenue").font(.system(size: 15, weight: .bold))
                            Spacer()
                            Image(systemName: "chevron.right")
                        }
                    }
                }.buttonStyle(.plain)
                HStack {
                    FMButton(title: "Export revenue", variant: .outline) {
                        Task { await vm.downloadDealerReport("revenue") }
                    }
                    FMButton(title: "Export returns", variant: .outline) {
                        Task { await vm.downloadDealerReport("returns") }
                    }
                }
            }.padding(16)
        }
        .background(FMTheme.bg)
        .navigationTitle("Revenue")
        .task { await vm.refreshDealerDashboard() }
    }

    private func fmt(_ n: Double?) -> String {
        String(format: "₹%.0f", n ?? 0)
    }
}

struct DealerShopkeepersRevenueView: View {
    @Environment(AppEnvironment.self) private var env

    var body: some View {
        let vm = env.returnsViewModel
        List(vm.shopkeeperRows) { row in
            VStack(alignment: .leading, spacing: 4) {
                Text(row.name).font(.headline)
                Text(row.area ?? "—").font(.caption).foregroundStyle(FMTheme.ink3)
                Text("Orders: \(row.totalOrders ?? 0) · Revenue: \(fmt(row.totalRevenue))")
                Text("Outstanding: \(row.outstandingOrders ?? 0) · Returns: \(row.returnedOrders ?? 0)")
            }
        }
        .navigationTitle("Shopkeepers")
        .task { await vm.refreshShopkeepers() }
    }

    private func fmt(_ n: Double?) -> String { String(format: "₹%.0f", n ?? 0) }
}

struct DealerReturnsListView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        let vm = env.returnsViewModel
        List(vm.returns) { row in
            Button { path.append(AppRoute.dealerReturnDetail(row.id)) } label: {
                VStack(alignment: .leading) {
                    Text(row.returnCode).font(.headline)
                    Text("\(row.status) · ₹\(Int(row.refundAmount ?? 0))").font(.caption)
                }
            }
        }
        .navigationTitle("Returns")
        .task { await vm.refreshReturns() }
    }
}

struct DealerReturnDetailView: View {
    @Environment(AppEnvironment.self) private var env
    let returnId: String
    @State private var remarks = ""

    var body: some View {
        let vm = env.returnsViewModel
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let row = vm.selectedReturn {
                    FMCard {
                        Text(row.returnCode).font(.headline)
                        Text("Status: \(row.status)")
                        Text("Amount: ₹\(Int(row.refundAmount ?? 0))")
                        ForEach(row.items ?? []) { item in
                            Text("• \(item.productName) × \(item.quantity)")
                        }
                    }
                    TextField("Remarks", text: $remarks)
                        .textFieldStyle(.roundedBorder)
                    if row.status == "REQUESTED" || row.status == "UNDER_REVIEW" {
                        FMButton(title: "Approve") { Task { await vm.approveReturn(returnId, remarks: remarks) } }
                        FMButton(title: "Reject", variant: .outline) { Task { await vm.rejectReturn(returnId, remarks: remarks) } }
                    }
                    if row.status == "APPROVED" && row.refundRequest == nil {
                        FMButton(title: "Raise refund to admin") { Task { await vm.raiseRefund(returnId, remarks: remarks) } }
                    }
                }
            }.padding(16)
        }
        .navigationTitle("Return")
        .task { await vm.loadReturn(returnId) }
    }
}

struct AdminRefundsListView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath

    var body: some View {
        let vm = env.returnsViewModel
        List(vm.refunds) { refund in
            Button { path.append(AppRoute.adminRefundDetail(refund.id)) } label: {
                VStack(alignment: .leading) {
                    Text(refund.refundCode).font(.headline)
                    Text("\(refund.status) · ₹\(Int(refund.amount ?? 0))").font(.caption)
                }
            }
        }
        .task { await vm.refreshRefunds() }
    }
}

struct AdminRefundDetailView: View {
    @Environment(AppEnvironment.self) private var env
    let refundId: String
    @State private var method = "UPI"
    @State private var reference = ""
    @State private var remarks = ""

    var body: some View {
        let vm = env.returnsViewModel
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let refund = vm.selectedRefund {
                    FMCard {
                        Text(refund.refundCode).font(.headline)
                        Text("Status: \(refund.status)")
                        Text("Amount: ₹\(Int(refund.amount ?? 0))")
                    }
                    if refund.status == "PENDING" {
                        FMButton(title: "Approve") { Task { await vm.approveRefund(refundId, remarks: remarks) } }
                        FMButton(title: "Reject", variant: .outline) { Task { await vm.rejectRefund(refundId, remarks: remarks) } }
                    }
                    if refund.status == "PENDING" || refund.status == "PROCESSING" {
                        TextField("Method", text: $method).textFieldStyle(.roundedBorder)
                        TextField("Transaction reference", text: $reference).textFieldStyle(.roundedBorder)
                        TextField("Remarks", text: $remarks).textFieldStyle(.roundedBorder)
                        FMButton(title: "Process refund") {
                            Task { await vm.processRefund(refundId, method: method.uppercased(), reference: reference, remarks: remarks) }
                        }
                    }
                }
            }.padding(16)
        }
        .navigationTitle("Refund")
        .task { await vm.loadRefund(refundId) }
    }
}
