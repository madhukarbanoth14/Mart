package com.mart.distribution.demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Scale

/** How product pack shots are framed inside the tile. */
enum class ProductImageStyle {
    /** Square grid cards — crop to fill the tile (edges may trim). */
    Grid,
    /** Product detail hero — full product visible, centered with padding. */
    Hero,
}

@Composable
fun ProductThumbnail(
    imageUrl: String?,
    brandLogoUrl: String?,
    productName: String,
    brandName: String?,
    modifier: Modifier = Modifier,
    style: ProductImageStyle = ProductImageStyle.Grid,
    cornerRadius: Dp = 12.dp,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val ref =
        imageUrl?.trim()?.takeIf { it.isNotBlank() }
            ?: brandLogoUrl?.trim()?.takeIf { it.isNotBlank() }
    val fallbackChar =
        (brandName?.firstOrNull() ?: productName.firstOrNull() ?: '#').uppercaseChar().toString()

    val innerPadding =
        when (style) {
            ProductImageStyle.Grid -> 0.dp
            ProductImageStyle.Hero -> 20.dp
        }
    val contentScale =
        when (style) {
            ProductImageStyle.Grid -> ContentScale.Crop
            ProductImageStyle.Hero -> ContentScale.Fit
        }
    val coilScale =
        when (style) {
            ProductImageStyle.Grid -> Scale.FILL
            ProductImageStyle.Hero -> Scale.FIT
        }
    val decodeSizePx =
        when (style) {
            ProductImageStyle.Grid -> with(density) { 480.dp.roundToPx() }
            ProductImageStyle.Hero -> with(density) { 960.dp.roundToPx() }
        }

    val frameShape = RoundedCornerShape(cornerRadius)
    val frameBackground =
        Brush.verticalGradient(
            colors =
                listOf(
                    Color(0xFFF5F5F5),
                    Color(0xFFEBEBEB),
                ),
        )

    Box(
        modifier =
            modifier
                .clip(frameShape)
                .background(frameBackground),
        contentAlignment = Alignment.Center,
    ) {
        if (ref != null) {
            val model =
                remember(ref, style, decodeSizePx, coilScale) {
                    ImageRequest.Builder(context)
                        .data(ref)
                        .crossfade(280)
                        .scale(coilScale)
                        .size(decodeSizePx)
                        .memoryCacheKey("$ref-$style")
                        .diskCacheKey("$ref-$style")
                        .build()
                }
            SubcomposeAsyncImage(
                model = model,
                contentDescription = productName,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentScale = contentScale,
                alignment = Alignment.Center,
                filterQuality = FilterQuality.High,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                error = {
                    ProductImageFallback(fallbackChar)
                },
            )
        } else {
            ProductImageFallback(fallbackChar)
        }
    }
}

@Composable
private fun ProductImageFallback(letter: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
