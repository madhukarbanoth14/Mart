package com.mart.distribution.demo.data.demo

import com.mart.distribution.demo.data.api.dto.CreateOrderItemDto
import com.mart.distribution.demo.data.api.dto.CreateOrderRequest
import com.mart.distribution.demo.data.api.dto.CreateBrandRequest
import com.mart.distribution.demo.data.api.dto.CreateProductRequest
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.api.dto.InvoiceDocumentDto
import com.mart.distribution.demo.data.api.dto.MockPaymentResponse
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.OrderItemDto
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.api.dto.AreaDto
import com.mart.distribution.demo.data.api.dto.UserBriefDto
import com.mart.distribution.demo.data.api.dto.UserAreaBriefDto
import com.mart.distribution.demo.data.api.dto.OnboardingDocumentDto
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.api.dto.UpdateProductRequest
import com.mart.distribution.demo.data.session.SessionUser
import java.util.UUID
import kotlin.math.roundToInt

/**
 * In-memory API stand-in for local-demo sessions (no Nest/Postgres).
 * Data mirrors `Mart/backend/prisma/seed.ts` product IDs (`demo-product-1` … `demo-product-12`).
 */
class LocalDemoMartStore {
    private val lock = Any()
    private var orders: MutableList<OrderDto> = mutableListOf()
    private val brands: MutableList<Brand> = STATIC_BRANDS.toMutableList()
    private val products: MutableList<ProductDto> = STATIC_PRODUCTS.toMutableList()
    private val areas: MutableList<AreaDto> =
        mutableListOf(
            AreaDto(
                id = "area-central",
                name = "Central Zone",
                dealerId = DEALER_ID,
                dealer = DEALER_BRIEF,
            ),
        )
    private val onboardedRows: MutableList<UserRowDto> = mutableListOf()
    private val onboardedSessions: MutableMap<String, SessionUser> = mutableMapOf()
    private val onboardingDocumentsByUser: MutableMap<String, MutableList<OnboardingDocumentDto>> =
        mutableMapOf()

    fun resetForNewSession() {
        synchronized(lock) {
            orders = mutableListOf(seedPendingOrder())
            stockQty.clear()
            brands.clear()
            brands.addAll(STATIC_BRANDS)
            products.clear()
            products.addAll(STATIC_PRODUCTS)
            areas.clear()
            areas.add(
                AreaDto(
                    id = "area-central",
                    name = "Central Zone",
                    dealerId = DEALER_ID,
                    dealer = DEALER_BRIEF,
                ),
            )
        }
    }

    fun areas(): List<AreaDto> =
        synchronized(lock) {
            areas.toList()
        }

    /**
     * Offline demo: new shopkeepers sign in with [LocalDemoAuthConfig.DEMO_PASSWORD] after admin approval.
     */
    fun createShopkeeper(
        name: String,
        email: String,
        phone: String?,
        areaId: String,
        onboardedByEmployeeId: String,
        onboardingNotes: String?,
        actorRole: String,
        documents: List<OnboardingDocumentDto> = emptyList(),
    ): UserRowDto {
        val key = email.trim().lowercase()
        require(key.isNotEmpty() && name.isNotBlank()) { "Name and email required" }
        synchronized(lock) {
            require(onboardedRows.none { it.email.equals(key, ignoreCase = true) }) {
                "A user with this email already exists"
            }
            require(STATIC_USERS.none { it.email.equals(key, ignoreCase = true) }) {
                "A user with this email already exists"
            }
            val area =
                when (areaId) {
                    "area-central" -> DEMO_AREA_CENTRAL
                    else -> UserAreaBriefDto(id = areaId, name = areaId)
                }
            val id = "demo-user-local-" + UUID.randomUUID().toString().replace("-", "").take(12)
            val createdAt = java.time.Instant.now().toString()
            val status = if (actorRole.equals("ADMIN", ignoreCase = true)) "ACTIVE" else "PENDING_APPROVAL"
            val onboardedBy = userBriefById(onboardedByEmployeeId)
            val row =
                UserRowDto(
                    id = id,
                    name = name.trim(),
                    email = key,
                    role = "SHOPKEEPER",
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                    area = area,
                    onboardedById = onboardedByEmployeeId,
                    onboardingNotes = onboardingNotes?.trim()?.takeIf { it.isNotEmpty() },
                    createdAt = createdAt,
                    status = status,
                    onboardedBy = onboardedBy,
                    approvedAt = if (status == "ACTIVE") createdAt else null,
                    approvedBy = if (status == "ACTIVE") onboardedBy else null,
                )
            onboardedRows.add(row)
            if (documents.isNotEmpty()) {
                onboardingDocumentsByUser[id] = documents.toMutableList()
            }
            if (status == "ACTIVE") {
                registerActiveSession(row)
            }
            return enrichRow(row)
        }
    }

    fun attachOnboardingDocuments(
        userId: String,
        documents: List<OnboardingDocumentDto>,
    ) {
        synchronized(lock) {
            if (documents.isEmpty()) return
            onboardingDocumentsByUser.getOrPut(userId) { mutableListOf() }.apply {
                clear()
                addAll(documents)
            }
            val idx = onboardedRows.indexOfFirst { it.id == userId }
            if (idx >= 0) {
                onboardedRows[idx] =
                    onboardedRows[idx].copy(onboardingDocuments = documents)
            }
            refreshDocumentFlagsForUser(userId)
        }
    }

    fun listDocuments(userId: String): List<OnboardingDocumentDto> =
        synchronized(lock) {
            onboardingDocumentsByUser[userId].orEmpty().toList()
        }

    fun uploadDocument(
        userId: String,
        documentType: String,
        file: java.io.File,
        displayName: String,
        mimeType: String?,
    ): OnboardingDocumentDto {
        val doc =
            OnboardingDocumentDto(
                id = "doc-${UUID.randomUUID()}",
                label = com.mart.distribution.demo.data.onboarding.BusinessDocumentTypes.labelFor(documentType),
                documentType = documentType,
                fileName = displayName,
                mimeType = mimeType,
                fileSize = file.length(),
                uploadedAt = java.time.Instant.now().toString(),
                verificationStatus = "PENDING_VERIFICATION",
                localPath = file.absolutePath,
            )
        synchronized(lock) {
            onboardingDocumentsByUser.getOrPut(userId) { mutableListOf() }.add(doc)
            refreshDocumentFlagsForUser(userId)
        }
        return doc
    }

    private fun refreshDocumentFlagsForUser(userId: String) {
        val docs = onboardingDocumentsByUser[userId].orEmpty()
        val hasEligible =
            docs.any {
                it.verificationStatus == "PENDING_VERIFICATION" || it.verificationStatus == "VERIFIED"
            }
        val email =
            (STATIC_USERS + onboardedRows).find { it.id == userId }?.email?.lowercase()
                ?: return
        val existing = onboardedSessions[email] ?: return
        onboardedSessions[email] =
            existing.copy(
                documentUploaded = docs.isNotEmpty(),
                canPlaceOrders = hasEligible,
                documentStatus =
                    when {
                        docs.isEmpty() -> "NOT_UPLOADED"
                        docs.all { it.verificationStatus == "REJECTED" } -> "REJECTED"
                        docs.any { it.verificationStatus == "VERIFIED" } -> "VERIFIED"
                        else -> "PENDING_VERIFICATION"
                    },
            )
    }

    private fun enrichRow(row: UserRowDto): UserRowDto =
        row.copy(onboardingDocuments = onboardingDocumentsByUser[row.id].orEmpty())

    fun userById(userId: String): UserRowDto? =
        synchronized(lock) {
            (STATIC_USERS + onboardedRows).find { it.id == userId }?.let(::enrichRow)
        }

    fun createDealer(
        name: String,
        email: String,
        phone: String?,
        areaId: String,
        onboardedByEmployeeId: String,
        onboardingNotes: String?,
        actorRole: String,
        documents: List<OnboardingDocumentDto> = emptyList(),
    ): UserRowDto {
        val key = email.trim().lowercase()
        require(key.isNotEmpty() && name.isNotBlank()) { "Name and email required" }
        synchronized(lock) {
            require(onboardedRows.none { it.email.equals(key, ignoreCase = true) }) {
                "A user with this email already exists"
            }
            require(STATIC_USERS.none { it.email.equals(key, ignoreCase = true) }) {
                "A user with this email already exists"
            }
            val areaIdx = areas.indexOfFirst { it.id == areaId }
            require(areaIdx >= 0) { "Area not found" }
            val id = "demo-user-dealer-local-" + UUID.randomUUID().toString().replace("-", "").take(12)
            val createdAt = java.time.Instant.now().toString()
            val status = if (actorRole.equals("ADMIN", ignoreCase = true)) "ACTIVE" else "PENDING_APPROVAL"
            val onboardedBy = userBriefById(onboardedByEmployeeId)
            val row =
                UserRowDto(
                    id = id,
                    name = name.trim(),
                    email = key,
                    role = "DEALER",
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                    area = null,
                    onboardedById = onboardedByEmployeeId,
                    onboardingNotes = onboardingNotes?.trim()?.takeIf { it.isNotEmpty() },
                    createdAt = createdAt,
                    status = status,
                    onboardedBy = onboardedBy,
                    approvedAt = if (status == "ACTIVE") createdAt else null,
                    approvedBy = if (status == "ACTIVE") onboardedBy else null,
                )
            onboardedRows.add(row)
            areas[areaIdx] =
                areas[areaIdx].copy(
                    dealerId = id,
                    dealer = UserBriefDto(id = id, name = name.trim(), email = key),
                )
            if (documents.isNotEmpty()) {
                onboardingDocumentsByUser[id] = documents.toMutableList()
            }
            if (status == "ACTIVE") {
                registerActiveSession(row)
            }
            return enrichRow(row)
        }
    }

    fun approveUser(
        userId: String,
        adminId: String,
    ): UserRowDto {
        synchronized(lock) {
            val idx = onboardedRows.indexOfFirst { it.id == userId }
            require(idx >= 0) { "User not found" }
            val current = onboardedRows[idx]
            require(current.status == "PENDING_APPROVAL") { "User is not awaiting approval" }
            val admin = userBriefById(adminId) ?: UserBriefDto(adminId, "Admin", null)
            val approvedAt = java.time.Instant.now().toString()
            val updated =
                current.copy(
                    status = "ACTIVE",
                    statusReason = null,
                    approvedAt = approvedAt,
                    approvedBy = admin,
                )
            onboardedRows[idx] = updated
            registerActiveSession(updated)
            return enrichRow(updated)
        }
    }

    /** Offline demo: simulates confirmation email after admin approval. */
    fun approvalConfirmationMessage(row: UserRowDto): String =
        "User approved. In live mode, login credentials would be emailed to ${row.email}."

    fun rejectUser(
        userId: String,
        reason: String?,
    ): UserRowDto {
        synchronized(lock) {
            val idx = onboardedRows.indexOfFirst { it.id == userId }
            require(idx >= 0) { "User not found" }
            val current = onboardedRows[idx]
            require(current.status == "PENDING_APPROVAL") { "User is not awaiting approval" }
            onboardedSessions.remove(current.email.lowercase())
            val updated =
                current.copy(
                    status = "REJECTED",
                    statusReason = reason?.trim()?.takeIf { it.isNotEmpty() } ?: "Rejected by admin",
                )
            onboardedRows[idx] = updated
            return updated
        }
    }

    fun deactivateUser(
        userId: String,
        reason: String?,
    ): UserRowDto {
        synchronized(lock) {
            val idx = onboardedRows.indexOfFirst { it.id == userId }
            require(idx >= 0) { "User not found" }
            val current = onboardedRows[idx]
            require(current.status != "DEACTIVATED") { "User is already deactivated" }
            onboardedSessions.remove(current.email.lowercase())
            val updated =
                current.copy(
                    status = "DEACTIVATED",
                    statusReason = reason?.trim()?.takeIf { it.isNotEmpty() } ?: "Deactivated by admin",
                )
            onboardedRows[idx] = updated
            return updated
        }
    }

    fun reactivateUser(
        userId: String,
        adminId: String,
    ): UserRowDto {
        synchronized(lock) {
            val idx = onboardedRows.indexOfFirst { it.id == userId }
            require(idx >= 0) { "User not found" }
            val current = onboardedRows[idx]
            require(current.status == "DEACTIVATED") { "Only deactivated users can be reactivated" }
            val admin = userBriefById(adminId) ?: UserBriefDto(adminId, "Admin", null)
            val updated =
                current.copy(
                    status = "ACTIVE",
                    statusReason = null,
                    approvedAt = java.time.Instant.now().toString(),
                    approvedBy = admin,
                )
            onboardedRows[idx] = updated
            registerActiveSession(updated)
            return updated
        }
    }

    fun pendingApprovalCount(): Int =
        synchronized(lock) {
            onboardedRows.count {
                it.status == "PENDING_APPROVAL" &&
                    (it.role.equals("DEALER", true) || it.role.equals("SHOPKEEPER", true))
            }
        }

    private fun registerActiveSession(row: UserRowDto) {
        onboardedSessions[row.email.lowercase()] =
            SessionUser(
                id = row.id,
                name = row.name,
                email = row.email,
                role = row.role,
                companyId = "demo-company",
            )
    }

    fun tryResolveOnboardedSession(
        email: String,
        password: String,
    ): SessionUser? {
        if (password != LocalDemoAuthConfig.DEMO_PASSWORD) return null
        val key = email.trim().lowercase()
        synchronized(lock) {
            val row = onboardedRows.find { it.email.equals(key, ignoreCase = true) }
            if (row != null && row.status != "ACTIVE") {
                return null
            }
            return onboardedSessions[key]
        }
    }

    fun loginStatusMessage(email: String): String? {
        val key = email.trim().lowercase()
        synchronized(lock) {
            val row = onboardedRows.find { it.email.equals(key, ignoreCase = true) } ?: return null
            return when (row.status) {
                "PENDING_APPROVAL" ->
                    "Your account is pending admin approval. Please try again after approval."
                "REJECTED" -> "Your account was not approved. Contact your administrator."
                "DEACTIVATED" -> "Your account has been deactivated. Contact your administrator."
                else -> null
            }
        }
    }

    fun products(): List<ProductDto> =
        synchronized(lock) {
            products.toList()
        }

    fun productById(id: String): ProductDto? =
        synchronized(lock) {
            products.firstOrNull { it.id == id }
        }

    fun brands(): List<Brand> =
        synchronized(lock) {
            brands.sortedBy { it.name.lowercase() }
        }

    fun createBrand(body: CreateBrandRequest): Brand {
        synchronized(lock) {
            val name = body.name.trim()
            require(name.isNotEmpty()) { "Brand name is required" }
            require(brands.none { it.name.equals(name, ignoreCase = true) }) {
                "Brand name already exists"
            }
            val created =
                Brand(
                    id = "demo-brand-local-" + UUID.randomUUID().toString().replace("-", "").take(10),
                    name = name,
                    logoUrl = body.logoUrl?.trim()?.takeIf { it.isNotEmpty() },
                )
            brands.add(created)
            return created
        }
    }

    fun deleteBrand(id: String): Brand {
        synchronized(lock) {
            val idx = brands.indexOfFirst { it.id == id }
            require(idx >= 0) { "Brand not found" }
            val removed = brands.removeAt(idx)
            for (i in products.indices) {
                val row = products[i]
                if (row.brandId == removed.id) {
                    products[i] = row.copy(brandId = null, brand = null)
                }
            }
            return removed
        }
    }

    private val stockQty = mutableMapOf<String, Int>()

    fun stock(): List<StockRowDto> =
        synchronized(lock) {
            stockQty.mapNotNull { (productId, qty) ->
                val p = products.firstOrNull { it.id == productId } ?: return@mapNotNull null
                StockRowDto(
                    id = "demo-stock-$productId",
                    quantity = qty,
                    product = p,
                    dealer = DEALER_BRIEF,
                )
            }
        }

    fun updateStockQuantity(stockId: String, quantity: Int): StockRowDto {
        val productId =
            stockId.removePrefix("demo-stock-").ifBlank {
                throw IllegalArgumentException("Invalid stock row")
            }
        synchronized(lock) {
            require(products.any { it.id == productId }) { "Product not found" }
            stockQty[productId] = quantity.coerceAtLeast(0)
            val p = products.first { it.id == productId }
            return StockRowDto(
                id = "demo-stock-$productId",
                quantity = stockQty.getValue(productId),
                product = p,
                dealer = DEALER_BRIEF,
            )
        }
    }

    fun upsertStock(productId: String, quantity: Int): StockRowDto =
        updateStockQuantity("demo-stock-$productId", quantity)

    fun users(): List<UserRowDto> =
        synchronized(lock) {
            (STATIC_USERS + onboardedRows).map(::enrichRow)
        }

    fun createEmployee(
        name: String,
        email: String,
        phone: String?,
    ): UserRowDto {
        val key = email.trim().lowercase()
        require(key.isNotEmpty() && name.isNotBlank()) { "Name and email required" }
        synchronized(lock) {
            require(onboardedRows.none { it.email.equals(key, ignoreCase = true) }) {
                "A user with this email already exists"
            }
            require(STATIC_USERS.none { it.email.equals(key, ignoreCase = true) }) {
                "A user with this email already exists"
            }
            val id = "demo-user-employee-local-" + UUID.randomUUID().toString().replace("-", "").take(12)
            val createdAt = java.time.Instant.now().toString()
            val row =
                UserRowDto(
                    id = id,
                    name = name.trim(),
                    email = key,
                    role = "EMPLOYEE",
                    phone = phone?.trim()?.takeIf { it.isNotEmpty() },
                    createdAt = createdAt,
                    status = "ACTIVE",
                    approvedAt = createdAt,
                )
            onboardedRows.add(row)
            registerActiveSession(row)
            return enrichRow(row)
        }
    }

    fun ordersForActor(
        userId: String,
        role: String,
    ): List<OrderDto> {
        val all = synchronized(lock) { orders.toList() }
        return when (role.uppercase()) {
            "SHOPKEEPER" -> all.filter { it.shopkeeperId == userId }
            "DEALER" ->
                all.filter {
                    it.dealerId == userId ||
                        (it.kind.equals("DEALER_RESTOCK", true) && it.shopkeeperId == userId)
                }
            else -> all
        }
    }

    fun createOrder(
        actorId: String,
        body: CreateOrderRequest,
    ): OrderDto {
        val shopkeeper = userBriefById(actorId) ?: error("Shopkeeper not in demo roster")
        val shopkeeperAreaId =
            synchronized(lock) {
                (STATIC_USERS + onboardedRows)
                    .firstOrNull { it.id == actorId }
                    ?.area
                    ?.id
            } ?: "area-central"
        val assignedDealer =
            synchronized(lock) {
                areas.firstOrNull { it.id == shopkeeperAreaId }?.dealer
            } ?: DEALER_BRIEF
        val productById =
            synchronized(lock) {
                products.associateBy { it.id }
            }
        val items = mutableListOf<OrderItemDto>()
        var subtotal = 0.0
        var gst = 0.0
        var discount = 0.0
        for (line in body.items) {
            val p = productById[line.productId] ?: continue
            val unit = (p.dealerPrice as? Number)?.toDouble() ?: (p.basePrice as? Number)?.toDouble() ?: 80.0
            val discPct = (p.shopkeeperDiscount as? Number)?.toDouble() ?: 5.0
            val lineSub = unit * line.quantity
            val lineDisc = lineSub * (discPct / 100.0)
            val taxable = lineSub - lineDisc
            val gstRate = (p.gstPercentage as? Number)?.toDouble() ?: 18.0
            val lineGst = taxable * (gstRate / 100.0)
            val final = taxable + lineGst
            subtotal += lineSub
            discount += lineDisc
            gst += lineGst
            items +=
                OrderItemDto(
                    productId = line.productId,
                    quantity = line.quantity,
                    price = unit,
                    gstAmount = lineGst,
                    discountAmount = lineDisc,
                    finalAmount = final,
                    product = p,
                )
        }
        val finalAmount = subtotal - discount + gst
        val id = "demo-local-" + System.currentTimeMillis()
        val order =
            OrderDto(
                id = id,
                status = "PENDING",
                paymentStatus = "UNPAID",
                shopkeeperId = actorId,
                dealerId = assignedDealer.id,
                totalAmount = round2(subtotal),
                gstAmount = round2(gst),
                discountAmount = round2(discount),
                finalAmount = round2(finalAmount),
                items = items,
                shopkeeper = shopkeeper,
                dealer = assignedDealer,
            )
        synchronized(lock) {
            orders.add(0, order)
        }
        return order
    }

    fun createDealerRestockOrder(
        dealerId: String,
        body: CreateOrderRequest,
    ): OrderDto {
        val dealer = userBriefById(dealerId) ?: error("Dealer not in demo roster")
        val warehouse = UserBriefDto(id = "demo-user-admin", name = "KNSR Warehouse", email = "admin@martdemo.com")
        val productById =
            synchronized(lock) {
                products.associateBy { it.id }
            }
        val items = mutableListOf<OrderItemDto>()
        var subtotal = 0.0
        var gst = 0.0
        var discount = 0.0
        for (line in body.items) {
            val p = productById[line.productId] ?: continue
            val unit = (p.dealerPrice as? Number)?.toDouble() ?: (p.basePrice as? Number)?.toDouble() ?: 80.0
            val discPct = (p.dealerDiscount as? Number)?.toDouble() ?: 10.0
            val lineSub = unit * line.quantity
            val lineDisc = lineSub * (discPct / 100.0)
            val taxable = lineSub - lineDisc
            val gstRate = (p.gstPercentage as? Number)?.toDouble() ?: 18.0
            val lineGst = taxable * (gstRate / 100.0)
            val final = taxable + lineGst
            subtotal += lineSub
            discount += lineDisc
            gst += lineGst
            items +=
                OrderItemDto(
                    productId = line.productId,
                    quantity = line.quantity,
                    price = unit,
                    gstAmount = lineGst,
                    discountAmount = lineDisc,
                    finalAmount = final,
                    product = p,
                )
        }
        val finalAmount = subtotal - discount + gst
        val id = "demo-restock-" + System.currentTimeMillis()
        val order =
            OrderDto(
                id = id,
                status = "PENDING",
                kind = "DEALER_RESTOCK",
                shopkeeperId = dealerId,
                dealerId = warehouse.id,
                totalAmount = round2(subtotal),
                gstAmount = round2(gst),
                discountAmount = round2(discount),
                finalAmount = round2(finalAmount),
                items = items,
                shopkeeper = dealer,
                dealer = warehouse,
            )
        synchronized(lock) {
            orders.add(0, order)
            for (line in body.items) {
                if (productById.containsKey(line.productId)) {
                    stockQty[line.productId] =
                        (stockQty[line.productId] ?: 0) + line.quantity
                }
            }
        }
        return order
    }

    fun orderById(orderId: String): OrderDto? =
        synchronized(lock) {
            orders.find { it.id == orderId }
        }

    fun previewReorder(orderId: String): com.mart.distribution.demo.data.api.dto.ReorderPreviewDto {
        val unavailable =
            "This product is currently unavailable and has been removed from the reorder cart."
        synchronized(lock) {
            val order = orders.find { it.id == orderId }
                ?: throw IllegalArgumentException("Order not found")
            val items = mutableListOf<com.mart.distribution.demo.data.api.dto.ReorderPreviewItemDto>()
            val skipped = mutableListOf<com.mart.distribution.demo.data.api.dto.ReorderSkippedItemDto>()
            val warnings = mutableListOf<String>()
            for (line in order.items.orEmpty()) {
                val product = products().find { it.id == line.productId }
                if (product == null) {
                    skipped.add(
                        com.mart.distribution.demo.data.api.dto.ReorderSkippedItemDto(
                            productId = line.productId,
                            reason = "deleted",
                        ),
                    )
                    warnings.add(unavailable)
                    continue
                }
                val available = stockQty[product.id] ?: 0
                if (available < line.quantity) {
                    skipped.add(
                        com.mart.distribution.demo.data.api.dto.ReorderSkippedItemDto(
                            productId = line.productId,
                            productName = product.name,
                            reason = "out_of_stock",
                        ),
                    )
                    warnings.add(unavailable)
                    continue
                }
                items.add(
                    com.mart.distribution.demo.data.api.dto.ReorderPreviewItemDto(
                        productId = line.productId,
                        quantity = line.quantity,
                        product = product,
                    ),
                )
            }
            return com.mart.distribution.demo.data.api.dto.ReorderPreviewDto(
                items = items,
                warnings = warnings.distinct(),
                skipped = skipped,
            )
        }
    }

    fun mockPayment(orderId: String): MockPaymentResponse {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val updated = orders[idx].copy(paymentStatus = "PAID")
            orders[idx] = updated
        }
        return MockPaymentResponse(orderId, "MOCK_CARD", "SUCCEEDED", "Payment successful (demo card)")
    }

    fun confirmOrder(orderId: String): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(o.status.equals("PENDING", ignoreCase = true)) { "Only pending orders can be confirmed" }
            if (!o.kind.equals("DEALER_RESTOCK", ignoreCase = true)) {
                for (item in o.items.orEmpty()) {
                    val current = stockQty.getOrPut(item.productId) { 200 }
                    stockQty[item.productId] = (current - item.quantity).coerceAtLeast(0)
                }
            }
            val updated = o.copy(status = "DEALER_CONFIRMED")
            orders[idx] = updated
            return updated
        }
    }

    fun markOutForDelivery(orderId: String): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(
                o.status.equals("DEALER_CONFIRMED", ignoreCase = true) ||
                    o.status.equals("ACCEPTED", ignoreCase = true),
            ) {
                "Only confirmed orders can be marked out for delivery"
            }
            val updated = o.copy(status = "OUT_FOR_DELIVERY")
            orders[idx] = updated
            return updated
        }
    }

    fun markDelivered(orderId: String): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(o.status.equals("OUT_FOR_DELIVERY", ignoreCase = true)) { "Only out-for-delivery orders can be marked delivered" }
            val updated = o.copy(status = "DELIVERED")
            orders[idx] = updated
            return updated
        }
    }

    fun cancelOrder(orderId: String): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(o.status.equals("PENDING", ignoreCase = true)) { "Only pending orders can be cancelled" }
            val updated = o.copy(status = "CANCELLED")
            orders[idx] = updated
            return updated
        }
    }

    fun requestReturn(orderId: String, reason: String): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(o.status.equals("DELIVERED", ignoreCase = true)) { "Only delivered orders can be returned" }
            val updated =
                o.copy(
                    status = "RETURN_REQUESTED",
                    returnReason = reason.trim(),
                    returnRequestedAt = java.time.Instant.now().toString(),
                )
            orders[idx] = updated
            return updated
        }
    }

    fun approveReturn(orderId: String): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(o.status.equals("RETURN_REQUESTED", ignoreCase = true)) { "No return request pending" }
            val now = java.time.Instant.now().toString()
            val updated =
                o.copy(
                    status = "RETURNED",
                    returnedAt = now,
                    paymentStatus =
                        if (o.paymentStatus.equals("PAID", ignoreCase = true)) {
                            "REFUNDED"
                        } else {
                            o.paymentStatus
                        },
                    refundedAt = if (o.paymentStatus.equals("PAID", ignoreCase = true)) now else o.refundedAt,
                )
            orders[idx] = updated
            return updated
        }
    }

    fun rejectReturn(orderId: String, note: String?): OrderDto {
        synchronized(lock) {
            val idx = orders.indexOfFirst { it.id == orderId }
            require(idx >= 0) { "Order not found" }
            val o = orders[idx]
            require(o.status.equals("RETURN_REQUESTED", ignoreCase = true)) { "No return request pending" }
            val updated =
                o.copy(
                    status = "DELIVERED",
                    returnReason = note?.trim()?.takeIf { it.isNotEmpty() } ?: o.returnReason,
                )
            orders[idx] = updated
            return updated
        }
    }

    fun createProduct(body: CreateProductRequest): ProductDto {
        synchronized(lock) {
            val id = "demo-product-local-" + UUID.randomUUID().toString().replace("-", "").take(10)
            val row =
                ProductDto(
                    id = id,
                    name = body.name.trim(),
                    brandType = body.brandType.uppercase(),
                    brandId = body.brandId,
                    brand = brands.firstOrNull { it.id == body.brandId },
                    shelf = body.shelf.uppercase(),
                    basePrice = round2(body.basePrice),
                    gstPercentage = round2(body.gstPercentage),
                    dealerDiscount = round2(body.dealerDiscount),
                    shopkeeperDiscount = round2(body.shopkeeperDiscount),
                )
            products.add(row)
            return row
        }
    }

    fun updateProduct(
        id: String,
        body: UpdateProductRequest,
    ): ProductDto {
        synchronized(lock) {
            val idx = products.indexOfFirst { it.id == id }
            require(idx >= 0) { "Product not found" }
            val old = products[idx]
            val updated =
                old.copy(
                    name = body.name?.trim()?.takeIf { it.isNotEmpty() } ?: old.name,
                    brandType = body.brandType?.uppercase() ?: old.brandType,
                    brandId = body.brandId ?: old.brandId,
                    brand = brands.firstOrNull { it.id == (body.brandId ?: old.brandId) },
                    shelf = body.shelf?.uppercase() ?: old.shelf,
                    basePrice = body.basePrice?.let(::round2) ?: old.basePrice,
                    gstPercentage = body.gstPercentage?.let(::round2) ?: old.gstPercentage,
                    dealerDiscount = body.dealerDiscount?.let(::round2) ?: old.dealerDiscount,
                    shopkeeperDiscount = body.shopkeeperDiscount?.let(::round2) ?: old.shopkeeperDiscount,
                )
            products[idx] = updated
            return updated
        }
    }

    fun deleteProduct(id: String): ProductDto {
        synchronized(lock) {
            val idx = products.indexOfFirst { it.id == id }
            require(idx >= 0) { "Product not found" }
            return products.removeAt(idx)
        }
    }

    fun invoiceByOrder(orderId: String): InvoiceDocumentDto {
        val o =
            synchronized(lock) {
                orders.find { it.id == orderId }
            } ?: error("Order not found")
        require(
            o.paymentStatus.equals("PAID", ignoreCase = true) ||
                o.status.equals("DEALER_CONFIRMED", ignoreCase = true) ||
                o.status.equals("DELIVERED", ignoreCase = true),
        ) {
            "Invoice available after payment or dealer confirmation"
        }
        return InvoiceDocumentDto(
            invoiceNumber = "INV-OFFLINE-${o.id.takeLast(8).uppercase()}",
            generatedAt = java.time.Instant.now().toString(),
            pdfUrl = null,
            order = o,
        )
    }

    private fun seedPendingOrder(): OrderDto {
        val p =
            synchronized(lock) {
                products.firstOrNull()
            } ?: STATIC_PRODUCTS[0]
        val unit = (p.basePrice as Number).toDouble()
        val qty = 2
        val lineSub = unit * qty
        val lineDisc = lineSub * 0.05
        val taxable = lineSub - lineDisc
        val lineGst = taxable * 0.18
        val final = taxable + lineGst
        val item =
            OrderItemDto(
                productId = p.id,
                quantity = qty,
                price = unit,
                gstAmount = lineGst,
                discountAmount = lineDisc,
                finalAmount = final,
                product = p,
            )
        return OrderDto(
            id = "demo-local-seed-1",
            status = "PENDING",
            shopkeeperId = SHOP1_ID,
            dealerId = DEALER_ID,
            totalAmount = round2(lineSub),
            gstAmount = round2(lineGst),
            discountAmount = round2(lineDisc),
            finalAmount = round2(final),
            items = listOf(item),
            shopkeeper = userBriefById(SHOP1_ID),
            dealer = DEALER_BRIEF,
        )
    }

    private fun userBriefById(id: String): UserBriefDto? {
        synchronized(lock) {
            STATIC_USERS.find { it.id == id }?.let {
                return UserBriefDto(it.id, it.name, it.email)
            }
            onboardedRows.find { it.id == id }?.let {
                return UserBriefDto(it.id, it.name, it.email)
            }
        }
        return when (id) {
            DEALER_ID -> DEALER_BRIEF
            else -> null
        }
    }

    private companion object {
        const val DEALER_ID = "demo-user-dealer"
        const val SHOP1_ID = "demo-user-shop1"
        const val SHOP2_ID = "demo-user-shop2"

        val DEALER_BRIEF = UserBriefDto(DEALER_ID, "City Dealer", "dealer@martdemo.com")

        val STATIC_BRANDS: List<Brand> =
            listOf(
                Brand("demo-brand-coca-cola", "Coca-Cola", null),
                Brand("demo-brand-varun-beverages", "Varun Beverages Ltd (Pepsi)", null),
                Brand("demo-brand-everest", "Everest", null),
                Brand("demo-brand-marico", "Marico", null),
                Brand("demo-brand-dabur", "Dabur", null),
                Brand("demo-brand-tata-consumer", "Tata Consumer Products", null),
                Brand("demo-brand-godrej", "Godrej", null),
                Brand("demo-brand-aachi", "Aachi", null),
                Brand("demo-brand-mtr", "MTR", null),
                Brand("demo-brand-anmol", "Anmol", null),
                Brand("demo-brand-wipro-consumer", "Wipro Consumer", null),
                Brand("demo-brand-hul", "Hindustan Unilever Limited (HUL)", null),
                Brand("demo-brand-pg", "P&G", null),
                Brand("demo-brand-itc", "ITC Limited", null),
                Brand("demo-brand-jumbofarms", "Jumbofarms", null),
                Brand("demo-brand-britannia", "Britannia", null),
                Brand("demo-brand-nestle", "Nestlé", null),
                Brand("demo-brand-cadbury", "Cadbury", null),
                Brand("demo-brand-lavian", "Lávian", null),
                Brand("demo-brand-colgate", "Colgate", null),
                Brand("demo-brand-reckitt", "Reckitt", null),
            )
        val BRAND_IDS: Map<String, String> = STATIC_BRANDS.associate { it.name to it.id }

        /** Readable FMCG-style names; IDs stay `demo-product-*` for parity with `prisma/seed.ts`. */
        val STATIC_PRODUCTS: List<ProductDto> =
            listOf(
                ProductDto("demo-product-1", "KNSR Premium Sona Masoori Rice (25 kg)", "OWN", BRAND_IDS["Jumbofarms"], null, "STAPLES", 1549, 18, 10, 5),
                ProductDto("demo-product-2", "KNSR Fortified Wheat Atta (10 kg)", "OWN", BRAND_IDS["Jumbofarms"], null, "STAPLES", 389, 18, 10, 5),
                ProductDto("demo-product-3", "KNSR Unpolished Toor Dal (5 kg)", "OWN", BRAND_IDS["Jumbofarms"], null, "STAPLES", 675, 18, 10, 5),
                ProductDto("demo-product-4", "KNSR Refined Sunflower Oil (5 L)", "OWN", BRAND_IDS["Marico"], null, "OILS_GHEE", 899, 18, 10, 5),
                ProductDto("demo-product-5", "KNSR Crystal Sugar (5 kg)", "OWN", BRAND_IDS["Jumbofarms"], null, "SUGAR_SALT_BASICS", 265, 18, 10, 5),
                ProductDto("demo-product-6", "KNSR Iodized Salt (12 × 1 kg)", "OWN", BRAND_IDS["Aachi"], null, "SUGAR_SALT_BASICS", 198, 18, 10, 5),
                ProductDto("demo-product-7", "National Kitchen King Masala (200 g)", "OTHER", BRAND_IDS["Everest"], null, "STAPLES", 185, 18, 10, 5),
                ProductDto("demo-product-8", "Tata Tea Gold (500 g)", "OTHER", BRAND_IDS["Tata Consumer Products"], null, "BEVERAGES", 378, 18, 10, 5),
                ProductDto("demo-product-9", "Nescafé Classic Coffee (200 g)", "OTHER", BRAND_IDS["Nestlé"], null, "BEVERAGES", 520, 18, 10, 5),
                ProductDto("demo-product-10", "Lizol Citrus Disinfectant (2 L)", "OTHER", BRAND_IDS["Reckitt"], null, "HOME_CARE", 295, 18, 10, 5),
                ProductDto("demo-product-11", "Britannia Good Day Cookies (Family pack)", "OTHER", BRAND_IDS["Britannia"], null, "SNACKS_BISCUITS", 145, 18, 10, 5),
                ProductDto("demo-product-12", "Bisleri Packaged Water (20 L jar)", "OTHER", BRAND_IDS["Coca-Cola"], null, "BEVERAGES", 85, 18, 10, 5),
            ).map { row ->
                row.copy(brand = STATIC_BRANDS.firstOrNull { it.id == row.brandId })
            }

        private val DEMO_AREA_CENTRAL = UserAreaBriefDto(id = "area-central", name = "Central Zone")

        val STATIC_USERS: List<UserRowDto> =
            listOf(
                UserRowDto("demo-user-admin", "Super Admin", "admin@martdemo.com", "ADMIN", "9000000001"),
                UserRowDto("demo-user-employee", "Field Employee", "employee@martdemo.com", "EMPLOYEE", "9000000002"),
                UserRowDto("demo-user-dealer", "City Dealer", "dealer@martdemo.com", "DEALER", "9000000003"),
                UserRowDto(
                    "demo-user-shop1",
                    "Shopkeeper One",
                    "shop1@martdemo.com",
                    "SHOPKEEPER",
                    "9000000004",
                    area = DEMO_AREA_CENTRAL,
                ),
                UserRowDto(
                    "demo-user-shop2",
                    "Shopkeeper Two",
                    "shop2@martdemo.com",
                    "SHOPKEEPER",
                    "9000000005",
                    area = DEMO_AREA_CENTRAL,
                ),
            )

        fun round2(v: Double): Double = (v * 100).roundToInt() / 100.0
    }
}
