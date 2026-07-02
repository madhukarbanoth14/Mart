package com.mart.distribution.demo.ui.flashmart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.R
import com.mart.distribution.demo.ui.util.pressScale
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBlueTintInk
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleBorder2
import com.mart.distribution.demo.ui.theme.WholesaleBrandGlow
import com.mart.distribution.demo.ui.theme.WholesaleBrandNavy
import com.mart.distribution.demo.ui.theme.WholesaleShadow
import com.mart.distribution.demo.ui.theme.WholesaleShadowSoft
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleInk2
import com.mart.distribution.demo.ui.theme.WholesaleInk4
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleRedTint
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.MartFieldDefaults

@Composable
fun FmLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    Image(
        painter = painterResource(R.drawable.flashmart_logo),
        contentDescription = "FlashMart",
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.28f)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun FmLogoHeader(
    modifier: Modifier = Modifier,
    markSize: Dp = 42.dp,
    title: String = "FlashMart",
    subtitle: String = "Distribution OS",
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FmLogoMark(size = markSize)
        Column {
            Text(
                title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.3).sp,
            )
            Text(
                subtitle,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun FmCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier =
            modifier
                .shadow(10.dp, shape, ambientColor = WholesaleShadow, spotColor = WholesaleShadow)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, WholesaleBorder, shape)
                .then(if (onClick != null) Modifier.pressScale(onClick = onClick) else Modifier)
                .padding(padding),
        content = content,
    )
}

@Composable
fun FmButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: FmButtonVariant = FmButtonVariant.Primary,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
) {
    val shape = RoundedCornerShape(14.dp)
    val gradient: Brush? =
        when (variant) {
            FmButtonVariant.Primary -> Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep))
            FmButtonVariant.Dark -> Brush.linearGradient(listOf(WholesaleInk2, WholesaleText))
            else -> null
        }
    val solid: Color =
        when (variant) {
            FmButtonVariant.Soft -> WholesaleBlueTint
            FmButtonVariant.Outline -> Color.White
            else -> Color.Transparent
        }
    val fg =
        when (variant) {
            FmButtonVariant.Primary, FmButtonVariant.Dark -> Color.White
            FmButtonVariant.Soft -> WholesaleBlueTintInk
            FmButtonVariant.Outline -> WholesaleText
            FmButtonVariant.Ghost -> WholesaleInk2
        }
    Box(
        modifier =
            modifier
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .height(52.dp)
                .alpha(if (enabled) 1f else 0.5f)
                .then(
                    if (variant == FmButtonVariant.Primary) {
                        Modifier.shadow(14.dp, shape, ambientColor = WholesaleBrandGlow, spotColor = WholesaleBrandGlow)
                    } else {
                        Modifier
                    },
                )
                .clip(shape)
                .then(
                    if (gradient != null) Modifier.background(gradient, shape) else Modifier.background(solid, shape),
                )
                .then(
                    if (variant == FmButtonVariant.Outline) Modifier.border(1.dp, WholesaleBorder2, shape) else Modifier,
                )
                .pressScale(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp, color = fg)
    }
}

enum class FmButtonVariant { Primary, Dark, Soft, Outline, Ghost }

@Composable
fun FmBadge(status: String, label: String? = null) {
    val upper = status.uppercase()
    val (bg, fg, text) =
        when (upper) {
            "PENDING", "PLACED", "PENDING_APPROVAL" -> Triple(WholesaleOrangeTint, WholesaleOrange, label ?: if (upper == "PENDING_APPROVAL") "Pending" else "Placed")
            "DEALER_CONFIRMED", "ACCEPTED" -> Triple(WholesaleBlueTint, WholesaleBlueTintInk, label ?: "Accepted")
            "OUT_FOR_DELIVERY", "OUT" -> Triple(WholesaleOrangeTint, WholesaleOrange, label ?: "Out for delivery")
            "DELIVERED" -> Triple(WholesaleGreenTint, WholesaleGreen, label ?: "Delivered")
            "RETURN_REQUESTED" -> Triple(WholesaleOrangeTint, WholesaleOrange, label ?: "Return requested")
            "RETURNED" -> Triple(WholesaleBlueTint, WholesaleBlueTintInk, label ?: "Returned")
            "REFUNDED" -> Triple(WholesaleBlueTint, WholesaleBlueTintInk, label ?: "Refunded")
            "PAID" -> Triple(WholesaleGreenTint, WholesaleGreen, label ?: "Paid")
            "UNPAID" -> Triple(WholesaleRedTint, WholesaleRed, label ?: "Unpaid")
            "ACTIVE" -> Triple(WholesaleGreenTint, WholesaleGreen, label ?: "Active")
            "REJECTED", "DEACTIVATED" -> Triple(WholesaleRedTint, WholesaleRed, label ?: status.replace('_', ' ').lowercase().replaceFirstChar { it.titlecase() })
            else -> Triple(WholesaleSurface3, WholesaleInk2, label ?: status)
        }
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(bg)
                .padding(horizontal = 11.dp, vertical = 6.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
fun FmStepper(
    value: Int,
    onChange: (Int) -> Unit,
    min: Int = 0,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(WholesaleSurface2)
                .border(1.dp, WholesaleBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FmStepperBtn(Icons.Filled.Remove, enabled = value > min) {
            onChange((value - 1).coerceAtLeast(min))
        }
        Text(
            "$value",
            modifier = Modifier.padding(horizontal = 4.dp),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleText,
        )
        FmStepperBtn(Icons.Filled.Add, enabled = true) { onChange(value + 1) }
    }
}

@Composable
private fun FmStepperBtn(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (enabled) Color.White else WholesaleSurface3)
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (enabled) WholesaleText else WholesaleInk4,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
fun FmSegmentedControl(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WholesaleSurface3)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { opt ->
            val on = opt == selected
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .then(if (on) Modifier.shadow(4.dp, RoundedCornerShape(10.dp), ambientColor = WholesaleShadowSoft, spotColor = WholesaleShadowSoft) else Modifier)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (on) Color.White else Color.Transparent)
                        .clickable { onSelect(opt) }
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    opt,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (on) WholesaleText else WholesaleMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun FmMoneyRow(
    label: String,
    value: String,
    strong: Boolean = false,
    accent: Color? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = if (strong) 6.dp else 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = if (strong) 16.sp else 14.sp,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Medium,
            color = if (strong) WholesaleText else WholesaleInk2,
        )
        Text(
            value,
            fontSize = if (strong) 19.sp else 14.sp,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.SemiBold,
            color = accent ?: if (strong) WholesaleText else WholesaleInk2,
        )
    }
}

@Composable
fun FmSectionLabel(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleMuted,
            letterSpacing = 0.5.sp,
        )
        if (action != null && onAction != null) {
            Text(
                action,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleBlue,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
fun FmAvatar(
    name: String,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    tint: Color = WholesaleBlue,
) {
    val initials =
        name.split("\\s+".toRegex()).take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
    val avatarShape = RoundedCornerShape(size * 0.32f)
    Box(
        modifier =
            Modifier
                .size(size)
                .shadow(6.dp, avatarShape, ambientColor = tint.copy(alpha = 0.5f), spotColor = tint.copy(alpha = 0.5f))
                .clip(avatarShape)
                .background(Brush.linearGradient(listOf(tint, tint.copy(alpha = 0.72f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials.ifEmpty { "?" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.38f).sp,
        )
    }
}

@Composable
fun FmAppHeader(
    title: String,
    subtitle: String? = null,
    kicker: String? = null,
    onBack: (() -> Unit)? = null,
    right: @Composable (() -> Unit)? = null,
    lightOnDark: Boolean = false,
) {
    val titleColor = if (lightOnDark) Color.White else WholesaleText
    val subtitleColor = if (lightOnDark) Color.White.copy(0.55f) else WholesaleMuted
    val kickerColor = if (lightOnDark) Color.White.copy(0.75f) else WholesaleBlue
    val dividerColor =
        if (lightOnDark) Color.White.copy(0.12f) else WholesaleBorder.copy(alpha = 0.65f)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
            if (onBack != null) {
                Box(
                    modifier =
                        Modifier
                            .padding(end = 12.dp, top = 2.dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, WholesaleBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("‹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WholesaleInk2)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                kicker?.let {
                    Text(it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = kickerColor)
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    letterSpacing = (-0.5).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, fontSize = 13.sp, color = subtitleColor)
                }
            }
        }
        right?.invoke()
    }
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = FmSpacing.screenH)
                .padding(top = 2.dp, bottom = FmSpacing.headerBottom),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor),
        )
    }
}

@Composable
fun FmIconButton(
    icon: ImageVector,
    onClick: (() -> Unit)?,
    badge: Int? = null,
    contentDescription: String? = null,
) {
    BadgedBox(
        badge = {
            if (badge != null && badge > 0) {
                Box(
                    modifier =
                        Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(WholesaleRed)
                            .padding(2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$badge", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
    ) {
        Box(
            modifier =
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White)
                    .border(1.dp, WholesaleBorder, RoundedCornerShape(13.dp))
                    .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = contentDescription, tint = WholesaleInk2, modifier = Modifier.size(20.dp))
        }
    }
}

data class FmNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val badge: Int? = null,
)

@Composable
fun FmBottomNav(
    items: List<FmNavItem>,
    activeId: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.95f), Color.White),
                    ),
                )
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 10.dp),
    ) {
        val barShape = RoundedCornerShape(20.dp)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .shadow(16.dp, barShape, ambientColor = WholesaleShadow, spotColor = WholesaleShadow)
                    .clip(barShape)
                    .background(Color.White)
                    .border(1.dp, WholesaleBorder, barShape)
                    .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val on = item.id == activeId
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .pressScale(pressedScale = 0.92f) { onChange(item.id) }
                            .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (on) WholesaleBlueTint else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BadgedBox(
                            badge = {
                                if (item.badge != null && item.badge > 0) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .size(16.dp)
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(WholesaleRed),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("${item.badge}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            },
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (on) WholesaleBlue else WholesaleInk4,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                    Text(
                        item.label,
                        fontSize = 11.sp,
                        fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (on) WholesaleBlue else WholesaleInk4,
                    )
                }
            }
        }
    }
}

@Composable
fun FmStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    accent: Color = WholesaleBlue,
    tint: Color = WholesaleBlueTint,
) {
    FmCard(modifier = modifier) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
        Spacer(Modifier.height(10.dp))
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = WholesaleText, letterSpacing = (-0.5).sp)
        sub?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, fontSize = 12.sp, color = WholesaleInk4)
        }
    }
}

data class FmHomeStat(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val bg: Color,
    val fg: Color,
)

@Composable
fun FmOutstandingHero(
    outstandingLabel: String,
    onPayNow: () -> Unit,
    onViewLedger: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heroShape = RoundedCornerShape(24.dp)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(18.dp, heroShape, ambientColor = WholesaleBrandGlow, spotColor = WholesaleBrandGlow)
                .clip(heroShape)
                .background(Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep, WholesaleBrandNavy)))
                .padding(20.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(150.dp)
                    .clip(RoundedCornerShape(75.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
        )
        Column {
            Text(
                "Outstanding balance",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.85f),
            )
            Text(
                outstandingLabel,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(top = 5.dp, bottom = 14.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onPayNow,
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = com.mart.distribution.demo.ui.theme.WholesaleGold,
                            contentColor = com.mart.distribution.demo.ui.theme.WholesaleGoldInk,
                        ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text("Pay now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                TextButton(onClick = onViewLedger) {
                    Text("View ledger", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun FmHomeStatGrid(
    stats: List<FmHomeStat>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(11.dp)) {
        stats.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                row.forEach { stat ->
                    FmCard(
                        modifier = Modifier.weight(1f).background(WholesaleSurface2, RoundedCornerShape(18.dp)),
                        padding = androidx.compose.foundation.layout.PaddingValues(15.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(stat.bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(stat.icon, null, tint = stat.fg, modifier = Modifier.size(19.dp))
                        }
                        Text(
                            stat.value,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = WholesaleText,
                            letterSpacing = (-0.3).sp,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Text(
                            stat.label,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WholesaleMuted,
                            modifier = Modifier.padding(top = 1.dp),
                        )
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FmQuickActionGrid(
    actions: List<Pair<ImageVector, String>>,
    onAction: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEachIndexed { index, (icon, label) ->
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .pressScale(onClick = { onAction(index) })
                        .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(WholesaleBlueTint),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = WholesaleBlue, modifier = Modifier.size(23.dp))
                }
                Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun FmPromoBanner(
    title: String,
    subtitle: String,
    kicker: String = "THIS WEEK",
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(com.mart.distribution.demo.ui.theme.WholesaleGoldTint)
                .padding(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.72f)) {
            Text(
                kicker,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = com.mart.distribution.demo.ui.theme.WholesaleGoldInk,
                letterSpacing = 0.5.sp,
            )
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WholesaleText,
                letterSpacing = (-0.3).sp,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                subtitle,
                fontSize = 12.5.sp,
                color = com.mart.distribution.demo.ui.theme.WholesaleGoldInk,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
fun FmHeroCard(
    totalOrders: Int,
    inProgress: Int,
    onNewOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val heroShape = RoundedCornerShape(24.dp)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(18.dp, heroShape, ambientColor = WholesaleBrandGlow, spotColor = WholesaleBrandGlow)
                .clip(heroShape)
                .background(Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep, WholesaleBrandNavy)))
                .border(1.dp, Color.White.copy(alpha = 0.10f), heroShape)
                .padding(20.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(170.dp)
                    .clip(RoundedCornerShape(85.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .size(120.dp)
                    .clip(RoundedCornerShape(60.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
        )
        Column {
            Text(
                "Ready to restock?",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.82f),
            )
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("$totalOrders", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Total orders", fontSize = 12.sp, color = Color.White.copy(alpha = 0.78f))
                }
                Box(Modifier.size(width = 1.dp, height = 48.dp).background(Color.White.copy(alpha = 0.25f)))
                Column {
                    Text("$inProgress", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("In progress", fontSize = 12.sp, color = Color.White.copy(alpha = 0.78f))
                }
            }
            Button(
                onClick = onNewOrder,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WholesaleBlueDeep),
            ) {
                Text("+ New order", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FmFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(99.dp))
                .background(if (selected) WholesaleBlueTint else Color.White)
                .border(
                    1.dp,
                    if (selected) WholesaleBlue.copy(alpha = 0.35f) else WholesaleBorder,
                    RoundedCornerShape(99.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) WholesaleBlueTintInk else WholesaleInk2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FmChipRow(
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (id, label) ->
            FmFilterChip(
                label = label,
                selected = selectedId == id,
                onClick = { onSelect(id) },
            )
        }
    }
}

// ── Enterprise layout & feedback ─────────────────────────────────────────────

@Composable
fun FmScreen(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(Modifier) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(WholesaleBg),
    ) {
        FmAppHeader(title = title, subtitle = subtitle, onBack = onBack)
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = FmSpacing.screenH),
        ) {
            content(Modifier.fillMaxSize())
        }
    }
}

@Composable
fun FmTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    error: String? = null,
    onDark: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            enabled = enabled,
            isError = error != null,
            shape = MartFieldDefaults.fieldShape,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors =
                if (onDark) {
                    MartFieldDefaults.outlinedOnDarkColors()
                } else {
                    MartFieldDefaults.outlinedColors()
                },
        )
        error?.let {
            Text(it, fontSize = 12.sp, color = WholesaleRed, modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun FmMiniBarChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = WholesaleBlue,
    height: Dp = 52.dp,
) {
    if (data.isEmpty()) return
    val max = data.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Row(
        modifier = modifier.fillMaxWidth().height(height),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEachIndexed { index, value ->
            val isLast = index == data.lastIndex
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(height * (value / max).coerceIn(0.08f, 1f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (isLast) barColor else barColor.copy(alpha = 0.35f)),
            )
        }
    }
}

@Composable
fun FmGoldDiscountBadge(
    percent: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        "$percent% off",
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        color = com.mart.distribution.demo.ui.theme.WholesaleGoldInk,
        modifier =
            modifier
                .clip(RoundedCornerShape(6.dp))
                .background(com.mart.distribution.demo.ui.theme.WholesaleGoldTint)
                .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

@Composable
fun FmPillSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search products or brands",
    onGridClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(WholesaleSurface3)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Outlined.Search, null, tint = WholesaleMuted, modifier = Modifier.size(20.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, color = WholesaleMuted, fontSize = 15.sp) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = WholesaleText),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = WholesaleBlue,
                ),
        )
        Icon(
            Icons.Outlined.GridView,
            contentDescription = "Browse categories",
            tint = WholesaleMuted,
            modifier =
                Modifier
                    .size(19.dp)
                    .then(
                        if (onGridClick != null) {
                            Modifier.clickable(onClick = onGridClick)
                        } else {
                            Modifier
                        },
                    ),
        )
    }
}

@Composable
fun FmDealerDeliveryFooter(
    dealerName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("⚡", fontSize = 15.sp)
        Text(
            "Delivered by $dealerName · usually within a day",
            fontSize = 12.5.sp,
            color = WholesaleMuted,
            lineHeight = 17.sp,
        )
    }
}

@Composable
fun FmSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search…",
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = WholesaleBlue.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, WholesaleBorder, RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Outlined.Search,
            contentDescription = null,
            tint = WholesaleBlue,
            modifier = Modifier.size(20.dp),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, color = WholesaleInk4) },
            singleLine = true,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = WholesaleText,
                    unfocusedTextColor = WholesaleText,
                    cursorColor = WholesaleBlue,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
        )
        if (value.isNotEmpty()) {
            Text(
                "Clear",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleBlue,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onValueChange("") }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun FmSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = FmSpacing.listH, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = WholesaleText,
            letterSpacing = (-0.3).sp,
        )
        if (action != null && onAction != null) {
            Text(
                action,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = WholesaleBlue,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
fun FmInfoBanner(
    message: String,
    modifier: Modifier = Modifier,
    accent: Color = WholesaleBlue,
    tint: Color = WholesaleBlueTint,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(tint)
                .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(accent),
        )
        Text(message, fontSize = 13.sp, lineHeight = 18.sp, color = WholesaleInk2)
    }
}

@Composable
fun FmErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    FmInfoBanner(message = message, modifier = modifier, accent = WholesaleRed, tint = WholesaleRedTint)
}

@Composable
fun FmSuccessBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    FmInfoBanner(message = message, modifier = modifier, accent = WholesaleGreen, tint = WholesaleGreenTint)
}

@Composable
fun FmLoadingState(
    message: String = "Loading…",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = WholesaleBlue, strokeWidth = 2.5.dp, modifier = Modifier.size(28.dp))
        Text(message, fontSize = 13.sp, color = WholesaleMuted, textAlign = TextAlign.Center)
    }
}

@Composable
fun FmEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(WholesaleSurface2)
                    .border(1.dp, WholesaleBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = WholesaleInk4, modifier = Modifier.size(32.dp))
        }
        Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WholesaleText, textAlign = TextAlign.Center)
        Text(message, fontSize = 13.sp, color = WholesaleMuted, textAlign = TextAlign.Center, lineHeight = 18.sp)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(6.dp))
            FmButton(actionLabel, onClick = onAction, variant = FmButtonVariant.Soft, fullWidth = false)
        }
    }
}

@Composable
fun FmEmptyStateHero(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier =
                    Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(WholesaleBlueTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = WholesaleBlue, modifier = Modifier.size(46.dp))
            }
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WholesaleGreen)
                        .border(3.dp, WholesaleBg, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            title,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WholesaleText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            message,
            fontSize = 14.5.sp,
            color = WholesaleMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(14.dp))
            FmButton(actionLabel, onClick = onAction, variant = FmButtonVariant.Primary, fullWidth = false)
        }
    }
}

@Composable
fun FmSkeletonBlock(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 14.dp,
    cornerRadius: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "skel")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skelAlpha",
    )
    Box(
        modifier =
            modifier
                .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth())
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(WholesaleSurface3.copy(alpha = alpha)),
    )
}

@Composable
fun FmSkeletonListScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                FmSkeletonBlock(width = 130.dp, height = 13.dp)
                FmSkeletonBlock(width = 170.dp, height = 22.dp, modifier = Modifier.padding(top = 8.dp))
            }
            FmSkeletonBlock(width = 42.dp, height = 42.dp, cornerRadius = 21.dp)
            FmSkeletonBlock(width = 42.dp, height = 42.dp, cornerRadius = 21.dp)
        }
        FmSkeletonBlock(height = 130.dp, cornerRadius = 24.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            FmSkeletonBlock(modifier = Modifier.weight(1f), height = 92.dp, cornerRadius = 16.dp)
            FmSkeletonBlock(modifier = Modifier.weight(1f), height = 92.dp, cornerRadius = 16.dp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            FmSkeletonBlock(modifier = Modifier.weight(1f), height = 92.dp, cornerRadius = 16.dp)
            FmSkeletonBlock(modifier = Modifier.weight(1f), height = 92.dp, cornerRadius = 16.dp)
        }
        FmCard(padding = androidx.compose.foundation.layout.PaddingValues(6.dp)) {
            repeat(3) { i ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FmSkeletonBlock(width = 40.dp, height = 40.dp, cornerRadius = 12.dp)
                    Column(Modifier.weight(1f)) {
                        FmSkeletonBlock(width = 140.dp, height = 14.dp)
                        FmSkeletonBlock(width = 90.dp, height = 11.dp, modifier = Modifier.padding(top = 7.dp))
                    }
                    FmSkeletonBlock(width = 56.dp, height = 20.dp, cornerRadius = 99.dp)
                }
                if (i < 2) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
                }
            }
        }
    }
}

@Composable
fun FmErrorScreen(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    primaryAction: String = "Try again",
    onPrimaryAction: () -> Unit,
    secondaryAction: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(WholesaleBg)
                .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(WholesaleRed.copy(0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = WholesaleRed, modifier = Modifier.size(50.dp))
        }
        Text(
            title,
            fontSize = 23.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WholesaleText,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(top = 26.dp),
        )
        Text(
            message,
            fontSize = 15.sp,
            color = WholesaleMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(top = 10.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            FmButton(primaryAction, onClick = onPrimaryAction, variant = FmButtonVariant.Primary)
            if (secondaryAction != null && onSecondaryAction != null) {
                FmButton(secondaryAction, onClick = onSecondaryAction, variant = FmButtonVariant.Outline)
            }
        }
    }
}

@Composable
fun FmDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    dismissLabel: String = "Cancel",
    confirmBusy: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!confirmBusy) onDismiss() },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FmSpacing.fieldGap)) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !confirmBusy) {
                Text(
                    if (confirmBusy) "Saving…" else confirmLabel,
                    color = WholesaleBlue,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !confirmBusy) {
                Text(dismissLabel, color = WholesaleMuted)
            }
        },
    )
}

@Composable
fun FmDataRow(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.pressScale(onClick = onClick) else Modifier)
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            subtitle?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, fontSize = 12.sp, color = WholesaleMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        trailing?.invoke()
    }
}
