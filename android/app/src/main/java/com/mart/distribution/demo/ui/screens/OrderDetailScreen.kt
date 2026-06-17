package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.orders.OrderDetailViewModel
import com.mart.distribution.demo.feature.orders.OrderDetailViewModelFactory
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.OrderStatusChip
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesableBlue
import com.mart.distribution.demo.ui.theme.WholesableBorder
import com.mart.distribution.demo.ui.theme.WholesableGreen
import com.mart.distribution.demo.ui.theme.WholesableMuted
import com.mart.distribution.demo.ui.theme.WholesableRed
import com.mart.distribution.demo.ui.theme.WholesableText
import com.mart.distribution.demo.ui.util.formatDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
) {
    val sessionUser by container.sessionRepository.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
    val roleUpper = sessionUser?.role?.uppercase() ?: ""

    if (sessionUser == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (roleUpper == "EMPLOYEE") {
        Scaffold(
            containerColor = WholesaleBg,
            topBar = {
                TopAppBar(
                    title = { Text("Order detail", fontWeight = FontWeight.Bold, color = WholesableText) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = WholesableMuted)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FmCard {
                    Text("Order details are not available in the employee app.",
                        fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WholesableText)
                    Spacer(Modifier.height(6.dp))
                    Text("Use the Onboarding tab to register dealers and shopkeepers.",
                        fontSize = 13.sp, color = WholesableMuted, lineHeight = 18.sp)
                }
                OutlinedButton(onClick = { navController.popBackStack() },
                ) {
                    Text("Back")
                }
            }
        }
        return
    }

    val vm: OrderDetailViewModel = viewModel(
        key = orderId, factory = OrderDetailViewModelFactory(container, orderId))
    val orderState by vm.orderState.collectAsStateWithLifecycle()
    val actionMsg by vm.actionMessage.collectAsStateWithLifecycle()
    val actionErr by vm.actionError.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(actionMsg) { actionMsg?.let { snackbar.showSnackbar(it); vm.clearMessages() } }
    LaunchedEffect(actionErr) { actionErr?.let { snackbar.showSnackbar(it); vm.clearMessages() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = WholesaleBg,
        topBar = {
            TopAppBar(
                title = { Text("Order detail", fontWeight = FontWeight.Bold, color = WholesableText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = WholesableMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        when (val st = orderState) {
            is LoadState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WholesableBlue) }
            is LoadState.Err -> Text(st.message, modifier = Modifier.padding(padding).padding(24.dp), color = WholesableRed)
            is LoadState.Ok -> {
                val ord = st.data
                val role = roleUpper
                val isPaid = ord.paymentStatus.equals("PAID", true)
                val canMockPay =
                    !isPaid &&
                        role in setOf("SHOPKEEPER", "ADMIN") &&
                        ord.status.equals("PENDING", true)
                val isRestock = ord.kind.equals("DEALER_RESTOCK", true)
                val canConfirm =
                    if (isRestock) {
                        role in setOf("ADMIN", "EMPLOYEE") && ord.status.equals("PENDING", true)
                    } else {
                        role in setOf("DEALER", "ADMIN") && ord.status.equals("PENDING", true)
                    }
                val canDispatch =
                    if (isRestock) {
                        role in setOf("ADMIN", "EMPLOYEE") &&
                            (ord.status.equals("DEALER_CONFIRMED", true) || ord.status.equals("ACCEPTED", true))
                    } else {
                        role in setOf("DEALER", "ADMIN") &&
                            (ord.status.equals("DEALER_CONFIRMED", true) || ord.status.equals("ACCEPTED", true))
                    }
                val canDeliver =
                    if (isRestock) {
                        role in setOf("ADMIN", "EMPLOYEE") && ord.status.equals("OUT_FOR_DELIVERY", true)
                    } else {
                        role in setOf("DEALER", "ADMIN") && ord.status.equals("OUT_FOR_DELIVERY", true)
                    }
                val canCancel = role in setOf("SHOPKEEPER","DEALER","ADMIN") && ord.status.equals("PENDING", true)
                val canDownloadInvoice =
                    isPaid ||
                        ord.status.equals("DEALER_CONFIRMED", true) ||
                        ord.status.equals("DELIVERED", true)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }

                    // Status + amount hero
                    item {
                        FmCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OrderStatusChip(ord.status)
                                    Text(
                                        if (isPaid) "Payment: Paid ✓" else "Payment: Pending",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isPaid) WholesableGreen else WholesableMuted,
                                    )
                                }
                                Text(formatDecimal(ord.finalAmount), fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold, color = WholesableText)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Subtotal", fontSize = 12.sp, color = WholesableMuted)
                                    Text(formatDecimal(ord.totalAmount), fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold, color = WholesableText)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Discount", fontSize = 12.sp, color = WholesableMuted)
                                    Text("− " + formatDecimal(ord.discountAmount), fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold, color = WholesableGreen)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("GST", fontSize = 12.sp, color = WholesableMuted)
                                    Text("+ " + formatDecimal(ord.gstAmount), fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold, color = WholesableText)
                                }
                            }
                            ord.dealer?.name?.let { dn ->
                                Spacer(Modifier.height(8.dp))
                                Text("Dealer: $dn", fontSize = 12.sp, color = WholesableMuted)
                            }
                        }
                    }

                    // Items
                    items(ord.items.orEmpty(), key = { it.id ?: it.productId }) { line ->
                        FmCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(line.product?.name ?: line.productId, fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold, color = WholesableText)
                                    Text("Qty ${line.quantity}", fontSize = 12.sp, color = WholesableMuted)
                                }
                                Text(formatDecimal(line.finalAmount), fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold, color = WholesableText)
                            }
                        }
                    }

                    // Actions
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (canMockPay) GradientGoldButton("Pay online (demo)", onClick = { vm.mockPay() })
                            if (canConfirm) FmOutlinedBtn({ Icon(Icons.Outlined.TaskAlt, null); Spacer(Modifier.width(8.dp)); Text("Confirm order") }) { vm.confirm() }
                            if (canDispatch) FmOutlinedBtn({ Icon(Icons.Outlined.LocalShipping, null); Spacer(Modifier.width(8.dp)); Text("Mark out for delivery") }) { vm.markOutForDelivery() }
                            if (canDeliver) FmOutlinedBtn({ Icon(Icons.Outlined.LocalShipping, null); Spacer(Modifier.width(8.dp)); Text("Mark as delivered") }) { vm.markDelivered() }
                            if (canCancel) FmOutlinedBtn({ Icon(Icons.Outlined.TaskAlt, null); Spacer(Modifier.width(8.dp)); Text("Cancel order") }) { vm.cancelOrder() }
                            if (canDownloadInvoice) {
                                FmOutlinedBtn({
                                    Icon(Icons.Outlined.Description, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (isPaid) "Download invoice" else "Invoice & PDF")
                                }) {
                                    navController.navigate("invoice/$orderId")
                                }
                            }
                            FmOutlinedBtn({ Icon(Icons.Outlined.LocalShipping, null); Spacer(Modifier.width(8.dp)); Text("Delivery tracking") }) { navController.navigate("tracking/$orderId") }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun FmCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, shape).clip(shape)
            .background(Color.White).border(1.dp, WholesableBorder, shape).padding(16.dp),
    ) { content() }
}

@Composable
private fun FmOutlinedBtn(label: @Composable RowScope.() -> Unit, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, WholesableBorder),
    ) { Row(verticalAlignment = Alignment.CenterVertically, content = label) }
}

@Composable
private fun RowSpace(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically, content = content)
}
