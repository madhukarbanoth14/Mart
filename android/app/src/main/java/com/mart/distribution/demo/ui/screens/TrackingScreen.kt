package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.BuildConfig
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBlueTint
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText

private data class TrackStep(
    val title: String,
    val subtitle: String,
    val done: Boolean,
    val active: Boolean = false,
    val simulated: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    orderId: String,
    navController: NavHostController,
    container: AppContainer,
) {
    var order by remember { mutableStateOf<OrderDto?>(null) }
    var loadErr by remember { mutableStateOf<String?>(null) }
    val mockPaid by container.demoFlowRepository.mockPaidOrderIds.collectAsStateWithLifecycle()
    val demoDelivered by container.demoFlowRepository.demoDeliveredOrderIds.collectAsStateWithLifecycle()

    LaunchedEffect(orderId, mockPaid) {
        loadErr = null
        try {
            order =
                if (container.sessionRepository.isLocalDemoMode()) {
                    container.localDemoMartStore.orderById(orderId)
                } else {
                    container.martApi.orderById(orderId)
                }
            if (order == null) loadErr = "Order not found"
        } catch (e: Exception) {
            loadErr = e.message ?: "Failed to load"
        }
    }

    val steps = remember(order, mockPaid, demoDelivered) {
        val o = order ?: return@remember emptyList()
        val paid = mockPaid.contains(orderId) || o.paymentStatus.equals("PAID", true)
        val accepted = o.status.equals("ACCEPTED", true) || o.status.equals("DELIVERED", true) || o.status.equals("DEALER_CONFIRMED", true)
        val deliveredApi = o.status.equals("DELIVERED", true)
        val deliveredDemo = demoDelivered.contains(orderId)
        val outForDelivery = o.status.equals("OUT_FOR_DELIVERY", true)
        listOf(
            TrackStep("Order placed", "Created on the server", done = true),
            TrackStep("Payment", if (paid) "Card payment successful" else "Pending payment",
                done = paid, active = !paid),
            TrackStep("Dealer confirmed", "Stock reserved · invoice generated",
                done = accepted, active = !accepted && paid),
            TrackStep("Out for delivery", "On the way to your store",
                done = outForDelivery || deliveredApi || deliveredDemo,
                active = accepted && !outForDelivery && !deliveredApi),
            TrackStep("Delivered", when {
                deliveredApi -> "Received at store"
                BuildConfig.DEMO_MODE -> "Tap below to simulate"
                else -> "Pending"
            }, done = deliveredApi || deliveredDemo,
                active = outForDelivery && !deliveredApi && !deliveredDemo,
                simulated = BuildConfig.DEMO_MODE && !deliveredApi && deliveredDemo),
        )
    }

    Scaffold(
        containerColor = WholesaleBg,
        topBar = {
            TopAppBar(
                title = { Text("Track order", fontWeight = FontWeight.Bold, color = WholesaleText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = WholesaleMuted)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Status hero card
            order?.let { o ->
                val delivered = o.status.equals("DELIVERED", true) || demoDelivered.contains(orderId)
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (delivered) WholesaleGreenTint else WholesaleBlueTint)
                        .padding(18.dp),
                ) {
                    Column {
                        Text(if (delivered) "Completed" else "Estimated delivery",
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            color = if (delivered) WholesaleGreen else WholesaleBlue)
                        Text(if (delivered) "Order delivered ✓" else "Today, by 6:00 PM",
                            fontSize = 20.sp, fontWeight = FontWeight.Bold,
                            color = if (delivered) WholesaleGreen else WholesaleBlue,
                            modifier = Modifier.padding(top = 4.dp))
                        Text("Order ${o.id.takeLast(8)}", fontSize = 12.sp,
                            color = if (delivered) WholesaleGreen.copy(0.8f) else WholesaleBlue.copy(0.8f),
                            modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            loadErr?.let { Text(it, color = WholesaleText.copy(alpha = 0.7f)) }

            // Timeline card
            val shape = RoundedCornerShape(16.dp)
            Column(
                modifier = Modifier.fillMaxWidth().shadow(2.dp, shape).clip(shape)
                    .background(Color.White).border(1.dp, WholesaleBorder, shape).padding(18.dp),
            ) {
                Text("Order journey", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = WholesaleText, modifier = Modifier.padding(bottom = 14.dp))
                steps.forEachIndexed { i, step ->
                    TrackTimelineRow(step = step, isLast = i == steps.lastIndex)
                }
            }

            val ord = order
            val canSimulate =
                ord != null &&
                    (
                        ord.status.equals("DEALER_CONFIRMED", true) ||
                            ord.status.equals("OUT_FOR_DELIVERY", true) ||
                            ord.status.equals("ACCEPTED", true)
                        ) &&
                    !ord.status.equals("DELIVERED", true) &&
                    !demoDelivered.contains(orderId)
            if (canSimulate && BuildConfig.DEMO_MODE) {
                GradientGoldButton("Simulate delivery complete",
                    onClick = { container.demoFlowRepository.markDemoDelivered(orderId) })
            }
        }
    }
}

@Composable
private fun TrackTimelineRow(step: TrackStep, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(when { step.done -> WholesaleGreen; step.active -> WholesaleBlue; else -> Color(0xFFE4E7EC) }),
                contentAlignment = Alignment.Center,
            ) {
                if (step.done) Text("✓", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                else if (step.active) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                else Box(Modifier.size(8.dp).clip(CircleShape).background(WholesaleMuted.copy(0.4f)))
            }
            if (!isLast) {
                Box(Modifier.width(2.dp).height(36.dp)
                    .background(if (step.done) WholesaleGreen.copy(0.35f) else Color(0xFFE4E7EC)))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 8.dp, top = 3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(step.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (step.done || step.active) WholesaleText else WholesaleMuted)
                if (step.simulated) Text("Simulated", fontSize = 10.sp, color = WholesaleBlue,
                    fontWeight = FontWeight.SemiBold)
            }
            Text(step.subtitle, fontSize = 12.sp,
                color = if (step.active) WholesaleBlue else WholesaleMuted, lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp))
        }
    }
}
