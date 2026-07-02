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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.ui.flashmart.FmAppHeader
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmSectionLabel
import com.mart.distribution.demo.ui.navigation.NavBackHandler
import com.mart.distribution.demo.ui.navigation.safePopBack
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.flashmart.FmAvatar
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueDeep
import com.mart.distribution.demo.ui.theme.WholesaleBrandNavy
import com.mart.distribution.demo.ui.theme.WholesaleDealerBlue
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.util.ApiErrorMessages

private data class TrackStep(
    val title: String,
    val subtitle: String,
    val done: Boolean,
    val active: Boolean = false,
    val simulated: Boolean = false,
)

@Composable
fun TrackingScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
) {
    val goBack = { navController.safePopBack() }
    NavBackHandler(goBack)
    var order by remember { mutableStateOf<OrderDto?>(null) }
    var loadErr by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val mockPaid by container.demoFlowRepository.mockPaidOrderIds.collectAsStateWithLifecycle()
    val demoDelivered by container.demoFlowRepository.demoDeliveredOrderIds.collectAsStateWithLifecycle()

    LaunchedEffect(orderId, mockPaid) {
        loadErr = null
        loading = true
        try {
            order =
                if (container.sessionRepository.isLocalDemoMode()) {
                    container.localDemoMartStore.orderById(orderId)
                } else {
                    container.martApi.orderById(orderId)
                }
            if (order == null) loadErr = "Order not found"
        } catch (e: Exception) {
            loadErr =
                ApiErrorMessages.fromThrowable(
                    e,
                    fallback = "Could not load tracking",
                    notFoundFallback = "Order not found.",
                )
        } finally {
            loading = false
        }
    }

    val steps =
        remember(order, mockPaid, demoDelivered, orderId) {
            val o = order ?: return@remember emptyList()
            val status = o.status.orEmpty()
            val paymentStatus = o.paymentStatus.orEmpty()
            val paid = mockPaid.contains(orderId) || paymentStatus.equals("PAID", true)
            val accepted =
                status.equals("ACCEPTED", true) ||
                    status.equals("DELIVERED", true) ||
                    status.equals("DEALER_CONFIRMED", true)
            val deliveredApi = status.equals("DELIVERED", true)
            val deliveredDemo = demoDelivered.contains(orderId)
            val outForDelivery = status.equals("OUT_FOR_DELIVERY", true)
            listOf(
                TrackStep("Order placed", "Created on the server", done = true),
                TrackStep(
                    "Payment",
                    if (paid) "Payment successful" else "Pending payment",
                    done = paid,
                    active = !paid,
                ),
                TrackStep(
                    "Dealer confirmed",
                    "Stock reserved · invoice generated",
                    done = accepted,
                    active = !accepted && paid,
                ),
                TrackStep(
                    "Out for delivery",
                    "On the way to your store",
                    done = outForDelivery || deliveredApi || deliveredDemo,
                    active = accepted && !outForDelivery && !deliveredApi,
                ),
                TrackStep(
                    "Delivered",
                    when {
                        deliveredApi -> "Received at store"
                        BuildConfig.DEMO_MODE -> "Tap below to simulate"
                        else -> "Pending"
                    },
                    done = deliveredApi || deliveredDemo,
                    active = outForDelivery && !deliveredApi && !deliveredDemo,
                    simulated = BuildConfig.DEMO_MODE && !deliveredApi && deliveredDemo,
                ),
            )
        }

    Scaffold(containerColor = WholesaleBg, topBar = {}) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            FmAppHeader(
                title = "Track order",
                subtitle = "Order #${orderId.takeLast(8).uppercase()}",
                onBack = goBack,
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = FmSpacing.listH)
                        .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(FmSpacing.itemGap),
            ) {
                when {
                    loading -> FmLoadingState(message = "Loading tracking…")
                    loadErr != null -> FmErrorBanner(message = loadErr!!)
                    else -> {
                        order?.let { o ->
                            val status = o.status.orEmpty()
                            val delivered = status.equals("DELIVERED", true) || demoDelivered.contains(orderId)
                            val outForDelivery = status.equals("OUT_FOR_DELIVERY", true)
                            val etaTitle = when {
                                delivered -> "Delivered"
                                outForDelivery -> "Today, by 6:00 PM"
                                else -> "Tomorrow, by 6:00 PM"
                            }
                            val etaSub = when {
                                delivered -> "Received at your store"
                                outForDelivery -> "Out for delivery"
                                else -> "Estimated delivery"
                            }
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(WholesaleGreen, WholesaleBlueDeep, WholesaleBrandNavy),
                                            ),
                                        )
                                        .padding(20.dp),
                            ) {
                                Column {
                                    Text(etaSub, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(0.85f))
                                    Text(etaTitle, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                                    Text(
                                        "Order ${o.id.takeLast(8).uppercase()}",
                                        fontSize = 13.sp,
                                        color = Color.White.copy(0.9f),
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                            }

                            o.dealer?.name?.let { dealerName ->
                                FmCard(padding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(13.dp),
                                    ) {
                                        FmAvatar(dealerName, size = 42.dp, tint = WholesaleDealerBlue)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(dealerName, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                                            Text("Your dealer", fontSize = 12.5.sp, color = WholesaleMuted)
                                        }
                                        Box(
                                            modifier =
                                                Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(WholesaleGreen.copy(0.12f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            androidx.compose.material3.Icon(
                                                Icons.Outlined.Phone,
                                                contentDescription = "Call dealer",
                                                tint = WholesaleGreen,
                                                modifier = Modifier.size(20.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        FmCard(modifier = Modifier.fillMaxWidth()) {
                            FmSectionLabel(title = "Order journey")
                            steps.forEachIndexed { i, step ->
                                TrackTimelineRow(step = step, isLast = i == steps.lastIndex)
                            }
                        }

                        val ord = order
                        val ordStatus = ord?.status.orEmpty()
                        val canSimulate =
                            ord != null &&
                                (
                                    ordStatus.equals("DEALER_CONFIRMED", true) ||
                                        ordStatus.equals("OUT_FOR_DELIVERY", true) ||
                                        ordStatus.equals("ACCEPTED", true)
                                    ) &&
                                !ordStatus.equals("DELIVERED", true) &&
                                !demoDelivered.contains(orderId)
                        if (canSimulate && BuildConfig.DEMO_MODE) {
                            FmButton(
                                text = "Simulate delivery complete",
                                onClick = { container.demoFlowRepository.markDemoDelivered(orderId) },
                                variant = FmButtonVariant.Outline,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TrackTimelineRow(
    step: TrackStep,
    isLast: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                step.done -> WholesaleGreen
                                step.active -> WholesaleBlue
                                else -> Color(0xFFE4E7EC)
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    step.done ->
                        Text("✓", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    step.active ->
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    else ->
                        Box(Modifier.size(8.dp).clip(CircleShape).background(WholesaleMuted.copy(0.4f)))
                }
            }
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (step.done) WholesaleGreen.copy(0.35f) else Color(0xFFE4E7EC)),
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 8.dp, top = 3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    step.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (step.done || step.active) WholesaleText else WholesaleMuted,
                )
                if (step.simulated) {
                    Text("Simulated", fontSize = 10.sp, color = WholesaleBlue, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                step.subtitle,
                fontSize = 12.sp,
                color = if (step.active) WholesaleBlue else WholesaleMuted,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
