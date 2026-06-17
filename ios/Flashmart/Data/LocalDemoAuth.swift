import Foundation

enum LocalDemoAuth {
    static let localDemoBearerToken = "local-demo-no-server"
    static let demoPassword = "Password@123"

    static let demoAccounts: [(role: String, email: String, phone: String)] = [
        ("Admin", "admin@martdemo.com", "9000000001"),
        ("Employee", "employee@martdemo.com", "9000000002"),
        ("Dealer", "dealer@martdemo.com", "9000000003"),
        ("Shopkeeper", "shop1@martdemo.com", "9000000004"),
        ("Shopkeeper", "shop2@martdemo.com", "9000000005"),
    ]

    private static let usersByEmail: [String: SessionUser] = [
        "admin@martdemo.com": SessionUser(id: "demo-user-admin", name: "Super Admin", email: "admin@martdemo.com", role: "ADMIN"),
        "employee@martdemo.com": SessionUser(id: "demo-user-employee", name: "Field Employee", email: "employee@martdemo.com", role: "EMPLOYEE"),
        "dealer@martdemo.com": SessionUser(id: "demo-user-dealer", name: "City Dealer", email: "dealer@martdemo.com", role: "DEALER"),
        "shop1@martdemo.com": SessionUser(id: "demo-user-shop1", name: "Shopkeeper One", email: "shop1@martdemo.com", role: "SHOPKEEPER"),
        "shop2@martdemo.com": SessionUser(id: "demo-user-shop2", name: "Shopkeeper Two", email: "shop2@martdemo.com", role: "SHOPKEEPER"),
    ]

    private static let usersByPhone: [String: SessionUser] = [
        "9000000001": usersByEmail["admin@martdemo.com"]!,
        "9000000002": usersByEmail["employee@martdemo.com"]!,
        "9000000003": usersByEmail["dealer@martdemo.com"]!,
        "9000000004": usersByEmail["shop1@martdemo.com"]!,
        "9000000005": usersByEmail["shop2@martdemo.com"]!,
    ]

    static func resolveDemoUser(identifier: String, password: String) -> SessionUser? {
        guard password == demoPassword else { return nil }
        let key = identifier.trimmingCharacters(in: .whitespacesAndNewlines)
        return usersByEmail[key.lowercased()] ?? usersByPhone[key]
    }
}
