package com.mart.distribution.demo.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mart.distribution.demo.data.api.MartApi
import com.mart.distribution.demo.data.api.dto.AreaDto
import com.mart.distribution.demo.data.api.dto.CreateOrderItemDto
import com.mart.distribution.demo.data.api.dto.CreateOrderRequest
import com.mart.distribution.demo.data.api.dto.CreateOrderWithPaymentRequest
import com.mart.distribution.demo.data.api.dto.CreateDealerRequest
import com.mart.distribution.demo.data.api.dto.CreateDealerResponse
import com.mart.distribution.demo.data.api.dto.CreateEmployeeRequest
import android.content.Context
import com.mart.distribution.demo.data.onboarding.OnboardingDocumentStorage
import com.mart.distribution.demo.data.onboarding.PendingOnboardingDocument
import com.mart.distribution.demo.data.api.dto.CreateProductRequest
import com.mart.distribution.demo.data.api.dto.CreateRazorpayOrderRequest
import com.mart.distribution.demo.data.api.dto.CreateAreaRequest
import com.mart.distribution.demo.data.api.dto.UpdateAreaRequest
import com.mart.distribution.demo.data.api.dto.CreateShopkeeperRequest
import com.mart.distribution.demo.data.api.dto.DealerSummaryDto
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.ProductsPagedResponse
import com.mart.distribution.demo.data.api.dto.ShopkeeperSummaryDto
import com.mart.distribution.demo.data.api.dto.StockRowDto
import com.mart.distribution.demo.data.api.dto.UpdateStockRequest
import com.mart.distribution.demo.data.api.dto.UpsertStockRequest
import com.mart.distribution.demo.data.api.dto.UpdateProductRequest
import com.mart.distribution.demo.data.api.dto.UpdateUserStatusRequest
import com.mart.distribution.demo.data.api.dto.catalogUnitPrice
import com.mart.distribution.demo.data.api.dto.discountPercentForRole
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.api.dto.VerifyRazorpayPaymentRequest
import com.mart.distribution.demo.data.demo.LocalDemoAuthConfig
import com.mart.distribution.demo.data.cart.CartLine
import com.mart.distribution.demo.data.cart.CartRepository
import com.mart.distribution.demo.data.demo.DemoFlowRepository
import com.mart.distribution.demo.data.demo.LocalDemoMartStore
import com.mart.distribution.demo.data.push.PushTokenRegistrar
import com.mart.distribution.demo.data.session.SessionRepository
import com.mart.distribution.demo.util.normalizeAreaName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File

sealed class LoadState<out T> {
    data object Idle : LoadState<Nothing>()

    data object Loading : LoadState<Nothing>()

    data class Ok<T>(
        val data: T,
    ) : LoadState<T>()

    data class Err(
        val message: String,
    ) : LoadState<Nothing>()
}

/** Shopkeeper home: paginated product feed with search + brand filters. */
data class ShopkeeperHomeFeedState(
    val items: List<ProductDto> = emptyList(),
    val page: Int = 0,
    val hasMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val appliedSearch: String? = null,
    val appliedBrandId: String? = null,
    val totalCount: Int? = null,
)

data class MainUiState(
    val products: LoadState<List<ProductDto>> = LoadState.Idle,
    val shelves: LoadState<List<String>> = LoadState.Idle,
    val orders: LoadState<List<OrderDto>> = LoadState.Idle,
    val stock: LoadState<List<StockRowDto>> = LoadState.Idle,
    val users: LoadState<List<UserRowDto>> = LoadState.Idle,
    val areas: LoadState<List<AreaDto>> = LoadState.Idle,
    val placeOrderResult: String? = null,
    val placeOrderError: String? = null,
    val placedOrder: OrderDto? = null,
    val pendingRazorpayOrderId: String? = null,
    val pendingRazorpayGatewayOrderId: String? = null,
    val pendingRazorpayKeyId: String? = null,
    val pendingRazorpayAmountPaise: Int? = null,
    val pendingRazorpayCurrency: String? = null,
    val pendingRazorpayMethod: String? = null,
    val paymentMessage: String? = null,
    val shopkeeperSummary: ShopkeeperSummaryDto? = null,
    val dealerSummary: DealerSummaryDto? = null,
    val shopkeeperHomeFeed: ShopkeeperHomeFeedState = ShopkeeperHomeFeedState(),
    /** productId -> available quantity from the shopkeeper's assigned dealer. */
    val availability: Map<String, Int> = emptyMap(),
    /** True only when availability was fetched successfully (gating is off otherwise). */
    val availabilityLoaded: Boolean = false,
    val myDocuments: LoadState<List<com.mart.distribution.demo.data.api.dto.OnboardingDocumentDto>> = LoadState.Idle,
    val documentCheckoutBlocked: Boolean = false,
    val maxOrderQuantity: Int = 10000,
    val quickQuantityChips: List<Int> = listOf(10, 25, 50, 100, 250, 500, 1000),
)

private const val SHOPKEEPER_HOME_PAGE_SIZE = 24
/** Backend caps `GET /products` at 100 per page. */
private const val PRODUCTS_API_PAGE_SIZE = 100
private const val CATALOG_CACHE_TTL_MS = 60_000L

class MainViewModel(
    private val martApi: MartApi,
    private val sessionRepository: SessionRepository,
    private val cartRepository: CartRepository,
    private val localDemoMartStore: LocalDemoMartStore,
    private val demoFlowRepository: DemoFlowRepository,
    private val appContext: Context,
    private val pushTokenRegistrar: PushTokenRegistrar,
) : ViewModel() {
    /** Client-side paging cache after loading full catalog from `GET /products`. */
    private var homeProductsCache: List<ProductDto>? = null
    private var homeProductsCacheKey: String? = null
    /** After first 404, skip repeated `GET /products/paged` attempts. */
    private var homePagedEndpointMissing: Boolean? = null
    private var loadProductsJob: Job? = null
    private var catalogCache: List<ProductDto>? = null
    private var catalogCacheAtMs: Long = 0L
    /** Cart snapshot taken before Razorpay checkout; restored if payment is cancelled. */
    private var razorpayCartRestore: List<CartLine>? = null
    /** Survives [clearPendingRazorpayLaunch] so callbacks can verify after the SDK closes. */
    private var activeRazorpayAppOrderId: String? = null
    private var activeRazorpayGatewayOrderId: String? = null
    private var razorpayResultHandled: Boolean = false

    private fun shouldShowLoading(current: LoadState<*>): Boolean = current !is LoadState.Ok

    private fun currentUserRoleOrDefault(): String =
        sessionRepository.getUserRole()?.uppercase() ?: "SHOPKEEPER"
    private fun nestJsonMessage(body: String): String? {
        return try {
            val o = JSONObject(body)
            if (!o.has("message") || o.isNull("message")) {
                null
            } else {
                when (val raw = o.get("message")) {
                    is String -> raw
                    is JSONArray ->
                        buildString {
                            val a = raw
                            for (i in 0 until a.length()) {
                                if (i > 0) append(' ')
                                append(a.optString(i))
                            }
                        }
                    else -> raw?.toString()
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun httpReadableMessage(e: Exception): String {
        if (e is HttpException) {
            val body = e.response()?.errorBody()?.string().orEmpty()
            val nested = nestJsonMessage(body)?.trim()?.takeIf { it.isNotEmpty() }
            val compact = body.replace("\n", " ").trim()
            val msg =
                nested ?: when {
                    compact.contains("\"message\"") -> compact
                    compact.isNotBlank() -> compact
                    else -> e.message()
                }
            return when (e.code()) {
                400, 422 -> msg.ifBlank { "Please check input and try again." }
                401 -> "Session expired. Please login again."
                403 ->
                    if (body.contains("DOCUMENT_REQUIRED") || msg.contains("upload at least one", true)) {
                        "To place orders, please upload at least one valid business document for verification."
                    } else {
                        msg.ifBlank { "You do not have permission for this action." }
                    }
                409 -> msg.ifBlank { "A user with this email or phone already exists." }
                500 ->
                    nested
                        ?: msg.takeIf { it.isNotBlank() && it.length < 500 }
                        ?: "Something went wrong. Please try again."
                404 ->
                    "Catalog API not found on server. Pull latest backend or use an updated API URL."
                else -> msg.ifBlank { "Request failed (${e.code()})." }
            }
        }
        return e.message ?: "Something went wrong. Please try again."
    }

    /**
     * Cloud Run may lag behind the app: older backends have only [MartApi.createOrder] (`POST /orders`),
     * not `POST /orders/create`. Fall back on 404 so release APK works until API is redeployed.
     */
    private suspend fun createShopkeeperOrderWithLegacyFallback(
        bodyLegacy: CreateOrderRequest,
        bodyCreate: CreateOrderWithPaymentRequest,
    ): OrderDto =
        try {
            val createdResp = martApi.createOrderWithPayment(bodyCreate)
            martApi.orderById(createdResp.orderId)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                martApi.createOrder(bodyLegacy)
            } else {
                throw e
            }
        }

    private suspend fun createDealerRestockWithLegacyFallback(
        bodyLegacy: CreateOrderRequest,
        bodyCreate: CreateOrderWithPaymentRequest,
    ): OrderDto =
        try {
            val createdResp = martApi.createDealerRestockOrderWithPayment(bodyCreate)
            martApi.orderById(createdResp.orderId)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                martApi.createDealerRestockOrder(bodyLegacy)
            } else {
                throw e
            }
        }

    private suspend fun currentUserRole(): String =
        sessionRepository.sessionUserFlow.first()?.role?.uppercase() ?: "SHOPKEEPER"

    private val _ui = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _ui.asStateFlow()

    val cartLines = cartRepository.lines

    init {
        viewModelScope.launch {
            var previousUserId: String? = sessionRepository.getUserSnapshot()?.id
            sessionRepository.sessionUserFlow.collect { user ->
                val currentUserId = user?.id
                if (previousUserId != null &&
                    currentUserId != null &&
                    previousUserId != currentUserId
                ) {
                    clearSessionScopedState()
                }
                previousUserId = currentUserId
            }
        }
    }

    /** Clears in-memory cart and role-specific UI when the signed-in user changes. */
    private fun clearSessionScopedState() {
        razorpayCartRestore = null
        activeRazorpayAppOrderId = null
        activeRazorpayGatewayOrderId = null
        razorpayResultHandled = false
        cartRepository.clear()
        homeProductsCache = null
        homeProductsCacheKey = null
        catalogCache = null
        catalogCacheAtMs = 0L
        _ui.value = MainUiState()
    }

    fun refreshForRole() {
        viewModelScope.launch {
            val user =
                sessionRepository.getUserSnapshot()
                    ?: sessionRepository.sessionUserFlow.first()
                    ?: return@launch
            when (user.role.uppercase()) {
                "EMPLOYEE" -> {
                    _ui.update {
                        it.copy(
                            products = LoadState.Idle,
                            orders = LoadState.Idle,
                            stock = LoadState.Idle,
                        )
                    }
                    loadUsers()
                    loadAreas()
                }
                "ADMIN", "SUPER_ADMIN" -> {
                    loadProducts()
                    loadShelves()
                    loadOrders()
                    loadUsers()
                    loadAreas()
                    _ui.update { it.copy(stock = LoadState.Idle) }
                }
                "DEALER" -> {
                    _ui.update {
                        it.copy(
                            products = LoadState.Idle,
                            shelves = LoadState.Idle,
                            users = LoadState.Idle,
                            areas = LoadState.Idle,
                        )
                    }
                    coroutineScope {
                        async { loadOrdersSuspend(showLoading = shouldShowLoading(_ui.value.orders)) }.await()
                        async { loadStockSuspend(showLoading = shouldShowLoading(_ui.value.stock)) }.await()
                        async { loadDealerSummarySuspend() }.await()
                        async { loadOrderingConfigSuspend() }.await()
                    }
                }
                else -> {
                    _ui.update {
                        it.copy(
                            stock = LoadState.Idle,
                            users = LoadState.Idle,
                            areas = LoadState.Idle,
                            products = LoadState.Idle,
                            shelves = LoadState.Idle,
                        )
                    }
                    coroutineScope {
                        async { loadOrdersSuspend(showLoading = shouldShowLoading(_ui.value.orders)) }.await()
                        async { loadShopkeeperSummarySuspend() }.await()
                        async { loadAvailabilitySuspend() }.await()
                        async { loadOrderingConfigSuspend() }.await()
                    }
                }
            }
        }
    }

    fun loadAvailability() {
        viewModelScope.launch { loadAvailabilitySuspend() }
    }

    fun loadOrderingConfig() {
        viewModelScope.launch { loadOrderingConfigSuspend() }
    }

    private suspend fun loadOrderingConfigSuspend() {
        if (sessionRepository.isLocalDemoMode()) return
        try {
            val cfg = martApi.orderingConfig()
            _ui.update {
                it.copy(
                    maxOrderQuantity = cfg.maxOrderQuantity,
                    quickQuantityChips = cfg.quickQuantityChips,
                )
            }
        } catch (_: Exception) {
            // Keep defaults from MainUiState.
        }
    }

    fun reorderFromOrder(
        orderId: String,
        onComplete: (success: Boolean, message: String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val preview =
                    if (sessionRepository.isLocalDemoMode()) {
                        localDemoMartStore.previewReorder(orderId)
                    } else {
                        martApi.reorderPreview(orderId)
                    }
                cartRepository.clear()
                val role = currentUserRoleOrDefault()
                for (item in preview.items) {
                    val product = item.product ?: continue
                    cartRepository.setLineQuantity(product, item.quantity, role)
                }
                onComplete(preview.items.isNotEmpty(), preview.warnings.firstOrNull())
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Could not reorder")
            }
        }
    }

    /**
     * Loads the shopkeeper's assigned-dealer stock so the catalog can block
     * out-of-stock items. Gating stays off (availabilityLoaded=false) if this
     * fails or the backend doesn't support the endpoint, so ordering still works.
     */
    private suspend fun loadAvailabilitySuspend() {
        if (sessionRepository.isLocalDemoMode()) {
            _ui.update { it.copy(availabilityLoaded = false) }
            return
        }
        val role = sessionRepository.sessionUserFlow.first()?.role?.uppercase()
        if (role != "SHOPKEEPER") {
            _ui.update { it.copy(availabilityLoaded = false) }
            return
        }
        try {
            val rows = martApi.availableStock()
            val map = rows.associate { it.productId to it.quantity }
            _ui.update { it.copy(availability = map, availabilityLoaded = true) }
        } catch (_: Exception) {
            _ui.update { it.copy(availabilityLoaded = false) }
        }
    }

    fun loadShopkeeperSummary() {
        viewModelScope.launch { loadShopkeeperSummarySuspend() }
    }

    private suspend fun loadShopkeeperSummarySuspend() {
        try {
            val summary =
                if (sessionRepository.isLocalDemoMode()) {
                    val orders = localDemoMartStore.ordersForActor("demo-user-shop1", "SHOPKEEPER")
                    ShopkeeperSummaryDto(
                        openOrders = orders.count { it.status.equals("PENDING", true) || it.status.equals("DEALER_CONFIRMED", true) || it.status.equals("OUT_FOR_DELIVERY", true) },
                        inDelivery = orders.count { it.status.equals("OUT_FOR_DELIVERY", true) },
                        lastTotal = orders.firstOrNull { it.status.equals("DELIVERED", true) }?.let { (it.finalAmount as? Number)?.toDouble() },
                        invoicesReady = orders.count { it.status.equals("DELIVERED", true) },
                    )
                } else {
                    martApi.myOrderSummary()
                }
            _ui.update { it.copy(shopkeeperSummary = summary) }
        } catch (_: Exception) {
        }
    }

    /** Catalog tab only — avoids loading full product list during sign-in. */
    fun ensureCatalogLoaded() {
        val state = _ui.value
        if (state.products is LoadState.Idle || state.products is LoadState.Err) {
            loadProducts()
        }
        if (state.shelves is LoadState.Idle || state.shelves is LoadState.Err) {
            loadShelves()
        }
        if (!state.availabilityLoaded) {
            loadAvailability()
        }
    }

    /** Dealer stock tab — load once; background refresh if already shown. */
    fun ensureStockLoaded() {
        when (val stock = _ui.value.stock) {
            is LoadState.Idle, is LoadState.Err -> loadStock()
            is LoadState.Ok -> loadStock(showLoading = false)
            else -> {}
        }
    }

    fun loadDealerSummary() {
        viewModelScope.launch { loadDealerSummarySuspend() }
    }

    private suspend fun loadDealerSummarySuspend() {
        try {
            val summary =
                if (sessionRepository.isLocalDemoMode()) {
                    val orders = localDemoMartStore.ordersForActor("demo-user-dealer", "DEALER")
                    DealerSummaryDto(
                        pendingOrders = orders.count { it.status.equals("PENDING", true) },
                        todaysDeliveries = orders.count { it.status.equals("DELIVERED", true) },
                        weeklyRevenue = orders.filter { it.status.equals("DELIVERED", true) }.sumOf { (it.finalAmount as? Number)?.toDouble() ?: 0.0 },
                    )
                } else {
                    martApi.dealerOrderSummary()
                }
            _ui.update { it.copy(dealerSummary = summary) }
        } catch (_: Exception) {
        }
    }

    fun refreshAll() {
        refreshForRole()
    }

    fun loadProducts() {
        loadProducts(search = null, brandId = null, shelf = null)
    }

    fun loadProducts(
        search: String?,
        brandId: String?,
        shelf: String?,
    ) {
        loadProductsJob?.cancel()
        loadProductsJob =
            viewModelScope.launch {
                val searchQ = search?.trim()?.takeIf { it.isNotEmpty() }
                val brandQ = brandId?.trim()?.takeIf { it.isNotEmpty() }
                val shelfQ = shelf?.trim()?.takeIf { it.isNotEmpty() }
                val unfiltered = searchQ == null && brandQ == null && shelfQ == null
                val now = System.currentTimeMillis()
                if (unfiltered &&
                    catalogCache != null &&
                    now - catalogCacheAtMs < CATALOG_CACHE_TTL_MS
                ) {
                    _ui.update { it.copy(products = LoadState.Ok(catalogCache!!)) }
                    return@launch
                }
                if (shouldShowLoading(_ui.value.products)) {
                    _ui.update { it.copy(products = LoadState.Loading) }
                }
                try {
                    val list =
                        if (sessionRepository.isLocalDemoMode()) {
                            filterCatalogProducts(
                                localDemoMartStore.products(),
                                searchQ,
                                brandQ,
                                shelfQ,
                            )
                        } else {
                            val merged = mutableListOf<ProductDto>()
                            var apiPage = 1
                            while (true) {
                                val batch =
                                    martApi.products(
                                        search = searchQ,
                                        brandId = brandQ,
                                        shelf = shelfQ,
                                        page = apiPage,
                                        limit = PRODUCTS_API_PAGE_SIZE,
                                    )
                                if (batch.isEmpty()) break
                                merged.addAll(batch)
                                if (batch.size < PRODUCTS_API_PAGE_SIZE) break
                                apiPage++
                            }
                            merged
                        }
                    if (unfiltered) {
                        catalogCache = list
                        catalogCacheAtMs = System.currentTimeMillis()
                    }
                    _ui.update { it.copy(products = LoadState.Ok(list)) }
                } catch (e: Exception) {
                    if (_ui.value.products !is LoadState.Ok) {
                        _ui.update {
                            it.copy(products = LoadState.Err(httpReadableMessage(e)))
                        }
                    }
                }
            }
    }

    private fun normalizeHomeSearch(s: String?) = s?.trim()?.takeIf { it.isNotEmpty() }

    private fun normalizeHomeBrandId(s: String?) = s?.trim()?.takeIf { it.isNotEmpty() }

    private fun filterLocalHomeProducts(
        all: List<ProductDto>,
        search: String?,
        brandId: String?,
    ): List<ProductDto> =
        filterCatalogProducts(
            all,
            normalizeHomeSearch(search),
            normalizeHomeBrandId(brandId),
            shelf = null,
        )

    private fun homeCatalogCacheKey(
        search: String?,
        brandId: String?,
    ): String = "${normalizeHomeSearch(search).orEmpty()}|${normalizeHomeBrandId(brandId).orEmpty()}"

    private fun clearHomeProductsCache() {
        homeProductsCache = null
        homeProductsCacheKey = null
    }

    private fun clearHomeProductsCacheIfKeyChanged(
        search: String?,
        brandId: String?,
    ) {
        val key = homeCatalogCacheKey(search, brandId)
        if (homeProductsCacheKey != key) {
            clearHomeProductsCache()
        }
    }

    /**
     * Loads the full filtered catalog via `GET /products` (paginated server-side, max 100/page).
     * Cloud Run often lacks `GET /products/paged` (404); this path works on all deployed APIs.
     */
    private suspend fun fetchAllProductsForHome(
        search: String?,
        brandId: String?,
    ): List<ProductDto> {
        val key = homeCatalogCacheKey(search, brandId)
        if (homeProductsCacheKey == key && homeProductsCache != null) {
            return homeProductsCache!!
        }
        val searchQ = normalizeHomeSearch(search)
        val brandQ = normalizeHomeBrandId(brandId)
        val merged = mutableListOf<ProductDto>()
        var apiPage = 1
        while (true) {
            val batch =
                martApi.products(
                    search = searchQ,
                    brandId = brandQ,
                    shelf = null,
                    page = apiPage,
                    limit = PRODUCTS_API_PAGE_SIZE,
                )
            if (batch.isEmpty()) break
            merged.addAll(batch)
            if (batch.size < PRODUCTS_API_PAGE_SIZE) break
            apiPage++
        }
        homeProductsCache = merged
        homeProductsCacheKey = key
        return merged
    }

    private suspend fun fetchShopkeeperHomePage(
        page: Int,
        search: String?,
        brandId: String?,
    ): ProductsPagedResponse {
        val limit = SHOPKEEPER_HOME_PAGE_SIZE
        val flat =
            if (sessionRepository.isLocalDemoMode()) {
                filterLocalHomeProducts(localDemoMartStore.products(), search, brandId)
            } else {
                if (homePagedEndpointMissing != true) {
                    try {
                        val resp =
                            martApi.productsPaged(
                                search = normalizeHomeSearch(search),
                                brandId = normalizeHomeBrandId(brandId),
                                shelf = null,
                                page = page,
                                limit = limit,
                            )
                        homePagedEndpointMissing = false
                        return resp
                    } catch (e: HttpException) {
                        if (e.code() != 404) throw e
                        homePagedEndpointMissing = true
                    }
                }
                fetchAllProductsForHome(search, brandId)
            }
        val start = (page - 1) * limit
        val slice = flat.drop(start).take(limit)
        val hasNext = start + slice.size < flat.size
        return ProductsPagedResponse(slice, page, limit, flat.size, hasNext)
    }

    private suspend fun loadShopkeeperHomeFeedInternal(
        reset: Boolean,
        search: String?,
        brandId: String?,
    ) {
        if (reset) {
            val s = normalizeHomeSearch(search)
            val b = normalizeHomeBrandId(brandId)
            clearHomeProductsCacheIfKeyChanged(s, b)
            _ui.update {
                it.copy(
                    shopkeeperHomeFeed =
                        ShopkeeperHomeFeedState(
                            isRefreshing = true,
                            appliedSearch = s,
                            appliedBrandId = b,
                        ),
                )
            }
            try {
                val resp = fetchShopkeeperHomePage(1, s, b)
                _ui.update { st ->
                    st.copy(
                        shopkeeperHomeFeed =
                            st.shopkeeperHomeFeed.copy(
                                items = resp.items,
                                page = 1,
                                hasMore = resp.hasNext,
                                isRefreshing = false,
                                error = null,
                                totalCount = resp.total,
                            ),
                    )
                }
            } catch (e: Exception) {
                _ui.update { st ->
                    st.copy(
                        shopkeeperHomeFeed =
                            st.shopkeeperHomeFeed.copy(
                                isRefreshing = false,
                                error = httpReadableMessage(e),
                            ),
                    )
                }
            }
        } else {
            val cur = _ui.value.shopkeeperHomeFeed
            if (!cur.hasMore || cur.isLoadingMore || cur.isRefreshing) return
            val nextPage = cur.page + 1
            _ui.update {
                it.copy(
                    shopkeeperHomeFeed =
                        it.shopkeeperHomeFeed.copy(
                            isLoadingMore = true,
                            error = null,
                        ),
                )
            }
            try {
                val resp = fetchShopkeeperHomePage(nextPage, cur.appliedSearch, cur.appliedBrandId)
                _ui.update { st ->
                    val nf = st.shopkeeperHomeFeed
                    st.copy(
                        shopkeeperHomeFeed =
                            nf.copy(
                                items = nf.items + resp.items,
                                page = resp.page,
                                hasMore = resp.hasNext,
                                isLoadingMore = false,
                                totalCount = resp.total,
                            ),
                    )
                }
            } catch (e: Exception) {
                _ui.update { st ->
                    st.copy(
                        shopkeeperHomeFeed =
                            st.shopkeeperHomeFeed.copy(
                                isLoadingMore = false,
                                error = httpReadableMessage(e),
                            ),
                    )
                }
            }
        }
    }

    fun loadShopkeeperHomeFeed(
        reset: Boolean,
        search: String?,
        brandId: String?,
    ) {
        viewModelScope.launch {
            loadShopkeeperHomeFeedInternal(reset, search, brandId)
        }
    }

    fun loadMoreShopkeeperHome() {
        viewModelScope.launch {
            loadShopkeeperHomeFeedInternal(reset = false, search = null, brandId = null)
        }
    }

    fun loadShelves() {
        viewModelScope.launch {
            val showLoading = shouldShowLoading(_ui.value.shelves)
            if (showLoading) {
                _ui.update { it.copy(shelves = LoadState.Loading) }
            }
            try {
                val rows =
                    if (sessionRepository.isLocalDemoMode()) {
                        localDemoMartStore.products().mapNotNull { it.shelf }.distinct()
                    } else {
                        martApi.productShelves()
                    }
                _ui.update { it.copy(shelves = LoadState.Ok(rows)) }
            } catch (e: Exception) {
                if (_ui.value.shelves !is LoadState.Ok) {
                    _ui.update { it.copy(shelves = LoadState.Err(e.message ?: "Shelf list unavailable")) }
                }
            }
        }
    }

    fun loadOrders() {
        viewModelScope.launch {
            loadOrdersSuspend(showLoading = shouldShowLoading(_ui.value.orders))
            // Stock changes after orders; keep shopkeeper availability fresh (no-op otherwise).
            loadAvailabilitySuspend()
        }
    }

    private suspend fun loadOrdersSuspend(showLoading: Boolean = shouldShowLoading(_ui.value.orders)) {
        if (showLoading) {
            _ui.update { it.copy(orders = LoadState.Loading) }
        }
        try {
            val list =
                if (sessionRepository.isLocalDemoMode()) {
                    val u = sessionRepository.sessionUserFlow.first()
                    if (u == null) {
                        _ui.update {
                            it.copy(orders = LoadState.Err("Not signed in"))
                        }
                        return
                    }
                    localDemoMartStore.ordersForActor(u.id, u.role)
                } else {
                    when (sessionRepository.sessionUserFlow.first()?.role?.uppercase()) {
                        "SHOPKEEPER" -> martApi.myOrders()
                        "DEALER" -> martApi.dealerOrders()
                        else -> martApi.orders()
                    }
                }
            _ui.update { it.copy(orders = LoadState.Ok(list)) }
        } catch (e: Exception) {
            if (_ui.value.orders !is LoadState.Ok) {
                _ui.update {
                    it.copy(orders = LoadState.Err(e.message ?: "Failed to load orders"))
                }
            }
        }
    }

    fun loadStock(showLoading: Boolean = shouldShowLoading(_ui.value.stock)) {
        viewModelScope.launch { loadStockSuspend(showLoading) }
    }

    fun updateStockQuantity(
        stockId: String,
        quantity: Int,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.updateStockQuantity(stockId, quantity)
                } else {
                    martApi.updateStock(stockId, UpdateStockRequest(quantity))
                }
                loadStockSuspend(showLoading = false)
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    fun addStockSku(
        productId: String,
        quantity: Int,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.upsertStock(productId, quantity)
                } else {
                    martApi.upsertStock(UpsertStockRequest(productId, quantity))
                }
                loadStockSuspend(showLoading = false)
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    private fun rememberRazorpayCheckout(
        appOrderId: String,
        gatewayOrderId: String,
        cartSnapshot: List<CartLine>?,
    ) {
        activeRazorpayAppOrderId = appOrderId
        activeRazorpayGatewayOrderId = gatewayOrderId
        razorpayCartRestore = cartSnapshot
        razorpayResultHandled = false
    }

    private fun clearRazorpayCheckoutSession() {
        activeRazorpayAppOrderId = null
        activeRazorpayGatewayOrderId = null
        razorpayCartRestore = null
        razorpayResultHandled = false
    }

    fun prepareRestockCheckout(
        items: List<Pair<ProductDto, Int>>,
        onFinished: (String?) -> Unit,
    ) {
        if (items.isEmpty()) {
            onFinished("Select at least one SKU")
            return
        }
        if (items.any { it.second <= 0 }) {
            onFinished("Enter a valid quantity for each SKU")
            return
        }
        viewModelScope.launch {
            try {
                val role = "DEALER"
                val cartLines =
                    items.map { (product, qty) ->
                        CartLine(
                            productId = product.id,
                            productName = product.name,
                            quantity = qty,
                            referenceUnitPrice = product.catalogUnitPrice(role),
                            gstPercentage = product.gstPercentage.toDoubleFromApiOrNull(),
                            discountPercent = product.discountPercentForRole(role),
                            imageUrl = product.imageUrl,
                            brandLogoUrl = product.brand?.logoUrl,
                        )
                    }
                razorpayCartRestore = null
                cartRepository.restoreLines(cartLines)
                _ui.update {
                    it.copy(
                        placeOrderError = null,
                        placeOrderResult = null,
                        paymentMessage = null,
                        placedOrder = null,
                        pendingRazorpayOrderId = null,
                        pendingRazorpayGatewayOrderId = null,
                        pendingRazorpayKeyId = null,
                        pendingRazorpayAmountPaise = null,
                        pendingRazorpayCurrency = null,
                        pendingRazorpayMethod = null,
                    )
                }
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    private suspend fun loadStockSuspend(showLoading: Boolean = shouldShowLoading(_ui.value.stock)) {
        if (showLoading) {
            _ui.update { it.copy(stock = LoadState.Loading) }
        }
        try {
            val list =
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.stock()
                } else {
                    martApi.stock()
                }
            _ui.update { it.copy(stock = LoadState.Ok(list)) }
        } catch (e: Exception) {
            if (_ui.value.stock !is LoadState.Ok) {
                _ui.update {
                    it.copy(stock = LoadState.Err(e.message ?: "Stock unavailable"))
                }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val showLoading = shouldShowLoading(_ui.value.users)
            if (showLoading) {
                _ui.update { it.copy(users = LoadState.Loading) }
            }
            try {
                val list =
                    if (sessionRepository.isLocalDemoMode()) {
                        localDemoMartStore.users()
                    } else {
                        martApi.users()
                    }
                _ui.update { it.copy(users = LoadState.Ok(list)) }
            } catch (e: Exception) {
                if (_ui.value.users !is LoadState.Ok) {
                    _ui.update {
                        it.copy(users = LoadState.Err(e.message ?: "Users unavailable"))
                    }
                }
            }
        }
    }

    fun loadAreas() {
        viewModelScope.launch {
            val showLoading = shouldShowLoading(_ui.value.areas)
            if (showLoading) {
                _ui.update { it.copy(areas = LoadState.Loading) }
            }
            try {
                val list =
                    if (sessionRepository.isLocalDemoMode()) {
                        localDemoMartStore.areas()
                    } else {
                        martApi.areas()
                    }
                _ui.update { it.copy(areas = LoadState.Ok(list)) }
            } catch (e: Exception) {
                if (_ui.value.areas !is LoadState.Ok) {
                    _ui.update {
                        it.copy(areas = LoadState.Err(e.message ?: "Areas unavailable"))
                    }
                }
            }
        }
    }

    fun createArea(
        name: String,
        onFinished: (String?) -> Unit,
    ) {
        val trimmed = normalizeAreaName(name)
        if (trimmed.length < 2) {
            onFinished("Enter an area name (min 2 characters)")
            return
        }
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    onFinished("Area management is not available in demo mode")
                    return@launch
                }
                martApi.createArea(CreateAreaRequest(name = trimmed))
                loadAreas()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    fun updateArea(
        areaId: String,
        name: String,
        onFinished: (String?) -> Unit,
    ) {
        val trimmed = normalizeAreaName(name)
        if (trimmed.length < 2) {
            onFinished("Enter an area name (min 2 characters)")
            return
        }
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    onFinished("Area management is not available in demo mode")
                    return@launch
                }
                martApi.updateArea(areaId, UpdateAreaRequest(name = trimmed))
                loadAreas()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    fun createEmployee(
        name: String,
        email: String,
        phone: String?,
        password: String?,
        onFinished: (String?, String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val actor = sessionRepository.sessionUserFlow.first() ?: run {
                    onFinished("Not signed in", null)
                    return@launch
                }
                if (!actor.role.equals("ADMIN", ignoreCase = true)) {
                    onFinished("Only administrators can create employees", null)
                    return@launch
                }
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.createEmployee(
                        name,
                        email,
                        normalizedPhone(phone),
                    )
                    loadUsers()
                    onFinished(
                        null,
                        "Employee created (demo). They can sign in with email and demo password ${LocalDemoAuthConfig.DEMO_PASSWORD}.",
                    )
                } else {
                    val resp =
                        martApi.createEmployee(
                            CreateEmployeeRequest(
                                name = name.trim(),
                                email = email.trim(),
                                phone = normalizedPhone(phone),
                                password = password?.trim()?.takeIf { it.length >= 8 },
                            ),
                        )
                    loadUsers()
                    val info =
                        buildString {
                            append(resp.message ?: "Employee created.")
                            resp.loginPassword?.let { pwd ->
                                append("\n\nShare these credentials:\nEmail: ${resp.loginEmail ?: email}\nPassword: $pwd")
                            }
                            if (resp.emailSent == false && resp.emailError != null) {
                                append("\n\nEmail not sent: ${resp.emailError}")
                            }
                        }
                    onFinished(null, info)
                }
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e), null)
            }
        }
    }

    fun onboardShopkeeper(
        name: String,
        email: String,
        phone: String?,
        password: String?,
        areaId: String,
        onboardingNotes: String?,
        documents: List<PendingOnboardingDocument> = emptyList(),
        shopName: String? = null,
        address: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        onFinished: (String?, String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val actor = sessionRepository.sessionUserFlow.first() ?: run {
                    onFinished("Not signed in", null)
                    return@launch
                }
                val pwd =
                    password?.trim()?.takeIf { it.length >= 8 }
                        ?: LocalDemoAuthConfig.DEMO_PASSWORD
                val pendingApproval = !actor.role.equals("ADMIN", ignoreCase = true)
                if (sessionRepository.isLocalDemoMode()) {
                    val row =
                    localDemoMartStore.createShopkeeper(
                        name,
                        email,
                        normalizedPhone(phone),
                            areaId,
                            onboardedByEmployeeId = actor.id,
                            onboardingNotes = onboardingNotes,
                            actorRole = actor.role,
                        )
                    val storedDocs =
                        OnboardingDocumentStorage.persistForUser(appContext, row.id, documents)
                    if (storedDocs.isNotEmpty()) {
                        localDemoMartStore.attachOnboardingDocuments(row.id, storedDocs)
                    }
                } else {
                    val created =
                        martApi.createShopkeeper(
                            CreateShopkeeperRequest(
                                name = name.trim(),
                                email = email.trim(),
                                phone = normalizedPhone(phone),
                                password = pwd,
                                areaId = areaId,
                                onboardingNotes = onboardingNotes?.trim()?.takeIf { it.isNotEmpty() },
                                shopName = shopName?.trim()?.takeIf { it.isNotEmpty() },
                                address = address?.trim()?.takeIf { it.isNotEmpty() },
                                latitude = latitude,
                                longitude = longitude,
                            ),
                        )
                    uploadOnboardingDocuments(created.id, documents)
                }
                loadUsers()
                onFinished(
                    null,
                    if (pendingApproval) {
                        "Submitted for admin approval with ${documents.size} document(s). No confirmation email is sent until admin approves."
                    } else {
                        "Shopkeeper created and can sign in now."
                    },
                )
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e), null)
            }
        }
    }

    private fun normalizedPhone(phone: String?): String? =
        phone?.filter(Char::isDigit)?.takeIf { it.isNotEmpty() }

    fun onboardDealer(
        name: String,
        email: String,
        phone: String?,
        password: String?,
        areaId: String,
        onboardingNotes: String?,
        documents: List<PendingOnboardingDocument> = emptyList(),
        shopName: String? = null,
        address: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        onFinished: (String?, CreateDealerResponse?, String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val actor = sessionRepository.sessionUserFlow.first() ?: run {
                    onFinished("Not signed in", null, null)
                    return@launch
                }
                val pendingApproval = !actor.role.equals("ADMIN", ignoreCase = true)
                if (sessionRepository.isLocalDemoMode()) {
                    val row =
                        localDemoMartStore.createDealer(
                            name,
                            email,
                            normalizedPhone(phone),
                            areaId = areaId,
                            onboardedByEmployeeId = actor.id,
                            onboardingNotes = onboardingNotes,
                            actorRole = actor.role,
                        )
                    val storedDocs =
                        OnboardingDocumentStorage.persistForUser(appContext, row.id, documents)
                    if (storedDocs.isNotEmpty()) {
                        localDemoMartStore.attachOnboardingDocuments(row.id, storedDocs)
                    }
                    loadUsers()
                    loadAreas()
                    onFinished(
                        null,
                        CreateDealerResponse(
                            id = row.id,
                            name = row.name,
                            email = row.email,
                            role = row.role,
                            phone = row.phone,
                            loginEmail = row.email,
                            userId = row.id,
                            resetPasswordToken =
                                if (!pendingApproval) {
                                    "demo-reset-${row.id.take(8)}"
                                } else {
                                    null
                                },
                            resetPasswordExpiresAt = null,
                            message =
                                if (pendingApproval) {
                                    "Dealer submitted for admin approval."
                                } else {
                                    "Dealer added. They sign in with email and demo password, or use Reset password on login."
                                },
                        ),
                        if (pendingApproval) {
                            "Submitted for admin approval with ${documents.size} document(s). Confirmation email will be sent after admin approval."
                        } else {
                            null
                        },
                    )
                } else {
                    val resp =
                        martApi.createDealer(
                            CreateDealerRequest(
                                name = name.trim(),
                                email = email.trim(),
                                phone = normalizedPhone(phone),
                                password = password?.trim()?.takeIf { it.length >= 8 },
                                areaId = areaId,
                                onboardingNotes = onboardingNotes?.trim()?.takeIf { it.isNotEmpty() },
                                shopName = shopName?.trim()?.takeIf { it.isNotEmpty() },
                                address = address?.trim()?.takeIf { it.isNotEmpty() },
                                latitude = latitude,
                                longitude = longitude,
                            ),
                        )
                    uploadOnboardingDocuments(resp.id, documents)
                    loadUsers()
                    loadAreas()
                    onFinished(
                        null,
                        resp,
                        if (pendingApproval) {
                            "Submitted for admin approval with ${documents.size} document(s). Confirmation email will be sent after admin approval."
                        } else {
                            null
                        },
                    )
                }
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e), null, null)
            }
        }
    }

    private suspend fun uploadOnboardingDocuments(
        userId: String,
        documents: List<PendingOnboardingDocument>,
    ) {
        for (doc in documents) {
            val file = java.io.File(doc.localPath)
            if (!file.exists()) continue
            val mime = doc.mimeType ?: "application/octet-stream"
            val labelBody =
                doc.label.toRequestBody("text/plain".toMediaTypeOrNull())
            val part =
                MultipartBody.Part.createFormData(
                    "file",
                    doc.displayName,
                    file.asRequestBody(mime.toMediaTypeOrNull()),
                )
            martApi.uploadOnboardingDocument(userId, labelBody, part)
        }
    }

    fun approveUser(
        userId: String,
        onFinished: (String?, String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val actor = sessionRepository.sessionUserFlow.first() ?: run {
                    onFinished("Not signed in", null)
                    return@launch
                }
                val message =
                    if (sessionRepository.isLocalDemoMode()) {
                        val approved = localDemoMartStore.approveUser(userId, actor.id)
                        localDemoMartStore.approvalConfirmationMessage(approved)
                    } else {
                        val resp = martApi.approveUser(userId)
                        buildString {
                            append(
                                when {
                                    resp.emailSent == true ->
                                        resp.message ?: "User approved. Login credentials emailed."
                                    !resp.emailError.isNullOrBlank() ->
                                        "${resp.message ?: "User approved."} Email not sent (${resp.emailError})."
                                    else -> resp.message ?: "User approved. They can sign in now."
                                },
                            )
                            if (resp.emailSent != true && !resp.loginPassword.isNullOrBlank()) {
                                append("\nPassword: ${resp.loginPassword}")
                            }
                        }
                    }
                loadUsers()
                onFinished(null, message)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not approve user", null)
            }
        }
    }

    fun rejectUser(
        userId: String,
        reason: String?,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.rejectUser(userId, reason)
                } else {
                    martApi.rejectUser(userId, UpdateUserStatusRequest(reason))
                }
                loadUsers()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not reject user")
            }
        }
    }

    fun deactivateUser(
        userId: String,
        reason: String?,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.deactivateUser(userId, reason)
                } else {
                    martApi.deactivateUser(userId, UpdateUserStatusRequest(reason))
                }
                loadUsers()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not deactivate user")
            }
        }
    }

    fun reactivateUser(
        userId: String,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val actor = sessionRepository.sessionUserFlow.first() ?: run {
                    onFinished("Not signed in")
                    return@launch
                }
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.reactivateUser(userId, actor.id)
                } else {
                    martApi.reactivateUser(userId)
                }
                loadUsers()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not reactivate user")
            }
        }
    }

    fun pendingApprovalCount(): Int {
        val users = _ui.value.users
        return if (users is LoadState.Ok) {
            users.data.count {
                it.status.equals("PENDING_APPROVAL", true) &&
                    (it.role.equals("DEALER", true) || it.role.equals("SHOPKEEPER", true))
            }
        } else if (sessionRepository.isLocalDemoMode()) {
            localDemoMartStore.pendingApprovalCount()
        } else {
            0
        }
    }

    fun clearPlaceOrderError() {
        if (_ui.value.placeOrderError != null) {
            _ui.update { it.copy(placeOrderError = null) }
        }
    }

    fun clearOrderFeedback() {
        _ui.update {
            it.copy(
                placeOrderResult = null,
                placeOrderError = null,
                placedOrder = null,
                paymentMessage = null,
            )
        }
    }

    fun createProduct(
        name: String,
        brandType: String,
        shelf: String,
        basePrice: Double,
        gstPct: Double,
        dealerDisc: Double,
        shopkeeperDisc: Double,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val req =
                    CreateProductRequest(
                        name = name.trim(),
                        brandType = brandType.uppercase(),
                        shelf = shelf.uppercase(),
                        basePrice = basePrice,
                        gstPercentage = gstPct,
                        dealerDiscount = dealerDisc,
                        shopkeeperDiscount = shopkeeperDisc,
                    )
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.createProduct(req)
                } else {
                    martApi.createProduct(req)
                }
                loadProducts()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not create product")
            }
        }
    }

    fun updateProduct(
        productId: String,
        name: String,
        brandType: String,
        shelf: String,
        basePrice: Double,
        gstPct: Double,
        dealerDisc: Double,
        shopkeeperDisc: Double,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val req =
                    UpdateProductRequest(
                        name = name.trim(),
                        brandType = brandType.uppercase(),
                        shelf = shelf.uppercase(),
                        basePrice = basePrice,
                        gstPercentage = gstPct,
                        dealerDiscount = dealerDisc,
                        shopkeeperDiscount = shopkeeperDisc,
                    )
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.updateProduct(productId, req)
                } else {
                    martApi.updateProduct(productId, req)
                }
                loadProducts()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not update product")
            }
        }
    }

    fun deleteProduct(
        productId: String,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.deleteProduct(productId)
                } else {
                    martApi.deleteProduct(productId)
                }
                loadProducts()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not delete product")
            }
        }
    }

    fun bulkCreateProducts(
        rows: List<CreateProductRequest>,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                rows.forEach { req ->
                    if (sessionRepository.isLocalDemoMode()) {
                        localDemoMartStore.createProduct(req)
                    } else {
                        martApi.createProduct(req)
                    }
                }
                loadProducts()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(e.message ?: "Could not import SKU sheet")
            }
        }
    }

    fun placeOrderFromCart() {
        if (!ensureCanCheckout()) return
        val lines = cartRepository.lines.value
        if (lines.isEmpty()) {
            _ui.update { it.copy(placeOrderError = "Cart is empty") }
            return
        }
        viewModelScope.launch {
            _ui.update {
                it.copy(
                    placeOrderError = null,
                    placeOrderResult = null,
                    placedOrder = null,
                    paymentMessage = null,
                    pendingRazorpayOrderId = null,
                    pendingRazorpayGatewayOrderId = null,
                    pendingRazorpayKeyId = null,
                    pendingRazorpayAmountPaise = null,
                    pendingRazorpayCurrency = null,
                    pendingRazorpayMethod = null,
                )
            }
            try {
                val bodyLegacy =
                    CreateOrderRequest(
                        items =
                            lines.map {
                                CreateOrderItemDto(
                                    productId = it.productId,
                                    quantity = it.quantity,
                                )
                            },
                    )
                val bodyCreate = CreateOrderWithPaymentRequest(items = bodyLegacy.items, paymentMode = "COD")
                val role = currentUserRole()
                val created =
                    if (sessionRepository.isLocalDemoMode()) {
                        val u = sessionRepository.sessionUserFlow.first() ?: return@launch
                        when {
                            u.role.equals("SHOPKEEPER", ignoreCase = true) ->
                                localDemoMartStore.createOrder(u.id, bodyLegacy)
                            u.role.equals("DEALER", ignoreCase = true) ->
                                localDemoMartStore.createDealerRestockOrder(u.id, bodyLegacy)
                            else -> {
                                _ui.update {
                                    it.copy(placeOrderError = "Only shopkeepers and dealers can place orders")
                                }
                                return@launch
                            }
                        }
                    } else {
                        when (role) {
                            "DEALER" -> martApi.createDealerRestockOrder(bodyLegacy)
                            "SHOPKEEPER" -> createShopkeeperOrderWithLegacyFallback(bodyLegacy, bodyCreate)
                            else -> {
                                _ui.update {
                                    it.copy(placeOrderError = "Only shopkeepers and dealers can place orders")
                                }
                                return@launch
                            }
                        }
                    }
                cartRepository.clear()
                _ui.update {
                    it.copy(
                        placedOrder = created,
                        placeOrderResult = "Order placed successfully.",
                    )
                }
                loadOrders()
                if (role == "DEALER") {
                    loadStock()
                }
            } catch (e: Exception) {
                _ui.update {
                    it.copy(placeOrderError = httpReadableMessage(e))
                }
            }
        }
    }

    fun placeOrderFromCartWithDemoPayment(paymentMethod: String) {
        if (!ensureCanCheckout()) return
        val lines = cartRepository.lines.value
        if (lines.isEmpty()) {
            _ui.update { it.copy(placeOrderError = "Cart is empty") }
            return
        }
        viewModelScope.launch {
            _ui.update {
                it.copy(
                    placeOrderError = null,
                    placeOrderResult = null,
                    placedOrder = null,
                    paymentMessage = null,
                    pendingRazorpayOrderId = null,
                    pendingRazorpayGatewayOrderId = null,
                    pendingRazorpayKeyId = null,
                    pendingRazorpayAmountPaise = null,
                    pendingRazorpayCurrency = null,
                    pendingRazorpayMethod = null,
                )
            }
            try {
                val bodyLegacy =
                    CreateOrderRequest(
                        items =
                            lines.map {
                                CreateOrderItemDto(
                                    productId = it.productId,
                                    quantity = it.quantity,
                                )
                            },
                    )
                val role = currentUserRole()
                val created =
                    if (sessionRepository.isLocalDemoMode()) {
                        val u = sessionRepository.sessionUserFlow.first() ?: return@launch
                        when {
                            u.role.equals("DEALER", ignoreCase = true) ->
                                localDemoMartStore.createDealerRestockOrder(u.id, bodyLegacy)
                            else -> localDemoMartStore.createOrder(u.id, bodyLegacy)
                        }
                    } else {
                        when (role) {
                            "DEALER" -> martApi.createDealerRestockOrder(bodyLegacy)
                            else -> martApi.createOrder(bodyLegacy)
                        }
                    }
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.mockPayment(created.id)
                    demoFlowRepository.markMockPaid(created.id)
                } else {
                    martApi.mockPayment(created.id)
                    demoFlowRepository.markMockPaid(created.id)
                }
                val paid = created.copy(paymentStatus = "PAID")
                cartRepository.clear()
                val label =
                    when (paymentMethod.lowercase()) {
                        "card" -> "Order placed. Demo card payment successful."
                        "upi" -> "Order placed. Demo UPI payment successful."
                        else -> "Order placed and paid."
                    }
                _ui.update {
                    it.copy(
                        placedOrder = paid,
                        placeOrderResult = label,
                        paymentMessage = "Payment successful.",
                    )
                }
                loadOrders()
                if (role == "DEALER") {
                    loadStock()
                }
            } catch (e: Exception) {
                _ui.update { it.copy(placeOrderError = httpReadableMessage(e)) }
            }
        }
    }

    fun placeOrderFromCartRazorpay(paymentMethod: String = "card") {
        if (!ensureCanCheckout()) return
        val lines = cartRepository.lines.value
        if (lines.isEmpty()) {
            _ui.update { it.copy(placeOrderError = "Cart is empty") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(placeOrderError = null, placeOrderResult = null, paymentMessage = null) }
            try {
                val items =
                    lines.map {
                        CreateOrderItemDto(
                            productId = it.productId,
                            quantity = it.quantity,
                        )
                    }
                if (sessionRepository.isLocalDemoMode()) {
                    placeOrderFromCartWithDemoPayment(paymentMethod)
                    return@launch
                }
                val role = currentUserRole()
                try {
                    val paymentBody = CreateOrderWithPaymentRequest(items = items, paymentMode = "RAZORPAY")
                    val createdResp =
                        if (role == "DEALER") {
                            martApi.createDealerRestockOrderWithPayment(paymentBody)
                        } else {
                            martApi.createOrderWithPayment(paymentBody)
                        }
                    val orderId = createdResp.orderId
                    val gatewayId: String
                    val keyId: String
                    val amountPaise: Int
                    val currencyOut: String
                    if (
                        !createdResp.razorpayOrderId.isNullOrBlank() &&
                        createdResp.keyId != null &&
                        createdResp.amount != null
                    ) {
                        gatewayId = createdResp.razorpayOrderId!!
                        keyId = createdResp.keyId!!
                        amountPaise = createdResp.amount!!
                        currencyOut = createdResp.currency ?: "INR"
                    } else {
                        val rz =
                            martApi.createRazorpayOrder(
                                CreateRazorpayOrderRequest(orderId = orderId, currency = "INR"),
                            )
                        gatewayId = rz.razorpayOrderId
                        keyId = rz.keyId
                        amountPaise = rz.amountPaise
                        currencyOut = rz.currency
                    }
                    rememberRazorpayCheckout(appOrderId = orderId, gatewayOrderId = gatewayId, cartSnapshot = lines)
                    _ui.update {
                        it.copy(
                            placeOrderResult = "Complete payment in Razorpay",
                            pendingRazorpayOrderId = orderId,
                            pendingRazorpayGatewayOrderId = gatewayId,
                            pendingRazorpayKeyId = keyId,
                            pendingRazorpayAmountPaise = amountPaise,
                            pendingRazorpayCurrency = currencyOut,
                            pendingRazorpayMethod = paymentMethod,
                        )
                    }
                } catch (e: HttpException) {
                    if (e.code() != 404) throw e
                    val bodyLegacy = CreateOrderRequest(items = items)
                    val created =
                        if (role == "DEALER") {
                            martApi.createDealerRestockOrder(bodyLegacy)
                        } else {
                            martApi.createOrder(bodyLegacy)
                        }
                    val rz =
                        martApi.createRazorpayOrder(
                            CreateRazorpayOrderRequest(orderId = created.id, currency = "INR"),
                        )
                    rememberRazorpayCheckout(
                        appOrderId = rz.orderId,
                        gatewayOrderId = rz.razorpayOrderId,
                        cartSnapshot = lines,
                    )
                    _ui.update {
                        it.copy(
                            placeOrderResult = "Complete payment in Razorpay",
                            pendingRazorpayOrderId = rz.orderId,
                            pendingRazorpayGatewayOrderId = rz.razorpayOrderId,
                            pendingRazorpayKeyId = rz.keyId,
                            pendingRazorpayAmountPaise = rz.amountPaise,
                            pendingRazorpayCurrency = rz.currency,
                            pendingRazorpayMethod = paymentMethod,
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(placeOrderError = httpReadableMessage(e)) }
            }
        }
    }

    fun initRazorpayForPlacedOrder() {
        val order = _ui.value.placedOrder ?: run {
            _ui.update { it.copy(paymentMessage = "Place order first before payment") }
            return
        }
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    localDemoMartStore.mockPayment(order.id)
                    _ui.update {
                        it.copy(
                            paymentMessage = "Demo mode payment marked successful.",
                            placeOrderError = null,
                        )
                    }
                    loadOrders()
                    return@launch
                }
                val created =
                    martApi.createRazorpayOrder(
                        CreateRazorpayOrderRequest(
                            orderId = order.id,
                            currency = "INR",
                        ),
                    )
                _ui.update {
                    it.copy(
                        pendingRazorpayOrderId = created.orderId,
                        pendingRazorpayGatewayOrderId = created.razorpayOrderId,
                        pendingRazorpayKeyId = created.keyId,
                        pendingRazorpayAmountPaise = created.amountPaise,
                        pendingRazorpayCurrency = created.currency,
                        paymentMessage = null,
                    )
                }
                rememberRazorpayCheckout(
                    appOrderId = created.orderId,
                    gatewayOrderId = created.razorpayOrderId,
                    cartSnapshot = null,
                )
            } catch (e: Exception) {
                _ui.update { it.copy(paymentMessage = e.message ?: "Could not start payment") }
            }
        }
    }

    fun clearPendingRazorpayLaunch() {
        _ui.update {
            it.copy(
                pendingRazorpayGatewayOrderId = null,
                pendingRazorpayKeyId = null,
                pendingRazorpayAmountPaise = null,
                pendingRazorpayCurrency = null,
                pendingRazorpayMethod = null,
            )
        }
    }

    private suspend fun abandonPendingRazorpayCheckout(userMessage: String) {
        val orderId = _ui.value.pendingRazorpayOrderId
        val role = currentUserRole()
        val isFreshCartCheckout = razorpayCartRestore != null
        if (isFreshCartCheckout && !orderId.isNullOrBlank()) {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    runCatching { localDemoMartStore.cancelOrder(orderId) }
                } else {
                    runCatching { martApi.cancelOrder(orderId) }
                }
            } catch (_: Exception) {
                // Best-effort cleanup; still restore cart and clear pending checkout state.
            }
        }
        if (isFreshCartCheckout) {
            razorpayCartRestore?.let { cartRepository.restoreLines(it) }
        }
        clearRazorpayCheckoutSession()
        _ui.update {
            it.copy(
                placeOrderResult = if (isFreshCartCheckout) null else it.placeOrderResult,
                paymentMessage = userMessage,
                pendingRazorpayOrderId = null,
                pendingRazorpayGatewayOrderId = null,
                pendingRazorpayKeyId = null,
                pendingRazorpayAmountPaise = null,
                pendingRazorpayCurrency = null,
                pendingRazorpayMethod = null,
            )
        }
        if (isFreshCartCheckout) {
            loadOrders()
            if (role == "DEALER") {
                loadStock()
            }
        }
    }

    private fun clearPendingRazorpayOnly(userMessage: String) {
        _ui.update {
            it.copy(
                paymentMessage = userMessage,
                pendingRazorpayOrderId = null,
                pendingRazorpayGatewayOrderId = null,
                pendingRazorpayKeyId = null,
                pendingRazorpayAmountPaise = null,
                pendingRazorpayCurrency = null,
                pendingRazorpayMethod = null,
            )
        }
    }

    fun onRazorpayResult(
        success: Boolean,
        razorpayOrderId: String?,
        razorpayPaymentId: String?,
        razorpaySignature: String?,
        error: String?,
    ) {
        if (!success && razorpayResultHandled) {
            return
        }
        if (!success) {
            viewModelScope.launch {
                if (razorpayCartRestore != null) {
                    abandonPendingRazorpayCheckout(
                        error ?: "Payment cancelled — order removed. Your cart has been restored.",
                    )
                } else {
                    clearPendingRazorpayOnly(error ?: "Payment cancelled or failed")
                }
            }
            return
        }
        if (razorpayResultHandled) {
            return
        }
        razorpayResultHandled = true
        val appOrderId =
            _ui.value.pendingRazorpayOrderId
                ?: activeRazorpayAppOrderId
                ?: _ui.value.placedOrder?.id
        val gatewayOrderId =
            razorpayOrderId?.trim()?.takeIf { it.isNotEmpty() }
                ?: activeRazorpayGatewayOrderId
        val paymentId = razorpayPaymentId?.trim()?.takeIf { it.isNotEmpty() }
        if (appOrderId.isNullOrBlank() || gatewayOrderId.isNullOrBlank() || paymentId.isNullOrBlank()) {
            razorpayResultHandled = false
            viewModelScope.launch {
                if (razorpayCartRestore != null) {
                    abandonPendingRazorpayCheckout("Payment result incomplete — order removed. Your cart has been restored.")
                } else {
                    clearPendingRazorpayOnly("Payment result incomplete, please retry.")
                }
            }
            return
        }
        viewModelScope.launch {
            try {
                if (!sessionRepository.isLocalDemoMode()) {
                    martApi.verifyRazorpayPayment(
                        VerifyRazorpayPaymentRequest(
                            orderId = appOrderId,
                            razorpayOrderId = gatewayOrderId,
                            razorpayPaymentId = paymentId,
                            razorpaySignature = razorpaySignature?.trim().orEmpty(),
                        ),
                    )
                }
                val order =
                    if (sessionRepository.isLocalDemoMode()) {
                        _ui.value.placedOrder?.copy(paymentStatus = "PAID")
                    } else {
                        martApi.orderById(appOrderId)
                    }
                cartRepository.clear()
                clearRazorpayCheckoutSession()
                val role = currentUserRole()
                _ui.update {
                    it.copy(
                        placedOrder = order,
                        placeOrderResult = "Order placed successfully",
                        paymentMessage = "Payment successful.",
                        pendingRazorpayOrderId = null,
                        pendingRazorpayGatewayOrderId = null,
                        pendingRazorpayKeyId = null,
                        pendingRazorpayAmountPaise = null,
                        pendingRazorpayCurrency = null,
                        pendingRazorpayMethod = null,
                    )
                }
                loadOrders()
                if (role == "DEALER") {
                    loadStock()
                }
            } catch (e: Exception) {
                razorpayResultHandled = false
                if (razorpayCartRestore != null) {
                    abandonPendingRazorpayCheckout(
                        e.message ?: "Payment verification failed — order removed. Your cart has been restored.",
                    )
                } else {
                    clearPendingRazorpayOnly(e.message ?: "Payment verification failed")
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            // Detach this device's push token while the auth token is still valid.
            runCatching { pushTokenRegistrar.unregister() }
            clearSessionScopedState()
            sessionRepository.clear()
            onDone()
        }
    }

    fun addToCart(product: ProductDto) {
        cartRepository.addOne(product, currentUserRoleOrDefault())
    }

    fun addQuantityToCart(
        product: ProductDto,
        quantityToAdd: Int,
    ) {
        if (quantityToAdd <= 0) return
        val role = currentUserRoleOrDefault()
        val cur = cartRepository.lines.value.find { it.productId == product.id }?.quantity ?: 0
        cartRepository.setLineQuantity(product, cur + quantityToAdd, role)
    }

    fun setCartQuantity(
        productId: String,
        quantity: Int,
    ) {
        cartRepository.setQuantity(productId, quantity)
    }

    fun setCartLineQuantity(
        product: ProductDto,
        quantity: Int,
    ) {
        cartRepository.setLineQuantity(product, quantity, currentUserRoleOrDefault())
    }

    fun removeCartLine(productId: String) {
        cartRepository.remove(productId)
    }

    fun dismissDocumentCheckoutBlock() {
        _ui.update { it.copy(documentCheckoutBlocked = false) }
    }

    fun showDocumentCheckoutBlock() {
        _ui.update { it.copy(documentCheckoutBlocked = true) }
    }

    private fun ensureCanCheckout(): Boolean {
        val user = sessionRepository.getUserSnapshot()
        if (user != null && !user.canPlaceOrders) {
            _ui.update {
                it.copy(
                    documentCheckoutBlocked = true,
                    placeOrderError = null,
                )
            }
            return false
        }
        return true
    }

    fun refreshAuthProfile() {
        viewModelScope.launch {
            if (sessionRepository.isLocalDemoMode()) return@launch
            try {
                val me = martApi.me()
                val prev = sessionRepository.getUserSnapshot() ?: return@launch
                sessionRepository.patchSessionUser(
                    prev.copy(
                        name = me.name ?: prev.name,
                        phone = me.phone,
                        areaName = me.area?.name,
                        assignedDealer = me.assignedDealer,
                        documentUploaded = me.documentUploaded,
                        canPlaceOrders = me.canPlaceOrders,
                        documentStatus = me.documentStatus,
                    ),
                )
            } catch (_: Exception) {
            }
        }
    }

    fun loadMyDocuments() {
        viewModelScope.launch {
            _ui.update { it.copy(myDocuments = LoadState.Loading) }
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    val u = sessionRepository.getUserSnapshot() ?: return@launch
                    val docs = localDemoMartStore.listDocuments(u.id)
                    _ui.update { it.copy(myDocuments = LoadState.Ok(docs)) }
                    return@launch
                }
                val docs = martApi.myDocuments()
                _ui.update { it.copy(myDocuments = LoadState.Ok(docs)) }
            } catch (e: Exception) {
                _ui.update { it.copy(myDocuments = LoadState.Err(httpReadableMessage(e))) }
            }
        }
    }

    fun uploadMyDocument(
        documentType: String,
        file: File,
        mimeType: String?,
        displayName: String,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                if (sessionRepository.isLocalDemoMode()) {
                    val u = sessionRepository.getUserSnapshot() ?: return@launch
                    localDemoMartStore.uploadDocument(u.id, documentType, file, displayName, mimeType)
                    refreshAuthProfile()
                    loadMyDocuments()
                    onFinished(null)
                    return@launch
                }
                val part =
                    MultipartBody.Part.createFormData(
                        "file",
                        displayName,
                        file.asRequestBody((mimeType ?: "application/octet-stream").toMediaTypeOrNull()),
                    )
                martApi.uploadMyDocument(
                    documentType.toRequestBody("text/plain".toMediaTypeOrNull()),
                    part,
                )
                refreshAuthProfile()
                loadMyDocuments()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    fun verifyUserDocument(
        userId: String,
        documentId: String,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                martApi.verifyDocument(userId, documentId)
                loadUsers()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    fun rejectUserDocument(
        userId: String,
        documentId: String,
        reason: String,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                martApi.rejectDocument(userId, documentId, UpdateUserStatusRequest(reason))
                loadUsers()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }

    fun recordPartnerFollowUp(
        userId: String,
        onFinished: (String?) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                martApi.recordFollowUp(userId)
                loadUsers()
                onFinished(null)
            } catch (e: Exception) {
                onFinished(httpReadableMessage(e))
            }
        }
    }
}
