import SwiftUI

// MARK: - SKU form

private struct SkuFormState {
    var name = ""
    var brandType = "OWN"
    var shelf = ProductShelf.staples.rawValue
    var basePrice = "0"
    var gstPct = "18"
    var dealerDisc = "10"
    var shopkeeperDisc = "5"

    static func from(_ product: Product) -> SkuFormState {
        SkuFormState(
            name: product.name,
            brandType: product.brandType,
            shelf: product.shelf ?? ProductShelf.staples.rawValue,
            basePrice: String(format: "%.0f", product.basePrice?.doubleValue ?? 0),
            gstPct: String(format: "%.0f", product.gstPercentage?.doubleValue ?? 18),
            dealerDisc: String(format: "%.0f", product.dealerDiscount?.doubleValue ?? 10),
            shopkeeperDisc: String(format: "%.0f", product.shopkeeperDiscount?.doubleValue ?? 5)
        )
    }

    func validationError() -> String? {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty { return "Enter product name" }
        guard Double(basePrice) != nil else { return "Enter a valid base price" }
        guard Double(gstPct) != nil else { return "Enter a valid GST %" }
        guard Double(dealerDisc) != nil else { return "Enter a valid dealer discount" }
        guard Double(shopkeeperDisc) != nil else { return "Enter a valid shopkeeper discount" }
        return nil
    }

    func toCreateRequest() -> CreateProductRequest? {
        guard validationError() == nil,
              let price = Double(basePrice),
              let gst = Double(gstPct),
              let dealer = Double(dealerDisc),
              let shop = Double(shopkeeperDisc) else { return nil }
        return CreateProductRequest(
            name: name.trimmingCharacters(in: .whitespacesAndNewlines),
            brandType: brandType,
            brandId: nil,
            shelf: shelf,
            basePrice: price,
            gstPercentage: gst,
            dealerDiscount: dealer,
            shopkeeperDiscount: shop
        )
    }
}

private struct SkuProductForm: View {
    let title: String
    @Binding var form: SkuFormState
    let submitLabel: String
    let busy: Bool
    let onSubmit: () -> Void

    var body: some View {
        FMCard {
            VStack(alignment: .leading, spacing: 12) {
                Text(title)
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(FMTheme.ink)

                FMTextField(label: "Product name", text: $form.name, icon: "cube.box", placeholder: "KNSR Premium Rice 25 kg")

                VStack(alignment: .leading, spacing: 6) {
                    Text("Brand type")
                        .font(.system(size: 12.5, weight: .semibold))
                        .foregroundStyle(FMTheme.ink3)
                    Picker("Brand type", selection: $form.brandType) {
                        Text("Own brand").tag("OWN")
                        Text("Other brand").tag("OTHER")
                    }
                    .pickerStyle(.segmented)
                }

                VStack(alignment: .leading, spacing: 6) {
                    Text("Shelf category")
                        .font(.system(size: 12.5, weight: .semibold))
                        .foregroundStyle(FMTheme.ink3)
                    Picker("Shelf", selection: $form.shelf) {
                        ForEach(ProductShelf.allCases.filter { $0 != .all }, id: \.rawValue) { shelf in
                            Text(shelf.label).tag(shelf.rawValue)
                        }
                    }
                    .pickerStyle(.menu)
                }

                HStack(spacing: 10) {
                    FMTextField(label: "Base price (₹)", text: $form.basePrice, icon: "indianrupeesign", placeholder: "1549", keyboard: .decimalPad)
                    FMTextField(label: "GST %", text: $form.gstPct, icon: "percent", placeholder: "18", keyboard: .decimalPad)
                }

                HStack(spacing: 10) {
                    FMTextField(label: "Dealer disc. %", text: $form.dealerDisc, icon: "tag", placeholder: "10", keyboard: .decimalPad)
                    FMTextField(label: "Shop disc. %", text: $form.shopkeeperDisc, icon: "tag.fill", placeholder: "5", keyboard: .decimalPad)
                }

                FMButton(title: busy ? "Saving…" : submitLabel, variant: .dark, icon: "checkmark", enabled: !busy, action: onSubmit)
            }
        }
    }
}

// MARK: - SKU management

struct SkuManagementView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    var embeddedInTab: Bool = false
    @State private var form = SkuFormState()
    @State private var editingProduct: Product?
    @State private var editForm = SkuFormState()
    @State private var creating = false
    @State private var savingEditId: String?
    @State private var deletingId: String?
    @State private var localError: String?
    @State private var showCsvImport = false
    @State private var csvText = ""
    @State private var csvBusy = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                if embeddedInTab {
                    FMTopBar(title: "SKU management", subtitle: "Products · pricing · discounts")
                } else {
                    FMTopBar(title: "SKU management", subtitle: "Create, edit & delete products", onBack: { path.removeLast() })
                }

                FMCard {
                    Text("Pricing, GST, and discount settings are applied server-side in order calculations.")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                }

                SkuProductForm(
                    title: "Add product",
                    form: $form,
                    submitLabel: "Add SKU",
                    busy: creating,
                    onSubmit: submitCreate
                )

                FMButton(title: "Upload SKU sheet (CSV demo)", variant: .outline, icon: "doc.text") {
                    showCsvImport = true
                }

                if let localError {
                    Text(localError).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }
                if let err = env.mainViewModel.placeOrderError {
                    Text(err).foregroundStyle(FMTheme.neg).font(.system(size: 13))
                }

                FMSectionLabel(title: "Products")
                productList
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 30)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { await env.mainViewModel.loadProducts() }
        .sheet(item: $editingProduct) { product in
            NavigationStack {
                ScrollView {
                    VStack(spacing: 14) {
                        SkuProductForm(
                            title: "Edit SKU",
                            form: $editForm,
                            submitLabel: savingEditId == product.id ? "Saving…" : "Save changes",
                            busy: savingEditId == product.id,
                            onSubmit: { submitEdit(product.id) }
                        )
                    }
                    .padding(16)
                }
                .background(FMTheme.bg)
                .navigationTitle("Edit SKU")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { editingProduct = nil }
                    }
                }
            }
            .presentationDetents([.large])
        }
        .alert("CSV import (demo)", isPresented: $showCsvImport) {
            TextField("Paste CSV rows", text: $csvText, axis: .vertical)
            Button("Cancel", role: .cancel) { }
            Button(csvBusy ? "Importing…" : "Import") { Task { await submitCsvImport() } }
        } message: {
            Text("One row per line:\nname,brandType,shelf,basePrice,gstPercentage,dealerDiscount,shopkeeperDiscount\nLegacy 6-column rows default shelf to STAPLES.")
        }
    }

    @ViewBuilder
    private var productList: some View {
        if case .ok(let list) = env.mainViewModel.products {
            if list.isEmpty {
                Text("No products yet. Add your first SKU above.")
                    .font(.system(size: 13))
                    .foregroundStyle(FMTheme.ink3)
                    .padding(.vertical, 20)
            } else {
                ForEach(list) { product in
                    skuRow(product)
                }
            }
        } else if case .loading = env.mainViewModel.products {
            ProgressView().frame(maxWidth: .infinity).padding()
        } else if case .err(let msg) = env.mainViewModel.products {
            Text(msg).foregroundStyle(FMTheme.neg).font(.system(size: 13))
        }
    }

    private func skuRow(_ product: Product) -> some View {
        FMCard(padding: 13) {
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 12) {
                    FMProductThumb(product: product, size: 48)
                    VStack(alignment: .leading, spacing: 4) {
                        Text(product.name)
                            .font(.system(size: 14.5, weight: .bold))
                        Text("\(product.brandType) · \(shelfLabel(product.shelf)) · GST \(Int(product.gstPercentage?.doubleValue ?? 0))%")
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink3)
                        Text("Dealer \(Int(product.dealerDiscount?.doubleValue ?? 0))% · Shop \(Int(product.shopkeeperDiscount?.doubleValue ?? 0))%")
                            .font(.system(size: 12))
                            .foregroundStyle(FMTheme.ink3)
                        Text(FMTheme.inr(product.basePrice?.doubleValue ?? 0))
                            .font(.system(size: 14, weight: .bold, design: .monospaced))
                            .foregroundStyle(FMTheme.brand)
                    }
                    Spacer()
                }
                HStack(spacing: 10) {
                    FMButton(title: "Edit", variant: .outline, fullWidth: true) {
                        editForm = SkuFormState.from(product)
                        editingProduct = product
                    }
                    FMButton(
                        title: deletingId == product.id ? "Deleting…" : "Delete",
                        variant: .outline,
                        fullWidth: true,
                        enabled: deletingId != product.id
                    ) {
                        Task {
                            deletingId = product.id
                            await env.mainViewModel.deleteProduct(product.id)
                            deletingId = nil
                        }
                    }
                }
            }
        }
    }

    private func shelfLabel(_ shelf: String?) -> String {
        guard let shelf else { return "—" }
        return ProductShelf(rawValue: shelf)?.label ?? shelf
    }

    private func submitCreate() {
        if let err = form.validationError() {
            localError = err
            return
        }
        guard let req = form.toCreateRequest() else { return }
        localError = nil
        Task {
            creating = true
            let ok = await env.mainViewModel.createProduct(
                name: req.name,
                brandType: req.brandType,
                shelf: req.shelf,
                basePrice: req.basePrice,
                gstPercentage: req.gstPercentage,
                dealerDiscount: req.dealerDiscount,
                shopkeeperDiscount: req.shopkeeperDiscount
            )
            creating = false
            if ok { form = SkuFormState() }
        }
    }

    private func submitEdit(_ id: String) {
        if let err = editForm.validationError() {
            localError = err
            return
        }
        guard let req = editForm.toCreateRequest() else { return }
        localError = nil
        Task {
            savingEditId = id
            let ok = await env.mainViewModel.updateProduct(
                id: id,
                name: req.name,
                brandType: req.brandType,
                shelf: req.shelf,
                basePrice: req.basePrice,
                gstPercentage: req.gstPercentage,
                dealerDiscount: req.dealerDiscount,
                shopkeeperDiscount: req.shopkeeperDiscount
            )
            savingEditId = nil
            if ok { editingProduct = nil }
        }
    }

    private func submitCsvImport() async {
        let rows = parseCsvRows(csvText)
        guard !rows.isEmpty else {
            localError = "No valid CSV rows found"
            return
        }
        csvBusy = true
        let ok = await env.mainViewModel.bulkCreateProducts(rows)
        csvBusy = false
        if ok {
            csvText = ""
            showCsvImport = false
        }
    }

    private func parseCsvRows(_ text: String) -> [CreateProductRequest] {
        text.split(whereSeparator: \.isNewline).compactMap { line in
            let cols = line.split(separator: ",").map { $0.trimmingCharacters(in: .whitespaces) }
            guard cols.count >= 6 else { return nil }
            let shelf = cols.count >= 7 ? cols[2] : "STAPLES"
            let offset = cols.count >= 7 ? 0 : -1
            guard let price = Double(cols[3 + offset]),
                  let gst = Double(cols[4 + offset]),
                  let dealer = Double(cols[5 + offset]),
                  let shop = Double(cols[6 + offset]) else { return nil }
            let brandType = cols.count >= 7 ? cols[1].uppercased() : cols[1].uppercased()
            let name = cols[0]
            guard !name.isEmpty else { return nil }
            return CreateProductRequest(
                name: name,
                brandType: brandType == "OTHER" ? "OTHER" : "OWN",
                brandId: nil,
                shelf: shelf,
                basePrice: price,
                gstPercentage: gst,
                dealerDiscount: dealer,
                shopkeeperDiscount: shop
            )
        }
    }
}

// MARK: - Brands management

struct BrandsManagementView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var showCreate = false
    @State private var newName = ""
    @State private var newLogoUrl = ""
    @State private var creating = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Brands", subtitle: "Manage brand catalog", onBack: { path.removeLast() })

                FMButton(title: "Add brand", variant: .soft, icon: "plus") {
                    showCreate = true
                }

                if case .ok(let list) = env.mainViewModel.brands {
                    ForEach(list) { brand in
                        FMCard(padding: 13) {
                            HStack {
                                FMAvatar(name: brand.name, size: 42)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(brand.name)
                                        .font(.system(size: 15, weight: .bold))
                                    if let m = brand.manufacturer {
                                        Text(m)
                                            .font(.system(size: 12))
                                            .foregroundStyle(FMTheme.ink3)
                                    }
                                }
                                Spacer()
                                Button {
                                    Task { await env.mainViewModel.deleteBrand(brand.id) }
                                } label: {
                                    Image(systemName: "trash")
                                        .foregroundStyle(FMTheme.neg)
                                }
                            }
                        }
                    }
                } else if case .loading = env.mainViewModel.brands {
                    ProgressView().frame(maxWidth: .infinity).padding()
                }
            }
            .padding(.horizontal, 16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { await env.mainViewModel.loadBrands() }
        .alert("New brand", isPresented: $showCreate) {
            TextField("Brand name", text: $newName)
            TextField("Logo URL (optional)", text: $newLogoUrl)
            Button("Cancel", role: .cancel) { }
            Button(creating ? "Saving…" : "Save") {
                Task {
                    creating = true
                    _ = await env.mainViewModel.createBrand(name: newName, logoUrl: newLogoUrl.isEmpty ? nil : newLogoUrl)
                    newName = ""
                    newLogoUrl = ""
                    creating = false
                    showCreate = false
                }
            }
        }
    }
}

// MARK: - Areas management

struct AdminAreasView: View {
    @Environment(AppEnvironment.self) private var env
    @Binding var path: NavigationPath
    @State private var newAreaName = ""
    @State private var saving = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                FMTopBar(title: "Manage areas", subtitle: "Territories for dealers & shopkeepers", onBack: { path.removeLast() })

                FMCard {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Add area").font(.system(size: 15, weight: .bold))
                        FMTextField(label: "Area name", text: $newAreaName, icon: "mappin.circle", placeholder: "North Zone")
                        FMButton(title: saving ? "Saving…" : "Add area", enabled: !saving && newAreaName.trimmingCharacters(in: .whitespaces).count >= 2) {
                            Task {
                                saving = true
                                if await env.mainViewModel.createArea(name: newAreaName) {
                                    newAreaName = ""
                                }
                                saving = false
                            }
                        }
                    }
                }

                if let err = env.mainViewModel.placeOrderError {
                    FMErrorBanner(text: err)
                }

                FMSectionLabel(title: "Existing areas")
                switch env.mainViewModel.areas {
                case .loading:
                    FMLoadingState(message: "Loading areas…")
                case .err(let msg):
                    FMErrorBanner(text: msg)
                case .ok(let list):
                    if list.isEmpty {
                        FMEmptyState(
                            icon: "map",
                            title: "No areas configured",
                            message: "Add your first territory above. Employees will assign dealers and shopkeepers to these areas."
                        )
                    } else {
                        ForEach(list) { area in
                            FMCard {
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(area.name).font(.system(size: 15, weight: .semibold))
                                        Text(area.dealer?.name ?? "No dealer assigned")
                                            .font(.system(size: 12))
                                            .foregroundStyle(FMTheme.ink3)
                                    }
                                    Spacer()
                                }
                            }
                        }
                    }
                case .idle:
                    EmptyView()
                }
            }
            .padding(16)
        }
        .background(FMTheme.bg)
        .navigationBarHidden(true)
        .task { await env.mainViewModel.loadAreas() }
    }
}
