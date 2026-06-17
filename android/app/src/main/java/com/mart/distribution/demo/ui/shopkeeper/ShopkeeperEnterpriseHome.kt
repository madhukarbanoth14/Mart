package com.mart.distribution.demo.ui.shopkeeper

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.Brand
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.ShopkeeperSummaryDto
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.feature.home.ShopkeeperHomeFeedState
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDark
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.wholesale.WholesaleProductCard
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Calendar

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun ShopkeeperEnterpriseHome(
    ui: MainUiState,
    brandsState: LoadState<List<Brand>>,
    selectedBrandId: String?,
    onSelectedBrandIdChange: (String?) -> Unit,
    summary: ShopkeeperSummaryDto?,
    shopName: String,
    onRefreshHomeFeed: (search: String?, brandId: String?) -> Unit,
    onLoadMore: () -> Unit,
    cartQuantity: (String) -> Int,
    onFirstAddToCart: (ProductDto) -> Unit,
    onIncrementCart: (ProductDto) -> Unit,
    onDecrementCart: (ProductDto) -> Unit,
    onOpenProduct: (String) -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenOrders: () -> Unit,
) {
    val feed: ShopkeeperHomeFeedState = ui.shopkeeperHomeFeed
    var searchDraft by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val open = summary?.openOrders ?: orders.count {
        it.status.equals("PENDING", true) || it.status.equals("DEALER_CONFIRMED", true) ||
            it.status.equals("OUT_FOR_DELIVERY", true) || it.status.equals("ACCEPTED", true)
    }
    val recentOrders = orders.take(4)
    val productRows = remember(feed.items) { feed.items.chunked(2) }
    val shelfCategories = remember { FmcgShelfCatalog.ids.take(8) }

    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            last to info.totalItemsCount
        }.distinctUntilChanged().debounce(120).collect { (last, total) ->
            if (total > 0 && last >= total - 3 && feed.hasMore && !feed.isLoadingMore &&
                !feed.isRefreshing && feed.error == null) onLoadMore()
        }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().background(WholesaleBg)) {

        // ── 1. Greeting header ─────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${greeting()}, ${shopName.split(" ").first()}",
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                        color = WholesaleBlue, letterSpacing = 0.3.sp,
                    )
                    Text(
                        text = shopName, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = WholesaleText, letterSpacing = (-0.5).sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                        .background(WholesaleBlueTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = shopName.firstOrNull()?.uppercase() ?: "S",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleBlue,
                    )
                }
            }
        }

        // ── 2. Flashmart hero card ─────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep)))
                    .padding(20.dp),
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .size(160.dp).clip(RoundedCornerShape(80.dp))
                        .background(Color.White.copy(alpha = 0.07f)),
                )
                Column {
                    Text("Ready to restock?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.82f))
                    Row(modifier = Modifier.padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("${orders.size}", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, letterSpacing = (-0.5).sp)
                            Text("Total orders", fontSize = 12.sp, color = Color.White.copy(0.78f))
                        }
                        Box(Modifier.width(1.dp).height(48.dp).background(Color.White.copy(0.25f)))
                        Column {
                            Text("$open", fontSize = 30.sp, fontWeight = FontWeight.Bold,
                                color = Color.White, letterSpacing = (-0.5).sp)
                            Text("In progress", fontSize = 12.sp, color = Color.White.copy(0.78f))
                        }
                    }
                    Button(
                        onClick = onOpenCatalog,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = WholesaleBlueDeep,
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Text("+ New order", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── 3. Quick actions ──────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(Triple("🛒", "Browse", onOpenCatalog),
                       Triple("📄", "Invoices", onOpenOrders),
                       Triple("🚚", "Track", onOpenOrders)).forEach { (emoji, label, action) ->
                    Column(
                        modifier = Modifier.weight(1f)
                            .shadow(1.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, WholesaleBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = action)
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(WholesaleBlueTint),
                            contentAlignment = Alignment.Center,
                        ) { Text(emoji, fontSize = 18.sp) }
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = WholesaleMuted)
                    }
                }
            }
        }

        // ── 4. Recent orders ──────────────────────────────────────────────
        if (recentOrders.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Recent orders", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = WholesaleMuted, letterSpacing = 0.5.sp)
                    Text("See all", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                        color = WholesaleBlue,
                        modifier = Modifier.clickable(onClick = onOpenOrders))
                }
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp)).background(Color.White)
                        .border(1.dp, WholesaleBorder, RoundedCornerShape(16.dp)),
                ) {
                    recentOrders.forEachIndexed { i, o ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenOrders)
                                .padding(horizontal = 16.dp, vertical = 13.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(
                                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp))
                                        .background(WholesaleSurface2),
                                    contentAlignment = Alignment.Center,
                                ) { Text("📦", fontSize = 16.sp) }
                                Column {
                                    Text(o.id.takeLast(8), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        color = WholesaleText)
                                    Text("${o.items?.size ?: 0} items", fontSize = 12.sp, color = WholesaleMuted)
                                }
                            }
                            val statusLabel = when (o.status.uppercase()) {
                                "PENDING" -> "Placed"
                                "DEALER_CONFIRMED", "ACCEPTED" -> "Accepted"
                                "OUT_FOR_DELIVERY" -> "On the way"
                                "DELIVERED" -> "Delivered"
                                else -> o.status
                            }
                            val (sBg, sFg) = when (o.status.uppercase()) {
                                "DELIVERED" -> WholesaleGreenTint to WholesaleGreen
                                "OUT_FOR_DELIVERY" -> WholesaleOrangeTint to WholesaleOrange
                                "DEALER_CONFIRMED", "ACCEPTED" -> WholesaleBlueTint to WholesaleBlue
                                else -> WholesaleSurface3 to WholesaleMuted
                            }
                            Box(Modifier.clip(RoundedCornerShape(99.dp)).background(sBg)
                                .padding(horizontal = 10.dp, vertical = 5.dp)) {
                                Text(statusLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                    color = sFg)
                            }
                        }
                        if (i < recentOrders.lastIndex)
                            Box(Modifier.fillMaxWidth().height(0.5.dp).background(WholesaleBorder))
                    }
                }
            }
        }

        // ── 5. Shop by catalog ─────────────────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Shop by Catalog", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                    color = WholesaleMuted, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp))
                shelfCategories.chunked(4).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        row.forEach { shelfId ->
                            Column(
                                modifier = Modifier.weight(1f)
                                    .shadow(1.dp, RoundedCornerShape(14.dp))
                                    .clip(RoundedCornerShape(14.dp)).background(Color.White)
                                    .border(1.dp, WholesaleBorder, RoundedCornerShape(14.dp))
                                    .clickable(onClick = onOpenCatalog)
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                    .background(WholesaleBlueTint),
                                    contentAlignment = Alignment.Center) {
                                    Text(FmcgShelfCatalog.emoji(shelfId), fontSize = 20.sp)
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(FmcgShelfCatalog.label(shelfId), fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold, color = WholesaleText,
                                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(9.dp))
                }
            }
        }

        // ── 6. Brand filter chips ──────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = selectedBrandId == null,
                    onClick = { onSelectedBrandIdChange(null); onRefreshHomeFeed(searchDraft.trim().takeIf { it.isNotEmpty() }, null) },
                    label = { Text("All brands") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WholesaleBlue, selectedLabelColor = Color.White))
                if (brandsState is LoadState.Ok) {
                    brandsState.data.take(12).forEach { brand ->
                        FilterChip(selected = selectedBrandId == brand.id,
                            onClick = { onSelectedBrandIdChange(brand.id); onRefreshHomeFeed(searchDraft.trim().takeIf { it.isNotEmpty() }, brand.id) },
                            label = { Text(brand.name, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WholesaleBlue, selectedLabelColor = Color.White))
                    }
                }
            }
        }

        // ── 7. Search bar + products label ─────────────────────────────────
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(13.dp))
                        .clip(RoundedCornerShape(13.dp)).background(Color.White)
                        .border(1.dp, WholesaleBorder, RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = WholesaleMuted,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    BasicTextField(
                        value = searchDraft, onValueChange = { searchDraft = it },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(color = WholesaleText, fontSize = 14.sp),
                        singleLine = true, cursorBrush = SolidColor(WholesaleBlue),
                        decorationBox = { inner ->
                            if (searchDraft.isEmpty()) Text("Search products, brands…",
                                color = WholesaleMuted, fontSize = 14.sp)
                            inner()
                        },
                    )
                    if (searchDraft.isNotBlank()) {
                        Text("Go", color = WholesaleBlue, fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .clickable { onRefreshHomeFeed(searchDraft.trim().takeIf { it.isNotEmpty() }, selectedBrandId) }
                                .padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡ Products", fontWeight = FontWeight.Bold, fontSize = 13.sp,
                        color = WholesaleMuted, letterSpacing = 0.5.sp)
                    feed.totalCount?.let { Text("$it SKUs", fontSize = 11.sp,
                        color = WholesaleMuted, fontWeight = FontWeight.SemiBold) }
                }
            }
        }

        // ── 8. Product grid ────────────────────────────────────────────────
        if (feed.error != null && feed.items.isEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Color.White).padding(16.dp)) {
                    Text(feed.error!!, color = WholesaleRed)
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onRefreshHomeFeed(searchDraft.trim().takeIf { it.isNotEmpty() }, selectedBrandId) }) {
                        Text("Retry") }
                }
            }
        }
        if (feed.isRefreshing && feed.items.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WholesaleBlue) } }
        }
        items(productRows.size) { rowIdx ->
            val row = productRows[rowIdx]
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { product ->
                    WholesaleProductCard(product = product, cartQty = cartQuantity(product.id),
                        onOpenDetail = { onOpenProduct(product.id) },
                        onFirstAdd = { onFirstAddToCart(product) },
                        onIncrement = { onIncrementCart(product) },
                        onDecrement = { onDecrementCart(product) },
                        modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        if (feed.isLoadingMore) {
            item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp), color = WholesaleBlue) } }
        }
        if (feed.error != null && feed.items.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp)).background(Color.White).padding(16.dp)) {
                    Text(feed.error!!, color = WholesaleRed)
                    OutlinedButton(onClick = { onRefreshHomeFeed(searchDraft.trim().takeIf { it.isNotEmpty() }, selectedBrandId) }) {
                        Text("Retry load more") }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}
