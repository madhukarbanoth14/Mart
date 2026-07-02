package com.mart.distribution.demo.ui.shopkeeper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.R
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.catalogUnitPrice
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.feature.home.filterCatalogProducts
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.flashmart.FlashmartFloatingCartBar
import com.mart.distribution.demo.ui.flashmart.FmQuantityInput
import com.mart.distribution.demo.ui.flashmart.FmQuantityPickerDialog
import com.mart.distribution.demo.ui.flashmart.FlashmartProductRow
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmIconButton
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmFilterChip
import com.mart.distribution.demo.ui.flashmart.FmPillSearchField
import com.mart.distribution.demo.ui.flashmart.FmSearchField
import com.mart.distribution.demo.ui.flashmart.FmSectionTitle
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import com.mart.distribution.demo.ui.util.pressScale
import kotlinx.coroutines.delay

private const val SHELF_ALL = "ALL"
private const val SEARCH_DEBOUNCE_MS = 300L

@Composable
fun ShopkeeperCatalogTab(
    ui: MainUiState,
    brandsState: LoadState<List<Brand>>,
    shelvesState: LoadState<List<String>>,
    selectedBrandId: String?,
    buyerRole: String = "SHOPKEEPER",
    onSelectBrand: (String?) -> Unit,
    cartQuantity: (String) -> Int,
    cartCount: Int,
    cartTotalLabel: String,
    onAddWithQuantity: (ProductDto, Int) -> Unit,
    onSetCartQuantity: (ProductDto, Int) -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenCart: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var selectedShelf by rememberSaveable { mutableStateOf<String?>(null) }
    var quantityPickerProduct by remember { mutableStateOf<ProductDto?>(null) }

    LaunchedEffect(query) {
        delay(SEARCH_DEBOUNCE_MS)
        debouncedQuery = query
    }

    val serverProducts =
        when (val p = ui.products) {
            is LoadState.Ok -> p.data
            else -> emptyList()
        }

    val presentShelves =
        remember(serverProducts) {
            serverProducts.mapNotNull { it.shelf?.uppercase()?.takeIf { s -> s.isNotBlank() } }.toSet()
        }

    val orderAgain =
        remember(ui.orders, serverProducts) {
            val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
            val ids = orders.flatMap { o -> o.items?.map { it.productId } ?: emptyList() }
                .distinct()
                .take(12)
            ids.mapNotNull { id -> serverProducts.firstOrNull { it.id == id } }
        }

    val trimmedQuery = debouncedQuery.trim()
    val inResults = trimmedQuery.isNotEmpty() || selectedShelf != null

    val displayedProducts =
        remember(serverProducts, trimmedQuery, selectedShelf, selectedBrandId) {
            filterCatalogProducts(
                serverProducts,
                trimmedQuery.takeIf { it.isNotEmpty() },
                selectedBrandId,
                selectedShelf?.takeUnless { it == SHELF_ALL },
            )
        }

    val headerTitle =
        when {
            selectedShelf != null && selectedShelf != SHELF_ALL -> FmcgShelfCatalog.label(selectedShelf)
            selectedShelf == SHELF_ALL -> "All products"
            else -> "Browse"
        }
    val shelfChips =
        remember(presentShelves) {
            listOf(SHELF_ALL to "All") +
                presentShelves.sorted().map { it to FmcgShelfCatalog.label(it) }
        }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (inResults && buyerRole.equals("SHOPKEEPER", ignoreCase = true)) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Products",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WholesaleText,
                        letterSpacing = (-0.5).sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FmIconButton(icon = Icons.Outlined.FilterList, onClick = null)
                        FmIconButton(
                            icon = Icons.Outlined.ShoppingCart,
                            onClick = onOpenCart,
                            badge = cartCount.takeIf { it > 0 },
                        )
                    }
                }
            } else {
                FmAppHeader(
                    title = headerTitle,
                    subtitle =
                        when {
                            trimmedQuery.isNotEmpty() -> "Search results"
                            selectedShelf != null -> "Wholesale catalog"
                            else -> "Categories & quick reorder"
                        },
                    onBack = if (selectedShelf != null) {{ selectedShelf = null }} else null,
                    right = {
                        FmIconButton(
                            icon = Icons.Outlined.ShoppingCart,
                            onClick = onOpenCart,
                            badge = cartCount.takeIf { it > 0 },
                        )
                    },
                )
            }
            FmPillSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search products or brands",
                onGridClick = if (selectedShelf != null) {{ selectedShelf = null; query = "" }} else null,
                modifier = Modifier.padding(horizontal = FmSpacing.listH).padding(bottom = 4.dp),
            )
            if (inResults && shelfChips.size > 1) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    shelfChips.forEach { (id, label) ->
                        val selected = (selectedShelf ?: SHELF_ALL) == id
                        FmFilterChip(
                            label = label,
                            selected = selected,
                            onClick = {
                                selectedShelf = if (id == SHELF_ALL) SHELF_ALL else id
                            },
                        )
                    }
                }
            }
            when (ui.products) {
                is LoadState.Loading -> FmLoadingState(message = "Loading catalog…", modifier = Modifier.weight(1f))
                is LoadState.Err ->
                    FmErrorBanner(
                        message = (ui.products as LoadState.Err).message,
                        modifier = Modifier.padding(horizontal = FmSpacing.listH),
                    )
                is LoadState.Ok ->
                    if (inResults) {
                        CatalogProductList(
                            products = displayedProducts,
                            ui = ui,
                            buyerRole = buyerRole,
                            cartCount = cartCount,
                            cartQuantity = cartQuantity,
                            onOpenProduct = onOpenProduct,
                            onRequestAdd = { quantityPickerProduct = it },
                            onSetCartQuantity = onSetCartQuantity,
                            onClearSearch = { query = ""; selectedShelf = null },
                        )
                    } else {
                        CatalogLanding(
                            presentShelves = presentShelves,
                            orderAgain = orderAgain,
                            ui = ui,
                            buyerRole = buyerRole,
                            cartQuantity = cartQuantity,
                            cartCount = cartCount,
                            onOpenShelf = { selectedShelf = it },
                            onOpenAll = { selectedShelf = SHELF_ALL },
                            onOpenProduct = onOpenProduct,
                            onRequestAdd = { quantityPickerProduct = it },
                            onSetCartQuantity = onSetCartQuantity,
                        )
                    }
                else -> {}
            }
        }

        if (cartCount > 0) {
            FlashmartFloatingCartBar(
                itemCount = cartCount,
                totalLabel = cartTotalLabel,
                onOpenCart = onOpenCart,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 16.dp, vertical = 88.dp),
            )
        }

        quantityPickerProduct?.let { product ->
            val stockCap =
                if (ui.availabilityLoaded) ui.availability[product.id] else null
            val maxQty =
                stockCap?.let { minOf(ui.maxOrderQuantity, it) } ?: ui.maxOrderQuantity
            FmQuantityPickerDialog(
                productName = product.name,
                max = maxQty.coerceAtLeast(1),
                quickChips = ui.quickQuantityChips,
                onDismiss = { quantityPickerProduct = null },
                onConfirm = { qty ->
                    onAddWithQuantity(product, qty)
                    quantityPickerProduct = null
                },
            )
        }
    }
}
@Composable
private fun CatalogProductList(
    products: List<ProductDto>,
    ui: MainUiState,
    buyerRole: String,
    cartCount: Int,
    cartQuantity: (String) -> Int,
    onOpenProduct: (String) -> Unit,
    onRequestAdd: (ProductDto) -> Unit,
    onSetCartQuantity: (ProductDto, Int) -> Unit,
    onClearSearch: () -> Unit = {},
) {
    if (products.isEmpty()) {
        FmEmptyState(
            icon = Icons.Outlined.GridView,
            title = "No products found",
            message = "Try a different search term or browse categories from the home view.",
            actionLabel = "Clear filters",
            onAction = onClearSearch,
            modifier = Modifier.fillMaxSize(),
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 12.dp,
            bottom = if (cartCount > 0) 100.dp else 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "${products.size} products",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleMuted,
                modifier = Modifier.padding(horizontal = FmSpacing.listH).padding(bottom = 2.dp),
            )
        }
        items(products, key = { it.id }) { product ->
            val maxQty = if (ui.availabilityLoaded) ui.availability[product.id] else null
            FlashmartProductRow(
                product = product,
                cartQty = cartQuantity(product.id),
                buyerRole = buyerRole,
                maxQuantity = maxQty,
                maxOrderQuantity = ui.maxOrderQuantity,
                onOpenDetail = { onOpenProduct(product.id) },
                onFirstAdd = { onRequestAdd(product) },
                onSetQuantity = { qty -> onSetCartQuantity(product, qty) },
            )
        }
    }
}
@Composable
private fun CatalogLanding(
    presentShelves: Set<String>,
    orderAgain: List<ProductDto>,
    ui: MainUiState,
    buyerRole: String,
    cartQuantity: (String) -> Int,
    cartCount: Int,
    onOpenShelf: (String) -> Unit,
    onOpenAll: () -> Unit,
    onOpenProduct: (String) -> Unit,
    onRequestAdd: (ProductDto) -> Unit,
    onSetCartQuantity: (ProductDto, Int) -> Unit,
) {
    val sections = remember(presentShelves) {
        FmcgShelfCatalog.sections.mapNotNull { section ->
            val visible = section.shelves.filter { presentShelves.isEmpty() || it in presentShelves }
            if (visible.isEmpty()) null else section.title to visible
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 14.dp, bottom = if (cartCount > 0) 100.dp else 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Promo banners
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PromoBanner(
                    title = "Best wholesale prices",
                    subtitle = "Save more on every bulk order",
                    badge = "DEALS",
                    badgeColor = WholesaleBlue,
                    imageRes = R.drawable.banner_deals,
                )
                PromoBanner(
                    title = "Fast restock",
                    subtitle = "Order today, delivered quick",
                    badge = "NEW",
                    badgeColor = WholesaleGreen,
                    imageRes = R.drawable.banner_restock,
                )
                PromoBanner(
                    title = "Daily essentials",
                    subtitle = "Everything your shop needs",
                    badge = "TOP",
                    badgeColor = WholesaleOrange,
                    imageRes = R.drawable.banner_essentials,
                )
            }
        }

        // Order Again
        if (orderAgain.isNotEmpty()) {
            item {
                FmSectionTitle(title = "Order again", modifier = Modifier.padding(top = 14.dp))
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    orderAgain.forEach { product ->
                        OrderAgainCard(
                            product = product,
                            buyerRole = buyerRole,
                            cartQty = cartQuantity(product.id),
                            maxOrderQuantity = ui.maxOrderQuantity,
                            onOpen = { onOpenProduct(product.id) },
                            onAdd = { onRequestAdd(product) },
                            onSetQuantity = { onSetCartQuantity(product, it) },
                        )
                    }
                }
            }
        }

        // Category sections
        sections.forEach { (title, shelves) ->
            item { FmSectionTitle(title = title, modifier = Modifier.padding(top = 16.dp)) }
            items(shelves.chunked(2)) { rowShelves ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    rowShelves.forEach { shelfId ->
                        CategoryTile(
                            label = FmcgShelfCatalog.label(shelfId),
                            imageRes = FmcgShelfCatalog.imageRes(shelfId),
                            onClick = { onOpenShelf(shelfId) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowShelves.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        // View all
        item {
            com.mart.distribution.demo.ui.flashmart.FmButton(
                text = "View all products",
                onClick = onOpenAll,
                variant = com.mart.distribution.demo.ui.flashmart.FmButtonVariant.Outline,
                modifier = Modifier.padding(horizontal = FmSpacing.listH, vertical = 18.dp),
            )
        }
    }
}

@Composable
private fun PromoBanner(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    imageRes: Int,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier.width(300.dp).height(132.dp).clip(shape),
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    0f to Color(0xF20A0A1A),
                    0.55f to Color(0x800A0A1A),
                    1f to Color(0x1A0A0A1A),
                ),
            ),
        )
        Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White,
                letterSpacing = (-0.3).sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.88f), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
@Composable
private fun CategoryTile(
    label: String,
    imageRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .shadow(2.dp, shape)
            .clip(shape)
            .background(Color.White)
            .pressScale(onClick = onClick),
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = label,
            modifier = Modifier.fillMaxWidth().height(108.dp),
            contentScale = ContentScale.Crop,
        )
        Text(
            label,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = WholesaleText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
@Composable
private fun OrderAgainCard(
    product: ProductDto,
    buyerRole: String,
    cartQty: Int,
    maxOrderQuantity: Int,
    onOpen: () -> Unit,
    onAdd: () -> Unit,
    onSetQuantity: (Int) -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val brandName = product.brand?.name ?: product.brandType
    val price = product.catalogUnitPrice(buyerRole)
    Column(
        modifier = Modifier.width(140.dp)
            .shadow(1.dp, shape).clip(shape).background(Color.White)
            .clickable(onClick = onOpen).padding(10.dp),
    ) {
        ProductThumbnail(
            imageUrl = product.imageUrl,
            brandLogoUrl = product.brand?.logoUrl,
            productName = product.name,
            brandName = brandName,
            style = ProductImageStyle.Grid,
            cornerRadius = 12.dp,
            modifier = Modifier.fillMaxWidth().height(96.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            product.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText,
            lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.height(30.dp),
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(formatDecimal(price), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            if (cartQty > 0) {
                FmQuantityInput(
                    value = cartQty,
                    onValueChange = onSetQuantity,
                    min = 0,
                    max = maxOrderQuantity,
                    compact = true,
                )
            } else {
                Box(
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
                        .background(WholesaleBlue).clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
private fun StepBtn(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp))
            .background(WholesaleBg).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WholesaleBlue)
    }
}
