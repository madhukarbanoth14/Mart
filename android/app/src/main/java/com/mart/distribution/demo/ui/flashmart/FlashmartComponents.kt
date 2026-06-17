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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBlueTintInk
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleBorder2
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

@Composable
fun FmCard(
    modifier: Modifier = Modifier,
    padding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier =
            modifier
                .shadow(2.dp, shape)
                .clip(shape)
                .background(Color.White)
                .border(1.dp, WholesaleBorder, shape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
    val (bg, fg, border) =
        when (variant) {
            FmButtonVariant.Primary -> Triple(WholesaleBlue, Color.White, Color.Transparent)
            FmButtonVariant.Dark -> Triple(WholesaleText, Color.White, Color.Transparent)
            FmButtonVariant.Soft -> Triple(WholesaleBlueTint, WholesaleBlueTintInk, Color.Transparent)
            FmButtonVariant.Outline -> Triple(Color.White, WholesaleText, WholesaleBorder2)
            FmButtonVariant.Ghost -> Triple(Color.Transparent, WholesaleInk2, Color.Transparent)
        }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = bg,
                contentColor = fg,
                disabledContainerColor = bg.copy(alpha = 0.45f),
                disabledContentColor = fg.copy(alpha = 0.6f),
            ),
        border = if (variant == FmButtonVariant.Outline) androidx.compose.foundation.BorderStroke(1.dp, border) else null,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp)
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
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (on) Color.White else Color.Transparent)
                        .then(if (on) Modifier.shadow(1.dp, RoundedCornerShape(9.dp)) else Modifier)
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
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(RoundedCornerShape(size * 0.32f))
                .background(Brush.linearGradient(listOf(tint, tint.copy(alpha = 0.75f)))),
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
) {
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
                    Text(it, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleBlue)
                    Spacer(Modifier.height(3.dp))
                }
                Text(
                    title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleText,
                    letterSpacing = (-0.5).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, fontSize = 13.sp, color = WholesaleMuted)
                }
            }
        }
        right?.invoke()
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
                .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 26.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, WholesaleBorder, RoundedCornerShape(20.dp))
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
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onChange(item.id) }
                            .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
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
                            modifier = Modifier.size(23.dp),
                        )
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

@Composable
fun FmHeroCard(
    totalOrders: Int,
    inProgress: Int,
    onNewOrder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.linearGradient(listOf(WholesaleBlue, WholesaleBlueDeep)))
                .padding(20.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(160.dp)
                    .clip(RoundedCornerShape(80.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
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
