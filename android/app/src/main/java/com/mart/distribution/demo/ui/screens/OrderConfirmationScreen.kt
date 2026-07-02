package com.mart.distribution.demo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.ui.flashmart.FmButton
import com.mart.distribution.demo.ui.flashmart.FmButtonVariant
import com.mart.distribution.demo.ui.flashmart.FmCard
import com.mart.distribution.demo.ui.flashmart.FmEmptyState
import com.mart.distribution.demo.ui.flashmart.FmErrorBanner
import com.mart.distribution.demo.ui.flashmart.FmLoadingState
import com.mart.distribution.demo.ui.flashmart.FmMoneyRow
import com.mart.distribution.demo.ui.theme.FmSpacing
import com.mart.distribution.demo.ui.theme.WholesaleBg
import com.mart.distribution.demo.ui.theme.WholesaleBorder
import com.mart.distribution.demo.ui.theme.WholesaleGreen
import com.mart.distribution.demo.ui.theme.WholesaleMuted
import com.mart.distribution.demo.ui.theme.WholesaleSurface2
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
            if (order == null) error = "Order not found"
        } catch (e: Exception) {
            error = e.message ?: "Could not load order"
        } finally {
            loading = false
        }
    }

    Scaffold(containerColor = WholesaleBg, topBar = {}) { padding ->
        when {
            loading ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    FmLoadingState(message = "Confirming your order…")
                }
            error != null ->
                Column(Modifier.fillMaxSize().padding(padding).padding(FmSpacing.screenH)) {
                    FmErrorBanner(message = error!!)
                    FmButton(text = "Go back", onClick = { navController.popBackStack() }, variant = FmButtonVariant.Outline)
                }
            order != null -> {
                val ord = order!!
                val paid = ord.paymentStatus.equals("PAID", true)
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Box(
                            modifier = Modifier.size(104.dp).clip(CircleShape).background(WholesaleGreen.copy(0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✓", fontSize = 48.sp, color = WholesaleGreen, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            if (paid) "Payment successful!" else "Order placed!",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WholesaleText,
                            letterSpacing = (-0.5).sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 22.dp),
                        )
                        Text(
                            "Your dealer has been notified and will confirm shortly.",
                            fontSize = 14.5.sp,
                            color = WholesaleMuted,
                            lineHeight = 21.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        FmCard(
                            modifier = Modifier.fillMaxWidth().padding(top = 26.dp),
                            padding = androidx.compose.foundation.layout.PaddingValues(18.dp),
                        ) {
                            FmMoneyRow("Order number", ord.id.takeLast(8).uppercase())
                            Box(Modifier.fillMaxWidth().height(1.dp).background(WholesaleBorder).padding(vertical = 12.dp))
                            FmMoneyRow(
                                if (paid) "Amount paid" else "Order total",
                                formatDecimal(ord.finalAmount ?: ord.totalAmount),
                            )
                            FmMoneyRow("Expected delivery", "Tomorrow, by 6 PM", accent = WholesaleGreen)
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 30.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        FmButton(
                            text = "Track order",
                            onClick = { navController.navigate("tracking/${ord.id}") },
                        )
                        FmButton(
                            text = "View invoice",
                            onClick = { navController.navigate("invoice/${ord.id}") },
                            variant = FmButtonVariant.Soft,
                        )
                    }
                }
            }
            else ->
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    FmEmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = "Order unavailable",
                        message = "We could not load confirmation details for this order.",
                        actionLabel = "Go back",
                        onAction = { navController.popBackStack() },
                    )
                }
        }
    }
}
