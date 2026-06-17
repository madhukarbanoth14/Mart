import SwiftUI

struct ProfileStoreAddressView: View {
    @Binding var path: NavigationPath
    @State private var storeName = ShopkeeperProfileStore.storeName
    @State private var address = ShopkeeperProfileStore.storeAddress
    @State private var phone = ShopkeeperProfileStore.storePhone
    @State private var error: String?

    var body: some View {
        profileFormScreen(title: "Store address", subtitle: "Delivery location", path: $path) {
            FMTextField(label: "Store name", text: $storeName, icon: "storefront")
            FMTextField(label: "Address", text: $address, icon: "mappin.and.ellipse")
            FMTextField(label: "Phone", text: $phone, icon: "phone", keyboard: .phonePad, filter: { FieldValidators.digitsOnly($0, maxLength: 10) })
            if let error { Text(error).foregroundStyle(FMTheme.neg).font(.system(size: 13)) }
            FMButton(title: "Save") {
                if let msg = FieldValidators.required(address, label: "address") {
                    error = msg
                    return
                }
                if let msg = FieldValidators.phone(phone) {
                    error = msg
                    return
                }
                ShopkeeperProfileStore.storeName = storeName
                ShopkeeperProfileStore.storeAddress = address
                ShopkeeperProfileStore.storePhone = phone
                path.removeLast()
            }
        }
    }
}

struct ProfilePaymentMethodsView: View {
    @Binding var path: NavigationPath

    var body: some View {
        profileFormScreen(title: "Payment methods", subtitle: "Saved for checkout", path: $path) {
            FMCard {
                VStack(alignment: .leading, spacing: 8) {
                    Text(ShopkeeperProfileStore.dummyCardMasked)
                        .font(.system(size: 15, weight: .semibold))
                    Text("UPI: \(ShopkeeperProfileStore.dummyUpiId)")
                        .font(.system(size: 13))
                        .foregroundStyle(FMTheme.ink3)
                    Text("Demo mode — card/UPI shown at checkout.")
                        .font(.system(size: 12))
                        .foregroundStyle(FMTheme.ink4)
                }
            }
            FMButton(title: "Done") { path.removeLast() }
        }
    }
}

struct ProfileGstDetailsView: View {
    @Binding var path: NavigationPath
    @State private var businessName = ShopkeeperProfileStore.gstBusinessName
    @State private var gstin = ShopkeeperProfileStore.gstin
    @State private var error: String?

    var body: some View {
        profileFormScreen(title: "GST details", subtitle: "Tax invoice info", path: $path) {
            FMTextField(label: "Business name", text: $businessName, icon: "building.2")
            FMTextField(label: "GSTIN", text: $gstin, icon: "doc.text", filter: { String($0.uppercased().filter { $0.isLetter || $0.isNumber }.prefix(15)) })
            if let error { Text(error).foregroundStyle(FMTheme.neg).font(.system(size: 13)) }
            FMButton(title: "Save") {
                if let msg = FieldValidators.required(businessName, label: "business name") {
                    error = msg
                    return
                }
                ShopkeeperProfileStore.gstBusinessName = businessName
                ShopkeeperProfileStore.gstin = gstin
                path.removeLast()
            }
        }
    }
}

struct ProfileNotificationsView: View {
    @Binding var path: NavigationPath
    @State private var orderAlerts = ShopkeeperProfileStore.orderAlerts
    @State private var deliveryAlerts = ShopkeeperProfileStore.deliveryAlerts
    @State private var promoAlerts = ShopkeeperProfileStore.promoAlerts

    var body: some View {
        profileFormScreen(title: "Notifications", subtitle: "Alert preferences", path: $path) {
            FMCard {
                Toggle("Order updates", isOn: $orderAlerts)
                Divider()
                Toggle("Delivery alerts", isOn: $deliveryAlerts)
                Divider()
                Toggle("Promotions", isOn: $promoAlerts)
            }
            FMButton(title: "Save") {
                ShopkeeperProfileStore.orderAlerts = orderAlerts
                ShopkeeperProfileStore.deliveryAlerts = deliveryAlerts
                ShopkeeperProfileStore.promoAlerts = promoAlerts
                path.removeLast()
            }
        }
    }
}

struct ProfileHelpView: View {
    @Binding var path: NavigationPath

    var body: some View {
        profileFormScreen(title: "Help & support", subtitle: "We're here to help", path: $path) {
            FMCard {
                FMRow(icon: "phone", title: "Call support", subtitle: "+91 1800-123-4567", showDivider: true)
                FMRow(icon: "envelope", title: "Email", subtitle: "support@flashmart.app", showDivider: true)
                FMRow(icon: "questionmark.circle", title: "FAQs", subtitle: "Orders, payments, delivery", showDivider: false)
            }
            FMButton(title: "Close") { path.removeLast() }
        }
    }
}

private func profileFormScreen<Content: View>(title: String, subtitle: String, path: Binding<NavigationPath>, @ViewBuilder content: () -> Content) -> some View {
    ScrollView {
        VStack(alignment: .leading, spacing: 14) {
            ProfileFormHeader(path: path, title: title, subtitle: subtitle)
            content()
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 30)
    }
    .background(FMTheme.bg)
    .navigationBarHidden(true)
}

private struct ProfileFormHeader: View {
    @Binding var path: NavigationPath
    let title: String
    let subtitle: String

    var body: some View {
        FMTopBar(title: title, subtitle: subtitle, onBack: { path.removeLast() })
    }
}
