import Foundation
import Observation

@Observable
final class CartStore {
    private(set) var lines: [CartLine] = []

    var itemCount: Int { lines.reduce(0) { $0 + $1.quantity } }

    func add(product: Product, quantity: Int = 1) {
        if let idx = lines.firstIndex(where: { $0.product.id == product.id }) {
            lines[idx].quantity += quantity
        } else {
            lines.append(CartLine(product: product, quantity: quantity))
        }
    }

    func setQuantity(productId: String, quantity: Int) {
        guard let idx = lines.firstIndex(where: { $0.product.id == productId }) else { return }
        if quantity <= 0 {
            lines.remove(at: idx)
        } else {
            lines[idx].quantity = quantity
        }
    }

    func remove(productId: String) {
        lines.removeAll { $0.product.id == productId }
    }

    func clear() {
        lines.removeAll()
    }

    func restoreLines(_ restored: [CartLine]) {
        lines = restored
    }
}
