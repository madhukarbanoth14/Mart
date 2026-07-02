import UIKit

enum InvoicePdfExporter {
    static func write(invoice: InvoiceDocument) throws -> URL {
        let pageRect = CGRect(x: 0, y: 0, width: 595, height: 842)
        let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
        let data = renderer.pdfData { context in
            context.beginPage()
            let titleAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 20),
                .foregroundColor: UIColor.black,
            ]
            let headingAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 14),
                .foregroundColor: UIColor.black,
            ]
            let bodyAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11),
                .foregroundColor: UIColor.darkGray,
            ]

            var y: CGFloat = 56
            "KNSR Mart — Distribution".draw(at: CGPoint(x: 48, y: y), withAttributes: titleAttrs)
            y += 36
            "Invoice \(invoice.invoiceNumber)".draw(at: CGPoint(x: 48, y: y), withAttributes: headingAttrs)
            y += 28
            if let generated = invoice.generatedAt {
                "Issued: \(generated)".draw(at: CGPoint(x: 48, y: y), withAttributes: bodyAttrs)
                y += 20
            }

            if let order = invoice.order {
                y += 12
                "Subtotal (pre-discount): \(FMTheme.inr(order.totalAmount?.doubleValue ?? 0))"
                    .draw(at: CGPoint(x: 48, y: y), withAttributes: bodyAttrs)
                y += 18
                "Discount: \(FMTheme.inr(order.discountAmount?.doubleValue ?? 0))"
                    .draw(at: CGPoint(x: 48, y: y), withAttributes: bodyAttrs)
                y += 18
                "GST: \(FMTheme.inr(order.gstAmount?.doubleValue ?? 0))"
                    .draw(at: CGPoint(x: 48, y: y), withAttributes: bodyAttrs)
                y += 18
                let boldBody: [NSAttributedString.Key: Any] = [
                    .font: UIFont.boldSystemFont(ofSize: 11),
                    .foregroundColor: UIColor.darkGray,
                ]
                "Payable: \(FMTheme.inr(order.finalAmount?.doubleValue ?? 0))"
                    .draw(at: CGPoint(x: 48, y: y), withAttributes: boldBody)
                y += 28
                "Line items".draw(at: CGPoint(x: 48, y: y), withAttributes: headingAttrs)
                y += 22
                for line in order.items ?? [] {
                    let name = line.product?.name ?? line.productId
                    let row = "\(name)  ×\(line.quantity)  \(FMTheme.inr(line.finalAmount?.doubleValue ?? 0))"
                    row.draw(at: CGPoint(x: 52, y: y), withAttributes: bodyAttrs)
                    y += 16
                    if y > 780 { break }
                }
            }
        }

        let safeName = invoice.invoiceNumber.replacingOccurrences(
            of: #"[^A-Za-z0-9-]"#,
            with: "_",
            options: .regularExpression
        )
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("invoice-\(safeName).pdf")
        try data.write(to: url)
        return url
    }
}
