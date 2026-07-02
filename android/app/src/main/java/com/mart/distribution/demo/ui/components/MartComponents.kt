package com.mart.distribution.demo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleBrandGlow
import com.mart.distribution.demo.ui.theme.WholesaleShadow
import com.mart.distribution.demo.ui.util.pressScale
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleRedTint
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleText

// ── Flashmart-style elevated card ────────────────────────────────────────────
@Composable
fun MartElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .shadow(10.dp, shape, ambientColor = WholesaleShadow, spotColor = WholesaleShadow)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, WholesaleBorder, shape)
            .padding(16.dp),
        content = content,
    )
}

// ── Flashmart primary button (indigo gradient with brand glow) ────────────────
@Composable
fun GradientGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .shadow(16.dp, shape, ambientColor = WholesaleBrandGlow, spotColor = WholesaleBrandGlow)
            .clip(shape)
            .background(Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep)), shape)
            .pressScale(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp, color = Color.White)
    }
}

// ── Flashmart status badge ────────────────────────────────────────────────────
@Composable
fun OrderStatusChip(status: String) {
    val upper = status.uppercase()
    val (bg, fg) = when (upper) {
        "PENDING"                        -> WholesaleOrangeTint to WholesaleOrange
        "DEALER_CONFIRMED", "ACCEPTED"   -> WholesaleBlueTint   to WholesaleBlue
        "OUT_FOR_DELIVERY"               -> WholesaleOrangeTint to WholesaleOrange
        "DELIVERED"                      -> WholesaleGreenTint  to WholesaleGreen
        "CANCELLED"                      -> WholesaleRedTint    to WholesaleRed
        else                             -> WholesaleSurface2   to WholesaleMuted
    }
    val label = when (upper) {
        "PENDING"           -> "Placed"
        "DEALER_CONFIRMED",
        "ACCEPTED"          -> "Accepted"
        "OUT_FOR_DELIVERY"  -> "On the way"
        "DELIVERED"         -> "Delivered"
        "CANCELLED"         -> "Cancelled"
        else                -> upper
    }
    val animBg by animateColorAsState(bg, tween(220), label = "chipBg")
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(animBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

// ── Section header (used in dashboards) ──────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleText,
            letterSpacing = (-0.4).sp,
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                color = WholesaleMuted,
                lineHeight = 18.sp,
            )
        }
    }
}

// ── Screen title bar (used on list/detail pages) ──────────────────────────────
@Composable
fun ScreenTitleBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(horizontal = 4.dp, vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleText,
            letterSpacing = (-0.6).sp,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .height(3.dp)
                .width(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(WholesaleBlue),
        )
    }
}
