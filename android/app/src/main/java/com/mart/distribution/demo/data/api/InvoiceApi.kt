package com.mart.distribution.demo.data.api

import com.mart.distribution.demo.data.api.dto.InvoiceDocumentDto
import retrofit2.http.GET
import retrofit2.http.Path

interface InvoiceApi {
    @GET("invoices/by-order/{orderId}")
    suspend fun invoiceByOrder(
        @Path("orderId") orderId: String,
    ): InvoiceDocumentDto
}
