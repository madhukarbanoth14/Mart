package com.mart.distribution.demo.ui.flashmart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.catalogUnitPrice
import com.mart.distribution.demo.data.api.dto.discountPercentForRole
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import kotlin.math.roundToInt

@Composable
fun FlashmartProductRow(
    product: ProductDto,
    cartQty: Int,
    buyerRole: String = "SHOPKEEPER",
    onOpenDetail: () -> Unit,
    onFirstAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val brandName = product.brand?.name ?: product.brandType
    val price = product.catalogUnitPrice(buyerRole)
    val mrp = product.mrp.toDoubleFromApiOrNull()
    val discPct =
        remember(product.id, price, mrp, buyerRole) {
            if (mrp != null && mrp > price && mrp > 0) {
                ((mrp - price) / mrp * 100).roundToInt()
            } else {
                product.discountPercentForRole(buyerRole).roundToInt()
            }
        }
    val gstPct = product.gstPercentage.toDoubleFromApiOrNull()?.roundToInt()
        ?: product.gstRate.toDoubleFromApiOrNull()?.roundToInt()

    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(1.dp, shape)
                .clip(shape)
                .background(Color.White)
                .clickable(onClick = onOpenDetail)
                .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductThumbnail(
            imageUrl = product.imageUrl,
            brandLogoUrl = product.brand?.logoUrl,
            productName = product.name,
            brandName = brandName,
            style = ProductImageStyle.Grid,
            cornerRadius = 13.dp,
            modifier = Modifier.size(62.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                brandName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WholesaleBlue,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleText,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 2.dp),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    formatDecimal(price),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleText,
                )
                product.weight?.takeIf { it.isNotBlank() }?.let {
                    Text("/ $it", fontSize = 11.sp, color = WholesaleInk4)
                }
                discPct?.takeIf { it > 0 }?.let {
                    Text(
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
                gstPct?.let {
                    Text(
                        "GST $it%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WholesaleMuted,
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WholesaleSurface3)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
        if (cartQty > 0) {
            FmStepper(
                value = cartQty,
                onChange = { v ->
                    when {
                        v <= 0 -> onDecrement()
                        v > cartQty -> onIncrement()
                        v < cartQty -> onDecrement()
                    }
                },
                min = 0,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WholesaleBlue)
                        .clickable(onClick = onFirstAdd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun FlashmartFloatingCartBar(
    itemCount: Int,
    totalLabel: String,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(WholesaleText)
                .clickable(onClick = onOpenCart)
                .padding(start = 18.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text("$itemCount", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
            Text("View cart", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(WholesaleBlue)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(totalLabel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Text("→", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
