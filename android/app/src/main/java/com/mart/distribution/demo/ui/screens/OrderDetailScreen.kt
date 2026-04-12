package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.orders.OrderDetailViewModel
import com.mart.distribution.demo.feature.orders.OrderDetailViewModelFactory
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard
import com.mart.distribution.demo.ui.components.OrderStatusChip
import com.mart.distribution.demo.ui.util.formatDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
) {
    val vm: OrderDetailViewModel =
        viewModel(
            key = orderId,
            factory = OrderDetailViewModelFactory(container, orderId),
        )
    val orderState by vm.orderState.collectAsStateWithLifecycle()
    val actionMsg by vm.actionMessage.collectAsStateWithLifecycle()
    val actionErr by vm.actionError.collectAsStateWithLifecycle()
    val sessionUser by container.sessionRepository.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
    val role = sessionUser?.role?.uppercase() ?: ""

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(actionMsg) {
        actionMsg?.let {
            snackbar.showSnackbar(it)
            vm.clearMessages()
        }
    }
    LaunchedEffect(actionErr) {
        actionErr?.let {
            snackbar.showSnackbar(it)
            vm.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { padding ->
        when (val st = orderState) {
            is LoadState.Loading ->
                Text("Loading…", modifier = Modifier.padding(padding).padding(24.dp))
            is LoadState.Err ->
                Text(st.message, modifier = Modifier.padding(padding).padding(24.dp), color = MaterialTheme.colorScheme.error)
            is LoadState.Ok -> {
                val ord = st.data
                val canMockPay =
                    role in setOf("SHOPKEEPER", "ADMIN", "EMPLOYEE") &&
                        ord.status.equals("PENDING", true)
                val canConfirm =
                    role in setOf("DEALER", "ADMIN", "EMPLOYEE") &&
                        ord.status.equals("PENDING", true)
                val hasInvoice = ord.status.equals("ACCEPTED", true) || ord.status.equals("DELIVERED", true)

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        RowSpace {
                            OrderStatusChip(ord.status)
                            Text(
                                formatDecimal(ord.finalAmount),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    }
                    item {
                        MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Server totals", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("Subtotal: ${formatDecimal(ord.totalAmount)}")
                            Text("Discount: ${formatDecimal(ord.discountAmount)}")
                            Text("GST: ${formatDecimal(ord.gstAmount)}")
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Dealer: ${ord.dealer?.name ?: ord.dealerId?.take(8) ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    items(ord.items.orEmpty(), key = { it.id ?: it.productId }) { line ->
                        MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            Text(line.product?.name ?: line.productId, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Qty ${line.quantity} · Line ${formatDecimal(line.finalAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (canMockPay) {
                                GradientGoldButton(
                                    text = "Pay (demo)",
                                    onClick = { vm.mockPay() },
                                )
                            }
                            if (canConfirm) {
                                OutlinedButton(
                                    onClick = { vm.confirm() },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.TaskAlt, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Confirm order")
                                    }
                                }
                            }
                            if (hasInvoice) {
                                OutlinedButton(
                                    onClick = { navController.navigate("invoice/$orderId") },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Description, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Invoice & PDF")
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { navController.navigate("tracking/$orderId") },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.LocalShipping, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delivery tracking (demo)")
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun RowSpace(content: @Composable RowScope.() -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
