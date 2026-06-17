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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.OrderStatusChip
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBlue
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleGreenTint
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleText
import com.mart.distribution.demo.ui.util.formatDecimal

@Composable
fun OrderConfirmationScreen(
    orderId: String,
    navController: NavController,
    prefetchedOrder: OrderDto? = null,
) {
    val container = LocalAppContainer.current
    var order by remember(orderId, prefetchedOrder) { mutableStateOf(prefetchedOrder) }
    var loading by remember(orderId) { mutableStateOf(prefetchedOrder == null) }
    var error by remember(orderId) { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId, prefetchedOrder) {
        if (prefetchedOrder != null) {
            order = prefetchedOrder
            loading = false
            return@LaunchedEffect
        }
        loading = true
        error = null
        try {
            order =
                if (container.sessionRepository.isLocalDemoMode()) {
                    container.localDemoMartStore.orderById(orderId)
                } else {
                    container.martApi.orderById(orderId)
                }
            if (order == null) {
                error = "Order not found"
            }
        } catch (e: Exception) {
            error = e.message ?: "Could not load order"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(WholesaleBg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when {
            loading ->
                CircularProgressIndicator(color = WholesaleBlue)
            error != null ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text(error!!, color = WholesaleMuted)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { navController.popBackStack() }) { Text("Go back") }
                }
            order != null -> {
                val ord = order!!
                Column(
                    modifier = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(48.dp))
                                .background(WholesaleGreenTint),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("✓", fontSize = 42.sp, color = WholesaleGreen, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(
                        if (ord.paymentStatus.equals("PAID", true)) "Payment successful" else "Order placed",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = WholesaleText,
                        letterSpacing = (-0.5).sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (ord.paymentStatus.equals("PAID", true)) {
                            "Your order has been paid and sent to your dealer for confirmation."
                        } else {
                            "Your order has been placed and sent to your dealer for confirmation."
                        },
                        fontSize = 14.sp,
                        color = WholesaleMuted,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )

                    Spacer(Modifier.height(24.dp))

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .border(1.dp, WholesaleBorder, RoundedCornerShape(18.dp))
                                .padding(18.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Order ID", fontSize = 13.sp, color = WholesaleMuted, fontWeight = FontWeight.SemiBold)
                            Text(ord.id.takeLast(10), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleText)
                        }
                        Box(Modifier.fillMaxWidth().height(0.5.dp).background(WholesaleBorder).padding(vertical = 12.dp))
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Amount paid", fontSize = 13.sp, color = WholesaleMuted, fontWeight = FontWeight.SemiBold)
                            Text(
                                formatDecimal(ord.finalAmount ?: ord.totalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WholesaleGreen,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Status", fontSize = 13.sp, color = WholesaleMuted, fontWeight = FontWeight.SemiBold)
                            OrderStatusChip(ord.status)
                        }
                        ord.paymentStatus?.let { ps ->
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Payment", fontSize = 13.sp, color = WholesaleMuted, fontWeight = FontWeight.SemiBold)
                                Text(ps, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WholesaleGreen)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    GradientGoldButton(
                        text = "View order details",
                        onClick = { navController.navigate("order/${ord.id}") },
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("main") { popUpTo("main") { inclusive = false } } },
                    ) {
                        Text("Back to home")
                    }
                }
            }
        }
    }
}
