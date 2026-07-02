import SwiftUI

private enum FinanceSection: String, CaseIterable {
    case overview = "Overview"
    case settlements = "Settlements"
    case dealers = "Dealers"
    case investor = "Investor"
    case commission = "Commission"
    case reports = "Reports"
    case audit = "Audit"
    case refunds = "Refunds"
}

struct AdminFinanceView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var section: FinanceSection = .overview
    @State private var selectedDealerId: String?
    @State private var globalRate = "8"
    @State private var dealerRate = ""
    @State private var productRate = ""
    @State private var selectedProductId: String?
    @State private var reportType = "settlements"
    @State private var reportFormat = "csv"
    @State private var showReportShare = false

    var body: some View {
        FMScreen(showNav: true) {
            VStack(alignment: .leading, spacing: 12) {
                FMTopBar(title: "Finance & settlements", subtitle: "Collections, commission, dealer payouts")

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(FinanceSection.allCases, id: \.self) { item in
                            financeChip(item.rawValue, selected: section == item) { section = item }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                if section != .commission && section != .audit && section != .refunds {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(["today", "week", "month", "year"], id: \.self) { p in
                                financeChip(p.capitalized, selected: env.financeViewModel.period == p) {
                                    env.financeViewModel.setPeriod(p)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }

                if let err = env.financeViewModel.error {
                    Text(err).font(.system(size: 13)).foregroundStyle(FMTheme.neg).padding(.horizontal, 16)
                }
                if let msg = env.financeViewModel.message {
                    Text(msg).font(.system(size: 13)).foregroundStyle(FMTheme.pos).padding(.horizontal, 16)
                }

                switch section {
                case .overview: financeOverviewSection
                case .settlements: financeSettlementsSection
                case .dealers: financeDealersSection
                case .investor: financeInvestorSection
                case .commission: financeCommissionSection
                case .reports: financeReportsSection
                case .audit: financeAuditSection
                case .refunds:
                    AdminRefundsListView(path: $path)
                }
            }
            .padding(.bottom, 14)
        }
        .task {
            await env.mainViewModel.loadUsers()
            await env.mainViewModel.loadProducts()
            await env.financeViewModel.refresh()
            if selectedDealerId == nil, case .ok(let list) = env.mainViewModel.users {
                selectedDealerId = list.first(where: { $0.role == "DEALER" })?.id
            }
            if selectedProductId == nil, case .ok(let list) = env.mainViewModel.products {
                selectedProductId = list.first?.id
            }
        }
        .onChange(of: env.financeViewModel.reportShareURL) { _, url in
            showReportShare = url != nil
        }
        .sheet(isPresented: $showReportShare, onDismiss: { env.financeViewModel.clearReportShareURL() }) {
            if let url = env.financeViewModel.reportShareURL {
                ShareSheet(items: [url])
            }
        }
    }

    private var financeOverviewSection: some View {
        let rev = env.financeViewModel.overview?.revenue
        let col = env.financeViewModel.overview?.collections
        return VStack(spacing: 12) {
            HStack(spacing: 10) {
                financeStat("GMV", FMTheme.inr(rev?.gmv ?? 0), accent: FMTheme.brand)
                financeStat("Commission", FMTheme.inr(rev?.platformCommission ?? 0), accent: FMTheme.pos)
            }
            HStack(spacing: 10) {
                financeStat("Dealer payables", FMTheme.inr(rev?.dealerPayables ?? 0), accent: FMTheme.goldInk)
                financeStat("Pending settlement", FMTheme.inr(rev?.pendingSettlement ?? 0), accent: FMTheme.warn)
            }
            HStack(spacing: 10) {
                financeStat("Net earnings", FMTheme.inr(rev?.netPlatformEarnings ?? 0), accent: FMTheme.pos)
                financeStat("Settled", FMTheme.inr(rev?.settledAmount ?? 0))
            }
            FMSectionLabel(title: "Payment collections").padding(.horizontal, 16)
            HStack(spacing: 10) {
                financeStat("Collected", FMTheme.inr(col?.total ?? 0))
                financeStat("Successful", "\(col?.successful ?? 0)")
            }
            HStack(spacing: 10) {
                financeStat("Failed", "\(col?.failed ?? 0)")
                financeStat("Refunded", "\(col?.refunded ?? 0)")
            }
        }
        .padding(.horizontal, 16)
    }

    private var financeInvestorSection: some View {
        let biz = env.financeViewModel.investor?.business
        let rev = env.financeViewModel.investor?.revenue
        let trend = env.financeViewModel.investor?.charts?.dailyRevenueTrend?.map { CGFloat($0.gmv ?? 0) } ?? []
        return VStack(spacing: 12) {
            FMSectionLabel(title: "Business overview").padding(.horizontal, 16)
            HStack(spacing: 10) {
                financeStat("Total orders", "\(biz?.totalOrders ?? 0)")
                financeStat("Delivered", "\(biz?.deliveredOrders ?? 0)")
            }
            HStack(spacing: 10) {
                financeStat("Dealers", "\(biz?.activeDealers ?? 0)")
                financeStat("Shopkeepers", "\(biz?.activeShopkeepers ?? 0)")
            }
            HStack(spacing: 10) {
                financeStat("Employees", "\(biz?.activeEmployees ?? 0)")
                financeStat("Net revenue", FMTheme.inr(rev?.netPlatformEarnings ?? 0))
            }
            FMSectionLabel(title: "Revenue trend (30 days)").padding(.horizontal, 16)
            FMCard {
                if trend.isEmpty {
                    Text("No revenue data yet").font(.system(size: 13)).foregroundStyle(FMTheme.ink3)
                } else {
                    FMMiniBarChart(data: trend, barColor: FMTheme.brand, height: 80)
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private var financeSettlementsSection: some View {
        let dealers = dealersList
        return VStack(alignment: .leading, spacing: 12) {
            FMSectionLabel(title: "Filter by status").padding(.horizontal, 16)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    financeChip("All", selected: env.financeViewModel.settlementStatusFilter == nil) {
                        env.financeViewModel.setSettlementStatusFilter(nil)
                    }
                    financeChip("Pending", selected: env.financeViewModel.settlementStatusFilter == "PENDING") {
                        env.financeViewModel.setSettlementStatusFilter("PENDING")
                    }
                    financeChip("Partial", selected: env.financeViewModel.settlementStatusFilter == "PARTIALLY_SETTLED") {
                        env.financeViewModel.setSettlementStatusFilter("PARTIALLY_SETTLED")
                    }
                    financeChip("Settled", selected: env.financeViewModel.settlementStatusFilter == "SETTLED") {
                        env.financeViewModel.setSettlementStatusFilter("SETTLED")
                    }
                }
                .padding(.horizontal, 16)
            }

            if !dealers.isEmpty {
                FMSectionLabel(title: "Generate settlement").padding(.horizontal, 16)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(dealers) { dealer in
                            financeChip(dealer.name, selected: selectedDealerId == dealer.id) {
                                selectedDealerId = dealer.id
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                FMButton(
                    title: env.financeViewModel.loading ? "Generating…" : "Generate 30-day settlement",
                    variant: .dark,
                    icon: "doc.badge.plus",
                    enabled: selectedDealerId != nil && !env.financeViewModel.loading
                ) {
                    guard let id = selectedDealerId else { return }
                    Task { await env.financeViewModel.generateSettlement(dealerId: id) }
                }
                .padding(.horizontal, 16)
            }

            FMSectionLabel(title: "Settlements").padding(.horizontal, 16)
            if env.financeViewModel.settlements.isEmpty {
                Text("No settlements yet.")
                    .font(.system(size: 13)).foregroundStyle(FMTheme.ink3).padding(.horizontal, 16)
            } else {
                VStack(spacing: 10) {
                    ForEach(env.financeViewModel.settlements) { settlement in
                        Button { path.append(AppRoute.financeSettlement(settlement.id)) } label: {
                            settlementRow(settlement)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }

    private var financeDealersSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            FMSectionLabel(title: "Dealer performance").padding(.horizontal, 16)
            if dealersList.isEmpty {
                Text("No dealers onboarded yet.").font(.system(size: 13)).foregroundStyle(FMTheme.ink3).padding(.horizontal, 16)
            } else {
                VStack(spacing: 10) {
                    ForEach(dealersList) { dealer in
                        Button {
                            path.append(AppRoute.financeDealer(dealer.id, dealer.name))
                        } label: {
                            FMCard(padding: 14) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(dealer.name).font(.system(size: 14.5, weight: .bold))
                                    Text(dealer.email).font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                                    Text("Tap for sales & settlement KPIs").font(.system(size: 12)).foregroundStyle(FMTheme.pos)
                                }
                            }
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }

    private var financeCommissionSection: some View {
        let products: [Product] = {
            guard case .ok(let list) = env.mainViewModel.products else { return [] }
            return list
        }()
        return VStack(alignment: .leading, spacing: 12) {
            FMSectionLabel(title: "Global commission %").padding(.horizontal, 16)
            FMTextField(label: "Rate", text: $globalRate, icon: "percent", placeholder: "8", keyboard: .decimalPad)
                .padding(.horizontal, 16)
            FMButton(title: "Save global rate", variant: .dark, icon: "checkmark") {
                if let rate = Double(globalRate) {
                    Task { await env.financeViewModel.updateGlobalCommission(rate: rate) }
                }
            }
            .padding(.horizontal, 16)

            if !dealersList.isEmpty {
                FMSectionLabel(title: "Dealer commission").padding(.horizontal, 16)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(dealersList) { d in
                            financeChip(d.name, selected: selectedDealerId == d.id) { selectedDealerId = d.id }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                FMTextField(label: "Dealer rate %", text: $dealerRate, icon: "percent", keyboard: .decimalPad)
                    .padding(.horizontal, 16)
                FMButton(title: "Save dealer commission", variant: .soft, icon: "checkmark", enabled: Double(dealerRate) != nil) {
                    guard let id = selectedDealerId, let rate = Double(dealerRate) else { return }
                    Task { await env.financeViewModel.updateDealerCommission(dealerId: id, rate: rate) }
                }
                .padding(.horizontal, 16)
            }

            if !products.isEmpty {
                FMSectionLabel(title: "Product commission").padding(.horizontal, 16)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(products.prefix(20)) { p in
                            financeChip(p.name, selected: selectedProductId == p.id) { selectedProductId = p.id }
                        }
                    }
                    .padding(.horizontal, 16)
                }
                FMTextField(label: "Product rate %", text: $productRate, icon: "percent", keyboard: .decimalPad)
                    .padding(.horizontal, 16)
                FMButton(title: "Save product commission", variant: .soft, icon: "checkmark", enabled: Double(productRate) != nil) {
                    guard let id = selectedProductId, let rate = Double(productRate) else { return }
                    Task { await env.financeViewModel.updateProductCommission(productId: id, rate: rate) }
                }
                .padding(.horizontal, 16)
            }

            FMButton(title: "Backfill revenue", variant: .outline, icon: "arrow.clockwise") {
                Task { await env.financeViewModel.backfillRevenues() }
            }
            .padding(.horizontal, 16)

            FMSectionLabel(title: "Active rules").padding(.horizontal, 16)
            ForEach(env.financeViewModel.commissionRules) { rule in
                FMCard {
                    Text("\(rule.ruleType) · \(rule.rate.doubleValue ?? 0)%").font(.system(size: 14, weight: .bold))
                    if let name = rule.dealer?.name {
                        Text("Dealer: \(name)").font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                    }
                    if let name = rule.product?.name {
                        Text("Product: \(name)").font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }

    private var financeReportsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            FMSectionLabel(title: "Report type").padding(.horizontal, 16)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    financeChip("Settlements", selected: reportType == "settlements") { reportType = "settlements" }
                    financeChip("Revenue", selected: reportType == "revenue") { reportType = "revenue" }
                    financeChip("Collections", selected: reportType == "collections") { reportType = "collections" }
                    financeChip("Returns", selected: reportType == "return-requests") { reportType = "return-requests" }
                    financeChip("Refunds", selected: reportType == "refund-history") { reportType = "refund-history" }
                }
                .padding(.horizontal, 16)
            }
            FMSectionLabel(title: "Format").padding(.horizontal, 16)
            HStack(spacing: 8) {
                financeChip("CSV", selected: reportFormat == "csv") { reportFormat = "csv" }
                financeChip("Excel", selected: reportFormat == "xlsx") { reportFormat = "xlsx" }
            }
            .padding(.horizontal, 16)
            FMButton(
                title: env.financeViewModel.loading ? "Downloading…" : "Download & share report",
                variant: .dark,
                icon: "square.and.arrow.up",
                enabled: !env.financeViewModel.loading
            ) {
                Task { await env.financeViewModel.downloadReport(type: reportType, format: reportFormat) }
            }
            .padding(.horizontal, 16)
        }
    }

    private var financeAuditSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            FMSectionLabel(title: "Audit trail").padding(.horizontal, 16)
            if env.financeViewModel.auditLogs.isEmpty {
                Text("No audit logs yet.").font(.system(size: 13)).foregroundStyle(FMTheme.ink3).padding(.horizontal, 16)
            } else {
                VStack(spacing: 8) {
                    ForEach(env.financeViewModel.auditLogs.prefix(50)) { log in
                        FMCard(padding: 12) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(log.action.replacingOccurrences(of: "_", with: " "))
                                    .font(.system(size: 13, weight: .semibold))
                                Text("\(log.entityType) · \(log.entityId.prefix(8))…")
                                    .font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                                Text(String(log.createdAt.prefix(19)).replacingOccurrences(of: "T", with: " "))
                                    .font(.system(size: 11)).foregroundStyle(FMTheme.ink4)
                            }
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
    }

    private var dealersList: [UserRow] {
        guard case .ok(let list) = env.mainViewModel.users else { return [] }
        return list.filter { $0.role == "DEALER" }
    }

    private func financeStat(_ label: String, _ value: String, accent: Color = FMTheme.ink) -> some View {
        FMCard(padding: 14) {
            Text(value).font(.system(size: 18, weight: .bold, design: .monospaced)).foregroundStyle(accent)
                .lineLimit(1).minimumScaleFactor(0.7)
            Text(label).font(.system(size: 12, weight: .semibold)).foregroundStyle(FMTheme.ink3)
        }
        .frame(maxWidth: .infinity)
    }

    private func financeChip(_ label: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.system(size: 13, weight: .semibold))
                .foregroundStyle(selected ? FMTheme.ink : FMTheme.ink3)
                .padding(.horizontal, 14).padding(.vertical, 9)
                .background(selected ? FMTheme.surface : FMTheme.surface2)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(selected ? FMTheme.brand.opacity(0.35) : FMTheme.line))
        }
        .buttonStyle(.plain)
    }

    private func settlementRow(_ s: DealerSettlement) -> some View {
        FMCard(padding: 14) {
            VStack(alignment: .leading, spacing: 4) {
                Text(s.settlementCode).font(.system(size: 14.5, weight: .bold))
                Text(s.dealer?.shopName ?? s.dealer?.name ?? "Dealer").font(.system(size: 12.5)).foregroundStyle(FMTheme.ink3)
                Text("\(s.settlementStartDate.prefix(10)) → \(s.settlementEndDate.prefix(10)) · \(s.totalOrders ?? 0) orders")
                    .font(.system(size: 12)).foregroundStyle(FMTheme.ink4)
                HStack {
                    Text("Payable \(FMTheme.inr(s.dealerPayable?.doubleValue ?? 0))").font(.system(size: 12.5, weight: .semibold))
                    Spacer()
                    FMBadge(status: s.settlementStatus)
                }
            }
        }
    }
}

struct AdminSettlementDetailView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let settlementId: String

    @State private var amount = ""
    @State private var utr = ""
    @State private var reference = ""
    @State private var remarks = ""
    @State private var paymentMethod = "NEFT"

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Settlement detail", onBack: { path.removeLast() })
                if let err = env.financeViewModel.error {
                    Text(err).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }
                if let s = env.financeViewModel.selectedSettlement {
                    FMCard {
                        VStack(alignment: .leading, spacing: 6) {
                            Text(s.settlementCode).font(.system(size: 16, weight: .bold))
                            Text(s.dealer?.shopName ?? s.dealer?.name ?? "").foregroundStyle(FMTheme.ink3)
                            Text("Gross \(FMTheme.inr(s.grossSales?.doubleValue ?? 0)) · GST \(FMTheme.inr(s.gstAmount?.doubleValue ?? 0))")
                            Text("Commission \(FMTheme.inr(s.commissionAmount?.doubleValue ?? 0))")
                            Text("Dealer payable \(FMTheme.inr(s.dealerPayable?.doubleValue ?? 0))")
                            Text("Settled \(FMTheme.inr(s.settledAmount?.doubleValue ?? 0)) · Balance \(FMTheme.inr(s.balanceAmount?.doubleValue ?? 0))")
                            FMBadge(status: s.settlementStatus)
                        }
                    }
                    if let payments = s.payments, !payments.isEmpty {
                        FMSectionLabel(title: "Payment history")
                        ForEach(payments) { p in
                            FMCard {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text("\(FMTheme.inr(p.amount.doubleValue ?? 0)) · \(p.paymentMethod)")
                                        .font(.system(size: 13, weight: .semibold))
                                    if let u = p.utrNumber { Text("UTR: \(u)").font(.system(size: 12)).foregroundStyle(FMTheme.ink3) }
                                    Text(String(p.paymentDate.prefix(10))).font(.system(size: 11)).foregroundStyle(FMTheme.ink4)
                                }
                            }
                        }
                    }
                    if (s.balanceAmount?.doubleValue ?? 0) > 0.01 {
                        FMSectionLabel(title: "Record payment")
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(["NEFT", "UPI", "RTGS", "BANK_TRANSFER"], id: \.self) { m in
                                    Button { paymentMethod = m } label: {
                                        Text(m.replacingOccurrences(of: "_", with: " "))
                                            .font(.system(size: 12, weight: .semibold))
                                            .padding(.horizontal, 12).padding(.vertical, 8)
                                            .background(paymentMethod == m ? FMTheme.brandTint : FMTheme.surface2)
                                            .clipShape(Capsule())
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                        FMTextField(label: "Amount", text: $amount, icon: "indianrupeesign", keyboard: .decimalPad)
                        FMTextField(label: "UTR number", text: $utr, icon: "number")
                        FMTextField(label: "Transaction reference", text: $reference, icon: "doc.text")
                        FMTextField(label: "Remarks", text: $remarks, icon: "text.alignleft")
                        FMButton(
                            title: "Mark payment",
                            variant: .dark,
                            icon: "checkmark",
                            enabled: Double(amount) != nil && (!utr.isEmpty || !reference.isEmpty)
                        ) {
                            guard let value = Double(amount) else { return }
                            Task {
                                let ok = await env.financeViewModel.recordPayment(
                                    settlementId: settlementId,
                                    amount: value,
                                    method: paymentMethod,
                                    utr: utr,
                                    reference: reference,
                                    remarks: remarks
                                )
                                if ok { path.removeLast() }
                            }
                        }
                    }
                } else {
                    Text("Loading…").foregroundStyle(FMTheme.ink3)
                }
            }
            .padding(.horizontal, 16).padding(.bottom, 30)
        }
        .background(FMTheme.bg).navigationBarHidden(true)
        .task { await env.financeViewModel.loadSettlement(settlementId) }
    }
}

struct AdminDealerPerformanceView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    let dealerId: String
    let dealerName: String

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: dealerName, subtitle: "Dealer performance", onBack: { path.removeLast() })
                if let err = env.financeViewModel.error {
                    Text(err).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }
                if let summary = env.financeViewModel.dealerPerformance?.summary {
                    HStack(spacing: 10) {
                        perfStat("Delivered", "\(summary.ordersDelivered ?? 0)")
                        perfStat("Gross sales", FMTheme.inr(summary.grossSales ?? 0))
                    }
                    HStack(spacing: 10) {
                        perfStat("Commission", FMTheme.inr(summary.commissionDeducted ?? 0))
                        perfStat("Earnings", FMTheme.inr(summary.dealerEarnings ?? 0))
                    }
                    perfStat("Pending settlement", FMTheme.inr(summary.pendingSettlement ?? 0))
                    let trend = env.financeViewModel.dealerPerformance?.dailySales?.map { CGFloat($0.gmv ?? 0) } ?? []
                    FMSectionLabel(title: "Daily sales")
                    FMCard {
                        if trend.isEmpty {
                            Text("No sales data").foregroundStyle(FMTheme.ink3)
                        } else {
                            FMMiniBarChart(data: trend, barColor: FMTheme.brand, height: 80)
                        }
                    }
                    if let top = env.financeViewModel.dealerPerformance?.topProducts, !top.isEmpty {
                        FMSectionLabel(title: "Top products")
                        ForEach(top) { p in
                            FMCard {
                                HStack {
                                    Text(p.name).font(.system(size: 14, weight: .bold))
                                    Spacer()
                                    Text("\(FMTheme.inr(p.revenue ?? 0)) · \(p.qty ?? 0) qty")
                                        .font(.system(size: 12)).foregroundStyle(FMTheme.ink3)
                                }
                            }
                        }
                    }
                } else if env.financeViewModel.loading {
                    Text("Loading…").foregroundStyle(FMTheme.ink3)
                }
            }
            .padding(.horizontal, 16).padding(.bottom, 30)
        }
        .background(FMTheme.bg).navigationBarHidden(true)
        .task { await env.financeViewModel.loadDealerPerformance(dealerId) }
    }

    private func perfStat(_ label: String, _ value: String) -> some View {
        FMCard(padding: 14) {
            Text(value).font(.system(size: 16, weight: .bold, design: .monospaced))
            Text(label).font(.system(size: 12, weight: .semibold)).foregroundStyle(FMTheme.ink3)
        }
        .frame(maxWidth: .infinity)
    }
}
