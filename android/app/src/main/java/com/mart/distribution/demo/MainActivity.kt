package com.mart.distribution.demo

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.mart.distribution.demo.ui.LocalAppContainer
import com.mart.distribution.demo.ui.MartNavHost
import com.mart.distribution.demo.ui.theme.WholesaleTheme
import com.mart.distribution.demo.data.payment.RazorpayBridge
import com.mart.distribution.demo.data.payment.RazorpayPaymentDataParser
import com.mart.distribution.demo.data.payment.RazorpayResultEvent
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    private val requestNotificationsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        maybeRequestNotificationPermission()
        val app = application as MartApplication
        setContent {
            WholesaleTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        MartNavHost()
                    }
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted =
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationsPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onPaymentSuccess(
        razorpayPaymentId: String?,
        paymentData: PaymentData?,
    ) {
        val parsed = RazorpayPaymentDataParser.fromSuccess(razorpayPaymentId, paymentData)
        RazorpayBridge.emit(
            RazorpayResultEvent(
                success = true,
                paymentId = parsed.paymentId,
                orderId = parsed.orderId,
                signature = parsed.signature,
            ),
        )
    }

    override fun onPaymentError(
        code: Int,
        response: String?,
        paymentData: PaymentData?,
    ) {
        val parsed = RazorpayPaymentDataParser.fromError(paymentData)
        RazorpayBridge.emit(
            RazorpayResultEvent(
                success = false,
                paymentId = parsed.paymentId,
                orderId = parsed.orderId,
                signature = parsed.signature,
                error = "Razorpay error $code: ${response ?: "Unknown error"}",
            ),
        )
    }
}
