import Foundation

extension Product {
    func discountPercent(forRole role: String) -> Double {
        let isDealer = role.uppercased() == "DEALER"
        let fromProduct = isDealer
            ? dealerDiscount?.doubleValue
            : shopkeeperDiscount?.doubleValue
        return fromProduct ?? (isDealer ? 10.0 : 5.0)
    }

    func catalogUnitPrice(forRole role: String) -> Double {
        let base = dealerPrice?.doubleValue ?? basePrice?.doubleValue ?? 0
        let disc = discountPercent(forRole: role) / 100.0
        return base * (1.0 - disc)
    }
}

struct OrderMath {
    let subtotal: Double
    let discount: Double
    let gst: Double
    let total: Double
}

enum ProductPricing {
    static func lineMath(product: Product, quantity: Int, role: String) -> OrderMath {
        let unit = product.catalogUnitPrice(forRole: role)
        let discPct = product.discountPercent(forRole: role) / 100.0
        let lineSub = unit / (1.0 - discPct) * Double(quantity)
        let lineDisc = lineSub * discPct
        let taxable = lineSub - lineDisc
        let gstRate = (product.gstPercentage?.doubleValue ?? 18) / 100.0
        let lineGst = taxable * gstRate
        return OrderMath(
            subtotal: round2(lineSub),
            discount: round2(lineDisc),
            gst: round2(lineGst),
            total: round2(taxable + lineGst)
        )
    }

    static func cartMath(lines: [CartLine], role: String) -> OrderMath {
        var sub = 0.0, disc = 0.0, gst = 0.0
        for line in lines {
            let m = lineMath(product: line.product, quantity: line.quantity, role: role)
            sub += m.subtotal
            disc += m.discount
            gst += m.gst
        }
        return OrderMath(
            subtotal: round2(sub),
            discount: round2(disc),
            gst: round2(gst),
            total: round2(sub - disc + gst)
        )
    }

    static func orderMath(order: Order) -> OrderMath {
        OrderMath(
            subtotal: order.totalAmount?.doubleValue ?? 0,
            discount: order.discountAmount?.doubleValue ?? 0,
            gst: order.gstAmount?.doubleValue ?? 0,
            total: order.finalAmount?.doubleValue ?? 0
        )
    }

    private static func round2(_ v: Double) -> Double {
        (v * 100).rounded() / 100
    }
}
