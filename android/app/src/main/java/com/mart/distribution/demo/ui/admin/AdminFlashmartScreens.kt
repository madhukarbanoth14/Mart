package com.mart.distribution.demo.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.data.api.dto.UserRowDto
import com.mart.distribution.demo.data.session.SessionUser
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.home.MainUiState
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.flashmart.FmStatCard
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleInkSurface
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleOrange
import com.mart.distribution.demo.ui.theme.WholesaleOrangeTint
import com.mart.distribution.demo.ui.theme.WholesaleRed
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

private enum class AdminUserFilter { ALL, PENDING, EMPLOYEES, DEALERS, SHOPKEEPERS }

@Composable
fun AdminFlashmartHome(
    ui: MainUiState,
    pendingCount: Int,
    onAddEmployee: () -> Unit,
    onAddShopkeeper: () -> Unit,
    onAddDealer: () -> Unit,
    onManageSkus: () -> Unit,
    onManageBrands: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenTeam: () -> Unit,
) {
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    val users = when (val u = ui.users) { is LoadState.Ok -> u.data; else -> emptyList() }
    val products = when (val p = ui.products) { is LoadState.Ok -> p.data; else -> emptyList() }
    val revenue =
        remember(orders) {
            orders.sumOf { o ->
                when (val v = o.finalAmount) {
                    is Number -> v.toDouble()
                    is String -> v.toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (pendingCount > 0) {
            item {
                FmCard(onClick = onOpenTeam, padding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(WholesaleOrangeTint)
                                        .padding(10.dp),
                            ) {
                                Icon(Icons.Outlined.Notifications, null, tint = WholesaleOrange, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    "$pendingCount pending approval${if (pendingCount == 1) "" else "s"}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WholesaleText,
                                )
                                Text(
                                    "Review dealer and shopkeeper onboarding",
                                    fontSize = 12.sp,
                                    color = WholesaleMuted,
                                )
                            }
                        }
                        FmBadge("PENDING_APPROVAL", label = "Review")
                    }
                }
            }
        }

        item {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(WholesaleInkSurface)
                        .padding(20.dp),
            ) {
                Column {
                    Text("Overview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.6f))
                    Text(formatDecimal(revenue), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "${orders.size} orders · ${users.size} network users",
                        fontSize = 12.sp,
                        color = Color.White.copy(0.65f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                FmStatCard("Orders", "${orders.size}", modifier = Modifier.weight(1f))
                FmStatCard("SKUs", "${products.size}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                FmStatCard(
                    "Dealers",
                    "${users.count { it.role.equals("DEALER", true) }}",
                    modifier = Modifier.weight(1f),
                )
                FmStatCard(
                    "Shopkeepers",
                    "${users.count { it.role.equals("SHOPKEEPER", true) }}",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminQuickTile("Orders", Icons.Outlined.ReceiptLong, onOpenOrders, Modifier.weight(1f))
                AdminQuickTile("SKUs", Icons.Outlined.Inventory2, onManageSkus, Modifier.weight(1f))
                AdminQuickTile("Team", Icons.Outlined.People, onOpenTeam, Modifier.weight(1f))
            }
        }

        item { FmButton("Add employee", onClick = onAddEmployee, variant = FmButtonVariant.Primary) }
        item { FmButton("Onboard shopkeeper", onClick = onAddShopkeeper, variant = FmButtonVariant.Soft) }
        item { FmButton("Onboard dealer", onClick = onAddDealer, variant = FmButtonVariant.Outline) }
        item { FmButton("Manage brands", onClick = onManageBrands, variant = FmButtonVariant.Soft) }
    }
}

@Composable
private fun AdminQuickTile(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FmCard(modifier = modifier, onClick = onClick, padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(WholesaleBlueTint).padding(10.dp),
            ) {
                Icon(icon, null, tint = WholesaleBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted)
        }
    }
}

@Composable
fun AdminOrdersTab(
    ui: MainUiState,
    onOpen: (String) -> Unit,
) {
    val orders = when (val o = ui.orders) { is LoadState.Ok -> o.data; else -> emptyList() }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Text("${orders.size} orders", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = WholesaleMuted) }
        items(orders, key = { it.id }) { ord ->
            AdminOrderCard(ord, onClick = { onOpen(ord.id) })
        }
    }
}

@Composable
private fun AdminOrderCard(order: OrderDto, onClick: () -> Unit) {
    FmCard(onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatDecimal(order.finalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                Text(
                    "${order.shopkeeper?.name ?: "—"} · ${order.dealer?.name ?: "—"}",
                    fontSize = 12.sp,
                    color = WholesaleMuted,
                )
                Text(order.createdAt?.take(10) ?: "—", fontSize = 11.sp, color = WholesaleMuted)
            }
            FmBadge(order.status)
        }
    }
}

@Composable
fun AdminUsersTab(
    ui: MainUiState,
    onAddEmployee: () -> Unit,
    onAddShopkeeper: () -> Unit,
    onAddDealer: () -> Unit,
    onReviewUser: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String, String?) -> Unit,
    onDeactivate: (String, String?) -> Unit,
    onReactivate: (String) -> Unit,
) {
    val users = when (val u = ui.users) { is LoadState.Ok -> u.data; else -> emptyList() }
    var filter by rememberSaveable { mutableStateOf(AdminUserFilter.ALL) }
    var confirmAction by remember { mutableStateOf<AdminUserAction?>(null) }

    val filtered =
        remember(users, filter) {
            when (filter) {
                AdminUserFilter.ALL ->
                    users.filter {
                        it.role.equals("EMPLOYEE", true) ||
                            it.role.equals("DEALER", true) ||
                            it.role.equals("SHOPKEEPER", true)
                    }
                AdminUserFilter.PENDING ->
                    users.filter {
                        it.status.equals("PENDING_APPROVAL", true) &&
                            (it.role.equals("DEALER", true) || it.role.equals("SHOPKEEPER", true))
                    }
                AdminUserFilter.EMPLOYEES -> users.filter { it.role.equals("EMPLOYEE", true) }
                AdminUserFilter.DEALERS -> users.filter { it.role.equals("DEALER", true) }
                AdminUserFilter.SHOPKEEPERS -> users.filter { it.role.equals("SHOPKEEPER", true) }
            }
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { FmButton("Add employee", onClick = onAddEmployee) }
        item { FmButton("Onboard shopkeeper", onClick = onAddShopkeeper, variant = FmButtonVariant.Outline) }
        item { FmButton("Onboard dealer", onClick = onAddDealer, variant = FmButtonVariant.Soft) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AdminFilterChip("Pending", filter == AdminUserFilter.PENDING) { filter = AdminUserFilter.PENDING }
                AdminFilterChip("Employees", filter == AdminUserFilter.EMPLOYEES) { filter = AdminUserFilter.EMPLOYEES }
                AdminFilterChip("All", filter == AdminUserFilter.ALL) { filter = AdminUserFilter.ALL }
                AdminFilterChip("Dealers", filter == AdminUserFilter.DEALERS) { filter = AdminUserFilter.DEALERS }
                AdminFilterChip("Shopkeepers", filter == AdminUserFilter.SHOPKEEPERS) { filter = AdminUserFilter.SHOPKEEPERS }
            }
        }
        item { FmSectionLabel(title = "Network · ${filtered.size}") }
        if (filtered.isEmpty()) {
            item {
                Text("No users in this view", color = WholesaleMuted, modifier = Modifier.padding(vertical = 24.dp))
            }
        }
        items(filtered, key = { it.id }) { row ->
            AdminUserRow(
                row = row,
                onReview = { onReviewUser(row.id) },
                onApprove = { confirmAction = AdminUserAction.Approve(row) },
                onReject = { confirmAction = AdminUserAction.Reject(row) },
                onDeactivate = { confirmAction = AdminUserAction.Deactivate(row) },
                onReactivate = { confirmAction = AdminUserAction.Reactivate(row) },
            )
        }
    }

    confirmAction?.let { action ->
        AdminUserActionDialog(
            action = action,
            onDismiss = { confirmAction = null },
            onConfirm = { reason ->
                when (action) {
                    is AdminUserAction.Approve -> onApprove(action.row.id)
                    is AdminUserAction.Reject -> onReject(action.row.id, reason)
                    is AdminUserAction.Deactivate -> onDeactivate(action.row.id, reason)
                    is AdminUserAction.Reactivate -> onReactivate(action.row.id)
                }
                confirmAction = null
            },
        )
    }
}

@Composable
private fun AdminFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

private sealed class AdminUserAction {
    data class Approve(val row: UserRowDto) : AdminUserAction()
    data class Reject(val row: UserRowDto) : AdminUserAction()
    data class Deactivate(val row: UserRowDto) : AdminUserAction()
    data class Reactivate(val row: UserRowDto) : AdminUserAction()
}

@Composable
private fun AdminUserActionDialog(
    action: AdminUserAction,
    onDismiss: () -> Unit,
    onConfirm: (reason: String?) -> Unit,
) {
    val (title, message) =
        when (action) {
            is AdminUserAction.Approve ->
                "Approve ${action.row.name}?" to
                    "Verify uploaded documents first. They will receive a confirmation email and can sign in after approval."
            is AdminUserAction.Reject ->
                "Reject ${action.row.name}?" to "They will not be able to sign in."
            is AdminUserAction.Deactivate ->
                "Deactivate ${action.row.name}?" to "Their login will be cancelled until reactivated."
            is AdminUserAction.Reactivate ->
                "Reactivate ${action.row.name}?" to "They will be able to sign in again."
        }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onConfirm(null) }) {
                Text(
                    when (action) {
                        is AdminUserAction.Approve -> "Approve"
                        is AdminUserAction.Reject -> "Reject"
                        is AdminUserAction.Deactivate -> "Deactivate"
                        is AdminUserAction.Reactivate -> "Reactivate"
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun AdminUserRow(
    row: UserRowDto,
    onReview: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit,
) {
    FmCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WholesaleBlueTint)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Text(row.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = WholesaleBlue)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(row.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesaleText)
                    Text(row.email, fontSize = 12.sp, color = WholesaleMuted)
                    row.phone?.let { Text("Phone · $it", fontSize = 11.sp, color = WholesaleMuted) }
                    row.area?.name?.let { Text("Area · $it", fontSize = 11.sp, color = WholesaleMuted) }
                    row.onboardedBy?.name?.let { Text("Onboarded by · $it", fontSize = 11.sp, color = WholesaleMuted) }
                    row.onboardingNotes?.let {
                        Text("Notes · $it", fontSize = 11.sp, color = WholesaleMuted, maxLines = 2)
                    }
                    if (row.onboardingDocuments.isNotEmpty()) {
                        Text(
                            "${row.onboardingDocuments.size} document(s) uploaded",
                            fontSize = 11.sp,
                            color = WholesaleBlue,
                        )
                    }
                    row.statusReason?.let {
                        Text("Reason · $it", fontSize = 11.sp, color = WholesaleRed)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FmBadge(status = row.status)
                    FmBadge(status = row.role, label = row.role.replace('_', ' '))
                }
            }
            when (row.status.uppercase()) {
                "PENDING_APPROVAL" ->
                    if (!row.role.equals("EMPLOYEE", true)) {
                        FmButton("Review details", onClick = onReview)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FmButton("Approve", onClick = onApprove, modifier = Modifier.weight(1f))
                            FmButton("Reject", onClick = onReject, modifier = Modifier.weight(1f), variant = FmButtonVariant.Outline)
                        }
                    }
                "ACTIVE" ->
                    if (!row.role.equals("EMPLOYEE", true)) {
                        FmButton("Deactivate login", onClick = onDeactivate, variant = FmButtonVariant.Outline)
                    }
                "DEACTIVATED" ->
                    if (!row.role.equals("EMPLOYEE", true)) {
                        FmButton("Reactivate", onClick = onReactivate)
                    }
            }
        }
    }
}

@Composable
fun AdminProfileTab(
    user: SessionUser,
    ui: MainUiState,
    onLogout: () -> Unit,
) {
    val users = when (val u = ui.users) { is LoadState.Ok -> u.data; else -> emptyList() }
    val pending =
        users.count {
            it.status.equals("PENDING_APPROVAL", true) &&
                (it.role.equals("DEALER", true) || it.role.equals("SHOPKEEPER", true))
        }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(WholesaleBg).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            FmCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(WholesaleBlueTint)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                    ) {
                        Text(user.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = WholesaleBlue, fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        Text(user.email, fontSize = 13.sp, color = WholesaleMuted)
                        Text("Administrator", fontSize = 12.sp, color = WholesaleMuted)
                    }
                    FmBadge("ACTIVE")
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FmStatCard("Pending", "$pending", modifier = Modifier.weight(1f))
                FmStatCard(
                    "Dealers",
                    "${users.count { it.role.equals("DEALER", true) }}",
                    modifier = Modifier.weight(1f),
                )
                FmStatCard(
                    "Shopkeepers",
                    "${users.count { it.role.equals("SHOPKEEPER", true) }}",
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
