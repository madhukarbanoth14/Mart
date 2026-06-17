package com.mart.distribution.demo.ui.shopkeeper

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.feature.home.filterCatalogProducts
import com.mart.distribution.demo.ui.flashmart.FlashmartFloatingCartBar
import com.mart.distribution.demo.ui.flashmart.FlashmartProductRow
import com.mart.distribution.demo.ui.flashmart.FmIconButton
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
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
    onFirstAddToCart: (ProductDto) -> Unit,
    onIncrementCart: (ProductDto) -> Unit,
    onDecrementCart: (ProductDto) -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenCart: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }
    var shelfFilter by rememberSaveable { mutableStateOf(SHELF_ALL) }

    LaunchedEffect(query) {
        delay(SEARCH_DEBOUNCE_MS)
        debouncedQuery = query
    }

    val serverProducts =
        when (val p = ui.products) {
            is LoadState.Ok -> p.data
            else -> emptyList()
        }

    val shelfIds =
        remember(shelvesState) {
            when (shelvesState) {
                is LoadState.Ok -> shelvesState.data
                else -> FmcgShelfCatalog.ids
            }
        }
    val categories = remember(shelfIds) { listOf(SHELF_ALL) + shelfIds }

    val displayedProducts =
        remember(serverProducts, debouncedQuery, shelfFilter, selectedBrandId) {
            filterCatalogProducts(
                serverProducts,
                debouncedQuery.trim().takeIf { it.isNotEmpty() },
                selectedBrandId,
                shelfFilter.takeUnless { it == SHELF_ALL },
            )
        }

    Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Catalog", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = WholesaleText, letterSpacing = (-0.5).sp)
                    FmIconButton(
                        icon = Icons.Outlined.ShoppingCart,
                        onClick = onOpenCart,
                        badge = cartCount.takeIf { it > 0 },
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .shadow(1.dp, RoundedCornerShape(13.dp))
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.White)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Search, null, tint = WholesaleInk4, modifier = Modifier.height(19.dp))
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        textStyle = TextStyle(color = WholesaleText, fontSize = 15.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(WholesaleBlue),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth()) {
                                if (query.isEmpty()) {
                                    Text("Search products or brands", color = WholesaleInk4, fontSize = 15.sp)
                                }
                                inner()
                            }
                        },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { cat ->
                        val selected = shelfFilter == cat
                        val label = if (cat == SHELF_ALL) "All" else FmcgShelfCatalog.label(cat)
                        CategoryChip(label = label, selected = selected, onClick = { shelfFilter = cat })
                    }
                }
            }

            when (val p = ui.products) {
                is LoadState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = WholesaleBlue)
                    }
                is LoadState.Err ->
                    Text(p.message, modifier = Modifier.padding(16.dp), color = com.mart.distribution.demo.ui.theme.WholesaleRed)
                is LoadState.Ok ->
                    if (displayedProducts.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No products found", color = WholesaleInk4)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding =
                                androidx.compose.foundation.layout.PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = if (cartCount > 0) 100.dp else 16.dp,
                                ),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            item {
                                Text(
                                    "${displayedProducts.size} products found",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WholesaleMuted,
                                    modifier = Modifier.padding(bottom = 2.dp),
                                )
                            }
                            items(displayedProducts, key = { it.id }) { product ->
                                FlashmartProductRow(
                                    product = product,
                                    cartQty = cartQuantity(product.id),
                                    buyerRole = buyerRole,
                                    onOpenDetail = { onOpenProduct(product.id) },
                                    onFirstAdd = { onFirstAddToCart(product) },
                                    onIncrement = { onIncrementCart(product) },
                                    onDecrement = { onDecrementCart(product) },
                                )
                            }
                        }
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
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = RoundedCornerShape(11.dp),
        color = if (selected) WholesaleText else Color.White,
        border =
            if (selected) {
                null
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, com.mart.distribution.demo.ui.theme.WholesaleBorder2)
            },
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else com.mart.distribution.demo.ui.theme.WholesaleInk2,
        )
    }
}
