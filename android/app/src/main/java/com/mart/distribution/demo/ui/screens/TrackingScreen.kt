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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mart.distribution.demo.AppContainer
import com.mart.distribution.demo.data.api.dto.OrderDto
import com.mart.distribution.demo.ui.components.GradientGoldButton
import com.mart.distribution.demo.ui.components.MartElevatedCard

private data class TrackStep(
    val title: String,
    val subtitle: String,
    val done: Boolean,
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

    LaunchedEffect(orderId) {
        loadErr = null
        try {
            val list = container.martApi.orders()
            order = list.find { it.id == orderId }
            if (order == null) loadErr = "Order not found"
        } catch (e: Exception) {
            loadErr = e.message ?: "Failed to load"
        }
    }

    val steps =
        remember(order, mockPaid, demoDelivered) {
            val o = order ?: return@remember emptyList()
            val paid = mockPaid.contains(orderId)
            val accepted = o.status.equals("ACCEPTED", true) || o.status.equals("DELIVERED", true)
            val deliveredApi = o.status.equals("DELIVERED", true)
            val deliveredDemo = demoDelivered.contains(orderId)
            listOf(
                TrackStep("Order placed", "Created on the server", done = true),
                TrackStep(
                    "Payment (demo)",
                    if (paid) "Mock gateway success" else "Use Pay (demo) on the order",
                    done = paid,
                    simulated = true,
                ),
                TrackStep(
                    "Confirmed by dealer",
                    "Stock reserved · invoice generated",
                    done = accepted,
                ),
                TrackStep(
                    "Out for delivery",
                    "Simulated route — no GPS in Phase 1",
                    done = accepted,
                    simulated = true,
                ),
                TrackStep(
                    "Delivered",
                    if (deliveredApi) "Status from API" else "Tap below to simulate",
                    done = deliveredApi || deliveredDemo,
                    simulated = !deliveredApi && deliveredDemo,
                ),
            )
        }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tracking") },
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
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MartElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Investor demo timeline",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Steps combine live API status with in-app simulation where noted.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            loadErr?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            order?.let { o ->
                Text("Order ${o.id.take(8)}…", style = MaterialTheme.typography.titleMedium)
            }
            steps.forEachIndexed { index, step ->
                TimelineRow(
                    step = step,
                    isLast = index == steps.lastIndex,
                )
            }
            val ord = order
            val canSimulateDeliver =
                ord != null &&
                    ord.status.equals("ACCEPTED", true) &&
                    !ord.status.equals("DELIVERED", true) &&
                    !demoDelivered.contains(orderId)
            if (canSimulateDeliver) {
                GradientGoldButton(
                    text = "Simulate delivery complete",
                    onClick = { container.demoFlowRepository.markDemoDelivered(orderId) },
                )
            }
            if (order?.status.equals("PENDING", true) && !mockPaid.contains(orderId)) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Complete mock payment from order detail first")
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    step: TrackStep,
    isLast: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier =
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (step.done) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            },
                        ),
            )
            if (!isLast) {
                Spacer(
                    Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(step.title, style = MaterialTheme.typography.titleMedium)
                if (step.simulated) {
                    Text(
                        "Simulated",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Text(
                step.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
