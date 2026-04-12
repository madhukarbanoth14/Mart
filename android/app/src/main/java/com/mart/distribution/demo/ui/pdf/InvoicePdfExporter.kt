package com.mart.distribution.demo.ui.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.mart.distribution.demo.data.api.dto.InvoiceDocumentDto
import com.mart.distribution.demo.ui.util.formatDecimal
import java.io.File
import java.io.FileOutputStream

object InvoicePdfExporter {
    fun write(
        context: Context,
        doc: InvoiceDocumentDto,
    ): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 20f
                color = Color.BLACK
                isFakeBoldText = true
            }
        val bodyPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 11f
                color = Color.DKGRAY
            }
        var y = 56f
        canvas.drawText("KNSR Mart — Distribution", 48f, y, titlePaint)
        y += 36f
        canvas.drawText("Invoice ${doc.invoiceNumber}", 48f, y, titlePaint.apply { textSize = 14f })
        y += 28f
        doc.generatedAt?.let {
            canvas.drawText("Issued: $it", 48f, y, bodyPaint)
            y += 20f
        }
        val order = doc.order
        if (order != null) {
            y += 12f
            canvas.drawText("Subtotal (pre-discount): ${formatDecimal(order.totalAmount)}", 48f, y, bodyPaint)
            y += 18f
            canvas.drawText("Discount: ${formatDecimal(order.discountAmount)}", 48f, y, bodyPaint)
            y += 18f
            canvas.drawText("GST: ${formatDecimal(order.gstAmount)}", 48f, y, bodyPaint)
            y += 18f
            bodyPaint.isFakeBoldText = true
            canvas.drawText("Payable: ${formatDecimal(order.finalAmount)}", 48f, y, bodyPaint)
            bodyPaint.isFakeBoldText = false
            y += 28f
            canvas.drawText("Line items", 48f, y, titlePaint.apply { textSize = 13f })
            y += 22f
            order.items?.forEach { line ->
                val name = line.product?.name ?: line.productId
                val row = "$name  ×${line.quantity}  ${formatDecimal(line.finalAmount)}"
                canvas.drawText(row, 52f, y, bodyPaint)
                y += 16f
                if (y > 780f) return@forEach
            }
        }
        pdf.finishPage(page)
        val safeName = doc.invoiceNumber.replace(Regex("[^A-Za-z0-9-]"), "_")
        val out = File(context.cacheDir, "invoice-$safeName.pdf")
        FileOutputStream(out).use { pdf.writeTo(it) }
        pdf.close()
        return out
    }
}
