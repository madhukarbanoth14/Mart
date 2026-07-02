package com.mart.distribution.demo.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import com.mart.distribution.demo.data.api.dto.CreateReturnItemRequest
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.feature.home.LoadState
import com.mart.distribution.demo.feature.orders.OrderDetailViewModel
import com.mart.distribution.demo.feature.orders.OrderDetailViewModelFactory
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmBadge
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmDataRow
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.navigation.safePopBack
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal
import kotlinx.coroutines.launch

@Composable
fun OrderDetailScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
    mainViewModel: com.mart.distribution.demo.feature.home.MainViewModel,
) {
    val goBack = { navController.safePopBack() }
    NavBackHandler(goBack)
    val sessionUser by container.sessionRepository.sessionUserFlow.collectAsStateWithLifecycle(initialValue = null)
    val roleUpper = sessionUser?.role?.uppercase() ?: ""

    if (sessionUser == null) {
        Box(Modifier.fillMaxSize().background(WholesaleBg), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = WholesaleBlue)
        }
        return
    }

    if (roleUpper == "EMPLOYEE") {
        Scaffold(containerColor = WholesaleBg, topBar = {}) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                FmAppHeader(title = "Order detail", subtitle = "Employee access", onBack = goBack)
                Column(
                    modifier = Modifier.padding(horizontal = FmSpacing.screenH),
                    verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
                ) {
                    FmCard {
                        Text(
                            "Order details are not available in the employee app.",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WholesaleText,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Use the Network tab to register dealers and shopkeepers.",
                            fontSize = 13.sp,
                            color = WholesaleMuted,
                            lineHeight = 18.sp,
                        )
                    }
                    FmButton(text = "Go back", onClick = goBack, variant = FmButtonVariant.Outline)
                }
            }
        }
        return
    }

    val vm: OrderDetailViewModel =
        viewModel(key = orderId, factory = OrderDetailViewModelFactory(container, orderId))
    val orderState by vm.orderState.collectAsStateWithLifecycle()
    val actionMsg by vm.actionMessage.collectAsStateWithLifecycle()
    val actionErr by vm.actionError.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mapsContext = androidx.compose.ui.platform.LocalContext.current
    var showReturnDialog by remember { mutableStateOf(false) }
    var returnReasonEnum by remember { mutableStateOf("DAMAGED_PRODUCT") }
    var returnComments by remember { mutableStateOf("") }
    var returnQtyByProduct by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(actionMsg) { actionMsg?.let { snackbar.showSnackbar(it); vm.clearMessages() } }
    LaunchedEffect(actionErr) { actionErr?.let { snackbar.showSnackbar(it); vm.clearMessages() } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = WholesaleBg,
        topBar = {},
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            val orderSubtitle =
                when (val st = orderState) {
                    is LoadState.Ok -> "Order #${st.data.id.takeLast(8).uppercase()}"
                    else -> "Order details"
                }
            FmAppHeader(title = "Order detail", subtitle = orderSubtitle, onBack = goBack)

            when (val st = orderState) {
                is LoadState.Loading ->
                    FmLoadingState(message = "Loading order…", modifier = Modifier.fillMaxSize())
                is LoadState.Err ->
                    FmErrorBanner(
                        message = st.message,
                        modifier = Modifier.padding(horizontal = FmSpacing.screenH),
                    )
                is LoadState.Ok -> {
                    val ord = st.data
                    val role = roleUpper
                    val isPaid = ord.paymentStatus.equals("PAID", true)
                    val canMockPay =
                        BuildConfig.DEMO_MODE &&
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
                    val canCancel = role in setOf("SHOPKEEPER", "DEALER", "ADMIN") && ord.status.equals("PENDING", true)
                    val canRequestReturn =
                        role == "SHOPKEEPER" && ord.status.equals("DELIVERED", true)
                    val canApproveReturn =
                        role in setOf("DEALER", "ADMIN", "EMPLOYEE") &&
                            ord.status.equals("RETURN_REQUESTED", true)
                    val canRejectReturn = canApproveReturn
                    val canDownloadInvoice =
                        isPaid ||
                            ord.status.equals("DEALER_CONFIRMED", true) ||
                            ord.status.equals("DELIVERED", true)

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = FmSpacing.listH),
                        verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
                    ) {
                        item {
                            FmCard {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FmBadge(ord.status)
                                        FmBadge(
                                            if (isPaid) "PAID" else "UNPAID",
                                            label = if (isPaid) "Payment received" else "Payment pending",
                                        )
                                    }
                                    Text(
                                        formatDecimal(ord.finalAmount),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WholesaleText,
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(com.mart.distribution.demo.ui.theme.WholesaleBorder.copy(alpha = 0.6f)),
                                )
                                Spacer(Modifier.height(8.dp))
                                FmMoneyRow("Subtotal", formatDecimal(ord.totalAmount))
                                FmMoneyRow("Discount", "− ${formatDecimal(ord.discountAmount)}", accent = WholesaleGreen)
                                FmMoneyRow("GST", "+ ${formatDecimal(ord.gstAmount)}")
                                FmMoneyRow("Total payable", formatDecimal(ord.finalAmount), strong = true)
                                ord.dealer?.name?.let { dn ->
                                    Spacer(Modifier.height(6.dp))
                                    Text("Dealer · $dn", fontSize = 12.sp, color = WholesaleMuted)
                                }
                            }
                        }

                        if (role == "DEALER") {
                            val sk = ord.shopkeeper
                            val addr = sk?.address?.takeIf { it.isNotBlank() }
                            val lat = sk?.latitude
                            val lng = sk?.longitude
                            item {
                                FmCard {
                                    FmSectionLabel(title = "Delivery location")
                                    Text(
                                        sk?.shopName?.takeIf { it.isNotBlank() }
                                            ?: "Order ${ord.id.takeLast(6).uppercase()}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WholesaleText,
                                    )
                                    if (addr != null) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(addr, fontSize = 13.sp, color = WholesaleText, lineHeight = 18.sp)
                                    }
                                    if (lat != null && lng != null) {
                                        Spacer(Modifier.height(12.dp))
                                        FmButton(
                                            text = "Open location in Maps",
                                            onClick = {
                                                val label = sk?.shopName?.takeIf { it.isNotBlank() } ?: "Delivery"
                                                val uri =
                                                    android.net.Uri.parse(
                                                        "geo:$lat,$lng?q=$lat,$lng(" +
                                                            android.net.Uri.encode(label) + ")",
                                                    )
                                                runCatching {
                                                    mapsContext.startActivity(
                                                        android.content.Intent(android.content.Intent.ACTION_VIEW, uri),
                                                    )
                                                }
                                            },
                                            variant = FmButtonVariant.Outline,
                                        )
                                    } else if (addr == null) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "No location shared during onboarding.",
                                            fontSize = 12.sp,
                                            color = WholesaleMuted,
                                        )
                                    }
                                }
                            }
                        }

                        ord.returnReason?.takeIf { it.isNotBlank() }?.let { reason ->
                            item {
                                FmCard {
                                    FmSectionLabel(title = "Return notes")
                                    Text(reason, fontSize = 13.sp, color = WholesaleMuted, lineHeight = 18.sp)
                                    ord.returnRequestedAt?.takeIf { it.isNotBlank() }?.let { at ->
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            "Requested: ${at.take(10)}",
                                            fontSize = 11.sp,
                                            color = WholesaleMuted,
                                        )
                                    }
                                }
                            }
                        }

                        item { FmSectionLabel(title = "Line items") }
                        itemsIndexed(
                            ord.items.orEmpty(),
                            key = { index, line -> line.id ?: "${line.productId}-$index" },
                        ) { _, line ->
                            FmCard {
                                FmDataRow(
                                    title = line.product?.name ?: line.productId,
                                    subtitle = "Qty ${line.quantity}",
                                    trailing = {
                                        Text(
                                            formatDecimal(line.finalAmount),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WholesaleText,
                                        )
                                    },
                                )
                            }
                        }

                        item {
                            FmSectionLabel(title = "Actions")
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (canMockPay) {
                                    FmButton(text = "Pay online (demo)", onClick = { vm.mockPay() })
                                }
                                if (canConfirm) {
                                    OrderActionButton("Confirm order") { vm.confirm() }
                                }
                                if (canDispatch) {
                                    OrderActionButton("Mark out for delivery") { vm.markOutForDelivery() }
                                }
                                if (canDeliver) {
                                    OrderActionButton("Mark as delivered") { vm.markDelivered() }
                                }
                                if (canCancel) {
                                    OrderActionButton("Cancel order") { vm.cancelOrder() }
                                }
                                if (canRequestReturn) {
                                    OrderActionButton("Request return") {
                                        val ord = (orderState as? LoadState.Ok)?.data
                                        returnQtyByProduct =
                                            ord?.items?.associate { (it.productId ?: it.product?.id ?: "") to it.quantity }
                                                ?.filterKeys { it.isNotBlank() }
                                                ?: emptyMap()
                                        showReturnDialog = true
                                    }
                                }
                                if (canApproveReturn) {
                                    FmButton(
                                        text = "Approve return",
                                        onClick = { vm.approveReturn() },
                                        variant = FmButtonVariant.Dark,
                                    )
                                }
                                if (canRejectReturn) {
                                    OrderActionButton("Reject return") { vm.rejectReturn() }
                                }
                                if (canDownloadInvoice) {
                                    OrderActionButton(if (isPaid) "Download invoice" else "Invoice & PDF") {
                                        navController.navigate("invoice/$orderId")
                                    }
                                }
                                OrderActionButton("Track order") {
                                    navController.navigate("tracking/$orderId")
                                }
                                if (role == "SHOPKEEPER") {
                                    OrderActionButton("Reorder") {
                                        mainViewModel.reorderFromOrder(orderId) { success, message ->
                                            scope.launch {
                                                if (success) {
                                                    navController.navigate("cart")
                                                    message?.let { snackbar.showSnackbar(it) }
                                                } else {
                                                    snackbar.showSnackbar(message ?: "Could not reorder")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
                else -> {}
            }
        }
    }

    if (showReturnDialog) {
        val reasonOptions =
            listOf(
                "DAMAGED_PRODUCT" to "Damaged product",
                "EXPIRED_PRODUCT" to "Expired product",
                "WRONG_PRODUCT" to "Wrong product",
                "QUALITY_ISSUE" to "Quality issue",
                "EXCESS_QUANTITY" to "Excess quantity",
                "OTHER" to "Other",
            )
        AlertDialog(
            onDismissRequest = { showReturnDialog = false },
            title = { Text("Request return") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select reason and products to return.", fontSize = 13.sp, color = WholesaleMuted)
                    reasonOptions.forEach { (value, label) ->
                        FilterChip(
                            selected = returnReasonEnum == value,
                            onClick = { returnReasonEnum = value },
                            label = { Text(label) },
                        )
                    }
                    OutlinedTextField(
                        value = returnComments,
                        onValueChange = { returnComments = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Comments (optional)") },
                        minLines = 2,
                    )
                    val ord = (orderState as? LoadState.Ok)?.data
                    ord?.items?.forEach { item ->
                        val pid = item.productId ?: item.product?.id ?: return@forEach
                        val name = item.product?.name ?: "Product"
                        val maxQty = item.quantity
                        val qty = returnQtyByProduct[pid] ?: maxQty
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(name, modifier = Modifier.weight(1f), fontSize = 13.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FmButton(
                                    text = "−",
                                    onClick = {
                                        if (qty > 0) {
                                            returnQtyByProduct = returnQtyByProduct + (pid to (qty - 1))
                                        }
                                    },
                                    modifier = Modifier.width(40.dp),
                                )
                                Text("$qty", modifier = Modifier.padding(horizontal = 8.dp))
                                FmButton(
                                    text = "+",
                                    onClick = {
                                        if (qty < maxQty) {
                                            returnQtyByProduct = returnQtyByProduct + (pid to (qty + 1))
                                        }
                                    },
                                    modifier = Modifier.width(40.dp),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FmButton(
                    text = "Submit",
                    onClick = {
                        val items =
                            returnQtyByProduct
                                .filter { it.value > 0 }
                                .map { CreateReturnItemRequest(it.key, it.value) }
                        if (items.isNotEmpty()) {
                            vm.requestReturnDetailed(
                                reason = returnReasonEnum,
                                reasonText = reasonOptions.firstOrNull { it.first == returnReasonEnum }?.second,
                                comments = returnComments.trim().ifBlank { null },
                                items = items,
                            )
                            returnComments = ""
                            showReturnDialog = false
                        }
                    },
                )
            },
            dismissButton = {
                FmButton(
                    text = "Cancel",
                    onClick = { showReturnDialog = false },
                    variant = FmButtonVariant.Outline,
                )
            },
        )
    }
}

@Composable
private fun OrderActionButton(
    label: String,
    onClick: () -> Unit,
) {
    FmButton(
        text = label,
        onClick = onClick,
        variant = FmButtonVariant.Outline,
    )
}
