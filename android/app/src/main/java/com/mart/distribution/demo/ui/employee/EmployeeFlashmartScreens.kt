package com.mart.distribution.demo.ui.employee

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmAvatar
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleText

private val EmpOrange = Color(0xFFC97A16)
private val EmpOrangeDeep = Color(0xFF92560A)

@Composable
fun EmployeeFlashmartHome(
    user: SessionUser,
    ui: MainUiState,
    onAddDealer: () -> Unit,
    onAddShopkeeper: () -> Unit,
    onOpenNetwork: () -> Unit,
) {
    val mine = employeeOnboarded(ui, user.id)
    val dealerCount = mine.count { it.role.equals("DEALER", true) }
    val shopCount = mine.count { it.role.equals("SHOPKEEPER", true) }
    val target = 120
    val progress = ((shopCount.toFloat() / target) * 100).toInt().coerceIn(0, 100)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Field executive", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = EmpOrange)
                    Text("${user.name.split(" ").firstOrNull()}'s desk", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                }
                FmAvatar(user.name, size = 42.dp, tint = EmpOrange)
            }
        }

        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(EmpOrange, EmpOrangeDeep)))
                        .padding(20.dp),
            ) {
                Column {
                    Text("This month's onboarding", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.85f))
                    Row(modifier = Modifier.padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column {
                            Text("$dealerCount", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Dealers", fontSize = 12.sp, color = Color.White.copy(0.8f))
                        }
                        Box(Modifier.size(width = 1.dp, height = 48.dp).background(Color.White.copy(0.2f)))
                        Column {
                            Text("$shopCount", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Shopkeepers", fontSize = 12.sp, color = Color.White.copy(0.8f))
                        }
                    }
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color.White.copy(0.2f)),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth(progress / 100f)
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(Color.White),
                        )
                    }
                    Text("$progress% of $target monthly target", fontSize = 11.sp, color = Color.White.copy(0.85f), modifier = Modifier.padding(top = 7.dp))
                }
            }
        }

        item {
            OnboardActionCard(
                title = "Add a dealer",
                sub = "Onboard a distributor to an area",
                tint = WholesaleGreenTint,
                iconTint = WholesaleGreen,
                icon = Icons.Outlined.LocalShipping,
                onClick = onAddDealer,
            )
        }
        item {
            OnboardActionCard(
                title = "Add a shopkeeper",
                sub = "Register a retail store",
                tint = WholesaleBlueTint,
                iconTint = WholesaleBlue,
                icon = Icons.Outlined.ShoppingBag,
                onClick = onAddShopkeeper,
            )
        }

        item { FmSectionLabel(title = "Recently onboarded", action = "View network", onAction = onOpenNetwork) }
        item {
            FmCard(padding = androidx.compose.foundation.layout.PaddingValues(4.dp)) {
                if (mine.isEmpty()) {
                    Text(
                        "Onboard someone to see them here",
                        modifier = Modifier.padding(26.dp).fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = com.mart.distribution.demo.ui.theme.WholesaleInk4,
                        fontSize = 13.sp,
                    )
                } else {
                    mine.take(3).forEachIndexed { i, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FmAvatar(row.name, size = 36.dp, tint = EmpOrange)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                                Text("${row.role} · ${row.area?.name ?: "—"}", fontSize = 12.sp, color = WholesaleMuted)
                            }
                            Text(
                                "NEW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WholesaleGreen,
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(WholesaleGreenTint)
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                        if (i < mine.take(3).lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardActionCard(
    title: String,
    sub: String,
    tint: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    FmCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(tint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                Text(sub, fontSize = 12.sp, color = WholesaleMuted)
            }
            Box(
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(WholesaleText),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmployeeNetworkTab(
    employeeId: String,
    ui: MainUiState,
) {
    val rows = employeeOnboarded(ui, employeeId)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "${rows.count { it.role.equals("DEALER", true) }} dealers · ${rows.count { it.role.equals("SHOPKEEPER", true) }} shopkeepers",
                fontSize = 13.sp,
                color = WholesaleMuted,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        if (rows.isEmpty()) {
            item {
                Text("You have not onboarded any partners yet.", color = WholesaleMuted, fontSize = 14.sp)
            }
        } else {
            items(rows, key = { it.id }) { row ->
                FmCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FmAvatar(row.name, size = 40.dp, tint = EmpOrange)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(row.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                            Text(row.email, fontSize = 12.sp, color = WholesaleMuted)
                            Text(row.role.replace('_', ' '), fontSize = 11.sp, color = WholesaleBlue, fontWeight = FontWeight.SemiBold)
                        }
                        FmBadge(row.status.ifBlank { "ACTIVE" })
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeProfileTab(
    user: SessionUser,
    ui: MainUiState,
    onLogout: () -> Unit,
) {
    val mine = employeeOnboarded(ui, user.id)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    FmAvatar(user.name, size = 56.dp, tint = EmpOrange)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text(user.email, fontSize = 13.sp, color = WholesaleMuted)
                    }
                    FmBadge("ACTIVE")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                com.mart.distribution.demo.ui.flashmart.FmStatCard(
                    "Dealers",
                    "${mine.count { it.role.equals("DEALER", true) }}",
                    modifier = Modifier.weight(1f),
                )
                com.mart.distribution.demo.ui.flashmart.FmStatCard(
                    "Shopkeepers",
                    "${mine.count { it.role.equals("SHOPKEEPER", true) }}",
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            FmCard(onClick = onLogout) {
                Text("Sign out", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleRed)
            }
        }
    }
}

private fun employeeOnboarded(ui: MainUiState, employeeId: String): List<UserRowDto> =
    when (val u = ui.users) {
        is LoadState.Ok ->
            u.data.filter {
                it.onboardedById == employeeId &&
                    (it.role.equals("SHOPKEEPER", true) || it.role.equals("DEALER", true))
            }.sortedByDescending { it.createdAt ?: "" }
        else -> emptyList()
    }
