package com.mart.distribution.demo.ui.wholesale

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.ProductDto
import com.mart.distribution.demo.data.api.dto.toDoubleFromApiOrNull
import com.mart.distribution.demo.ui.components.ProductImageStyle
import com.mart.distribution.demo.ui.components.ProductThumbnail
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleImageGradientEnd
import com.mart.distribution.demo.ui.theme.WholesaleImageGradientStart
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import kotlin.math.roundToInt

@Composable
fun WholesaleProductCard(
    product: ProductDto,
    cartQty: Int,
    onOpenDetail: () -> Unit,
    onFirstAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mrp = product.mrp.toDoubleFromApiOrNull()
    val price =
        product.dealerPrice.toDoubleFromApiOrNull()
            ?: product.basePrice.toDoubleFromApiOrNull()
    val discountPct =
        if (mrp != null && price != null && mrp > price) {
            ((mrp - price) / mrp * 100).roundToInt()
        } else {
            null
        }

    Surface(
        modifier =
            modifier
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, WholesaleBorder, RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenDetail),
        color = androidx.compose.ui.graphics.Color.White,
        shadowElevation = 1.dp,
    ) {
        Column {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.35f)
                        .background(
                            Brush.linearGradient(
                                listOf(WholesaleImageGradientStart, WholesaleImageGradientEnd),
                            ),
                        ),
            ) {
                ProductThumbnail(
                    imageUrl = product.imageUrl,
                    brandLogoUrl = product.brand?.logoUrl,
                    productName = product.name,
                    brandName = product.brand?.name,
                    style = ProductImageStyle.Grid,
                    cornerRadius = 0.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                if (discountPct != null && discountPct > 0) {
                    Text(
                        text = "$discountPct% OFF",
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .padding(7.dp)
                                .background(WholesaleRed, RoundedCornerShape(20.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                product.sku?.takeIf { it.isNotBlank() }?.let { sku ->
                    Text(
                        text = sku,
                        color = WholesaleBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = product.name,
                    color = WholesaleText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                product.weight?.takeIf { it.isNotBlank() }?.let { w ->
                    Text(
                        text = w,
                        color = WholesaleMuted,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatDecimal(price ?: product.basePrice),
                        color = WholesaleText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                    )
                    mrp?.let {
                        Text(
                            text = formatDecimal(it),
                            color = WholesaleMuted,
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(start = 4.dp, bottom = 1.dp),
                        )
                    }
                }
                Spacer(Modifier.height(7.dp))
                if (cartQty <= 0) {
                    Button(
                        onClick = onFirstAdd,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = WholesaleBlue,
                                contentColor = androidx.compose.ui.graphics.Color.White,
                            ),
                        contentPadding = ButtonDefaults.ContentPadding,
                    ) {
                        Text("+ ADD", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WholesaleBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .clickable(onClick = onDecrement),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Filled.Remove,
                                contentDescription = "Decrease",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = "$cartQty",
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                        )
                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .clickable(onClick = onIncrement),
                            contentAlignment = Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                Icons.Filled.Add,
                                contentDescription = "Increase",
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
