package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.flashmart.FmQuantityInput
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.theme.WholesaleTheme
import com.mart.distribution.demo.ui.util.bulkShippingCaption
import com.mart.distribution.demo.ui.util.formatDecimal
import com.mart.distribution.demo.ui.util.shelfLabel
import kotlin.math.roundToInt

private sealed interface ProductDetailLoadState {
    data object Loading : ProductDetailLoadState
    data class Ready(val product: ProductDto) : ProductDetailLoadState
    data class Failed(val message: String) : ProductDetailLoadState
}

@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    maxOrderQuantity: Int = 10000,
    onAddToCart: (ProductDto, Int) -> Unit,
) {
    val container = LocalAppContainer.current
    var reloadNonce by remember(productId) { mutableIntStateOf(0) }
    var qty by remember(productId) { mutableIntStateOf(1) }

    val loadState =
        produceState<ProductDetailLoadState>(
            initialValue = ProductDetailLoadState.Loading,
            productId,
            reloadNonce,
        ) {
            value = ProductDetailLoadState.Loading
            value =
                runCatching {
                    if (container.sessionRepository.isLocalDemoMode()) {
                        container.localDemoMartStore.productById(productId)
                    } else {
                        container.martApi.productById(productId)
                    }
                }.fold(
                    onSuccess = { p ->
                        if (p != null) ProductDetailLoadState.Ready(p) else ProductDetailLoadState.Failed("Product not found")
                    },
                    onFailure = { e -> ProductDetailLoadState.Failed(e.message ?: "Could not load product") },
                )
        }

    WholesaleTheme {
        when (val state = loadState.value) {
            ProductDetailLoadState.Loading ->
                Box(Modifier.fillMaxSize().background(WholesaleBg), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.CircularProgressIndicator(color = WholesaleBlue)
                }

            is ProductDetailLoadState.Failed ->
                Column(
                    modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    FmAppHeader(title = "Product", onBack = { navController.popBackStack() })
                    androidx.compose.material3.Text(state.message, color = com.mart.distribution.demo.ui.theme.WholesaleRed)
                    Spacer(Modifier.height(12.dp))
                    FmButton("Retry", onClick = { reloadNonce++ }, fullWidth = false)
                }

            is ProductDetailLoadState.Ready -> {
                val product = state.product
                val price = product.dealerPrice.toDoubleFromApiOrNull() ?: product.basePrice.toDoubleFromApiOrNull()
                val mrp = product.mrp.toDoubleFromApiOrNull()
                val discPct =
                    if (mrp != null && price != null && mrp > price) {
                        ((mrp - price) / mrp * 100).roundToInt()
                    } else {
                        null
                    }
                val lineTotal = (price ?: 0.0) * qty

                Box(modifier = Modifier.fillMaxSize().background(WholesaleBg)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FmAppHeader(
                            title = product.name,
                            subtitle = product.brand?.name,
                            onBack = { navController.popBackStack() },
                        )
                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            ProductThumbnail(
                                imageUrl = product.imageUrl,
                                brandLogoUrl = product.brand?.logoUrl,
                                productName = product.name,
                                brandName = product.brand?.name,
                                style = ProductImageStyle.Hero,
                                cornerRadius = 16.dp,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(16.dp)),
                            )

                            FmCard {
                                product.brand?.name?.let {
                                    androidx.compose.material3.Text(
                                        it.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WholesaleBlue,
                                        letterSpacing = 0.3.sp,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                                androidx.compose.material3.Text(
                                    product.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WholesaleText,
                                    lineHeight = 24.sp,
                                )
                                product.weight?.let {
                                    Spacer(Modifier.height(4.dp))
                                    androidx.compose.material3.Text(it, fontSize = 12.sp, color = WholesaleMuted)
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    androidx.compose.material3.Text(
                                        formatDecimal(price ?: product.basePrice),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = WholesaleText,
                                    )
                                    mrp?.let {
                                        androidx.compose.material3.Text(
                                            formatDecimal(it),
                                            fontSize = 13.sp,
                                            color = WholesaleMuted,
                                            textDecoration = TextDecoration.LineThrough,
                                        )
                                    }
                                    discPct?.takeIf { it > 0 }?.let {
                                        androidx.compose.material3.Text(
                                            "$it% off",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WholesaleGreen,
                                            modifier =
                                                Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(WholesaleGreenTint)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                androidx.compose.material3.Text(
                                    shelfLabel(product.shelf),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WholesaleMuted,
                                    modifier =
                                        Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(WholesaleSurface3)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                )
                            }

                            FmSectionLabel(title = "Pricing & details")
                            FmCard {
                                FmMoneyRow("List price", formatDecimal(product.basePrice))
                                FmMoneyRow("Dealer price", formatDecimal(product.dealerPrice))
                                FmMoneyRow("MRP", formatDecimal(product.mrp ?: product.basePrice))
                                FmMoneyRow("GST", "${formatDecimal(product.gstRate ?: product.gstPercentage)}%")
                                FmMoneyRow("Shopkeeper discount", "${formatDecimal(product.shopkeeperDiscount)}%")
                                product.sku?.takeIf { it.isNotBlank() }?.let { FmMoneyRow("SKU", it) }
                                product.caseQty?.let { FmMoneyRow("Case qty", "$it") }
                                bulkShippingCaption(product)?.let {
                                    Spacer(Modifier.height(8.dp))
                                    androidx.compose.material3.Text(it, fontSize = 12.sp, color = WholesaleBlue)
                                }
                            }

                            FmSectionLabel(title = "Quantity")
                            FmCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    FmQuantityInput(
                                        value = qty,
                                        onValueChange = { qty = it.coerceAtLeast(1) },
                                        min = 1,
                                        max = maxOrderQuantity,
                                    )
                                    androidx.compose.material3.Text(
                                        formatDecimal(lineTotal),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WholesaleText,
                                    )
                                }
                            }
                            Spacer(Modifier.height(100.dp))
                        }
                    }

                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, WholesaleBg, WholesaleBg)))
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                    ) {
                        FmButton(
                            text = "Add to cart · ${formatDecimal(lineTotal)}",
                            onClick = {
                                onAddToCart(product, qty)
                                navController.popBackStack()
                            },
                            variant = FmButtonVariant.Primary,
                        )
                    }
                }
            }
        }
    }
}
