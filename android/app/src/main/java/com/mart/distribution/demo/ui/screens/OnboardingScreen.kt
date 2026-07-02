package com.mart.distribution.demo.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder2
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleSurface
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText
import kotlinx.coroutines.launch

private enum class OnboardingArt { CATALOG, TRACK, INVOICE }

private data class OnboardingPage(
    val kicker: String,
    val title: String,
    val body: String,
    val art: OnboardingArt,
)

private val onboardingPages =
    listOf(
        OnboardingPage(
            kicker = "ORDER",
            title = "Your whole shop,\none tap away",
            body = "Browse FMCG products at dealer prices. Build your cart and reorder favourites in seconds.",
            art = OnboardingArt.CATALOG,
        ),
        OnboardingPage(
            kicker = "DELIVER",
            title = "Delivered by your\nlocal dealer",
            body = "Orders route straight to your assigned distributor. Track every delivery live, right to your counter.",
            art = OnboardingArt.TRACK,
        ),
        OnboardingPage(
            kicker = "BILL",
            title = "GST invoices,\nsorted automatically",
            body = "Every order generates a compliant tax invoice. Download, share, and file — no paperwork.",
            art = OnboardingArt.INVOICE,
        ),
    )

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == onboardingPages.size - 1

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(WholesaleSurface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                "Skip",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleMuted,
                modifier = Modifier.clickable(onClick = onFinish),
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { pageIndex ->
            val page = onboardingPages[pageIndex]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    OnboardingArtView(page.art)
                }
                Spacer(Modifier.height(30.dp))
                Text(
                    page.kicker,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = WholesaleBlue,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    page.title,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 36.sp,
                    color = WholesaleText,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    page.body,
                    fontSize = 15.5.sp,
                    lineHeight = 22.sp,
                    color = WholesaleMuted,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(bottom = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageDots(count = onboardingPages.size, active = pagerState.currentPage)
            Spacer(Modifier.height(22.dp))
            FmButton(
                text = if (isLast) "Get started" else "Continue",
                onClick = {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
            )
        }
    }
}

@Composable
private fun PageDots(count: Int, active: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(count) { i ->
            val width by animateDpAsState(if (i == active) 22.dp else 7.dp, label = "dot")
            Box(
                modifier =
                    Modifier
                        .height(7.dp)
                        .width(width)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (i == active) WholesaleBlue else WholesaleBorder2),
            )
        }
    }
}

@Composable
private fun OnboardingArtView(art: OnboardingArt) {
    when (art) {
        OnboardingArt.CATALOG -> CatalogArt()
        OnboardingArt.TRACK -> TrackArt()
        OnboardingArt.INVOICE -> InvoiceArt()
    }
}

@Composable
private fun CatalogArt() {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(200.dp).clip(CircleShape).background(WholesaleBlueTint),
        )
        val items = listOf("Atta" to "Rs 420", "Oil" to "Rs 180", "Salt" to "Rs 28", "Tea" to "Rs 95")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { (name, price) ->
                        Column(
                            modifier =
                                Modifier
                                    .width(112.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(WholesaleSurface)
                                    .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(WholesaleSurface2),
                            )
                            Text(name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                            Text(price, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WholesaleBlue)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackArt() {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(200.dp).clip(CircleShape).background(WholesaleGreenTint),
        )
        Column(
            modifier =
                Modifier
                    .width(250.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(WholesaleSurface)
                    .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(WholesaleBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Inventory2, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text("Today, by 6 PM", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                    Text("Sharma Distributors", fontSize = 11.sp, color = WholesaleMuted)
                }
            }
            val steps = listOf("Placed" to true, "Accepted" to true, "Out for delivery" to false, "Delivered" to false)
            steps.forEach { (label, done) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (done) WholesaleGreen else WholesaleSurface3),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) {
                            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(11.dp))
                        }
                    }
                    Text(
                        label,
                        fontSize = 12.5.sp,
                        fontWeight = if (done) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (done) WholesaleText else WholesaleInk4,
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceArt() {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(200.dp).clip(CircleShape).background(WholesaleOrangeTint),
        )
        Column(
            modifier =
                Modifier
                    .width(210.dp)
                    .rotate(-3f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WholesaleSurface),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(WholesaleInkSurface)
                        .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Tax Invoice", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("INV-1245", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
            }
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                InvoiceRow("Subtotal", "Rs 4,180", WholesaleMuted, FontWeight.Normal)
                InvoiceRow("GST", "Rs 512", WholesaleMuted, FontWeight.Normal)
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(1.dp)
                            .background(WholesaleBorder2),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Total", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                    Text("Rs 4,692", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WholesaleGreen)
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(label: String, value: String, color: Color, weight: FontWeight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, fontSize = 12.sp, color = color, fontWeight = weight)
        Text(value, fontSize = 12.sp, color = color, fontWeight = weight)
    }
}
