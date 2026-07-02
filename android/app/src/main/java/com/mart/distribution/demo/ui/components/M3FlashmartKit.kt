package com.mart.distribution.demo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBlueTintInk
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface3
import com.mart.distribution.demo.ui.theme.WholesaleText

/**
 * Small Material 3 building blocks for the Flashmart auth/onboarding redesign
 * (mirrors `Flashmart 2/android/m3.jsx`). Scoped to this screen group only —
 * does not touch the app-wide WholesaleLight ColorScheme.
 */

@Composable
fun RegistrationStepper(
    step: Int,
    totalSteps: Int,
    stepLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Step ${step + 1} of $totalSteps",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WholesaleBlue,
            )
            Text(stepLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            repeat(totalSteps) { i ->
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(if (i <= step) WholesaleBlue else WholesaleSurface3),
                )
            }
        }
    }
}

@Composable
fun OtpBoxes(
    code: String,
    length: Int = 6,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp, alignment = Alignment.CenterHorizontally),
    ) {
        repeat(length) { i ->
            val ch = code.getOrNull(i)
            val isActive = i == code.length && ch == null
            val borderColor = if (ch != null || isActive) WholesaleBlue else WholesaleBorder
            Box(
                modifier =
                    Modifier
                        .size(width = 46.dp, height = 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WholesaleSurface3)
                        .border(if (ch != null || isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    ch?.toString() ?: "",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WholesaleText,
                )
            }
        }
    }
}

data class RoleCardSpec(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val body: String,
    val bg: Color,
    val fg: Color,
)

@Composable
fun RoleCard(
    spec: RoleCardSpec,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) WholesaleBlueTint else Color.White)
                .border(if (selected) 2.dp else 1.dp, if (selected) WholesaleBlue else WholesaleBorder, shape)
                .clickable(onClick = onClick)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(spec.bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(spec.icon, contentDescription = null, tint = spec.fg, modifier = Modifier.size(26.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(spec.title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
            Text(
                spec.body,
                fontSize = 13.sp,
                color = WholesaleMuted,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Box(
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) WholesaleBlue else Color.Transparent)
                    .border(if (selected) 0.dp else 2.dp, WholesaleBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
fun M3StatusChip(
    label: String,
    bg: Color = WholesaleBlueTint,
    fg: Color = WholesaleBlueTintInk,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(99.dp))
                .background(bg)
                .padding(horizontal = 11.dp, vertical = 5.dp),
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}
